package com.diozero.sampleapps;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Sample applications
 * Filename:     ButtonTest.java
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

import com.diozero.api.GpioPullUpDown;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.Button;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

/**
 * Input test application. To run:
 * <ul>
 * <li>Built-in:<br>
 * {@code sudo java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar com.diozero.sampleapps.ButtonTest 25}</li>
 * <li>pigpgioj:<br>
 * {@code sudo java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar:diozero-provider-pigpio-$DIOZERO_VERSION.jar:pigpioj-java-2.4.jar com.diozero.sampleapps.ButtonTest 25}</li>
 * </ul>
 */
public class ButtonTest {
	public static void main(String[] args) {
		if (args.length < 1) {
			Logger.error("Usage: {} <input-pin> [pud]", ButtonTest.class.getName());
			System.exit(1);
		}
		GpioPullUpDown pud = GpioPullUpDown.PULL_UP;
		if (args.length > 1) {
			pud = GpioPullUpDown.valueOf(args[1]);
		}
		test(Integer.parseInt(args[0]), pud);
	}

	public static void test(final int inputPin, final GpioPullUpDown pud) {
		final int delay_s = 10;
		try (final Button button = new Button(inputPin, pud)) {
			button.whenPressed(nanoTime -> Logger.info("Pressed"));
			button.whenReleased(nanoTime -> Logger.info("Released"));
			button.addListener(event -> Logger.info("Event: {}", event));
			Logger.info("Waiting for {}s - *** Press the button connected to input pin {} ***",
					Integer.valueOf(delay_s), Integer.valueOf(inputPin));
			SleepUtil.sleepSeconds(delay_s);
		} catch (RuntimeIOException ioe) {
			Logger.error(ioe, "Error: {}", ioe);
		} finally {
			Diozero.shutdown();
		}
	}
}
