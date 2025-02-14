package com.diozero.internal.provider.builtin;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     NativeGpioInputDevice.java
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

import org.tinylog.Logger;

import com.diozero.api.DigitalInputEvent;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.provider.builtin.gpio.GpioChip;
import com.diozero.internal.provider.builtin.gpio.GpioLine;
import com.diozero.internal.provider.builtin.gpio.GpioLineEventListener;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.GpioDigitalInputDeviceInterface;
import com.diozero.internal.spi.MmapGpioInterface;

public class NativeGpioInputDevice extends AbstractInputDevice<DigitalInputEvent>
		implements GpioDigitalInputDeviceInterface, GpioLineEventListener {
	private GpioChip chip;
	private int gpio;
	private GpioLine line;

	public NativeGpioInputDevice(DefaultDeviceFactory deviceFactory, String key, GpioChip chip, PinInfo pinInfo,
			GpioPullUpDown pud, GpioEventTrigger trigger, MmapGpioInterface mmapGpio) {
		super(key, deviceFactory);

		gpio = pinInfo.getDeviceNumber();
		int offset = pinInfo.getLineOffset();
		if (offset == PinInfo.NOT_DEFINED) {
			throw new IllegalArgumentException("Line offset not defined for pin " + pinInfo);
		}
		this.chip = chip;

		line = chip.provisionGpioInputDevice(offset, pud, trigger);
		// XXX Remove this once kernel 5.5 is widely adopted - pull-up / pull-down
		// control is not possible with gpiod v1
		if (mmapGpio != null) {
			mmapGpio.setPullUpDown(gpio, pud);
		}
	}

	@Override
	public int getGpio() {
		return gpio;
	}

	@Override
	public boolean getValue() throws RuntimeIOException {
		return line.getValue() == 0 ? false : true;
	}

	@Override
	public void setDebounceTimeMillis(int debounceTime) {
		Logger.warn("Debounce not supported");
	}

	@Override
	protected void enableListener() {
		Logger.trace("enableListener(), {}", Integer.valueOf(gpio));
		chip.register(line.getFd(), this);
	}

	@Override
	protected void disableListener() {
		Logger.trace("disableListener(), {}", Integer.valueOf(gpio));
		chip.deregister(line.getFd());
	}

	@Override
	public void closeDevice() {
		Logger.trace("closeDevice(), {}", Integer.valueOf(gpio));
		disableListener();
		line.close();
	}

	@Override
	public void event(int lineFd, int eventDataId, long epochTimeMs, long timestampNanos) {
		accept(new DigitalInputEvent(gpio, epochTimeMs, timestampNanos,
				eventDataId == GpioChip.GPIOEVENT_EVENT_RISING_EDGE));
	}
}
