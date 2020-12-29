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

package quickparse.semantics.interpreters.typed.exception;

import quickparse.grammar.Rule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class ParameterTypeMismatchException extends InterpreterException {

    private final Rule rule;
    private final Method method;
    private final List<Class<?>> expectedTypes;
    private final int paramIndex;
    private final Class<?> expectedType;

    private static String getMessage(Rule rule, Method method, List<Class<?>> expectedTypes, int paramIndex){
        Parameter param = method.getParameters()[paramIndex];
        return String.format("Parameter \"%s %s\" of method \"%s\" has not the expected " +
                "type \"%s\" for symbol %s (%s) in rule \"%s\". " +
                "The expected parameter types for the method are %s", param.getType(), param.getName(),
                method, expectedTypes.get(paramIndex), paramIndex,rule.getSymbols().get(paramIndex), rule, expectedTypes);
    }

    public ParameterTypeMismatchException(Rule rule, Method method, List<Class<?>> expectedTypes, int paramIndex) {
        super(getMessage(rule,method, expectedTypes ,paramIndex));
        this.rule = rule;
        this.method = method;
        this.expectedTypes = expectedTypes;
        this.paramIndex = paramIndex;
        this.expectedType = expectedTypes.get(paramIndex);
    }
}
