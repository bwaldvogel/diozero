package com.diozero.sampleapps;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Sample applications
 * Filename:     LEDTest.java
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

import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.LED;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

/**
 * LED blink sample application.
 */
public class LEDTest {
	public static void main(String[] args) {
		if (args.length < 1) {
			Logger.error("Usage: {} <gpio>", LEDTest.class.getName());
			System.exit(1);
		}
		if (args.length == 2) {
			test(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		} else {
			test(Integer.parseInt(args[0]));
		}
	}

	private static void test(int chip, int line) {
		PinInfo pin_info = DeviceFactoryHelper.getNativeDeviceFactory().getBoardPinInfo()
				.getByChipAndLineOffsetOrThrow(chip, line);
		try (LED led = new LED(pin_info, true, false)) {
			test(led);
		} catch (RuntimeIOException e) {
			Logger.error(e, "Error: {}", e);
		} finally {
			// Required if there are non-daemon threads that will prevent the
			// built-in clean-up routines from running
			Diozero.shutdown();
		}
	}

	private static void test(int pin) {
		try (LED led = new LED(pin)) {
			test(led);
		} catch (RuntimeIOException e) {
			Logger.error(e, "Error: {}", e);
		} finally {
			// Required if there are non-daemon threads that will prevent the
			// built-in clean-up routines from running
			Diozero.shutdown();
		}
	}

	private static void test(LED led) {
		int blinks = 5;
		Logger.info("On");
		led.on();
		SleepUtil.sleepSeconds(1);
		Logger.info("Off");
		led.off();
		SleepUtil.sleepSeconds(1);
		Logger.info("Toggle");
		led.toggle();
		SleepUtil.sleepSeconds(1);
		Logger.info("Toggle");
		led.toggle();
		SleepUtil.sleepSeconds(1);

		Logger.info("Blink {} times", Integer.valueOf(blinks));
		led.blink(0.5f, 0.5f, blinks, false);

		Logger.info("Done");
	}
}
