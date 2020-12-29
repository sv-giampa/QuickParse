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

import quickparse.grammar.Grammar;

/**
 * Defines a {@link Grammar grammar} symbol. A grammar symbol can identify a construct
 * (i.e., a 'non-terminal' symbol) or a token (i.e., a 'terminal' symbol).
 */
public abstract class Symbol {

	/**
	 * The name of the symbol
	 * @return the name of this symbol as a {@link String}
	 */
	public abstract String getName();

	/**
	 * Solves the subclass of this symbol ({@link ConstructSymbol} or {@link TokenSymbol} and computes the given
	 * {@link Function}
	 * @param context the context used for computation
	 * @param symbolFunction the function that must be computed on this symbol
	 * @param <R> the return type of the function
	 * @param <C> the context type for the function
	 * @return the return value returned by computing the {@link Function}
	 */
	public abstract <R, C> R accept(C context, Function<R, C> symbolFunction);

	/**
	 * If this symbol is a {@link TokenSymbol} instance, computes the given {@link TokenAction} on it.
	 * @param action the {@link TokenAction} to compute
	 */
	public final void ifToken(TokenAction action){
		if(this instanceof TokenSymbol)
			action.apply((TokenSymbol) this);
	}

	/**
	 * If this symbol is a {@link ConstructSymbol} instance, computes the given {@link ConstructAction} on it.
	 * @param action the {@link ConstructAction} to compute
	 */
	public final void ifConstruct(ConstructAction action){
		if(this instanceof ConstructSymbol)
			action.apply((ConstructSymbol) this);
	}

	/**
	 * If this symbol is a {@link ConstructSymbol} instance, computes the given {@link TokenFunction} on it.
	 * @param context the contex used to compute the {@link TokenFunction}
	 * @param function the {@link TokenFunction} to compute
	 * @param <R> the return type of the function
	 * @param <C> the context type for the function
	 * @return the return value returned by computing the {@link TokenFunction}
	 */
	public final <R,C> R ifToken(C context, TokenFunction<R,C> function){
		if(this instanceof TokenSymbol)
			return function.apply(context, (TokenSymbol) this);
		return null;
	}

	/**
	 * If this symbol is a {@link ConstructSymbol} instance, computes the given {@link ConstructFunction} on it.
	 * @param context the contex used to compute the {@link ConstructFunction}
	 * @param function the {@link ConstructFunction} to compute
	 * @param <R> the return type of the function
	 * @param <C> the context type for the function
	 * @return the return value returned by computing the {@link ConstructFunction}
	 */
	public final <R,C> R ifConstruct(C context, ConstructFunction<R,C> function){
		if(this instanceof ConstructSymbol)
			return function.apply(context, (ConstructSymbol) this);
		return null;
	}

	/**
	 * An action that can be executed on a {@link TokenSymbol}
	 */
	@FunctionalInterface
	public interface TokenAction {
		/**
		 * Implements the {@link TokenAction}
		 * @param s the {@link TokenSymbol} to apply this action to
		 */
		void apply(TokenSymbol s);
	}

	/**
	 * An action that can be executed on a {@link ConstructSymbol}
	 */
	@FunctionalInterface
	public interface ConstructAction {
		/**
		 * Implements the {@link ConstructAction}
		 * @param s the {@link ConstructSymbol} to apply this action to
		 */
		void apply(ConstructSymbol s);
	}

	/**
	 * A function that can be executed on a {@link TokenSymbol}
	 * @param <C> the type of context that is used to compute the function
	 * @param <R> the result type of the function
	 */
	@FunctionalInterface
	public interface TokenFunction<R,C> {
		/**
		 * Implements the {@link TokenFunction function}
		 * @param context the context used to compute the function result
		 * @param s the input {@link TokenSymbol} for the function
		 * @return the result of the function
		 */
		R apply(C context, TokenSymbol s);
	}



	/**
	 * A function that can be executed on a {@link ConstructSymbol}
	 * @param <C> the type of context that is used to compute the function
	 * @param <R> the result type of the function
	 */
	@FunctionalInterface
	public interface ConstructFunction<R,C> {
		/**
		 * Implements the {@link ConstructFunction function}
		 * @param context the context used to compute the function result
		 * @param s the input {@link ConstructSymbol} for the function
		 * @return the result of the function
		 */
		R apply(C context, ConstructSymbol s);
	}

	/**
	 * Represents a function that can be computed on a {@link ConstructSymbol} or, alternatively,
	 * on a {@link TokenSymbol}. This class implements the SyntaxTreeVisitor design pattern by extending
	 * the {@link ConstructFunction} and the {@link TokenFunction} classes.
	 * @param <R> the return type of the function
	 * @param <C> the type of context that is used to compute the function
	 */
	public interface Function<R, C> extends ConstructFunction<R,C>, TokenFunction<R,C> {

		R apply(C context, TokenSymbol symbol);

		R apply(C context, ConstructSymbol symbol);
	}
}
