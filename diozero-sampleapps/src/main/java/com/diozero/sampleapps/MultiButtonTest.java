package com.diozero.sampleapps;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Sample applications
 * Filename:     MultiButtonTest.java
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
import com.diozero.util.SleepUtil;

/**
 * Input test application. To run:
 * <ul>
 * <li>Built-in:<br>
 * {@code sudo java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar com.diozero.sampleapps.MultiButtonTest 193 35 0}</li>
 * <li>pigpgioj:<br>
 * {@code sudo java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar:diozero-provider-pigpio-$DIOZERO_VERSION.jar:pigpioj-java-2.4.jar com.diozero.sampleapps.MultiButtonTest 193 35 0}</li>
 * </ul>
 */
public class MultiButtonTest {
	public static void main(String[] args) {
		if (args.length < 1) {
			Logger.error("Usage: {} <input-pin> <input-pin> <input-pin>", MultiButtonTest.class.getName());
			System.exit(1);
		}
		test(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
	}

	public static void test(int inputPin1, int inputPin2, int inputPin3) {
		try (Button button1 = new Button(inputPin1, GpioPullUpDown.PULL_UP);
				Button button2 = new Button(inputPin2, GpioPullUpDown.PULL_UP);
				Button button3 = new Button(inputPin3, GpioPullUpDown.PULL_UP)) {
			button1.whenPressed(nanoTime -> Logger.info("1 Pressed"));
			button1.whenReleased(nanoTime -> Logger.info("1 Released"));
			button1.addListener(event -> Logger.info("1 Event: {}", event));

			button2.whenPressed(nanoTime -> Logger.info("2 Pressed"));
			button2.whenReleased(nanoTime -> Logger.info("2 Released"));
			button2.addListener(event -> Logger.info("2 Event: {}", event));

			button3.whenPressed(nanoTime -> Logger.info("3 Pressed"));
			button3.whenReleased(nanoTime -> Logger.info("3 Released"));
			button3.addListener(event -> Logger.info("3 Event: {}", event));

			Logger.debug("Waiting for 10s - *** Press the button connected to an input pin ***");
			SleepUtil.sleepSeconds(10);
		} catch (RuntimeIOException e) {
			Logger.error(e, "Error: {}", e);
		}
	}
}
