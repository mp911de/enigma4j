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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Inventory of Enigma {@link Model}s.
 *
 * @author Mark Paluch
 */
public class Inventory {

	private final Map<ModelIdentifier, Model> models;

	private Inventory(Map<ModelIdentifier, Model> models) {
		this.models = models;
	}

	/**
	 * Load the inventory.
	 * @return
	 */
	public static Inventory load() {

		var mapper = new ObjectMapper();

		try (var is = Inventory.class.getResourceAsStream("/inventory.json")) {

			ObjectNode node = mapper.createParser(is).readValueAsTree();
			var notches = node.get("notches").traverse(mapper.getFactory().getCodec()).readValueAs(Notches.class);
			var models = parseModels(mapper, node, notches);

			return new Inventory(models);
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot load inventory", e);
		}
	}

	private static Map<ModelIdentifier, Model> parseModels(ObjectMapper mapper, ObjectNode node, Notches notches)
			throws IOException {

		Map<ModelIdentifier, Model> models = new LinkedHashMap<>();
		Map<String, Map<String, Object>> modelDescriptors = node.get("models").traverse(mapper.getFactory().getCodec())
				.readValueAs(Map.class);

		for (var entry : modelDescriptors.entrySet()) {

			var identifier = new ModelIdentifier(entry.getKey());
			var model = parseModel(identifier, notches, entry.getValue());

			models.put(identifier, model);
		}

		return models;
	}

	/**
	 * Return the model identifiers.
	 * @return
	 */
	public Collection<ModelIdentifier> getModelIdentifiers() {
		return models.keySet();
	}

	/**
	 * Retrieve an Enigma model by {@link ModelIdentifier}.
	 * @param identifier
	 * @return
	 */
	public Model getModel(ModelIdentifier identifier) {

		Assert.notNull(identifier, "ModelIdentifier must not be null");

		var model = models.get(identifier);

		if (model == null) {
			throw new NoSuchElementException("No such model: %s".formatted(identifier));
		}

		return model;
	}

	@SuppressWarnings("unchecked")
	private static Model parseModel(ModelIdentifier identifier, Notches notches, Map<String, Object> modelDescriptor) {

		var alphabet = new Alphabet((String) modelDescriptor.get("alphabet"));
		var rotorDescriptor = (Map<String, String>) modelDescriptor.get("rotors");
		var defaultConfiguration = (List<String>) modelDescriptor.get("defaultConfiguration");
		var notchRef = (String) modelDescriptor.get("notch");
		var rotorNotches = notches.get(notchRef);

		var rotors = parseRotors(rotorNotches, rotorDescriptor);
		var defaultRotors = getDefaultRotors(rotors, defaultConfiguration);

		return new Model(identifier.id(), alphabet, rotors, defaultRotors);

	}

	private static List<Rotor> getDefaultRotors(List<Rotor> rotors, @Nullable List<String> defaultConfiguration) {

		List<Rotor> result = new ArrayList<>();

		if (defaultConfiguration == null) {
			return rotors;
		}

		for (var rotorRef : defaultConfiguration) {
			result.add(getRotor(rotors, rotorRef));
		}

		return result;
	}

	private static Rotor getRotor(List<Rotor> rotors, String rotorRef) {

		for (var rotor : rotors) {
			if (rotor.name().equals(rotorRef)) {
				return rotor;
			}
		}

		throw new NoSuchElementException("Cannot find Rotor '%s' for the default configuration".formatted(rotorRef));
	}

	private static List<Rotor> parseRotors(RotorNotches rotorNotches, Map<String, String> rotorDescriptor) {

		List<Rotor> rotors = new ArrayList<>();

		for (var entry : rotorDescriptor.entrySet()) {

			var rotorName = entry.getKey();
			var wiring = entry.getValue();
			rotors.add(new Rotor(rotorName, wiring, rotorNotches.getNotchesFor(rotorName)));
		}

		return rotors;
	}

	/**
	 * Identifies an Enigma model. Typically the model name.
	 */
	public static record ModelIdentifier(String id) {

		public ModelIdentifier {
			Assert.hasText(id, "Identifier must not be empty");
		}

		@Override
		public String toString() {
			return id;
		}
	}

	static record RotorNotches(Map<String, List<String>> at) {

		@JsonCreator
		RotorNotches {
		}

		public List<String> getNotchesFor(String rotorName) {

			Assert.hasText(rotorName, "Rotor name must not be null and not empty");
			var notches = at.get(rotorName);

			if (notches == null) {
				return Collections.emptyList();
			}

			return Collections.unmodifiableList(notches);
		}
	}

	static record Notches(Map<String, RotorNotches> notches) {

		@JsonCreator
		Notches {
		}

		public RotorNotches get(String notchRef) {

			var rotorNotches = notches.get(notchRef);

			if (rotorNotches == null) {
				throw new IllegalArgumentException("No notch configuration found for '%s'".formatted(notchRef));
			}

			return rotorNotches;
		}
	}

}
