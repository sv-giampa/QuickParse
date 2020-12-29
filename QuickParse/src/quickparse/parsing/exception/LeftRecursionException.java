/*
 * Copyright 2020 Salvatore Giampa'
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
 *
 */

package quickparse.parsing.exception;

import quickparse.grammar.symbol.Symbol;

public class LeftRecursionException extends RuntimeException {

	private static final long serialVersionUID = -489936479166788762L;

	final Symbol headSymbol;
	final Symbol firstSymbol;

	public LeftRecursionException(Symbol headSymbol, Symbol firstSymbol) {
		super(String.format(
				"Infinite left recusion detected for %s -> %s. The used parser cannot solve left recursions.",
				headSymbol, firstSymbol));
		this.headSymbol = headSymbol;
		this.firstSymbol = firstSymbol;
	}

}
