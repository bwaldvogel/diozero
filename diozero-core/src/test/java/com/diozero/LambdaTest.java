package com.diozero;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
 * #%L
 * Device I/O Zero - Core
 * %%
 * Copyright (C) 2016 diozero
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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.pmw.tinylog.Logger;

import com.diozero.api.GpioScheduler;
import com.diozero.util.SleepUtil;

public class LambdaTest {
	private static final Random RANDOM = new Random(System.nanoTime());

	public static void main(String[] args) {
		Consumer<Void> consumer = (Void v) -> System.out.println("Hello consumer");

		consumer.accept(null);

		Command c = () -> System.out.println("Hello lambda");
		c.action();
		
		carryOutWork(() -> System.out.println("In here 1"));
		
		LambdaTest t = new LambdaTest();
		
		Logger.info("Using local invoke utility");
		for (int i=0; i<10; i++) {
			invoke(t::getValue, t::setValue);
			SleepUtil.sleepSeconds(1);
		}
		
		Logger.info("Using GpioScheduler");
		GpioScheduler.getInstance().invokeAtFixedRate(t::getValue, t::setValue, 100, 1000, TimeUnit.MILLISECONDS);
		for (int i=0; i<10; i++) {
			SleepUtil.sleepSeconds(1);
		}
		
		GpioScheduler.getInstance().shutdown();
	}
	
	static void invoke(Supplier<Float> source, Consumer<Float> sink) {
		sink.accept(source.get());
	}
	
	public float getValue() {
		return RANDOM.nextFloat();
	}
	
	public void setValue(float f) {
		System.out.println("setValue(" + f + ")");
	}

	interface Command extends Consumer<Void> {
		@Override
		default void accept(Void v) {
			action();
		}

		void action();
	}
	
	public static void carryOutWork(SimpleFuncInterface sfi){
		sfi.abc();
	}
}

@FunctionalInterface
interface SimpleFuncInterface {
	void abc();
}
