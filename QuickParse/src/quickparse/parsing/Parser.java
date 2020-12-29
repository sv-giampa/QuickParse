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

package quickparse.parsing;

import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.parsing.exception.UnexpectedSymbolException;

/**
 * Represents a generic parser.
 * A parser takes a {@link CharSequence sequence of characters} as input and returns a {@link SyntaxTree syntax tree} as output.
 */
public interface Parser {
	/**
	 * Parses a {@link CharSequence} starting from the proper axiom of a {@link quickparse.grammar.Grammar grammar}
	 * @param source the source {@link CharSequence} to parse
	 * @return A {@link SyntaxTree} object
	 * @throws ExpectedSymbolsException if an expected symbol is not found at some position
	 * @throws UnexpectedSymbolException if an unexpected symbol is found at the end of the source {@link CharSequence}
	 */
	SyntaxTree parse(CharSequence source) throws ExpectedSymbolsException, UnexpectedSymbolException;


	/**
	 * Parses a {@link CharSequence} starting from the specified construct of a {@link quickparse.grammar.Grammar grammar}
	 * @param source the source {@link CharSequence} to parse
	 * @param axiom the construct name to use as the axiom for the parsing process
	 * @return A {@link SyntaxTree} object
	 * @throws ExpectedSymbolsException if an expected symbol is not found at some position
	 * @throws UnexpectedSymbolException if an unexpected symbol is found at the end of the source {@link CharSequence}
	 */
	SyntaxTree parse(CharSequence source, String axiom) throws ExpectedSymbolsException, UnexpectedSymbolException;
}
