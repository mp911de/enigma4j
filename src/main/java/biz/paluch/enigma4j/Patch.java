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

/**
 * Patch between two {@code characters}.
 *
 * @author Mark Paluch
 */
public record Patch(char from, char to) {

	/**
	 * Unpatched instance.
	 */
	public static Patch UNPATCHED = new Patch(Character.MIN_VALUE, Character.MIN_VALUE);

	/**
	 * Factory method to create a {@link Patch} using a fluent API.
	 * @param from
	 * @return
	 */
	public static OngoingPatch from(char from) {
		return to -> new Patch(from, to);
	}

	/**
	 * Return {@code true} if the patch represents a patched variant.
	 * @return
	 */
	public boolean isPatched() {
		return from != Character.MIN_VALUE && to != Character.MIN_VALUE;
	}

	/**
	 * Check whether this patch overlaps with another {@link Patch}.
	 * @param other
	 * @return
	 */
	public boolean overlapsWith(Patch other) {

		if (this.from() == other.from() || this.from() == other.to()) {
			return true;
		}

		return this.to() == other.to() || this.to() == other.from();
	}

	/**
	 * Continuation interface.
	 */
	public interface OngoingPatch {

		/**
		 * Create a new {@link Patch} by providing the target {@code character}.
		 * @param to
		 * @return
		 */
		Patch to(char to);

	}
}
