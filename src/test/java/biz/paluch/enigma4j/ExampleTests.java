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

/**
 * Tests showing the code as it is used in the readme.
 *
 * @author Mark Paluch
 */
class ExampleTests {

	@Test
	void simple() {

		var inventory = Inventory.load();
		var modelI = inventory.getModel(new Inventory.ModelIdentifier("Enigma I"));
		var enigma = modelI.createInstance();
		var rotorPositions = enigma.getRotorPositions();

		assertThat(enigma.process("WURSTSALAT")).isEqualTo("FXYKEONOEX");

		enigma.setRotorPositions(rotorPositions);
		assertThat(enigma.process("FXYKEONOEX")).isEqualTo("WURSTSALAT");
	}

	@Test
	void config() {

		var inventory = Inventory.load();
		var modelI = inventory.getModel(new Inventory.ModelIdentifier("Enigma I"));

		var enigma = modelI.createInstance(c -> {
			c.rotors((rotorConfiguration, rotorSelector) -> rotorConfiguration
					.withRotors(rotorSelector.getRotors("II", "I", "III")).withPositions(1, 2, 3));
		});

		assertThat(enigma.process("WURSTSALAT")).isEqualTo("BPLKHVVVKT");
	}

}
