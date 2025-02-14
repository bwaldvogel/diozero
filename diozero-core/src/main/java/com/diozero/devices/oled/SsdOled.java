package com.diozero.devices.oled;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     SsdOled.java
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

import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.tinylog.Logger;

import com.diozero.api.DeviceInterface;
import com.diozero.api.DigitalOutputDevice;
import com.diozero.util.SleepUtil;

public abstract class SsdOled implements DeviceInterface {
	private static final int SPI_FREQUENCY = 8_000_000;

	private static final byte DISPLAY_OFF = (byte) 0xAE;
	private static final byte DISPLAY_ON = (byte) 0xAF;

	protected SsdOledCommunicationChannel device;
	protected DigitalOutputDevice dcPin;
	protected DigitalOutputDevice resetPin;
	protected int width;
	protected int height;
	protected byte[] buffer;
	protected int imageType;

	public SsdOled(int controller, int chipSelect, DigitalOutputDevice dcPin, DigitalOutputDevice resetPin, int width,
			int height, int imageType) {
		device = new SsdOledCommunicationChannel.SpiCommunicationChannel(chipSelect, controller, SPI_FREQUENCY);

		this.dcPin = dcPin;
		this.resetPin = resetPin;

		this.width = width;
		this.height = height;
		this.imageType = imageType;
	}

	protected abstract void init();

	protected void reset() {
		resetPin.setOn(true);
		SleepUtil.sleepMillis(1);
		resetPin.setOn(false);
		SleepUtil.sleepMillis(10);
		resetPin.setOn(true);
	}

	protected void command(byte... commands) {
		dcPin.setOn(false);
		device.write(commands);
	}

	protected void data() {
		dcPin.setOn(true);
		device.write(buffer);
	}

	protected void data(int offset, int length) {
		dcPin.setOn(true);
		device.write(buffer, offset, length);
	}

	protected abstract void goTo(int x, int y);

	protected abstract void home();

	public void display() {
		home();

		data();
	}

	public abstract void display(BufferedImage image);

	public void clear() {
		Arrays.fill(buffer, (byte) 0);
		display();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setDisplayOn(boolean on) {
		command(on ? DISPLAY_ON : DISPLAY_OFF);
	}

	@Override
	public void close() {
		Logger.trace("close()");
		clear();
		setDisplayOn(false);
		device.close();
	}

	public int getNativeImageType() {
		return imageType;
	}

	public abstract void invertDisplay(boolean invert);
}
