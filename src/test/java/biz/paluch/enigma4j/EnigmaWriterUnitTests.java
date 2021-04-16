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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnigmaWriter}.
 *
 * @author Mark Paluch
 */
class EnigmaWriterUnitTests {

	Inventory inventory = Inventory.load();

	// commercial model
	Model model = inventory.getModel(new Inventory.ModelIdentifier("D"));

	@Test
	void shouldWriteBytes() {

		var instance = model.createInstance();

		var out = new StringWriter();

		var pw = new PrintWriter(new EnigmaWriter(out, instance));
		pw.print("hallowelt");

		assertThat(out.toString()).isEqualToIgnoringCase("alsyipmon");
	}

}
