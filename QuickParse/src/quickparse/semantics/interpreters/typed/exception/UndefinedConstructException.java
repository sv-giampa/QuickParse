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

import java.lang.reflect.Method;

public class UndefinedConstructException extends InterpreterException {
    public final String symbol;
    public final Method method;

    public UndefinedConstructException(String symbol, Method method) {
        super(String.format("Undefined construct symbol \"%s\", used to annotate the method \"%s\"", symbol, method));
        this.symbol = symbol;
        this.method = method;
    }
}
