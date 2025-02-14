package com.diozero.internal.provider.builtin.gpio;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     GpioChip.java
 * 
 * This file is part of the diozero project. More information about this project
 * can be found at https://www.diozero.com/.
 * %%
 * Copyright (C) 2016 - 2022 diozero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.tinylog.Logger;

import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.RuntimeIOException;
import com.diozero.util.DiozeroScheduler;
import com.diozero.util.LibraryLoader;

public class GpioChip extends GpioChipInfo implements AutoCloseable, GpioLineEventListener {
	public static Map<Integer, GpioChip> openAllChips() throws IOException {
		LibraryLoader.loadSystemUtils();

		Map<Integer, GpioChip> chips = Files.list(Paths.get("/dev"))
				.filter(p -> p.getFileName().toString().startsWith("gpiochip"))
				.map(p -> NativeGpioDevice.openChip(p.toString())) //
				.filter(p -> p != null) // openChip will return null if it is unable to open the chip
				.collect(Collectors.toMap(GpioChip::getChipId, chip -> chip));

		if (chips.isEmpty()) {
			Logger.error("Unable to open any gpiochip files in /dev");
		}

		// Calculate the line offset for the chips
		// This allows GPIOs to be auto-detected as the GPIO number is chip offset +
		// line offset
		AtomicInteger offset = new AtomicInteger(0);
		chips.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			entry.getValue().setLineOffset(offset.getAndAdd(entry.getValue().getNumLines()));
		});

		return chips;
	}

	public static List<GpioChipInfo> getChips() {
		LibraryLoader.loadSystemUtils();

		return NativeGpioDevice.getChips();
	}

	public static GpioChip openChip(int chipNum) {
		LibraryLoader.loadSystemUtils();

		return openChip("/dev/" + GPIO_CHIP_FILENAME_PREFIX + "/" + chipNum);
	}

	public static GpioChip openChip(String chipDeviceFile) {
		LibraryLoader.loadSystemUtils();

		return NativeGpioDevice.openChip(chipDeviceFile);
	}

	private static final int EPOLL_FD_NOT_CREATED = -1;
	private static final String GPIO_CHIP_FILENAME_PREFIX = "gpiochip";

	// Linerequest flags
	// https://elixir.bootlin.com/linux/v4.9.127/source/include/uapi/linux/gpio.h#L58
	private static final int GPIOHANDLE_REQUEST_INPUT = 1 << 0;
	private static final int GPIOHANDLE_REQUEST_OUTPUT = 1 << 1;
	private static final int GPIOHANDLE_REQUEST_ACTIVE_LOW = 1 << 2;
	private static final int GPIOHANDLE_REQUEST_OPEN_DRAIN = 1 << 3;
	private static final int GPIOHANDLE_REQUEST_OPEN_SOURCE = 1 << 4;

	// Eventrequest flags
	// https://elixir.bootlin.com/linux/v4.9.127/source/include/uapi/linux/gpio.h#L109
	private static final int GPIOEVENT_REQUEST_RISING_EDGE = 1 << 0;
	private static final int GPIOEVENT_REQUEST_FALLING_EDGE = 1 << 1;
	private static final int GPIOEVENT_REQUEST_BOTH_EDGES = (1 << 0) | (1 << 1);

	// GPIO event types
	// https://elixir.bootlin.com/linux/v4.9.127/source/include/uapi/linux/gpio.h#L136
	public static final int GPIOEVENT_EVENT_RISING_EDGE = 0x01;
	public static final int GPIOEVENT_EVENT_FALLING_EDGE = 0x02;

	private final int chipId;
	private final int chipFd;
	private int lineOffset;
	private GpioLine[] lines;
	private final Map<String, GpioLine> linesByName;
	private int epollFd;
	private final Map<Integer, GpioLineEventListener> fdToListener;
	private final AtomicBoolean running;
	private final BlockingQueue<NativeGpioEvent> eventQueue;

	private Future<?> processEventsFuture;
	private Future<?> eventLoopFuture;

	private GpioChip(String name, String label, int chipFd, GpioLine... lines) {
		super(name, label, lines.length);

		LibraryLoader.loadSystemUtils();

		chipId = Integer.parseInt(name.substring(GPIO_CHIP_FILENAME_PREFIX.length()));
		this.chipFd = chipFd;
		this.lines = lines;
		linesByName = new HashMap<>();
		for (GpioLine line : lines) {
			linesByName.put(line.getName(), line);
		}

		epollFd = EPOLL_FD_NOT_CREATED;

		fdToListener = new HashMap<>();

		running = new AtomicBoolean(false);
		eventQueue = new LinkedBlockingQueue<>();
	}

	public int getChipId() {
		return chipId;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	private void setLineOffset(int lineOffset) {
		this.lineOffset = lineOffset;
	}

	public GpioLine[] getLines() {
		return lines;
	}

	public GpioLine getLineByName(String name) {
		if (linesByName == null) {
			return null;
		}
		return linesByName.get(name);
	}

	public GpioLine provisionGpioInputDevice(int offset, GpioPullUpDown pud, GpioEventTrigger trigger) {
		if (offset < 0 || offset >= lines.length) {
			throw new IllegalArgumentException("Invalid GPIO offset " + offset + " must 0.." + (lines.length - 1));
		}
		// Pull-up / pull-down config available in Kernel 5.5 via gpio_v2_line_flag
		// GPIO_V2_LINE_FLAG_BIAS_*
		// https://microhobby.com.br/blog/2020/02/02/new-linux-kernel-5-5-new-interfaces-in-gpiolib/
		// As on 19/10/2020 Raspberry is on Kernel 5.4.51
		int handle_flags = GPIOHANDLE_REQUEST_INPUT;
		int event_flags;
		switch (trigger) {
		case RISING:
			event_flags = GPIOEVENT_REQUEST_RISING_EDGE;
			break;
		case FALLING:
			event_flags = GPIOEVENT_REQUEST_FALLING_EDGE;
			break;
		case BOTH:
			event_flags = GPIOEVENT_REQUEST_BOTH_EDGES;
			break;
		default:
			event_flags = 0;
		}
		int line_fd = NativeGpioDevice.provisionGpioInputDevice(chipFd, offset, handle_flags, event_flags);
		if (line_fd < 0) {
			throw new RuntimeIOException("Error in provisionGpioInputDevice: " + line_fd);
		}
		lines[offset].setFd(line_fd);

		return lines[offset];
	}

	public GpioLine provisionGpioOutputDevice(int offset, int initialValue) {
		if (offset < 0 || offset >= lines.length) {
			throw new IllegalArgumentException("Invalid GPIO offset " + offset + " must 0.." + (lines.length - 1));
		}
		int line_fd = NativeGpioDevice.provisionGpioOutputDevice(chipFd, offset, initialValue);
		if (line_fd < 0) {
			throw new RuntimeIOException("Error in provisionGpioOutputDevice: " + line_fd);
		}
		lines[offset].setFd(line_fd);

		return lines[offset];
	}

	@Override
	public synchronized void close() {
		Logger.trace("close()");

		stopEventProcessing();

		// Close all of the lines
		if (lines != null) {
			for (GpioLine line : lines) {
				line.close();
			}
			lines = null;
		}
		linesByName.clear();

		// Finally close the GPIO chip itself
		NativeGpioDevice.close(chipFd);
	}

	public void register(int fd, GpioLineEventListener listener) {
		startEventProcessing();

		int rc = NativeGpioDevice.epollAddFileDescriptor(epollFd, fd);
		if (rc < 0) {
			throw new RuntimeIOException("Error adding file descriptor '" + fd + "' to epoll");
		}

		fdToListener.put(Integer.valueOf(fd), listener);
	}

	public void deregister(int lineFd) {
		if (epollFd == EPOLL_FD_NOT_CREATED) {
			Logger.debug("Attempt to register an epoll fd without epoll being initiated");
			return;
		}

		if (fdToListener.containsKey(Integer.valueOf(lineFd))) {
			int rc = NativeGpioDevice.epollRemoveFileDescriptor(epollFd, lineFd);
			fdToListener.remove(Integer.valueOf(lineFd));
			if (fdToListener.isEmpty()) {
				stopEventProcessing();
			}
			if (rc < 0) {
				throw new RuntimeIOException("Error removing file descriptor '" + lineFd + "' from epoll");
			}
		}
	}

	@Override
	public void event(int lineFd, int eventDataId, long epochTimeMs, long timestampNanos) {
		// Add the event to the tail of the queue
		eventQueue.offer(new NativeGpioEvent(lineFd, eventDataId, epochTimeMs, timestampNanos));
	}

	private void eventLoop() {
		if (epollFd == EPOLL_FD_NOT_CREATED) {
			Logger.error("Cannot start event loop as epoll not initialised");
			return;
		}

		Logger.trace("Starting event loop for chip {}", Integer.valueOf(chipId));
		NativeGpioDevice.eventLoop(epollFd, -1, this);
		Logger.info("Event loop finished");
	}

	private void startEventProcessing() {
		// TODO Investigate use of SelectorProvider instead
		if (epollFd == EPOLL_FD_NOT_CREATED) {
			int rc = NativeGpioDevice.epollCreate();
			if (rc < 0) {
				throw new RuntimeIOException("Error in epollCreate: " + rc);
			}
			epollFd = rc;

			running.getAndSet(true);
			processEventsFuture = DiozeroScheduler.getNonDaemonInstance().submit(this::processEvents);
			eventLoopFuture = DiozeroScheduler.getNonDaemonInstance().submit(this::eventLoop);
		}
	}

	private void stopEventProcessing() {
		// First stop the event processing thread
		running.set(false);
		if (processEventsFuture != null) {
			// An alternative approach would be to send a "poison" message to indicate that
			// the thread should be stopped and then call processEventsFuture.get() and only
			// call cancel if the call to get times out or is interrupted
			processEventsFuture.cancel(true);
			processEventsFuture = null;
		}

		// Stop the epoll event loop
		if (epollFd != EPOLL_FD_NOT_CREATED) {
			Logger.trace("Stopping the epoll_wait event loop");
			NativeGpioDevice.stopEventLoop(epollFd);
			epollFd = EPOLL_FD_NOT_CREATED;
		}

		// Stop the Java thread that initiated the epoll event loop (note shouldn't be
		// necessary)
		if (eventLoopFuture != null && !eventLoopFuture.isDone()) {
			// Call get with a short timeout instead and only call cancel if it times out
			try {
				Logger.trace("Waiting for event loop to complete...");
				eventLoopFuture.get(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				Logger.debug("Event loop didn't exit, cancelling: {}", e);
				eventLoopFuture.cancel(true);
			} catch (CancellationException e) {
				// Ignore
			}
			eventLoopFuture = null;
		}
	}

	private void processEvents() {
		Thread.currentThread().setName("diozero-GpioChip-processEvents-" + hashCode());

		try {
			while (running.get()) {
				// Blocking queue - take the event at the head of the queue, waiting if
				// necessary until an event becomes available.
				NativeGpioEvent event = eventQueue.take();
				Integer line_fd = Integer.valueOf(event.lineFd);
				GpioLineEventListener listener = fdToListener.get(line_fd);
				if (listener == null) {
					// There may still be pending events on the queue after removing a listener
					Logger.debug("No listener for line fd {}, event data: '{}'", line_fd,
							Integer.valueOf(event.eventDataId));
				} else {
					listener.event(event.lineFd, event.eventDataId, event.epochTimeMs, event.timestampNanos);
				}
			}
		} catch (InterruptedException e) {
			// Result of the processEventsFuture.cancel(true) call within the
			// stopEventProcessing() method
			Thread.currentThread().interrupt();
			running.set(false);
		}

		Logger.debug("Finished");
	}

	private static class NativeGpioEvent {
		int lineFd;
		int eventDataId;
		long epochTimeMs;
		long timestampNanos;

		public NativeGpioEvent(int lineFd, int eventDataId, long epochTimeMs, long timestampNanos) {
			this.lineFd = lineFd;
			this.eventDataId = eventDataId;
			this.epochTimeMs = epochTimeMs;
			this.timestampNanos = timestampNanos;
		}
	}
}
