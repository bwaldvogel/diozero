package com.diozero.internal.provider.builtin;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     DefaultNativeSerialDevice.java
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

import com.diozero.api.RuntimeIOException;
import com.diozero.api.SerialDevice;
import com.diozero.internal.provider.builtin.serial.NativeSerialDevice;
import com.diozero.internal.spi.AbstractDevice;
import com.diozero.internal.spi.DeviceFactoryInterface;
import com.diozero.internal.spi.InternalSerialDeviceInterface;

public class DefaultNativeSerialDevice extends AbstractDevice implements InternalSerialDeviceInterface {
	private NativeSerialDevice device;

	public DefaultNativeSerialDevice(DeviceFactoryInterface deviceFactory, String key, String deviceFile, int baud,
			SerialDevice.DataBits dataBits, SerialDevice.StopBits stopBits, SerialDevice.Parity parity,
			boolean readBlocking, int minReadChars, int readTimeoutMillis) {
		super(key, deviceFactory);

		device = new NativeSerialDevice(deviceFile, baud, dataBits, stopBits, parity, readBlocking, minReadChars,
				readTimeoutMillis);
	}

	@Override
	protected void closeDevice() throws RuntimeIOException {
		Logger.trace("closeDevice()");
		device.close();
	}

	@Override
	public int read() {
		return device.read();
	}

	@Override
	public byte readByte() {
		return device.readByte();
	}

	@Override
	public void writeByte(byte bVal) {
		device.writeByte(bVal);
	}

	@Override
	public int read(byte[] buffer) {
		return device.read(buffer);
	}

	@Override
	public void write(byte... data) {
		device.write(data);
	}

	@Override
	public int bytesAvailable() {
		return device.bytesAvailable();
	}
}
