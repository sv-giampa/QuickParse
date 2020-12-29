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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * Defines the symbol used in grammars to represent a token.<br>
 * A token is matched against a given pattern, that is expressed as a standard Java Regular Expression.
 */
public class TokenSymbol extends Symbol {
	private String name;
	private Pattern pattern;
	private static Map<List<Object>, TokenSymbol> flyweight = new WeakHashMap<>();

	/**
	 * Gets a new {@link TokenSymbol} instance for the given name and pattern.<br>
	 * This method uses the Flyweight design pattern by GoF to cache already created instances.
	 * @param name the name of the token
	 * @param pattern the Java Regular Expression to match the token against.
	 * @return a {@link TokenSymbol} instance
	 */
	public static TokenSymbol get(String name, String pattern) {
		if(name == null)
			throw new IllegalArgumentException("token name cennot be null, but it can be the empty string");
		if(pattern == null || pattern.equals(""))
			throw new IllegalArgumentException("token pattern cennot be null or the empty string");
		List<Object> key = Arrays.asList(name, pattern);
		if (flyweight.containsKey(key)) {
			return flyweight.get(key);
		} else {
			TokenSymbol obj = new TokenSymbol(name, pattern);
			flyweight.put(key, obj);
			return obj;
		}
	}

	private TokenSymbol(String name, String pattern) {
		this.name = name;
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the pattern that matches this {@link TokenSymbol}
	 * @return a compiled {@link Pattern}
	 */
	public Pattern getPattern() {
		return pattern;
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
		result = prime * result + ((pattern == null) ? 0
				: pattern.toString()
						.hashCode());
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
		TokenSymbol other = (TokenSymbol) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.toString()
				.equals(other.pattern.toString()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + ":" + pattern;
	}

}
