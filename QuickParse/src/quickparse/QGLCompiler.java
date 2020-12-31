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

package quickparse;

import quickparse.grammar.Grammar;
import quickparse.grammar.Rule;
import quickparse.parsing.RecursiveDescentParser;
import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.parsing.syntaxtree.SyntaxTreeVisitor;
import quickparse.semantics.interpreters.exception.SemanticsException;
import quickparse.parsing.Parser;
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.exception.UnexpectedSymbolException;
import quickparse.parsing.syntaxtree.TokenNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class implements a compiler for grammars. It allows to build {@link Grammar} objects
 * represented through the Quick Grammar Language.<br>
 *
 * <h1>Quick Grammar Language (QGL) specifications</h1>
 * It is a simple formal language designed to quickly describe grammars.<br>
 * A QGL source string is composed by two macro blocks:
 * <ul>
 *     <li>Ignored Patterns: a list of Regular Expressions that represents text pattern to be ignored
 *     everywhere in the source of the built grammar (e.g., white-spaces or comments); See {@link Grammar}
 *     for more details.</li>
 *     <li>Grammar Rules: the list of grammar rules; each rule is encoded by specifying a construct that is the
 *     rule head, followed by '->' or '=' symbols, and by the list of symbols that produce the head construct.
 *     </li>
 * </ul>
 *
 * <h2>Ignored Patterns</h2>
 * Each ignored pattern must be specified on a single line, before any grammar rule, in the following way:<br>
 * <pre><code>ignore:pattern/$</code></pre>
 * where 'pattern' is the Regular Expression as supported by the Java Language, and '/$' is the termination
 * character sequence. Note that the pattern starts just after the ':' separator.
 *
 * <h2>Grammar Rules</h2>
 * Each grammar rule must be specified on a single line in one of the following ways:<br>
 * <pre><code>HEAD -> SYMBOL1 SYMBOL2 ...</code></pre>
 * <pre><code>HEAD = SYMBOL1 SYMBOL2 ...</code></pre>
 * Where 'HEAD' is the name of the construct that is produce by the rule, 'SYMBOL#' are the names of the constructs  or
 * the token symbols that produce the 'HEAD' construct.
 * A token symbol can be specified as a component of the rule body, by specifying a name (that can be empty) and a
 * pattern, as in the following example:<br>
 * <pre><code>my_construct -> my_token:my_regex1/$ ...</code></pre>
 * <pre><code>my_construct -> :my_regex2/$ ...</code></pre>
 * The first rule body specifies a token that is named 'my_token' and is produced by the 'my_regex1' pattern.<br>
 * The second rule body specifies a token that has no name (starts directly with the separator ':') and is produced by the 'my_regex2' pattern.<br>
 *
 */
public class QGLCompiler {

	private static Grammar qglGrammar = Grammar.create()
			.ignorePatterns(
					"//[^\\n]*",	// comment line
					"(?s)(/\\*.*?\\*/)"	// comment block
			)

			.addRule(Rule.head("grammar").produces("ignore_patterns", "rules"))

			.addRule(Rule.head("ignore_patterns").produces("ignore_pattern", ":\\n", "ignore_patterns"))
			.addRule(Rule.head("ignore_patterns").produces("ignore_pattern"))
			.addRule(Rule.head("ignore_patterns").produces(":( |\t)*\\n", "ignore_patterns"))
			.addRule(Rule.head("ignore_patterns").produces())
			.addRule(Rule.head("ignore_pattern").produces(":( |\t)*ignore\\:", "ignored_pattern:.*?\\/\\$", ":( |\t)*"))

			.addRule(Rule.head("rules").produces("rule", ":( |\t)*\\n", "rules"))
			.addRule(Rule.head("rules").produces("rule"))
			.addRule(Rule.head("rules").produces(":( |\t)*\\n", "rules"))
			.addRule(Rule.head("rules").produces())

			.addRule(Rule.head("rule").produces(":( |\t)*", "construct", ":( |\t)*(\\=|\\-\\>)", "rule_body"))
			.addRule(Rule.head("rule_body").produces("rule_tail"))
			.addRule(Rule.head("rule_body").produces(":( |\t)*/"))
			.addRule(Rule.head("rule_tail").produces(":( |\t)*", "symbol", "rule_tail"))
			.addRule(Rule.head("rule_tail").produces(":( |\t)*", "symbol"))

			.addRule(Rule.head("symbol").produces("token"))
			.addRule(Rule.head("symbol").produces("construct"))
			.addRule(Rule.head("token").produces("token_name:([a-zA-Z_][a-zA-Z0-9_\\-]*)", "token_pattern"))
			.addRule(Rule.head("token").produces("token_pattern"))
			.addRule(Rule.head("token_pattern").produces(":\\:", "token_pattern:.*?\\/\\$"))
			.addRule(Rule.head("construct").produces("construct_name:[a-zA-Z_][a-zA-Z0-9_\\-]*"))
			.build();

	private static Parser qglParser = new RecursiveDescentParser(qglGrammar);

	private static class QGLSyntaxTreeVisitor implements SyntaxTreeVisitor {
		Grammar grammar;
		Grammar.Builder grammarBuilder;
		Rule.Builder ruleBuilder;
		String tokenName;

		@Override
		public void token(TokenNode node) {
			switch(node.name){
				case "token_name":
					tokenName = node.value.toString();
					break;
				case "token_pattern":
					String tokenPattern = node.value.subSequence(0, node.value.length()-2).toString();
					ruleBuilder.token(tokenName, tokenPattern);
					break;
				case "construct_name":
					if(ruleBuilder==null){ // the node is a rule head
						ruleBuilder = Rule.head(node.value.toString());
					} else { // the node is a rule body symbol
						ruleBuilder.construct(node.value.toString());
					}
					break;
				case "ignored_pattern":
					String pattern = node.value.subSequence(0, node.value.length()-2).toString();
					grammarBuilder.ignorePatterns(pattern);
					break;
			}
		}

		@Override
		public void enterConstruct(ConstructNode node) {
			switch (node.name){
				case "grammar": // init grammar builder
					grammarBuilder = Grammar.create();
					break;
				case "rule": // nullify the rule builder
					ruleBuilder = null;
					break;
				case "token":
					tokenName = ""; // default token name, when absent
					break;
			}
		}

		@Override
		public void exitConstruct(ConstructNode node) {
			switch (node.name){
				case "grammar": // build the grammar
					grammar = grammarBuilder.build();
					break;
				case "rule": // add the rule to the grammar
					grammarBuilder.addRule(ruleBuilder);
					break;
			}
		}

		public Grammar getGrammar() {
			return grammar;
		}

		public Rule getLastRule() {
			return ruleBuilder.build();
		}
	}

	/**
	 * Compiles a {@link Grammar grammar} from a resource text file.
	 * @param classLoader the class loader to use to load the resource
	 * @param resource the resource path
	 * @return the compiled {@link Grammar grammar}
	 * @throws IOException if an I/O error occurs while reading the resource
	 * @throws IllegalArgumentException if the specified resource cannot be found through the specified class loader
	 * @throws ExpectedSymbolsException if an expected symbol is not found at some position
	 * @throws UnexpectedSymbolException if an unexpected symbol is found at the end of the source
	 */
	public static Grammar fromResource(ClassLoader classLoader, String resource) throws IOException, ExpectedSymbolsException, UnexpectedSymbolException {
		InputStream input = classLoader.getResourceAsStream(resource);
		if(input == null)
			throw new IllegalArgumentException("the specified resource '" + resource +
					"' cannot be found through the specified class loader '" + classLoader + "'");
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(input));
		String line;
		String source;
		while((line = r.readLine()) != null){
			sb.append(line);
			sb.append('\n');
		}
		source = sb.toString();
		input.close();
		return compile(source);
	}

	/**
	 * Compiles a {@link Grammar grammar} from a resource text file.
	 * @param source the source string
	 * @return the compiled {@link Grammar grammar}
	 * @throws ExpectedSymbolsException if an expected symbol is not found at some position
	 * @throws UnexpectedSymbolException if an unexpected symbol is found at the end of the source
	 */
	public static Grammar compile(CharSequence source) throws ExpectedSymbolsException, UnexpectedSymbolException {
		//System.out.println("QGLGrammar:\n\t" + qglGrammar.toString().replaceAll("\n", "\n\t"));
		return new RecursiveDescentParser(qglGrammar) 	// selects recursive descent strategy
				.parse(source)							// do parsing
				.accept(new QGLSyntaxTreeVisitor())		// do semantic analysis
				.getGrammar();							// gets result
	}
}
