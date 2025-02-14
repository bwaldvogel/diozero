package com.diozero.internal.spi;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     AbstractInputDevice.java
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

import com.diozero.api.DeviceEvent;
import com.diozero.api.function.DeviceEventConsumer;

public abstract class AbstractInputDevice<T extends DeviceEvent> extends AbstractDevice {
	private DeviceEventConsumer<T> listener;

	public AbstractInputDevice(String key, DeviceFactoryInterface deviceFactory) {
		super(key, deviceFactory);
	}

	public void accept(T event) {
		if (listener != null) {
			listener.accept(event);
		}
	}

	@SuppressWarnings("static-method")
	public boolean generatesEvents() {
		return false;
	}

	public final void setListener(DeviceEventConsumer<T> listener) {
		this.listener = listener;
		enableListener();
	}

	public final void removeListener() {
		disableListener();
		listener = null;
	}

	protected void enableListener() {
		// Inherit and override
	}

	protected void disableListener() {
		// Inherit and override
	}

	protected boolean isListenerEnabled() {
		return listener != null;
	}

	@Override
	protected void closeDevice() {
		removeListener();
	}
}
