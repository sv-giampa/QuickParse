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

package quickparse.semantics.interpreters.typed;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import quickparse.grammar.Grammar;
import quickparse.grammar.Rule;
import quickparse.grammar.symbol.Symbol;
import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.semantics.SyntaxTreeFunction;
import quickparse.semantics.interpreters.exception.SemanticsException;
import quickparse.semantics.interpreters.typed.annotation.SyntaxConstruct;
import quickparse.semantics.interpreters.typed.annotation.SyntaxConstructs;
import quickparse.semantics.interpreters.typed.annotation.SyntaxToken;
import quickparse.semantics.interpreters.typed.annotation.SyntaxTokens;
import quickparse.semantics.interpreters.typed.exception.*;
import quickparse.parsing.syntaxtree.TokenNode;
import quickparse.semantics.interpreters.Interpreter;
import quickparse.semantics.interpreters.typed.exception.*;

/**
 *
 */
public abstract class TypedInterpreter<T> implements Interpreter<T> {
	private Grammar grammar;
	private Map<String, Method> tokens = new HashMap<>();
	private Map<String, Method> constructs = new HashMap<>();
	private Map<Symbol, Class<?>> symbolReturnTypes = new HashMap<>();

	public TypedInterpreter(Grammar grammar) {
		this.grammar = grammar;
		init();
	}

	private final void init() {
		mapMethods();
		mapReturnTypes();
		checkMethodSignatures();
	}

	private final void mapMethods() {
		Method[] methods = this.getClass().getDeclaredMethods();
		for (Method method : methods) {
			Set<SyntaxToken> syntaxTokens = new HashSet<>();
			Set<SyntaxConstruct> syntaxConstructs = new HashSet<>();

			if (method.isAnnotationPresent(SyntaxToken.class))
				syntaxTokens.add(method.getAnnotation(SyntaxToken.class));
			if (method.isAnnotationPresent(SyntaxTokens.class))
				for(SyntaxToken annotation : method.getAnnotation(SyntaxTokens.class).value())
					syntaxTokens.add(annotation);

			if (method.isAnnotationPresent(SyntaxConstruct.class))
				syntaxConstructs.add(method.getAnnotation(SyntaxConstruct.class));
			if (method.isAnnotationPresent(SyntaxConstructs.class))
				for(SyntaxConstruct annotation : method.getAnnotation(SyntaxConstructs.class).value())
					syntaxConstructs.add(annotation);

			for(SyntaxToken annotation : syntaxTokens){
				String tokenName = annotation.value();
				if(grammar.getTokenSymbol(tokenName) == null || tokenName.equals(""))
					throw new UndefinedTokenException(tokenName, method);
				if (method.getParameterCount() > 1 ||
						(method.getParameterCount()==1 &&
								!method.getParameterTypes()[0].isAssignableFrom(CharSequence.class))){
					throw new TokenMethodParameterException(grammar.getTokenSymbol(tokenName), method);
				}
				if(tokens.containsKey(tokenName))
					throw new DoubleTokenAnnotationException(tokenName, tokens.get(tokenName), method);
				tokens.put(tokenName, method);
			}

			for(SyntaxConstruct annotation : syntaxConstructs){
				String constructName = annotation.value();
				if(grammar.getRules(constructName)==null || constructName.equals(""))
					throw new UndefinedConstructException(constructName, method);
				if(constructs.containsKey(constructName))
					throw new DoubleConstructAnnotationException(constructName, constructs.get(constructName), method);
				constructs.put(constructName, method);
			}
		}
	}

	private final void mapReturnTypes() {
		Map<Symbol, Method> symbolLastMethod = new HashMap<>();
		for (Rule rule : grammar) {
			Method method = getRuleMethod(rule);
			if (method != null) {
				if (!symbolLastMethod.containsKey(rule.getHead())) {
					symbolLastMethod.put(rule.getHead(), method);
					//System.out.println(rule.getHead() + " => " + method.getReturnType());
				} else {
					Method lastMethod = symbolLastMethod.get(rule.getHead());
					if (method.getReturnType().isAssignableFrom(lastMethod.getReturnType())) {
						symbolLastMethod.put(rule.getHead(), method);
					} else if (!lastMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
						throw new ReturnTypeMismatchException(rule.getHead(), method, lastMethod);
					}
				}
			}
		}

		for (Map.Entry<Symbol, Method> entry : symbolLastMethod.entrySet())
			symbolReturnTypes.put(entry.getKey(), entry.getValue().getReturnType());

		Class<?> defaultReturnType = List.class;
		for (Rule rule : grammar) {
			Method method = getRuleMethod(rule);
			if (method == null) {
				if (!symbolReturnTypes.containsKey(rule.getHead())) {
					symbolReturnTypes.put(rule.getHead(), defaultReturnType);
				} else {
					Class<?> mappedType = symbolReturnTypes.get(rule.getHead());
					if (defaultReturnType.isAssignableFrom(mappedType)) {
						symbolReturnTypes.put(rule.getHead(), defaultReturnType);
					} else if (!mappedType.isAssignableFrom(defaultReturnType)) {
						throw new DefaultReturnTypeMismatchException(symbolLastMethod.get(rule.getHead()), rule);
					}
				}
			}
		}

//		System.out.println("symbolReturnTypes:");
//		for (Map.Entry entry : symbolReturnTypes.entrySet())
//			.printf("\t%s => %s\n", entry.getKey(), entry.getValue());
	}

	private final void checkMethodSignatures() {
		for (Rule rule : grammar) {
			Method method = getRuleMethod(rule);
			if (method != null) {
				// build expected type signature
				List<Class<?>> expectedTypes = new LinkedList<>();
				for (Symbol symbol : rule) {
					symbol.ifConstruct(construct -> {
						if(symbolReturnTypes.containsKey(construct))
							expectedTypes.add(symbolReturnTypes.get(construct));
					});
					symbol.ifToken(token -> {
						if (tokens.containsKey(token.getName()))
							expectedTypes.add(tokens.get(token.getName()).getReturnType());
						else if(!token.getName().equals(""))
							expectedTypes.add(CharSequence.class);
					});
				}
				expectedTypes.removeIf(aClass -> aClass == void.class);

				List<Class<?>> actualTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
				if (expectedTypes.size() != actualTypes.size())
					throw new ParameterCountMismatchException(method, rule, expectedTypes);

				for (int i = 0; i < expectedTypes.size(); i++) {
					if (!actualTypes.get(i).isAssignableFrom(expectedTypes.get(i)))
						throw new ParameterTypeMismatchException(rule, method, expectedTypes, i);
				}
			}
		}
	}

	private final Method getRuleMethod(Rule rule) {
		if (constructs.containsKey(rule.getHead().getName()))
			return constructs.get(rule.getHead().getName());
		else
			return null;
	}

	public final Grammar getGrammar() {
		return grammar;
	}

	private SyntaxTree currentNode;
	private Map<Object, SyntaxTree> treeByResult = new HashMap<>();

	/**
	 * Gets the node that this interpreter is currently analyzing.<br>
	 * This method is useful to generate detailed semantic errors from inside the annotated methods of the interpreter.
	 * @return A {@link SyntaxTree syntax node}
	 */
	protected final SyntaxTree getCurrentSyntaxNode(){
		return currentNode;
	}

	/**
	 * Gets the node whose interpretation generated the given result object.<br>
	 * This method is useful to generate detailed semantic errors from inside the annotated methods of the interpreter.
	 * @param result a result object generated by an annotated method of this interpreter
	 * @return A {@link SyntaxTree syntax node}
	 * @throws IllegalArgumentException if the given result object is not the direct
	 * result of the analysis of a syntax node
	 */
	protected final SyntaxTree getSyntaxNodeByResult(Object result){
		if(treeByResult.containsKey(result))
			return treeByResult.get(result);
		else
			throw new IllegalArgumentException(
					"The given result object is not the direct result of the analysis of a syntax node");
	}

	private SyntaxTreeFunction syntaxTreeFunction = new SyntaxTreeFunction() {
		@Override
		public Object token(TokenNode node) {
			if (tokens.containsKey(node.name)) {
				Method method = tokens.get(node.name);
				try {
					currentNode = node;
					method.setAccessible(true);
					Object result = method.invoke(TypedInterpreter.this, node.value);
					treeByResult.put(result, node);
					return result;
				} catch(InvocationTargetException e){
					throw new InterpreterException(e.getCause());
				} catch (Exception e) {
					throw new InterpreterException(e);
				}
			} else if (!node.name.equals("")) {
				treeByResult.put(node.value, node);
				return node.value;
			} else
				return null;
		}

		@Override
		public Object construct(ConstructNode node, List<Object> childrenResults) {
			Method method;
			if (constructs.containsKey(node.name)) {
				method = constructs.get(node.name);
			} else {
				//System.out.println("returning children results: " + childrenResults);

				if(childrenResults.size()==1 && childrenResults.get(0) instanceof List) {
					//System.out.println("forwarding child result");
					childrenResults = (List<Object>) childrenResults.get(0);
				} else if(childrenResults.size() > 1) {
					//System.out.println("merging children results");
					List<Object> merge = new ArrayList<>(childrenResults.size());
					for (Object obj : childrenResults) {
						if (obj instanceof List)
							merge.addAll((List<Object>) obj);
						else
							merge.add(obj);
					}
					childrenResults = merge;
				}
				return childrenResults;
			}

			try {
				currentNode = node;
				method.setAccessible(true);
				Object[] values = childrenResults.toArray();
				Object result = method.invoke(TypedInterpreter.this, values);
				treeByResult.put(result, node);
				return result;
			} catch(InvocationTargetException e){
				throw new InterpreterException(e.getCause());
			} catch (Exception e) {
				throw new InterpreterException(e);
			}
		}
	};


	@Override
	public final T analyze(SyntaxTree syntaxTree) throws SemanticsException {
		try {
			@SuppressWarnings("unchecked")
			T res = (T) syntaxTreeFunction.apply(syntaxTree);
			return res;
		}catch(InterpreterException e){
			throw new SemanticsException(e.getCause());
		}
	}
}
