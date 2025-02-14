package com.diozero.internal.provider.builtin.gpio;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     NativeGpioDevice.java
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

import java.util.List;

public class NativeGpioDevice {
	static native List<GpioChipInfo> getChips();

	/**
	 * Open the specified GPIO chip
	 *
	 * @param filename File path to the chip, e.g. /dev/gpiochip0
	 * @return The NativeGpioChip
	 */
	static native GpioChip openChip(String filename);

	static native int provisionGpioInputDevice(int chipFd, int offset, int handleFlags, int eventFlags);

	static native int provisionGpioOutputDevice(int chipFd, int offset, int initialValue);

	static native int getValue(int lineFd);

	static native int setValue(int lineFd, int value);

	static native int epollCreate();

	static native int epollAddFileDescriptor(int epollFd, int lineFd);

	static native int epollRemoveFileDescriptor(int epollFd, int lineFd);

	/*-
	 * The timeout argument specifies the number of milliseconds that epoll_wait() will block
	 * Specifying a timeout of -1 causes epoll_wait() to block indefinitely, while specifying a timeout
	 * equal to zero cause epoll_wait() to return immediately, even if no events are available
	 */
	static native void eventLoop(int epollFd, int timeoutMillis, GpioLineEventListener listener);

	static native int stopEventLoop(int epollFd);

	/**
	 * Close a file descriptor
	 *
	 * @param fd The file descriptor to close
	 */
	static native void close(int fd);
}
