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

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Unit tests for {@link Enigma}.
 *
 * @author Mark Paluch
 */
class EnigmaUnitTests {

	Inventory inventory = Inventory.load();

	// commercial model, ETW, I, II, III, UKW
	Model modelD = inventory.getModel(new Inventory.ModelIdentifier("D"));

	// Enigma I, ETW, III, II, I, UKW A
	Model modelI = inventory.getModel(new Inventory.ModelIdentifier("Enigma I"));

	@TestFactory
	Stream<DynamicTest> shouldProperlyEncryptSingleChars() {

		record EncryptionCase(String input, String output) {
		}

		return Stream.of(new EncryptionCase("A", "H"), new EncryptionCase("Z", "T"))
				.map(data -> DynamicTest.dynamicTest(data.toString(), () -> {

					var instance = modelD.createInstance();

					var result = instance.process(data.input());
					assertThat(result).isEqualTo(data.output());
				}));
	}

	@TestFactory
	Stream<DynamicTest> shouldProperlyDecryptSingleChars() {

		record EncryptionCase(String input, String output) {
		}

		return Stream.of(new EncryptionCase("H", "A"), new EncryptionCase("T", "Z"))
				.map(data -> DynamicTest.dynamicTest(data.toString(), () -> {

					var instance = modelD.createInstance();

					var result = instance.process(data.input());
					assertThat(result).isEqualTo(data.output());
				}));
	}

	@Test
	void shouldRejectNonAlphabetChars() {

		var instance = modelD.createInstance();

		assertThatIllegalArgumentException().isThrownBy(() -> instance.process("\n"));
	}

	@Test
	void advanceAndEncrypt() {

		var instance = modelD.createInstance();

		assertThat(instance.process("hallo")).isEqualToIgnoringCase("alsyi");
		assertThat(instance.getRotorPositions()).containsExactly(5, 0, 0);

		assertThat(instance.process("welt")).isEqualToIgnoringCase("pmon");
		assertThat(instance.getRotorPositions()).containsExactly(9, 0, 0);
	}

	@Test
	void encryptShouldAdvancePositions() {

		var instance = modelD.createInstance();

		var before = instance.getRotorPositions();
		instance.process("a");
		var after = instance.getRotorPositions();

		assertThat(before).containsExactly(0, 0, 0);
		assertThat(after).containsExactly(1, 0, 0);
	}

	@Test
	void initializeShouldSetRotorPositions() {

		var instance = modelD.createInstance();

		instance.process("H");
		instance.setRotorPositions(0, 0, 0);

		assertThat(instance.process("H")).isEqualTo("A");
	}

	@Test
	void shouldEncryptAfterAdvance() {

		var instance = modelD.createInstance();

		instance.setRotorPositions(1, 0, 0);
		assertThat(instance.process("A")).isEqualToIgnoringCase("L");
	}

	@Test
	void advanceShouldRotateNextRotor() {

		var instance = modelI.createInstance();

		assertThat(instance.process("ABCDEFGHIJKLMNOPQRSTU")).isEqualTo("SKPTTUPKYFVACQLMWMEHE");
		assertThat(instance.getRotorPositions()).containsExactly(21, 0, 0);

		assertThat(instance.process("VWXYZ")).isEqualTo("HENIK");
		assertThat(instance.getRotorPositions()).containsExactly(0, 1, 0);

		assertThat(instance.process("FFFFF")).isEqualTo("WXQAZ");
		assertThat(instance.getRotorPositions()).containsExactly(5, 1, 0);
	}

	@Test
	void shouldApplyPatching() {

		var instance = modelI.createInstance(
				configurator -> configurator.withPatching(patching -> patching.withPatch("AC").withPatch("BF")));

		assertThat(instance.process("ABCDEFGHIJKLMNOPQRSTU")).isEqualTo("YOKTTGPKYBVCAQLMWMEHE");
	}

}
