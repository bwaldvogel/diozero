package com.diozero.sampleapps.perf;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Sample applications
 * Filename:     GpioReadPerfTest.java
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

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.RuntimeIOException;

/**
 * GPIO output performance test application. To run:
 * <ul>
 * <li>built-in:<br>
 * {@code java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar com.diozero.sampleapps.GpioReadPerfTest 12 100000}</li>
 * <li>pigpgioj:<br>
 * {@code sudo java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar:diozero-provider-pigpio-$DIOZERO_VERSION.jar:pigpioj-java-2.4.jar com.diozero.sampleapps.GpioReadPerfTest 12 5000000}</li>
 * <li>mmap:<br>
 * {@code java -cp tinylog-api-$TINYLOG_VERSION.jar:tinylog-impl-$TINYLOG_VERSION.jar:diozero-core-$DIOZERO_VERSION.jar:diozero-sampleapps-$DIOZERO_VERSION.jar:diozero-provider-mmap-$DIOZERO_VERSION.jar com.diozero.sampleapps.GpioReadPerfTest 12 40000000}</li>
 * </ul>
 */
public class GpioReadPerfTest {
	private static final int ITERATIONS = 100_000;

	public static void main(String[] args) {
		if (args.length < 1) {
			Logger.error("Usage: {} <pin-number> [<iterations>]", GpioReadPerfTest.class.getName());
			System.exit(1);
		}

		int pin = Integer.parseInt(args[0]);

		int iterations = ITERATIONS;
		if (args.length > 1) {
			iterations = Integer.parseInt(args[1]);
		}

		test(pin, iterations);
	}

	public static void test(int pin, int iterations) {
		try (DigitalInputDevice gpio = new DigitalInputDevice(pin)) {
			for (int j = 0; j < 5; j++) {
				long start_nano = System.nanoTime();
				for (int i = 0; i < iterations; i++) {
					gpio.getValue();
				}
				long duration_ns = System.nanoTime() - start_nano;

				Logger.info("Duration for {} iterations: {}s", Integer.valueOf(iterations),
						String.format("%.4f", Float.valueOf(((float) duration_ns) / 1000 / 1000 / 1000)));
			}
		} catch (RuntimeIOException e) {
			Logger.error(e, "Error: {}", e);
		}
	}
}
