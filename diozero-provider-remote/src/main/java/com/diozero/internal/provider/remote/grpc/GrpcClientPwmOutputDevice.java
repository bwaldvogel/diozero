package com.diozero.internal.provider.remote.grpc;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Remote Provider
 * Filename:     GrpcClientPwmOutputDevice.java
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

import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AbstractDevice;
import com.diozero.internal.spi.InternalPwmOutputDeviceInterface;

public class GrpcClientPwmOutputDevice extends AbstractDevice implements InternalPwmOutputDeviceInterface {
	private GrpcClientDeviceFactory deviceFactory;
	private int gpio;

	public GrpcClientPwmOutputDevice(GrpcClientDeviceFactory deviceFactory, String key, PinInfo pinInfo, int frequency,
			float initialValue) {
		super(key, deviceFactory);

		this.deviceFactory = deviceFactory;
		gpio = pinInfo.getDeviceNumber();

		deviceFactory.provisionPwmOutputDevice(gpio, frequency, initialValue);
	}

	@Override
	public int getGpio() {
		return gpio;
	}

	@Override
	public int getPwmNum() {
		return gpio;
	}

	@Override
	public float getValue() throws RuntimeIOException {
		return deviceFactory.pwmRead(gpio);
	}

	@Override
	public void setValue(float value) throws RuntimeIOException {
		deviceFactory.pwmWrite(gpio, value);
	}

	@Override
	public int getPwmFrequency() {
		return deviceFactory.getPwmFrequency(gpio);
	}

	@Override
	public void setPwmFrequency(int frequencyHz) throws RuntimeIOException {
		deviceFactory.setPwmFrequency(gpio, frequencyHz);
	}

	@Override
	protected void closeDevice() throws RuntimeIOException {
		deviceFactory.closeGpio(gpio);
	}
}
