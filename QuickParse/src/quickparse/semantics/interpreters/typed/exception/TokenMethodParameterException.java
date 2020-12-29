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

import quickparse.grammar.symbol.TokenSymbol;

import java.lang.reflect.Method;

public class TokenMethodParameterException extends InterpreterException {
    public final TokenSymbol tokenSymbol;
    public final Method method;

    private static String getMessage(TokenSymbol tokenSymbol, Method method){
        return "";
    }

    public TokenMethodParameterException(TokenSymbol tokenSymbol, Method method) {
        super(getMessage(tokenSymbol, method));
        this.tokenSymbol = tokenSymbol;
        this.method = method;
    }
}
