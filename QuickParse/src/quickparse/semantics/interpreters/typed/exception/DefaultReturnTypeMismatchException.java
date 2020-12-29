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

public class DefaultReturnTypeMismatchException extends InterpreterException {
    private Method method;
    private Rule rule;

    public DefaultReturnTypeMismatchException(Method method, Rule rule) {
        super(String.format("Rule \"%s\" is not interpreted and its return type is as default (java.util.List), " +
                "but it is incompatible with the most generic return type for the construct \"%s\" that is declared by " +
                "the \"%s\" method", rule, rule.getHead(), method));
        this.method = method;
        this.rule = rule;
    }
}
