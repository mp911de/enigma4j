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

import java.util.List;

import org.springframework.util.Assert;

/**
 * Value object capturing a rotor configuration along its wiring and {@code notches}.
 * Special types of rotors are {@link #isReversing() reversing} rotors that reverse the
 * signal and {@link #isEntry() entry rotors} that pre-process the entered value.
 *
 * @author Mark Paluch
 */
public record Rotor(String name, String wiring, List<String> notches) {

	public Rotor {
		Assert.hasText(name, "Name must not be empty");
		Assert.hasText(wiring, "Key must not be empty");
		Assert.notNull(notches, "Notches must not be null");
	}

	public boolean isReversing() {
		return name.startsWith("UKW");
	}

	public boolean isEntry() {
		return name.startsWith("ETW");
	}

	public boolean isRotating() {
		return !isEntry() && !isReversing();
	}
}
