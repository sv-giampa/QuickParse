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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quickparse.grammar.Grammar;
import quickparse.grammar.Rule;
import quickparse.grammar.symbol.ConstructSymbol;
import quickparse.grammar.symbol.Symbol;
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.grammar.symbol.TokenSymbol;
import quickparse.parsing.exception.LeftRecursionException;
import quickparse.parsing.exception.UnexpectedSymbolException;
import quickparse.parsing.syntaxtree.TokenNode;

public class RecursiveDescentParser implements Parser {

	/**
	 * The grammar tht is interpreted by this parser
	 */
	private Grammar grammar;

	/**
	 * Recursively searches for a path from a rule to another. Helper method used to
	 * detect infinite recursions.
	 *
	 * @param r1 source rule
	 * @param r2 destination rule
	 * @return true if a path exists, false otherwise
	 */
	private boolean pathExists(Symbol r1, Symbol r2, Map<Symbol, Set<Symbol>> leftRec) {
		if (r1.equals(r2))
			return true;
		if (leftRec.containsKey(r1)) {
			for (Symbol next : leftRec.get(r1)) {
				if (pathExists(next, r2, leftRec))
					return true;
			}
		}
		return false;
	}

	private void detectLeftRecursions() throws LeftRecursionException {
		Map<Symbol, Set<Symbol>> leftRec = new HashMap<>();
		for (Rule rule : grammar)
			if (rule.getSymbols().size() > 0) {
				Symbol first = rule.getSymbols()
						.get(0);
				if (first instanceof ConstructSymbol) {
					leftRec.computeIfAbsent(rule.getHead(), k -> new HashSet<>())
							.add(first);
				}
			}

		for (Rule rule : grammar)
			if (rule.getSymbols().size() > 0) {
				// searches for infinite left recursions
				Symbol first = rule.getSymbols().get(0);

				if (first instanceof ConstructSymbol) {
					if (pathExists(first, rule.getHead(), leftRec))
						throw new LeftRecursionException(rule.getHead(), first);
				}
			}
	}

	public RecursiveDescentParser(Grammar grammar) throws LeftRecursionException {
		this.grammar = grammar;
		detectLeftRecursions();
	}

	@Override
	public SyntaxTree parse(CharSequence source, String axiom) throws ExpectedSymbolsException, UnexpectedSymbolException {
		// declare a LRU cache used to skip already parsed rules:
		// for every rule evaluated on a given index of the source,
		// it maps the syntax node already computed
		Map<List<Object>, SyntaxTree> lruCache = new LinkedHashMap<List<Object>, SyntaxTree>(
				200, 0.75f, true) {
			private static final long serialVersionUID = 7665786891333606339L;
			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				if (size() >= 200)
					return true;
				return false;
			}
		};

		class Context {
			int startIndex = 0;
			Set<TokenSymbol> expectedSymbols = new HashSet<>();
			int expectedAt = 0;
			boolean root = true;
			int unexpectedSymbolIndex;
		}

		Symbol.Function<SyntaxTree, Context> symbolFunction = new Symbol.Function<SyntaxTree, Context>() {

			@Override
			public SyntaxTree apply(Context context, TokenSymbol token) {
				List<Object> cacheKey = Arrays.asList(token, context.startIndex);
				if (lruCache.containsKey(cacheKey)) {
					// System.out.printf("Cache hit at %s for token %s\n", context.startIndex, token);
					return lruCache.get(cacheKey);
				} else {
					int startIndex = context.startIndex;
					CharSequence subSeq = source.subSequence(startIndex, source.length());
					Matcher matcher = token.getPattern()
							.matcher(subSeq);

					if(!matcher.lookingAt()) {
						startIndex = skipIgnoreableCharacters(source, context.startIndex);
						subSeq = source.subSequence(startIndex, source.length());
						matcher = token.getPattern()
								.matcher(subSeq);
					}

					if (matcher.lookingAt()) {
						TokenNode node = new TokenNode(source, startIndex, startIndex + matcher.end(),
								token.getName(), token.getPattern());
						lruCache.put(cacheKey, node);
						return node;
					} else {
						//System.out.printf("token %s not matched at %s\n", token, startIndex);
						if (startIndex > context.expectedAt) {
							context.expectedAt = startIndex;
							context.expectedSymbols.clear();
						}
						if(startIndex == context.expectedAt)
							context.expectedSymbols.add(token);
						return null;
					}
				}
			}

			@Override
			public SyntaxTree apply(Context context, ConstructSymbol construct) {
				List<Object> cacheKey = Arrays.asList(construct, context.startIndex);
				if (lruCache.containsKey(cacheKey)) {
					// System.out.printf("Cache hit at %s for construct %s\n", context.startIndex, construct);
					return lruCache.get(cacheKey);
				} else {
					// return tokenCache.computeIfAbsent(cacheKey, k -> {
					boolean root = context.root;
					context.root = false;
					int startIndex = context.startIndex;
					ConstructSymbol head = construct;
					List<Rule> rules = grammar.getRules()
							.get(head);

					SyntaxTree node = null;

					for (Rule rule : rules) {
						int index = startIndex;
						LinkedList<SyntaxTree> children = new LinkedList<>();
						for (Symbol symbol : rule.getSymbols()) {
							context.startIndex = index;
							SyntaxTree syntaxTree = symbol.accept(context, this);
							if (syntaxTree == null) {
								context.startIndex = startIndex;
								children = null;
								break;
							} else {
								index = syntaxTree.end;
								children.add(syntaxTree);
							}
						}

						if (children != null) {
							boolean success = false;
							int start = startIndex, end = startIndex;
							if (!children.isEmpty()) {
								start = children.getFirst().start;
								end = children.getLast().end;
							}

							if (root) {
								end = skipIgnoreableCharacters(source, end);
								if (end == source.length())
									success = true;
								else if (end > context.unexpectedSymbolIndex)
									context.unexpectedSymbolIndex = end;
							} else
								success = true;

							if (success) {
								node = new ConstructNode(source, start, end, head.getName(), children, rule);
								break;
							}
						}
					}
					lruCache.put(cacheKey, node);
					return node;
				}
			}
		};

		Context context = new Context();

		SyntaxTree node = grammar.getConstructSymbol(axiom).accept(context, symbolFunction);

		if (node == null) {
			if (!context.expectedSymbols.isEmpty())
				throw new ExpectedSymbolsException(source, context.expectedAt, context.expectedSymbols);
			else {
				throw new UnexpectedSymbolException(source, context.unexpectedSymbolIndex);
			}
		}

		return node;
	}

	@Override
	public SyntaxTree parse(CharSequence source) throws ExpectedSymbolsException, UnexpectedSymbolException {
		return parse(source, grammar.getAxiom().getName());
	}

	private int skipIgnoreableCharacters(CharSequence source, int startIndex) {
		boolean trySkip = true;
		while (trySkip) {
			trySkip = false;
			for (Pattern p : grammar.getIgnoredPatterns()) {
				CharSequence subSeq = source.subSequence(startIndex, source.length());
				Matcher m = p.matcher(subSeq);
				if (m.lookingAt()) {
					startIndex += m.end();
					// System.out.printf("new startIndex=%d; pattern=%s, source=\"%s\",
					// subSeq=\"%s\"\n", startIndex, p,
					// source, subSeq);
					trySkip = true;
					break;
				}
			}
		}
		return startIndex;
	}

	@Override
	public String toString() {
		return "RecursiveDescentParser @ Grammar:\n\t" + grammar.toString().replaceAll("\n", "\n\t");
	}

}
