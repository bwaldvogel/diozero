package com.diozero.internal.spi;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     I2CDeviceFactoryInterface.java
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

import java.util.Collections;
import java.util.List;

import org.tinylog.Logger;

import com.diozero.api.DeviceAlreadyOpenedException;
import com.diozero.api.I2CConstants;
import com.diozero.api.RuntimeIOException;

public interface I2CDeviceFactoryInterface extends DeviceFactoryInterface {
	String I2C_PREFIX = "-I2C-";

	default InternalI2CDeviceInterface provisionI2CDevice(int controller, int address,
			I2CConstants.AddressSize addressSize) throws RuntimeIOException {
		String key = createI2CKey(controller, address);

		// Check if this pin is already provisioned
		if (isDeviceOpened(key)) {
			throw new DeviceAlreadyOpenedException("Device " + key + " is already in use");
		}

		InternalI2CDeviceInterface device = createI2CDevice(key, controller, address, addressSize);
		deviceOpened(device);

		return device;
	}

	InternalI2CDeviceInterface createI2CDevice(String key, int controller, int address,
			I2CConstants.AddressSize addressSize) throws RuntimeIOException;

	default List<Integer> getI2CBusNumbers() {
		Logger.error("getI2CBusNumbers not supported by this provider");
		return Collections.emptyList();
	}

	default int getI2CFunctionalities(int controller) {
		Logger.error("getI2CFunctionalities not supported by this provider");
		return 0;
	}

	static String createI2CKey(String keyPrefix, int controller, int address) {
		return keyPrefix + I2C_PREFIX + controller + "-0x" + Integer.toHexString(address);
	}
}
