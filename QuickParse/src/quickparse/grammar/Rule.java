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

import quickparse.grammar.symbol.ConstructSymbol;
import quickparse.grammar.symbol.Symbol;
import quickparse.grammar.symbol.TokenSymbol;

/**
 * Represents a {@link Grammar grammar} production rule.
 * A grammar rule is composed by an head symbol, which is the construct produced by the rule, and a body,
 * which is the list of the {@link ConstructSymbol construct} or {@link TokenSymbol token} symbols that
 * compose the head construct.<br>
 * A rule is an iterable object of {@link Symbol symbols}.<br>
 * A rule is {@link Comparable comparable} to other rules, basing on their generality:
 * <ul>
 *     <li>A rule is more general than another if it has longer body than the other;</li>
 *     <li>Two rule that have the same body size, are compared respect to the order in which they have been created.</li>
 * </ul>
 */
public class Rule implements Comparable<Rule>, Iterable<Symbol> {
	private static int nextId; // used to compare rule respect to the creation order.
	private int id;
	private ConstructSymbol head;
	private List<Symbol> symbols = new ArrayList<>();

	/**
	 * Creates a new  {@link Rule.Builder} by initializing it with the given head construct symbol.
	 * @param symbol the head symbol, which is the construct that the rule produces.
	 * @return a new {@link Rule.Builder} instance
	 */
	public static Rule.Builder head(String symbol) {
		return new Builder(symbol);
	}

	private Rule() {
		id = nextId++;
	}

	/**
	 * Gets the construct symbol produced by this {@link Rule}
	 * @return a {@link ConstructSymbol} instance
	 */
	public ConstructSymbol getHead() {
		return head;
	}

	/**
	 * Gets the production body of this {@link Rule}
	 * @return the list of {@link Symbol symbols} that produce the {@link #getHead() head} construct
	 */
	public List<Symbol> getSymbols() {
		return symbols;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		if (symbols == null) {
			if (other.symbols != null)
				return false;
		} else if (!symbols.equals(other.symbols))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(head)
				.append(" ->");
		if(symbols.isEmpty())
			sb.append(' ').append('/');
		else
			symbols.forEach(s -> {
				sb.append(' ').append(s);
				s.ifToken(t->sb.append("/$"));
			});
		return sb.toString();
	}

	/**
	 * Class that implements the Builder creational design pattern, that is used to build new {@link Rule rules}.
	 */
	public static class Builder {
		private boolean buildingFinished = false;
		private Rule rule = new Rule();

		private Builder(String symbol) {
			rule.head = ConstructSymbol.get(symbol);
		}

		private void checkState() {
			if (buildingFinished)
				throw new IllegalStateException("SyntaxRule was already built.");
		}

		/**
		 * Sets the list of symbols that compose the head construct of the {@link Rule rule}.<br>
		 * Invoking this method is equivalent to invoke one or more times the {@link #construct(String)} and the
		 * {@link #token(String, String)} methods, and then invoking the {@link #build()} method to tget the rule.<br>
		 * <br>
		 * An invocation of this method must be done by coding the symbols as plain strings:
		 * <ul>
		 *     <li>a Construct Symbol is specified by simply indicating the name of the symbol as string
		 *     (e.g., "if" or "else");</li>
		 *     <li>a Token Symbol is specified by indicating the name of the symbol, followed by a colon and a
		 *     regular expression that matches it (e.g., "var_name:[a-zA-Z][a-zA-Z0-9\_]*", for a Java variable name)
		 *     </li>
		 * </ul>
		 * This method can be helpful to simplify the definition of a {@link Grammar grammar} directly in the code and
		 * to render it more readable by humans, respect to invoking many and many times the {@link #construct(String)}
		 * and the {@link #token(String, String)} methods in the same grammar.
		 *
		 * @param symbols the body symbols
		 * @return a new {@link Rule rule}
		 * @see #construct(String)
		 * @see #token(String, String)
		 */
		public Rule produces(String... symbols) {
			for (String symbol : symbols) {
				if (symbol.contains(":")) {
					// token
					String[] token = symbol.split(":", 2);
					token(token[0], token[1]);
				} else {
					// construct
					construct(symbol);
				}
			}
			return build();
		}

		/**
		 * Specifies a construct to insert in the body of the {@link Rule rule}. A construct is identified by its name only.
		 * @param name the construct name
		 * @return this same {@link Builder builder}
		 */
		public Builder construct(String name) {
			rule.symbols.add(ConstructSymbol.get(name));
			return this;
		}


		/**
		 * Specifies a token to insert in the body of the {@link Rule rule}. A token is identified by its name and by its pattern.
		 * The pattern of a token is a Regular Expression that is interpreted by the Java Regular Expression framework.
		 * @param name the token name
		 * @param pattern the Regular Expression that matches the token symbol.
		 * @return this same {@link Builder builder}
		 */
		public Builder token(String name, String pattern) {
			rule.symbols.add(TokenSymbol.get(name, pattern));
			return this;
		}

		/**
		 * Builds the rule.
		 * @return a new {@link Rule} instance
		 */
		public Rule build() {
			checkState();
			buildingFinished = true;
			((ArrayList<Symbol>) rule.symbols).trimToSize();
			rule.symbols = Collections.unmodifiableList(rule.symbols);
			return rule;
		}
	}

	@Override
	public int compareTo(Rule other) {
		if (symbols.size() != other.symbols.size()) {
			return symbols.size() < other.symbols.size() ? 1 : symbols.size() > other.symbols.size() ? -1 : 0;
		} else {
			return id > other.id ? 1 : id < other.id ? -1 : 0;
		}
	}

	@Override
	public Iterator<Symbol> iterator() {
		return new Iterator<Symbol>() {
			Iterator<Symbol> it = symbols.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Symbol next() {
				return it.next();
			}
		};
	}

}
