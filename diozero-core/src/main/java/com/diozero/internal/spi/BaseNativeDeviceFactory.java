package com.diozero.internal.spi;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     BaseNativeDeviceFactory.java
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

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.BoardPinInfo;
import com.diozero.sbc.LocalBoardInfoUtil;
import com.diozero.sbc.LocalSystemInfo;

/**
 * Helper class for instantiating different devices via the configured provider.
 * To set the provider edit
 * META-INF/services/com.diozero.spi.provider.NativeDeviceFactoryInterface While
 * the ServiceLoader supports multiple service providers, only the first entry
 * in this file is used
 */

public abstract class BaseNativeDeviceFactory extends AbstractDeviceFactory implements NativeDeviceFactoryInterface {
	private static final String NATIVE_PREFIX = "Native";

	private List<DeviceFactoryInterface> deviceFactories = new ArrayList<>();
	private BoardInfo boardInfo;

	public BaseNativeDeviceFactory() {
		super(NATIVE_PREFIX);
	}

	@SuppressWarnings("static-method")
	protected BoardInfo lookupBoardInfo() {
		return LocalBoardInfoUtil.lookupLocalBoardInfo();
	}

	@Override
	public synchronized final BoardInfo getBoardInfo() {
		if (boardInfo == null) {
			// Note this has been separated from the constructor to allow derived classes to
			// override default behaviour, in particular remote devices using e.g. Firmata
			// protocol
			boardInfo = lookupBoardInfo();
			boardInfo.populateBoardPinInfo();
		}
		return boardInfo;
	}

	@Override
	public final BoardPinInfo getBoardPinInfo() {
		return getBoardInfo();
	}

	@Override
	public float getVRef() {
		return getBoardInfo().getAdcVRef();
	}

	@Override
	public float getCpuTemperature() {
		return LocalSystemInfo.getInstance().getCpuTemperature();
	}

	@Override
	public final void registerDeviceFactory(DeviceFactoryInterface deviceFactory) {
		deviceFactories.add(deviceFactory);
	}

	@Override
	public final void close() {
		Logger.trace("close()");

		// Shutdown all of the other non-native device factories
		for (DeviceFactoryInterface df : deviceFactories) {
			if (!df.isClosed()) {
				df.close();
			}
		}

		// Now close all devices provisioned directly by this device factory
		super.close();

		// Finally invoke the shutdown hook on the device factory itself
		shutdown();
	}

	public abstract void shutdown();
}
