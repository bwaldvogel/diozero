package com.diozero.internal.provider.builtin.i2c;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     NativeI2CDeviceJavaRaf.java
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

import java.io.IOException;
import java.io.RandomAccessFile;

import org.tinylog.Logger;

import com.diozero.api.DeviceBusyException;
import com.diozero.api.I2CConstants;
import com.diozero.api.I2CDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AbstractDevice;
import com.diozero.internal.spi.DeviceFactoryInterface;
import com.diozero.internal.spi.InternalI2CDeviceInterface;
import com.diozero.util.FileDescriptorUtil;

/**
 * <p>
 * Native Java implementation of the I2C SMBus commands using a Java
 * {@link RandomAccessFile} to read to and write from the device. Makes use of a
 * single native method to select the slave address.
 * </p>
 *
 * <p>
 * Reference
 * <a href="https://www.kernel.org/doc/Documentation/i2c/dev-interface">Kernel
 * I2C dev interface</a> and
 * <a href="https://www.kernel.org/doc/Documentation/i2c/smbus-protocol">SMBus
 * Protocol</a>.
 * </p>
 * <p>
 * <em>Warning</em> Not all methods have been tested!
 * </p>
 */
@Deprecated
public class NativeI2CDeviceJavaRaf extends AbstractDevice implements InternalI2CDeviceInterface {
	private static final int EBUSY = -16;

	private RandomAccessFile deviceFile;
	private int controller;
	private int deviceAddress;

	public NativeI2CDeviceJavaRaf(DeviceFactoryInterface deviceFactory, String key, int controller, int address,
			I2CConstants.AddressSize addressSize, boolean force) {
		super(key, deviceFactory);

		this.controller = controller;
		this.deviceAddress = address;
		String device_file = "/dev/i2c-" + controller;

		try {
			deviceFile = new RandomAccessFile(device_file, "rwd");
			int fd = FileDescriptorUtil.getNativeFileDescriptor(deviceFile.getFD());
			int rc = NativeI2C.selectSlave(fd, deviceAddress, force);
			if (rc < 0) {
				close();
				if (rc == EBUSY) {
					throw new DeviceBusyException("Error, I2C device " + controller + "-0x"
							+ Integer.toHexString(deviceAddress) + " is busy");
				}
				throw new RuntimeIOException("Error selecting I2C address " + controller + "-0x"
						+ Integer.toHexString(deviceAddress) + ": " + rc);
			}
		} catch (IOException e) {
			close();
			throw new RuntimeIOException(
					"Error opening I2C device " + controller + "-0x" + Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public void closeDevice() {
		if (deviceFile != null) {
			try {
				deviceFile.close();
			} catch (IOException e) {
				Logger.error(e, "Error closing I2C device {}-0x{}: {}", Integer.valueOf(controller),
						Integer.toHexString(deviceAddress), e);
			}
		}
	}

	@Override
	public boolean probe(I2CDevice.ProbeMode mode) {
		try {
			return deviceFile.readUnsignedByte() >= 0;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void writeQuick(byte bit) {
		throw new UnsupportedOperationException("I2C write quick isn't possible via sysfs");
	}

	@Override
	public byte readByte() {
		try {
			return (byte) deviceFile.readUnsignedByte();
		} catch (IOException e) {
			throw new RuntimeIOException(
					"Error in I2C readByte for device i2c-" + controller + "-0x" + Integer.toHexString(deviceAddress),
					e);
		}
	}

	@Override
	public void writeByte(byte data) {
		try {
			deviceFile.writeByte(data & 0xff);
		} catch (IOException e) {
			throw new RuntimeIOException(
					"Error in I2C writeByte for device i2c-" + controller + "-0x" + Integer.toHexString(deviceAddress),
					e);
		}
	}

	@Override
	public byte readByteData(int register) {
		try {
			deviceFile.writeByte(register);
			return (byte) deviceFile.readUnsignedByte();
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C readByteData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public void writeByteData(int register, byte data) {
		byte[] buffer = new byte[2];
		buffer[0] = (byte) register;
		buffer[1] = data;
		try {
			deviceFile.write(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C writeByteData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public short readWordData(int register) {
		try {
			deviceFile.writeByte(register);
			// This is Big Endian, however SMBus interface specifies Little Endian so need
			// to swap...
			// short val = deviceFile.readShort();
			byte[] buffer = new byte[2];
			deviceFile.read(buffer);
			return (short) ((buffer[1] << 8) | (buffer[0] & 0xff));
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C readWordData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public void writeWordData(int register, short data) {
		byte[] buffer = new byte[3];
		buffer[0] = (byte) register;
		// Little Endian
		buffer[1] = (byte) (data & 0xff);
		buffer[2] = (byte) ((data >> 8) & 0xff);
		try {
			deviceFile.write(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C writeWordData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public short readWordSwapped(int register) {
		try {
			deviceFile.writeByte(register);
			// This is Big Endian
			return deviceFile.readShort();
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C readWordData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public void writeWordSwapped(int register, short data) {
		try {
			// This is Big Endian
			deviceFile.writeShort(data);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C writeWordData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public short processCall(int register, short data) {
		// An approximation of SMBus Process Call
		writeWordData(register, data);
		return readWordData(register);
		/*-
		try {
			return deviceFile.readShort();
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C processCall for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
		*/
	}

	@Override
	public byte[] readBlockData(int register) {
		byte[] rx_data;

		try {
			byte[] buffer = new byte[MAX_I2C_BLOCK_SIZE + 1];
			deviceFile.write(register);
			int read = deviceFile.read(buffer);
			if (read == -1) {
				throw new RuntimeIOException("Error in I2C readBlockData for device i2c-" + controller + "-0x"
						+ Integer.toHexString(deviceAddress) + " - end of file reached");
			}
			int length = buffer[0] & 0xff;
			if (read != length + 1) {
				Logger.warn("Expected to read " + (length + 1) + " bytes, actually read " + read);
			}
			rx_data = new byte[length];

			System.arraycopy(buffer, 1, rx_data, 0, length);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C readBlockData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}

		return rx_data;
	}

	@Override
	public void writeBlockData(int register, byte... data) {
		byte[] buffer = new byte[data.length + 2];
		buffer[0] = (byte) register;
		buffer[1] = (byte) data.length;
		System.arraycopy(data, 0, buffer, 2, data.length);

		try {
			deviceFile.write(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C writeBlockData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public byte[] blockProcessCall(int register, byte... txData) {
		// An approximation of SMBus Block Process Call
		writeBlockData(register, txData);
		byte[] buffer = readBlockData(register);
		/*-
		byte[] buffer = new byte[txData.length];
		try {
			deviceFile.read(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C blockProcessCall for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
		*/
		return buffer;
	}

	@Override
	public int readI2CBlockData(int register, byte[] buffer) {
		if (buffer.length > MAX_I2C_BLOCK_SIZE) {
			throw new RuntimeIOException("Invalid buffer length - max length is " + MAX_I2C_BLOCK_SIZE);
		}

		try {
			deviceFile.write(register);
			return deviceFile.read(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C readI2CBlockData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public void writeI2CBlockData(int register, byte... data) {
		byte[] buffer = new byte[data.length + 1];
		buffer[0] = (byte) register;
		System.arraycopy(data, 0, buffer, 1, data.length);

		try {
			deviceFile.write(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException("Error in I2C writeI2CBlockData for device i2c-" + controller + "-0x"
					+ Integer.toHexString(deviceAddress), e);
		}
	}

	@Override
	public int readBytes(byte[] buffer) {
		try {
			return deviceFile.read(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException(
					"Error in I2C readBytes for device i2c-" + controller + "-0x" + Integer.toHexString(deviceAddress),
					e);
		}
	}

	@Override
	public void writeBytes(byte... data) {
		try {
			deviceFile.write(data);
		} catch (IOException e) {
			throw new RuntimeIOException(
					"Error in I2C readBytes for device i2c-" + controller + "-0x" + Integer.toHexString(deviceAddress),
					e);
		}
	}

	@Override
	public void readWrite(I2CMessage[] messages, byte[] buffer) {
		// TODO Simulate read-write?!
		throw new UnsupportedOperationException("I2C readWrite not supported");
	}
}
