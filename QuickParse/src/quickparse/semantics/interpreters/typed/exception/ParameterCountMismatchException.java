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
import java.util.List;

public class ParameterCountMismatchException extends InterpreterException {
    public final Method method;
    public final Rule rule;
    public final List<Class<?>> expectedParameterTypes;
    public final int mismatch;

    private static String getMessage(Rule rule, Method method, List<Class<?>> expectedParameterTypes){
        if(method.getParameterCount() > expectedParameterTypes.size()){
            return String.format("Method %s has more parameters than expected for syntax rule %s. " +
                    "The expected parameter types for the method are %s",method,rule, expectedParameterTypes);
        } else {
            return String.format("Method %s has less parameters than expected for syntax rule %s. " +
                    "The expected parameter types for the method are %s",method, rule, expectedParameterTypes);
        }
    }

    public ParameterCountMismatchException(Method method, Rule rule, List<Class<?>> expectedParameterTypes) {
        super(getMessage(rule,method,expectedParameterTypes));
        this.method = method;
        this.rule = rule;
        this.expectedParameterTypes = expectedParameterTypes;
        this.mismatch = expectedParameterTypes.size()-method.getParameterCount();
    }
}