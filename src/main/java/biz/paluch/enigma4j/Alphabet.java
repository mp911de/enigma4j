/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.enigma4j;

import java.util.Arrays;
import java.util.Locale;

import org.springframework.util.Assert;

/**
 * Value object representing the supported alphabet consisting of a series of unique
 * characters.
 *
 * @author Mark Paluch
 */
public record Alphabet(String chars) {

	public static Alphabet DEFAULT = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

	public Alphabet(String chars) {

		Assert.hasText(chars, "Alphabet must not be empty");

		var az = chars.toUpperCase(Locale.ROOT).toCharArray();
		Arrays.sort(az);
		this.chars = new String(az);
	}

	/**
	 * Return whether {@code character} is part of the alphabet.
	 * @param character
	 * @return
	 */
	public boolean contains(char character) {
		return contains(new String(new char[] { character }));
	}

	/**
	 * Return whether {@code character} is part of the alphabet.
	 * @param character
	 * @return
	 */
	public boolean contains(String character) {
		return chars.contains(character);
	}

	/**
	 * Return the alphabetical index of the {@code character}.
	 * @param character
	 * @return
	 */
	public int indexOf(String character) {
		return chars.indexOf(character);
	}

	/**
	 * Create a mapping array between the alphabet character and its respective position
	 * within the alphabet.
	 * @return
	 */
	int[] alphabetIndex() {

		var chars = toCharMap();

		int highest = chars[chars.length - 1];
		var charmap = new int[highest + 1];

		for (var i = 0; i < chars.length; i++) {
			charmap[chars[i]] = i;
		}

		return charmap;
	}

	/**
	 * Create a {@code char[]} with all alphabet characters.
	 * @return
	 */
	char[] toCharMap() {
		return this.chars.toCharArray();
	}

	/**
	 * Create a mapping index of the {@code wiring} characters to their alphabetical
	 * order.
	 * @param wiring
	 * @return
	 */
	int[] mappingIndex(String wiring) {

		var chars = toCharMap();
		int highest = chars[chars.length - 1];

		var wiringChars = wiring.toCharArray();
		var charmap = new int[highest + 1];

		var index = 0;
		for (var wiringChar : wiringChars) {
			charmap[wiringChar] = index++;
		}

		return charmap;
	}
}
