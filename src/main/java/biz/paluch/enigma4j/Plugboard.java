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

import org.springframework.util.Assert;

/**
 * Configurable patching of characters swaps supported by military editions of Enigma. The
 * patch bay allows up to 10 re-routing of letter mappings. Each mapping must be unique
 * and cannot overlap with already existing mappings.
 * <p>
 * Patching allows routing of characters so that {@code A} can be swapped with {@code T}
 * and vice-versa. To specify a patch mapping, use the {@link #withPatch(String)} method.
 * <p>
 * Example:
 *
 * <pre class="code">
 *       Patching patching = …;
 *       patching.withPatch("AE").withPatch("TZ");
 * </pre>
 *
 * @author Mark Paluch
 */
public class Plugboard {

	private final Alphabet alphabet;

	private final Patch[] patches;

	private final char[] table;

	private Plugboard(Alphabet alphabet) {

		Assert.notNull(alphabet, "Alphabet must not be null");

		this.alphabet = alphabet;

		var patch = new Patch[10];
		Arrays.fill(patch, Patch.UNPATCHED);

		this.patches = patch;
		this.table = createTable(patches);
	}

	private char[] createTable(Patch[] patches) {

		int max = Character.MIN_VALUE;

		for (var patch : patches) {
			max = Math.max(max, Math.max(patch.from(), patch.to()));
		}

		var table = new char[max + 1];

		for (var i = Character.MIN_VALUE; i < table.length; i++) {
			table[i] = i;
		}

		for (var patch : patches) {
			table[patch.from()] = patch.to();
			table[patch.to()] = patch.from();
		}

		return table;
	}

	private Plugboard(Alphabet alphabet, Patch[] patches) {

		this.alphabet = alphabet;
		this.patches = patches;
		this.table = createTable(patches);
	}

	/**
	 * Factory method to create an empty {@link Patchin}.
	 * @param alphabet the alphabet to use.
	 * @return
	 */
	public static Plugboard empty(Alphabet alphabet) {
		return new Plugboard(alphabet);
	}

	/**
	 * Create a new {@link Plugboard} with {@code tuple} applied. The tuple defines two
	 * characters from the underlying {@link Alphabet}.
	 * <p>
	 * Supports up to 10 patches between unique characters from the {@link Alphabet}.
	 * Patching a source/target character multiple times is not supported.
	 * @param tuple a 2-character tuple of the letter mapping between two different
	 * characters from the supported {@link Alphabet}. Directionality (source/target,
	 * target/source) has no significance.
	 * @return a new {@link Plugboard}.
	 */
	public Plugboard withPatch(String tuple) {

		Assert.hasLength(tuple, "Tuple must not be empty");
		Assert.isTrue(tuple.length() == 2, "Tuple must contain exactly two letters.");

		var c1 = tuple.charAt(0);
		var c2 = tuple.charAt(1);

		if (c1 == c2) {
			throw new IllegalArgumentException("Cannot patch between the same character");
		}

		if (!alphabet.contains(c1)) {
			throw new IllegalArgumentException("Character " + c1 + " is not supported by " + alphabet);
		}

		if (!alphabet.contains(c2)) {
			throw new IllegalArgumentException("Character " + c2 + " is not supported by " + alphabet);
		}

		var toPatch = Patch.from(c1).to(c2);

		for (var existingPatch : patches) {
			if (existingPatch.isPatched() && existingPatch.overlapsWith(toPatch)) {
				throw new IllegalStateException("This patch overlaps with %s".formatted(existingPatch));
			}
		}

		var unpatchedIndex = findUnpatchedIndex();

		var patches = new Patch[this.patches.length];
		System.arraycopy(this.patches, 0, patches, 0, patches.length);
		patches[unpatchedIndex] = toPatch;

		return new Plugboard(alphabet, patches);
	}

	private int findUnpatchedIndex() {

		for (var i = 0; i < patches.length; i++) {

			if (patches[i].isPatched()) {
				continue;
			}

			return i;
		}

		throw new IllegalStateException("Cannot find free patch index. All slots are patched");
	}

	/**
	 * Apply patch routing to a character.
	 * @param from the input character.
	 * @return routed character.
	 */
	public char route(char from) {

		if (table.length > from) {
			return table[from];
		}

		return from;
	}

}
