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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.springframework.util.Assert;

/**
 * Value object describing an Enigma model with its supported {@link Alphabet} and
 * available {@link Rotor}s.
 *
 * @author Mark Paluch
 */
public record Model(String name, Alphabet alphabet, List<Rotor> rotors, List<Rotor> defaultConfiguration) {

	/**
	 * Create an {@link Enigma} using its configuration defaults.
	 * @return
	 */
	public Enigma createInstance() {
		return new Enigma(alphabet, defaultConfiguration, Plugboard.empty(alphabet));
	}

	/**
	 * Create an {@link Enigma} applying {@link EnigmaConfigurator}.
	 * @param configurator the callback accepting {@link EnigmaConfigurator}.
	 * @return
	 */
	public Enigma createInstance(Consumer<EnigmaConfigurator> configurator) {

		Assert.notNull(configurator, "Configurator must not be null");

		var rc = new DefaultRotorConfiguration(rotors, new ArrayList<>(), new ArrayList<>(), defaultConfiguration);
		var ec = new DefaultEnigmaConfiguration(rc, Plugboard.empty(alphabet));

		configurator.accept(ec);

		rc.verify();

		var enigma = new Enigma(alphabet, rc.configuration(), ec.plugboard);
		enigma.setRotorPositions(rc.rotorPositions().stream().mapToInt(it -> it).toArray());

		return enigma;
	}

	/**
	 * Interface to configure a {@link Enigma} instance to be created.
	 */
	interface EnigmaConfigurator {

		/**
		 * Apply a {@link Rotor} configuration.
		 * @param rotorConfigurator
		 * @return
		 */
		EnigmaConfigurator rotors(BiConsumer<RotorConfiguration, RotorSelector> rotorConfigurator);

		/**
		 * Apply a patching to the enigma configuration.
		 * @param plugboard
		 * @return
		 */
		EnigmaConfigurator withPatching(Plugboard plugboard);

		/**
		 * Apply a patching to the enigma configuration.
		 * @param patchingConfigurator
		 * @return
		 */
		EnigmaConfigurator withPatching(UnaryOperator<Plugboard> patchingConfigurator);

	}

	/**
	 * Query interface to obtain {@link Rotor} objects.
	 */
	interface RotorSelector {

		/**
		 * Return multiple {@link Rotor}s by {@code name}.
		 * @param names the rotor names.
		 * @return collection of rotors in the order of {@code names}.
		 * @throws NoSuchElementException if a {@link Rotor} could not be looked up by its
		 * name.
		 */
		Collection<Rotor> getRotors(String... names);

		/**
		 * Return all available rotors.
		 * @return
		 */
		Collection<Rotor> getRotors();

		/**
		 * Look up a {@link Rotor} by {@code name}.
		 * @param name the rotor name.
		 * @return the rotor.
		 * @throws NoSuchElementException if a {@link Rotor} could not be looked up by its
		 * {@code name}.
		 */
		Rotor getRotor(String name);

	}

	/**
	 * Configuration interface to attach {@link Rotor}s and their positions.
	 */
	interface RotorConfiguration {

		/**
		 * Add {@code rotors} to the Enigma machine.
		 * @param rotors
		 * @return
		 */
		RotorConfiguration withRotors(Iterable<Rotor> rotors);

		/**
		 * Add {@code rotors} to the Enigma machine.
		 * @param rotors
		 * @return
		 */
		RotorConfiguration withRotors(Rotor... rotors);

		/**
		 * Configure {@code rotorPositions}.
		 * @param rotorPositions
		 * @return
		 */
		RotorConfiguration withPositions(int... rotorPositions);

	}

	static class DefaultEnigmaConfiguration implements EnigmaConfigurator {

		private final DefaultRotorConfiguration rotorConfiguration;

		private Plugboard plugboard;

		public DefaultEnigmaConfiguration(DefaultRotorConfiguration rotorConfiguration, Plugboard plugboard) {
			this.rotorConfiguration = rotorConfiguration;
			this.plugboard = plugboard;
		}

		@Override
		public EnigmaConfigurator rotors(BiConsumer<RotorConfiguration, RotorSelector> rotorConfigurator) {

			Assert.notNull(rotorConfigurator, "Rotor configurator must not be null");

			rotorConfigurator.accept(rotorConfiguration, rotorConfiguration);
			return this;
		}

		@Override
		public EnigmaConfigurator withPatching(Plugboard plugboard) {

			Assert.notNull(plugboard, "Patching must not be null");

			this.plugboard = plugboard;
			return this;
		}

		@Override
		public EnigmaConfigurator withPatching(UnaryOperator<Plugboard> patchingConfigurator) {

			Assert.notNull(patchingConfigurator, "Patching configurator must not be null");

			return withPatching(patchingConfigurator.apply(this.plugboard));
		}

	}

	static record DefaultRotorConfiguration(List<Rotor> inventory, List<Rotor> configuration,
			List<Integer> rotorPositions,
			List<Rotor> defaultConfiguration) implements RotorConfiguration, RotorSelector {

		@Override
		public Collection<Rotor> getRotors(String... names) {

			Assert.notNull(names, "Names must not be null");

			return Arrays.stream(names).map(this::getRotor).toList();
		}

		@Override
		public Collection<Rotor> getRotors() {
			return Collections.unmodifiableList(inventory);
		}

		@Override
		public Rotor getRotor(String name) {

			Assert.notNull(name, "Name must not be null");

			for (var rotor : inventory) {
				if (rotor.name().equalsIgnoreCase(name)) {
					return rotor;
				}
			}

			throw new NoSuchElementException("Rotor '%s' not found".formatted(name));
		}

		@Override
		public DefaultRotorConfiguration withRotors(Iterable<Rotor> rotors) {

			Assert.notNull(rotors, "Rotors must not be null");

			rotors.forEach(configuration::add);
			return this;
		}

		@Override
		public DefaultRotorConfiguration withRotors(Rotor... rotors) {

			Assert.notNull(rotors, "Rotors must not be null");

			configuration.addAll(Arrays.asList(rotors));
			return this;
		}

		@Override
		public RotorConfiguration withPositions(int... rotorPositions) {

			Assert.notNull(rotorPositions, "Rotor positions must not be null");

			for (var rotorPosition : rotorPositions) {
				this.rotorPositions.add(rotorPosition);
			}

			return this;
		}

		void verify() {

			if (configuration.isEmpty()) {
				configuration.addAll(defaultConfiguration);
			}

			var rotatingRotors = configuration.stream().filter(Rotor::isRotating).count();

			while (rotorPositions.size() < rotatingRotors) {
				rotorPositions.add(0);
			}

			for (var i = 0; i < configuration.size(); i++) {

				var rotor = configuration.get(i);

				if (i != 0 && rotor.isEntry()) {
					throw new IllegalStateException(
							"Entry Rotor '%s' must be the first rotor and not on position %d of %d"
									.formatted(rotor.name(), i, configuration.size() - 1));
				}

				if (i != configuration.size() - 1 && rotor.isReversing()) {
					throw new IllegalStateException(
							"Reversing Rotor '%s' must be the last rotor and not on position %d of %d"
									.formatted(rotor.name(), i, configuration.size() - 1));
				}
			}
		}
	}

}
