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
import java.io.Writer;

import org.springframework.util.Assert;

/**
 * {@link Writer} implementation using {@link Enigma} for character processing.
 *
 * @author Mark Paluch
 */
public class EnigmaWriter extends Writer {

	private final Writer out;

	private final Enigma enigma;

	public EnigmaWriter(Writer out, Enigma enigma) {

		Assert.notNull(out, "Writer must not be null");

		this.out = out;
		this.enigma = enigma;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		out.write(enigma.process(cbuf, off, len));

	}

	@Override
	public void flush() throws IOException {
		out.flush();

	}

	@Override
	public void close() throws IOException {
		out.close();
	}

}
