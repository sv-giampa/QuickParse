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

package quickparse.grammar.symbol;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Defines the symbol used in grammars to represent a syntactical construct.<br>
 * <br>
 * For example, given the grammatical rule<br>
 * <pre><code>A -&#62; '(' B ')'</code></pre><br>
 * A and B are syntactical constructs that can be represented by this class.
 */
public class ConstructSymbol extends Symbol {

	private static Map<String, ConstructSymbol> flyweight = new WeakHashMap<>();

	/**
	 * Gets a new {@link ConstructSymbol} instance for the given string.<br>
	 * This method uses the Flyweight design pattern by GoF to cache already created instances.
	 *
	 * @param symbol the symbol to wrap into the {@link ConstructSymbol} instance
	 * @return a {@link ConstructSymbol} instance
	 */
	public static ConstructSymbol get(String symbol) {
		if(symbol == null || symbol.equals(""))
			throw new IllegalArgumentException("symbol cannot be null or the empty string");
		if (flyweight.containsKey(symbol)) {
			return flyweight.get(symbol);
		} else {
			ConstructSymbol obj = new ConstructSymbol(symbol);
			flyweight.put(symbol, obj);
			return obj;
		}
	}

	private String name;

	private ConstructSymbol(String symbol) {
		this.name = symbol;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <R, C> R accept(C context, Function<R, C> symbolFunction) {
		return symbolFunction.apply(context, this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ConstructSymbol other = (ConstructSymbol) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
