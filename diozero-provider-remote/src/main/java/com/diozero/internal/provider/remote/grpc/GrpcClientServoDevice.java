package com.diozero.internal.provider.remote.grpc;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Remote Provider
 * Filename:     GrpcClientServoDevice.java
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
import com.diozero.internal.spi.InternalServoDeviceInterface;

public class GrpcClientServoDevice extends AbstractDevice implements InternalServoDeviceInterface {
	private GrpcClientDeviceFactory deviceFactory;
	private int gpio;

	public GrpcClientServoDevice(GrpcClientDeviceFactory deviceFactory, String key, PinInfo pinInfo, int frequency,
			int minPulseWidthUs, int maxPulseWidthUs, int initialPulseWidthUs) {
		super(key, deviceFactory);

		this.deviceFactory = deviceFactory;
		gpio = pinInfo.getDeviceNumber();

		deviceFactory.provisionServoDevice(gpio, frequency, minPulseWidthUs, maxPulseWidthUs, initialPulseWidthUs);
	}

	@Override
	public int getGpio() {
		return gpio;
	}

	@Override
	public int getServoNum() {
		return gpio;
	}

	@Override
	public int getPulseWidthUs() throws RuntimeIOException {
		return deviceFactory.servoRead(gpio);
	}

	@Override
	public void setPulseWidthUs(int pulseWidthUs) throws RuntimeIOException {
		deviceFactory.servoWrite(gpio, pulseWidthUs);
	}

	@Override
	public int getServoFrequency() {
		return deviceFactory.getServoFrequency(gpio);
	}

	@Override
	public void setServoFrequency(int frequencyHz) throws RuntimeIOException {
		deviceFactory.setServoFrequency(gpio, frequencyHz);
	}

	@Override
	protected void closeDevice() throws RuntimeIOException {
		deviceFactory.closeGpio(gpio);
	}
}
