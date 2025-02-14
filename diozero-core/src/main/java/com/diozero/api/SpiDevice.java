package com.diozero.api;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     SpiDevice.java
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

import com.diozero.internal.spi.InternalSpiDeviceInterface;
import com.diozero.internal.spi.SpiDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;

/**
 * Serial Peripheral Interface device
 */
public class SpiDevice implements SpiDeviceInterface {
	/**
	 * I2C device builder. Default values:
	 * <ul>
	 * <li>controller: 0</li>
	 * <li>frequency: 2MHz</li>
	 * <li>clockMode: Mode 0</li>
	 * <li>lsbFirst: false</li>
	 * </ul>
	 */
	public static class Builder {
		private int controller = SpiConstants.DEFAULT_SPI_CONTROLLER;
		private int chipSelect;
		private int frequency = SpiConstants.DEFAULT_SPI_CLOCK_FREQUENCY;
		private SpiClockMode clockMode = SpiConstants.DEFAULT_SPI_CLOCK_MODE;
		private boolean lsbFirst = SpiConstants.DEFAULT_LSB_FIRST;

		protected Builder(int chipSelect) {
			this.chipSelect = chipSelect;
		}

		/**
		 * Set the SPI controller number
		 *
		 * @param controller the SPI controller number
		 * @return this builder instance
		 */
		public Builder setController(int controller) {
			this.controller = controller;
			return this;
		}

		/**
		 * SPI chip select number
		 *
		 * @param chipSelect the chip select number
		 * @return this builder instance
		 */
		public Builder setChipSelect(int chipSelect) {
			this.chipSelect = chipSelect;
			return this;
		}

		/**
		 * Set the SPI clock frequency
		 *
		 * @param frequency the SPI clock frequency
		 * @return this builder instance
		 */
		public Builder setFrequency(int frequency) {
			this.frequency = frequency;
			return this;
		}

		/**
		 * Set the SPI {@link SpiClockMode clock mode}
		 *
		 * @param clockMode the SPI clock mode to use
		 * @return this builder instance
		 */
		public Builder setClockMode(SpiClockMode clockMode) {
			this.clockMode = clockMode;
			return this;
		}

		/**
		 * Set the byte order
		 *
		 * @param lsbFirst True for little endian
		 * @return this builder instance
		 */
		public Builder setLsbFirst(boolean lsbFirst) {
			this.lsbFirst = lsbFirst;
			return this;
		}

		/**
		 * Provision a new SPI device
		 *
		 * @return a new SPI device instance
		 */
		public SpiDevice build() {
			return new SpiDevice(controller, chipSelect, frequency, clockMode, lsbFirst);
		}
	}

	/**
	 * Construct a new SPI device builder instance using the specified chip select
	 * value
	 *
	 * @param chipSelect SPI chip select
	 * @return SPI device builder
	 */
	public static Builder builder(int chipSelect) {
		return new Builder(chipSelect);
	}

	private InternalSpiDeviceInterface delegate;
	private int maxBufferSize;

	public SpiDevice(int chipSelect) throws RuntimeIOException {
		this(DeviceFactoryHelper.getNativeDeviceFactory(), SpiConstants.DEFAULT_SPI_CONTROLLER, chipSelect,
				SpiConstants.DEFAULT_SPI_CLOCK_FREQUENCY, SpiConstants.DEFAULT_SPI_CLOCK_MODE,
				SpiConstants.DEFAULT_LSB_FIRST);
	}

	public SpiDevice(int controller, int chipSelect) throws RuntimeIOException {
		this(DeviceFactoryHelper.getNativeDeviceFactory(), controller, chipSelect,
				SpiConstants.DEFAULT_SPI_CLOCK_FREQUENCY, SpiConstants.DEFAULT_SPI_CLOCK_MODE,
				SpiConstants.DEFAULT_LSB_FIRST);
	}

	public SpiDevice(int controller, int chipSelect, int frequency, SpiClockMode mode, boolean lsbFirst)
			throws RuntimeIOException {
		this(DeviceFactoryHelper.getNativeDeviceFactory(), controller, chipSelect, frequency, mode, lsbFirst);
	}

	public SpiDevice(SpiDeviceFactoryInterface deviceFactory, int controller, int chipSelect, int frequency,
			SpiClockMode mode, boolean lsbFirst) throws RuntimeIOException {
		delegate = deviceFactory.provisionSpiDevice(controller, chipSelect, frequency, mode, lsbFirst);
		maxBufferSize = deviceFactory.getSpiBufferSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws RuntimeIOException {
		Logger.trace("close()");
		if (delegate.isOpen()) {
			delegate.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getController() {
		return delegate.getController();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getChipSelect() {
		return delegate.getChipSelect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte... txBuffer) throws RuntimeIOException {
		int written = 0;
		do {
			int to_write = Math.min(txBuffer.length - written, maxBufferSize);
			delegate.write(txBuffer, written, to_write);
			written += to_write;
		} while (written < txBuffer.length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] txBuffer, int txOffset, int length) throws RuntimeIOException {
		delegate.write(txBuffer, txOffset, length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] writeAndRead(byte... out) throws RuntimeIOException {
		return delegate.writeAndRead(out);
	}
}
