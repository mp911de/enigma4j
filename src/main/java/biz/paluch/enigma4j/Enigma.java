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

import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Runtime instance of a {@link Model Enigma Model}. An enigma is configured with
 * {@link Rotor Rotors} that are set to a particular position. New instances are created
 * with all rotors set to zero. The order of rotors follows in the order of signal flow,
 * typically starting with a non-rotating entry rotor (ETW, {@code Eintrittswalze}), then
 * a number of cipher rotors (I, II, …) and the reverser (UKW, {@code Umkehrwalze}).
 * <p>
 * Right now, {@link Plugboard} is not yet supported. The machine only accepts characters
 * from the configured {@link Alphabet} and rejects
 *
 * @author Mark Paluch
 */
public class Enigma {

	private static final Logger LOG = LoggerFactory.getLogger(Enigma.class);

	private final Alphabet alphabet;

	private final Plugboard plugboard;

	private final int rotatingRotors;

	private final AbstractRotorContext context;

	Enigma(Alphabet alphabet, List<Rotor> rotors, Plugboard plugboard) {

		Assert.notEmpty(rotors, "Rotors must not be empty");

		this.alphabet = alphabet;
		this.plugboard = plugboard;
		this.rotatingRotors = (int) rotors.stream().filter(Rotor::isRotating).count();
		this.context = createContext(alphabet, rotors);
	}

	private AbstractRotorContext createContext(Alphabet alphabet, List<Rotor> rotors) {

		AbstractRotorContext current = null;

		for (var i = 0; i < rotors.size(); i++) {

			var ctx = new DefaultRotorContext(rotors.get(i), alphabet);

			if (i == 0) {
				current = ctx;
				current.prev = AbstractRotorContext.HEAD;
			}
			else {
				current.next = ctx;
				ctx.prev = current;
			}

			if (i == rotors.size() - 1) {
				ctx.next = AbstractRotorContext.TAIL;
			}
			current = ctx;
		}

		while (current != null && current.prev != AbstractRotorContext.HEAD) {
			current = current.prev;
		}

		return current;
	}

	/**
	 * Enter a {@code input} for Enigma processing.
	 * @param input
	 * @return
	 */
	public String process(String input) {

		Assert.notNull(input, "Input must not be null");

		var chars = input.toCharArray();

		return new String(process(chars, 0, chars.length));
	}

	/**
	 * Processes a portion of an array of characters.
	 * @param input array of characters.
	 * @param offset offset from which to start writing characters.
	 * @param len number of characters to write.
	 * @throws IndexOutOfBoundsException Implementations should throw this exception if
	 * {@code offset} is negative, or {@code len} is negative, or {@code offset + len} is
	 * negative or greater than the length of the given array
	 */
	public char[] process(char[] input, int offset, int len) {

		for (var i = 0; i < len; i++) {

			var ch = input[i + offset];
			if (!alphabet.contains(Character.toUpperCase(ch))) {
				throw new IllegalArgumentException("Character '%s' (0x%s) is not part of the alphabet".formatted(ch,
						Integer.toHexString(ch).toUpperCase(Locale.ROOT)));
			}
		}

		var out = new char[len];
		for (var i = 0; i < len; i++) {

			var ch = input[i + offset];
			out[i] = plugboard.route(context.enter(plugboard.route(Character.toUpperCase(ch))));
		}

		return out;

	}

	/**
	 * Apply rotor positions of rotating rotors where the first position maps to the first
	 * rotor and so on.
	 * @param positions the positions to apply. Must match the number of rotors.
	 * @throws IllegalArgumentException if the number of position doesn't match to the
	 * number of rotating rotors.
	 * @see Rotor#isRotating()
	 */
	public void setRotorPositions(int... positions) {

		Assert.notNull(positions, "Positions must not be null");
		Assert.isTrue(positions.length == rotatingRotors,
				() -> "Number of positions (%d) does not match number of rotors (%d)".formatted(positions.length,
						rotatingRotors));

		var index = 0;
		for (var context = this.context; context != AbstractRotorContext.TAIL; context = context.next) {
			if (context.isRotating()) {
				context.setPosition(positions[index++]);
			}
		}
	}

	/**
	 * Return the current rotor positions of rotating rotors.
	 * @return
	 * @see Rotor#isRotating()
	 */
	public int[] getRotorPositions() {

		var positions = new int[rotatingRotors];
		var index = 0;
		for (var context = this.context; context != AbstractRotorContext.TAIL; context = context.next) {
			if (context.isRotating()) {
				positions[index++] = context.getPosition();
			}
		}

		return positions;
	}

	/**
	 * Context to model the processing pipeline.
	 */
	static abstract class AbstractRotorContext {

		static final AbstractRotorContext HEAD = new AbstractRotorContext() {
			@Override
			char processInput(char input) {
				return 0;
			}

			@Override
			char processOutput(char input) {
				return 0;
			}

			@Override
			boolean advance() {
				return false;
			}

			@Override
			void setPosition(int position) {
			}

			@Override
			public String toString() {
				return "HEAD";
			}
		};

		static final AbstractRotorContext TAIL = new AbstractRotorContext() {
			@Override
			char processInput(char input) {
				return 0;
			}

			@Override
			char processOutput(char input) {
				return 0;
			}

			@Override
			boolean advance() {
				return false;
			}

			@Override
			void setPosition(int position) {
			}

			@Override
			public String toString() {
				return "TAIL";
			}
		};

		@Nullable
		AbstractRotorContext next;

		@Nullable
		AbstractRotorContext prev;

		/**
		 * Process rotor (forward) inbound wiring.
		 * @param input
		 * @return
		 */
		abstract char processInput(char input);

		/**
		 * Process rotor (reverse) outbound wiring.
		 * @param input
		 * @return
		 */
		abstract char processOutput(char input);

		/**
		 * Advance rotor position by {@code 1}.
		 * @return
		 */
		abstract boolean advance();

		/**
		 * Set the rotor position to {@code position}.
		 * @param position
		 */
		abstract void setPosition(int position);

		/**
		 * Enter a character into the machine for processing.
		 * @param input
		 * @return
		 */
		public char enter(char input) {

			var transformed = input;

			var reverse = this;

			for (var context = this; context != TAIL; context = context.next) {

				var in = transformed;
				transformed = context.processInput(transformed);

				LOG.debug("FWD [" + context + "] " + in + " -> " + transformed);
				reverse = context;
			}

			reverse = reverse.prev;

			// reflector
			for (; reverse != HEAD; reverse = reverse.prev) {

				var in = transformed;
				transformed = reverse.processOutput(transformed);

				LOG.debug("REV [" + reverse + "] " + in + " -> " + transformed);
			}

			return transformed;
		}

		public boolean isRotating() {
			return false;
		}

		public int getPosition() {
			return 0;
		}

	}

	static class DefaultRotorContext extends AbstractRotorContext {

		private final Rotor rotor;

		private final char[] mapping;

		private final int[] reverseMapping;

		private final BitSet notches;

		private final int[] alphabetIndex;

		private final char[] charMap;

		private int currentPosition;

		DefaultRotorContext(Rotor rotor, Alphabet alphabet) {

			this.rotor = rotor;

			this.mapping = rotor.wiring().toCharArray();
			this.notches = new BitSet(this.mapping.length);

			for (var notch : rotor.notches()) {
				notches.set(alphabet.indexOf(notch));
			}

			this.alphabetIndex = alphabet.alphabetIndex();
			this.reverseMapping = alphabet.mappingIndex(rotor.wiring());
			this.charMap = alphabet.toCharMap();
		}

		@Override
		char processInput(char input) {

			if (this.prev == HEAD) {
				for (var context = !rotor.isRotating() ? this.next : this; context != TAIL; context = context.next) {
					if (!context.advance()) {
						break;
					}
				}
			}

			var inputIndex = alphabetIndex[input];
			var outIndex = (inputIndex + currentPosition) % mapping.length;
			var wiring = mapping[outIndex];

			var verdrehung = (alphabetIndex[wiring] - currentPosition);
			verdrehung = (0 > verdrehung ? charMap.length + verdrehung : verdrehung) % mapping.length;

			return charMap[verdrehung];
		}

		@Override
		char processOutput(char input) {

			var inputPin = (alphabetIndex[input] + currentPosition) % mapping.length;
			var translatedInput = charMap[inputPin];

			var inputIndex = reverseMapping[translatedInput];
			var outIndex = (inputIndex - currentPosition) % mapping.length;
			outIndex = (0 > outIndex ? charMap.length + outIndex : outIndex) % mapping.length;

			var wiring = charMap[outIndex];
			var verdrehung = (alphabetIndex[wiring]) % mapping.length;

			return charMap[verdrehung];
		}

		@Override
		boolean advance() {

			if (!isRotating()) {
				return false;
			}

			currentPosition++;

			if (currentPosition >= mapping.length) {
				currentPosition = 0;
			}
			var notchPosition = currentPosition - 1;

			if (0 > notchPosition) {
				notchPosition = notchPosition + mapping.length;
			}

			return notches.get(notchPosition);
		}

		@Override
		void setPosition(int position) {

			if (rotor.isEntry() || rotor.isReversing()) {
				currentPosition = 0;
				return;
			}

			currentPosition = position;
		}

		@Override
		public boolean isRotating() {
			return rotor.isRotating();
		}

		@Override
		public int getPosition() {
			return currentPosition;
		}

		@Override
		public String toString() {
			return rotor + "[" + currentPosition + ']';
		}

	}

}
