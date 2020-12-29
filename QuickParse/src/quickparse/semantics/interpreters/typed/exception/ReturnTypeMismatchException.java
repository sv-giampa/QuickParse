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

import quickparse.grammar.symbol.Symbol;

import java.lang.reflect.Method;

public class ReturnTypeMismatchException extends RuntimeException {
    public final Symbol symbol;
    private final Method method1;
    private final Method method2;

    public ReturnTypeMismatchException(Symbol symbol, Method method1, Method method2) {
        super(String.format("The interpreter methods \"%s\" and \"%s\" for symbol \"%s\" have not " +
                        "the same return type. You must change the return types to be compatible (e.g. be equal or " +
                        "be one a supertype of the other)", method1, method2, symbol));
        this.symbol = symbol;
        this.method1 = method1;
        this.method2 = method2;
    }
}
