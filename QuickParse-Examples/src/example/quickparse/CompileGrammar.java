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

package example.quickparse;

import quickparse.QGLCompiler;
import quickparse.grammar.Grammar;
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.exception.UnexpectedSymbolException;
import quickparse.semantics.interpreters.exception.SemanticsException;

public class CompileGrammar {

    public static void main(String[] args) throws SemanticsException, ExpectedSymbolsException, UnexpectedSymbolException {
        String source =
                "ignore \\n/$\n" +
                "ignore \\n/$\n" + // doubled pattern, it will be ignored
                "ignore  /$\n" + // white space pattern
                "A -> a:a c/$ B :c/$ \n  \n"+

                "// this is a comment line\n" +

                "B = b:b/$ B\n" +

                "/* this is\n" +
                "a comment block */\n" +

                "B = /\n";

        System.out.println("Compiling from source string...");
        Grammar grammar = QGLCompiler.compile(source);
        System.out.println("Compiled grammar:");
        System.out.println(grammar);

        System.out.println();

        System.out.println("Compiling from grammar.toString()...");
        grammar = QGLCompiler.compile(grammar.toString());
        System.out.println("Compiled grammar:");
        System.out.println(grammar);
    }
}
