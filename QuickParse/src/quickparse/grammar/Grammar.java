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

package quickparse.grammar;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import quickparse.grammar.symbol.Symbol;
import quickparse.grammar.symbol.ConstructSymbol;
import quickparse.grammar.symbol.TokenSymbol;

/**
 * Represents a BNF (Backus-Naur Form) grammar as a set of syntactical rules.
 * To create a new instance of this class, the method {@link #create()} must be used.
 * A grammar is an iterable object whose elements are the grammar rules.
 */
public class Grammar implements Iterable<Rule> {
	private Set<Pattern> ignoredPatterns = new HashSet<>();
	private ConstructSymbol axiom = null;
	private Map<ConstructSymbol, List<Rule>> rules = new HashMap<>();
	private List<Rule> ruleList = new LinkedList<>();
	private Map<String, TokenSymbol> tokenSymbols = new TreeMap<>();
	private Map<String, ConstructSymbol> constructSymbols = new TreeMap<>();

	private Grammar() {}

	/**
	 * Returns the list of patterns to ignore. The ignored patterns usually are spaces and comment patterns
	 * (e.g. '//...\n' or '#...\n')
	 * @return a list of {@link Pattern patterns}
	 */
	public Set<Pattern> getIgnoredPatterns() {
		return ignoredPatterns;
	}

	/**
	 * Returns the rules contained in this grammar.
	 * @return A map that associates a {@link ConstructSymbol} to the rules contained in this grammar that produce it.
	 */
	public Map<ConstructSymbol, List<Rule>> getRules() {
		return rules;
	}

	/**
	 * Gets all rules used to produce the specified construct (i.e., rules whose head is the specified symbol)
	 * @param symbol the head of the rules
	 * @return the list of rules that produce the specified construct
	 * @see #getRules(ConstructSymbol)
	 */
	public List<Rule> getRules(String symbol) {
		return rules.get(ConstructSymbol.get(symbol));
	}

	/**
	 * Gets all rules used to produce the specified construct (i.e., rules whose head is the specified symbol)
	 * @param symbol the head of the rules
	 * @return the list of rules that produce the specified construct
	 * @see #getRules(String)
	 */
	public List<Rule> getRules(ConstructSymbol symbol) {
		return rules.get(symbol);
	}

	/**
	 * Gets all the token symbols that are specified in this grammar.
	 * @return an unmodifiable collection of {@link TokenSymbol}
	 */
	public Collection<TokenSymbol> getTokenSymbols() {
		return Collections.unmodifiableCollection(tokenSymbols.values());
	}
	/**
	 * Gets all the construct symbols that are specified in this grammar.
	 * @return an unmodifiable collection of {@link ConstructSymbol}
	 */
	public Collection<ConstructSymbol> getConstructSymbols() {
		return Collections.unmodifiableCollection(constructSymbols.values());
	}

	/**
	 * Gets the {@link TokenSymbol} associated to the given token name, if any.
	 * @param name the name of the token
	 * @return a {@link TokenSymbol} object
	 */
	public TokenSymbol getTokenSymbol(String name){
		return tokenSymbols.get(name);
	}

	/**
	 * Gets the {@link ConstructSymbol} associated to the given construct name, if any.
	 * @param name the name of the construct
	 * @return a {@link ConstructSymbol} object
	 */
	public ConstructSymbol getConstructSymbol(String name){
		return constructSymbols.get(name);
	}

	/**
	 * Gets the axiom of the grammar (i.e., the first symbol from which a parser should start to interpret the grammar)
	 * @return a {@link ConstructSymbol} object representing the axiom
	 */
	public ConstructSymbol getAxiom() {
		return axiom;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Pattern pattern : ignoredPatterns)
			sb.append("ignore ").append(pattern).append("/$").append('\n');
		for (List<Rule> ruleSet : rules.values())
			for (Rule rule : ruleSet)
				sb.append(rule).append('\n');
		return sb.toString();
	}

	/**
	 * Creates a new {@link Grammar.Builder} that allows the construction of a grammar, rule by rule.
	 * @return a {@link Grammar.Builder} instance
	 */
	public static Grammar.Builder create() {
		return new Builder();
	}

	/**
	 * Allows the construction of a {@link Grammar grammar}, rule by rule
	 */
	public static class Builder {
		private Map<ConstructSymbol, TreeSet<Rule>> rules = new HashMap<>();
		private Set<String> ignoredPatternStrings = new HashSet<>();
		Grammar grammar = new Grammar();

		/**
		 * Adds a new rule to the {@link Grammar grammar}, taking a rule builder as input. This method invokes the
		 * {@link Rule.Builder#build()} method on the given rule builder, first. Then, it adds the new rule to the
		 * grammar.
		 * @param ruleBuilder a rule builder
		 * @return this same {@link Builder builder}
		 */
		public Builder addRule(Rule.Builder ruleBuilder) {
			return addRule(ruleBuilder.build());
		}

		/**
		 * Adds a rule to the {@link Grammar grammar}.
		 * @param rule the rule to add.
		 * @return this same {@link Builder builder}
		 */
		public Builder addRule(Rule rule) {
			ConstructSymbol head = rule.getHead();
			if (grammar.axiom == null)
				grammar.axiom = head;
			TreeSet<Rule> ruleSet = rules.computeIfAbsent(head, k -> new TreeSet<>());
			ruleSet.add(rule);
			return this;
		}

		/**
		 * Adds the given patterns to the ignored ones of the grammar that is being built.
		 * @param patterns the patterns the grammar must ignore
		 * @return this same {@link Builder builder}
		 */
		public Builder ignorePatterns(String... patterns) {
			for (String pattern : patterns)
				if(!ignoredPatternStrings.contains(pattern)) {
					grammar.ignoredPatterns.add(Pattern.compile(pattern));
					ignoredPatternStrings.add(pattern);
				}
			return this;
		}

		/**
		 * Builds the {@link Grammar grammar}. This method sets the {@link #getAxiom() axiom} of the grammar to be
		 * the head of the first rule added to the builder.
		 * @return the new {@link Grammar grammar}
		 */
		public Grammar build() {
			return build(grammar.axiom.getName());
		}

		/**
		 * Builds the {@link Grammar grammar}, setting up the given construct symbol as axiom.
		 * This method is useful if and only if when the head of the fist rule added to the grammar is not the axiom.
		 * @param axiom the string representation of the construct symbol that must be set as axiom for
		 * the {@link Grammar grammar}
		 * @return the new {@link Grammar grammar}
		 */
		public Grammar build(String axiom) {
			grammar.axiom = ConstructSymbol.get(axiom);
			if (!rules.containsKey(grammar.axiom))
				throw new IllegalStateException(
						String.format("There is no rule related to the specified axiom \"%s\"", axiom));

			for (Entry<ConstructSymbol, TreeSet<Rule>> entry : rules.entrySet())
				grammar.rules.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));

			for (Set<Rule> ruleSet : rules.values())
				for (Rule rule : ruleSet)
					for (Symbol symbol : rule.getSymbols())
						symbol.ifConstruct(s -> {
							if (!rules.containsKey(s))
								grammar.rules.put(s, new ArrayList<>(0));
						});

			for (List<Rule> ruleSet : grammar.rules.values())
				grammar.ruleList.addAll(ruleSet);

			grammar.rules = Collections.unmodifiableMap(grammar.rules);
			grammar.ignoredPatterns = Collections.unmodifiableSet(grammar.ignoredPatterns);

			Map<TokenSymbol, Rule> foundTokens = new HashMap<>();
			for(Rule rule : grammar){
				for(Symbol symbol : rule)
					if(!symbol.getName().equals(""))
						symbol.ifToken(s->{
							if(foundTokens.containsKey(s))
								throw new IllegalStateException(String.format("A token name must be " +
										"declared once in all the grammar. Token %s is declared " +
										"twice in the following rules: (%s), (%s)",
										symbol.getName(), rule, foundTokens.get(s)
								));
							foundTokens.put(s, rule);
						});
			}

			for(Rule rule : grammar) {
				if(!grammar.constructSymbols.containsKey(rule.getHead().getName()))
					grammar.constructSymbols.put(rule.getHead().getName(), rule.getHead());
				for (Symbol symbol : rule) {
					symbol.ifToken(s -> {
						if (!s.getName().equals(""))
							grammar.tokenSymbols.put(s.getName(), s);
					});
					symbol.ifConstruct(s -> {
						if (!grammar.constructSymbols.containsKey(s.getName()))
							grammar.constructSymbols.put(s.getName(), s);
					});
				}
			}

			return grammar;
		}
	}

	@Override
	public Iterator<Rule> iterator() {
		return new Iterator<Rule>() {
			Iterator<Rule> it = ruleList.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Rule next() {
				return it.next();
			}
		};
	}

}
