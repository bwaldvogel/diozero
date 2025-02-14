package com.diozero.sampleapps.mfrc522;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Sample applications
 * Filename:     DumpInfo.java
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

import com.diozero.devices.MFRC522;
import com.diozero.util.Hex;
import com.diozero.util.SleepUtil;

public class DumpInfo {
	public static void main(String[] args) {
		if (args.length < 4) {
			Logger.error("Usage: {} <spi-controller> <chip-select> <rst-gpio> <auth-key-hex>", ReadBlock.class.getName());
			System.exit(1);
		}
		int index = 0;
		int controller = Integer.parseInt(args[index++]);
		int chip_select = Integer.parseInt(args[index++]);
		int reset_pin = Integer.parseInt(args[index++]);
		byte[] auth_key = Hex.decodeHex(args[index++]);
		
		try (MFRC522 rfid = new MFRC522(controller, chip_select, reset_pin)) {
			rfid.dumpVersionToConsole();
			rfid.setLogReadsAndWrites(true);
			while (true) {
				Logger.info("Scanning for RFID tags...");
				MFRC522.UID uid = null;
				while (uid == null) {
					SleepUtil.sleepSeconds(1);
					if (rfid.isNewCardPresent()) {
						uid = rfid.readCardSerial();
					}
				}
				Logger.info("Found card with UID 0x{}, type: {}", Hex.encodeHexString(uid.getUidBytes()), uid.getType());
				
				rfid.dumpToConsole(uid, auth_key);
			}
		}
	}
}
