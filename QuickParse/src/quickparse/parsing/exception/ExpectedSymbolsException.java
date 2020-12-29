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

import java.util.Collections;
import java.util.Set;

import quickparse.grammar.symbol.TokenSymbol;

public class ExpectedSymbolsException extends Exception {
	private static final long serialVersionUID = 3417445039412648719L;

	public final Set<TokenSymbol> expectedSymbols;
	public final CharSequence source;
	public final int position;

	private static String escapeChar(CharSequence source, int position){
		if(position >= source.length())
			return "<end-of-source>";
		char ch = source.charAt(position);
		switch(ch){
			case ' ':
				return "<white space>";
			case '\n':
				return "<new-line>";
			case '\r':
				return "<carriage-return>";
			case '\t':
				return "<tab>";
			default:
				return "'" + ch + "'";
		}
	}

	public ExpectedSymbolsException(CharSequence source, int position, Set<TokenSymbol> expectedSymbols) {
		super("At position " + position + ": Expected symbols " + expectedSymbols +
				", but " + escapeChar(source, position) + " was found");
		this.expectedSymbols = Collections.unmodifiableSet(expectedSymbols);
		this.source = source;
		this.position = position;
	}
}
