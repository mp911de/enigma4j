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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link Plugboard}.
 *
 * @author Mark Paluch
 */
class PlugboardUnitTests {

	@Test
	void shouldRouteCharactersUnchanged() {

		var plugboard = Plugboard.empty(Alphabet.DEFAULT);

		assertThat(plugboard.route('A')).isEqualTo('A');
	}

	@Test
	void shouldApplyRouting() {

		var plugboard = Plugboard.empty(Alphabet.DEFAULT).withPatch("AT");

		assertThat(plugboard.route('A')).isEqualTo('T');
		assertThat(plugboard.route('T')).isEqualTo('A');
		assertThat(plugboard.route('B')).isEqualTo('B');
	}

	@Test
	void shouldRejectUnmappedAlphabetCharacters() {

		assertThatIllegalArgumentException().isThrownBy(() -> Plugboard.empty(new Alphabet("B")).withPatch("AT"));
		assertThatIllegalArgumentException().isThrownBy(() -> Plugboard.empty(new Alphabet("B")).withPatch("BT"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "AT", "TA", "AB", "BT", "TB", "BA" })
	void shouldRejectOverlappingPatching(String patch) {

		var plugboard = Plugboard.empty(Alphabet.DEFAULT).withPatch("AT");

		assertThatIllegalStateException().isThrownBy(() -> plugboard.withPatch(patch));
	}

	@Test
	void shouldRejectSameLetterPatch() {
		assertThatIllegalArgumentException().isThrownBy(() -> Plugboard.empty(Alphabet.DEFAULT).withPatch("AA"));
	}

}
