package com.diozero.animation.easing;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     EasingFunctions.java
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


import java.util.HashMap;
import java.util.Map;

public class EasingFunctions {
	private static Map<String, EasingFunction> EASING_FUNCTIONS;
	static {
		EASING_FUNCTIONS = new HashMap<>();
		
		EASING_FUNCTIONS.put(Linear.LINEAR, Linear::ease);
		EASING_FUNCTIONS.put(Quad.IN, Quad::easeIn);
		EASING_FUNCTIONS.put(Quad.OUT, Quad::easeOut);
		EASING_FUNCTIONS.put(Quad.IN_OUT, Quad::easeInOut);
		EASING_FUNCTIONS.put(Cubic.IN, Cubic::easeIn);
		EASING_FUNCTIONS.put(Cubic.OUT, Cubic::easeOut);
		EASING_FUNCTIONS.put(Cubic.IN_OUT, Cubic::easeInOut);
		EASING_FUNCTIONS.put(Quart.IN, Quart::easeIn);
		EASING_FUNCTIONS.put(Quart.OUT, Quart::easeOut);
		EASING_FUNCTIONS.put(Quart.IN_OUT, Quart::easeInOut);
		EASING_FUNCTIONS.put(Quintic.IN, Quintic::easeIn);
		EASING_FUNCTIONS.put(Quintic.OUT, Quintic::easeOut);
		EASING_FUNCTIONS.put(Quintic.IN_OUT, Quintic::easeInOut);
		EASING_FUNCTIONS.put(Sine.IN, Sine::easeIn);
		EASING_FUNCTIONS.put(Sine.OUT, Sine::easeOut);
		EASING_FUNCTIONS.put(Sine.IN_OUT, Sine::easeInOut);
		EASING_FUNCTIONS.put(Exponential.IN, Exponential::easeIn);
		EASING_FUNCTIONS.put(Exponential.OUT, Exponential::easeOut);
		EASING_FUNCTIONS.put(Exponential.IN_OUT, Exponential::easeInOut);
		EASING_FUNCTIONS.put(Circular.IN, Circular::easeIn);
		EASING_FUNCTIONS.put(Circular.OUT, Circular::easeOut);
		EASING_FUNCTIONS.put(Circular.IN_OUT, Circular::easeInOut);
		EASING_FUNCTIONS.put(Back.IN, Back::easeIn);
		EASING_FUNCTIONS.put(Back.OUT, Back::easeOut);
		EASING_FUNCTIONS.put(Back.IN_OUT, Back::easeInOut);
		EASING_FUNCTIONS.put(Bounce.IN, Bounce::easeIn);
		EASING_FUNCTIONS.put(Bounce.OUT, Bounce::easeOut);
		EASING_FUNCTIONS.put(Bounce.IN_OUT, Bounce::easeInOut);
		EASING_FUNCTIONS.put(Elastic.IN, Elastic::easeIn);
		EASING_FUNCTIONS.put(Elastic.OUT, Elastic::easeOut);
		EASING_FUNCTIONS.put(Elastic.IN_OUT, Elastic::easeInOut);
	}
	
	public static EasingFunction forName(String name) {
		return EASING_FUNCTIONS.get(name);
	}
}
