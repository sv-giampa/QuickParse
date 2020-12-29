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

import quickparse.semantics.interpreters.exception.SemanticsException;
import quickparse.semantics.interpreters.typed.annotation.SyntaxToken;
import quickparse.grammar.Grammar;
import quickparse.grammar.Rule;
import quickparse.parsing.RecursiveDescentParser;
import quickparse.parsing.Parser;
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.exception.UnexpectedSymbolException;
import quickparse.semantics.interpreters.typed.TypedInterpreter;
import quickparse.semantics.interpreters.typed.annotation.SyntaxConstruct;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaultyINIData {

    private static final Grammar grammar = Grammar.create()
            .ignorePatterns(" ") // ignore spaces only
            .addRule(Rule.head("ini").produces("sections"))
            .addRule(Rule.head("sections").produces("section", "sections"))

            // ignores malformed section name lines (":[^\\n]*\\n")
            .addRule(Rule.head("sections").produces(":[^\\n]*\\n", "sections"))

            .addRule(Rule.head("sections").produces())
            .addRule(Rule.head("section").produces(":\\[", "section-name:[a-zA-Z0-9_\\-]+", ":\\]", ":\\n", "pairs"))
            .addRule(Rule.head("pairs").produces("pair", ":\\n", "pairs"))

            // ignores malformed pair lines, but finish the pairs list if it finds a new section name (":[^(\\[[a-zA-Z0-9_\\-]+\\])\\n]*\\n")
            .addRule(Rule.head("pairs").produces(":[^(\\[[a-zA-Z0-9_\\-]+\\])\\n]*\\n", "pairs"))

            .addRule(Rule.head("pairs").produces("pair"))
            .addRule(Rule.head("pairs").produces())
            .addRule(Rule.head("pair").produces("key:[a-zA-Z0-9_\\-]+", ":\\=", "value:[^\\n]*"))
            .build();

    private static final TypedInterpreter<Map<String,Map<String,String>>> interpreter = new TypedInterpreter(grammar){

        @SyntaxConstruct("pair")
        public AbstractMap.SimpleEntry<String, String> pair(String key, String value){
            return new AbstractMap.SimpleEntry<>(key, value);
        }

        @SyntaxToken("key")
        @SyntaxToken("value")
        @SyntaxToken("section-name")
        public String tokenToString(CharSequence token){
            return token.toString();
        }

        @SyntaxConstruct("section")
        public AbstractMap.SimpleEntry<String, Map<String, String>> section(String name, List<AbstractMap.SimpleEntry<String,String>> pairs){
            Map<String, String> section = new HashMap<>();
            for(Map.Entry<String, String> entry : pairs)
                section.put(entry.getKey(), entry.getValue());
            return new AbstractMap.SimpleEntry<>(name,section);
        }

        @SyntaxConstruct("ini")
        public Map<String, Map<String,String>> ini(List<AbstractMap.SimpleEntry<String,Map<String,String>>> sections){
            Map<String, Map<String,String>> ini = new HashMap<>();
            for(Map.Entry<String, Map<String,String>> entry : sections) {
                if(ini.containsKey(entry.getKey()))
                    ini.get(entry.getKey()).putAll(entry.getValue());
                else
                    ini.put(entry.getKey(), entry.getValue());
            }
            return ini;
        }
    };

    private static Parser parser = new RecursiveDescentParser(grammar);

    public static Map<String, Map<String, String>> compile(CharSequence source) throws UnexpectedSymbolException, ExpectedSymbolsException, SemanticsException {
        return interpreter.analyze(parser.parse(source));
    }

    public static void main(String[] args) throws UnexpectedSymbolException, ExpectedSymbolsException, SemanticsException {
        String source =
                "this FAULTY section line is ignored\n" +
                "[section1]\n" +
                "k1=v1\n" +
                "\n" +
                "k2=v2\n" +
                "[section2]\n" +
                "k1=v1\n" +
                "this FAULTY pair line is ignored\n" +
                "k2=v2\n" +
                "\n" +
                "\n" +
                "[section1]\n" +
                "k3=v3\n" +
                "k4=v4";

        System.out.println(compile(source));
    }
}
