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

import quickparse.grammar.Grammar;
import quickparse.grammar.Rule;
import quickparse.parsing.Parser;
import quickparse.parsing.RecursiveDescentParser;
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.parsing.exception.UnexpectedSymbolException;
import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.parsing.syntaxtree.SyntaxTreeVisitor;
import quickparse.parsing.syntaxtree.TokenNode;

import java.util.ArrayList;
import java.util.List;

public class CSVParser {
    private Grammar grammar;
    private Parser parser;

    public CSVParser(char separator){
        grammar = Grammar.create()
                .ignorePatterns(" ")

                .addRule(Rule.head("csv").produces("tuples"))

                .addRule(Rule.head("tuples").produces("tuple", ":\\n+", "tuples"))
                .addRule(Rule.head("tuples").produces("tuple"))
                .addRule(Rule.head("tuples").produces())

                .addRule(Rule.head("tuple").produces("elements"))

                .addRule(Rule.head("elements").produces("element", ":\\"+separator, "elements"))
                .addRule(Rule.head("elements").produces("element"))

                .addRule(Rule.head("element").produces("doubleQuotedText"))
                .addRule(Rule.head("element").produces("singleQuotedText"))
                .addRule(Rule.head("element").produces("unquotedText"))

                .addRule(Rule.head("doubleQuotedText").produces(":\\\"", "doubleQuotedTextTail", ":\\\""))
                .addRule(Rule.head("doubleQuotedTextTail").produces("doubleQuotedTextFragment:[^\\\"\\\\]*", "escapeSequence", "doubleQuotedTextTail"))
                .addRule(Rule.head("doubleQuotedTextTail").produces("doubleQuotedTextFinalFragment:[^\\\"]*"))

                .addRule(Rule.head("singleQuotedText").produces(":\\\'", "singleQuotedTextTail", ":\\\'"))
                .addRule(Rule.head("singleQuotedTextTail").produces("singleQuotedTextFragment:[^\\\'\\\\]*", "escapeSequence", "singleQuotedTextTail"))
                .addRule(Rule.head("singleQuotedTextTail").produces("singleQuotedTextFinalFragment:[^\\\']*"))
                .addRule(Rule.head("escapeSequence").produces("escapeSequence:\\\\."))

                .addRule(Rule.head("unquotedText").produces("unquotedText:[^\\\"\\\'\\"+separator+"\\n][^\\"+separator+"\\n]*|\b"))

                .build();

        parser = new RecursiveDescentParser(grammar);
    }

    private static class CsvVisitor implements SyntaxTreeVisitor{
        ArrayList<List<String>> csv;
        ArrayList<String> tuple;
        StringBuilder elementBuilder;

        @Override
        public void token(TokenNode node) {
            switch (node.name){
                case "singleQuotedTextFragment":
                case "singleQuotedTextFinalFragment":
                case "doubleQuotedTextFragment":
                case "doubleQuotedTextFinalFragment":
                case "unquotedText":
                    elementBuilder.append(node.value);
                    break;
                case "escapeSequence":
                    char ch = node.value.charAt(1);
                    switch(ch){
                        case 'n':
                            ch = '\n';
                            break;
                        case 't':
                            ch = '\t';
                            break;
                        case 'r':
                            ch = '\r';
                            break;
                        default:
                    }
                    elementBuilder.append(ch);
                    break;
            }
        }

        @Override
        public void enterConstruct(ConstructNode node) {
            switch (node.name){
                case "csv":
                    csv = new ArrayList<>();
                    break;
                case "tuple":
                    tuple = new ArrayList<>();
                    break;
                case "element":
                    elementBuilder = new StringBuilder();
                    break;
            }
        }

        @Override
        public void exitConstruct(ConstructNode node) {
            switch (node.name){
                case "csv":
                    csv.trimToSize();
                    break;
                case "tuple":
                    tuple.trimToSize();
                    if(csv != null)
                        csv.add(tuple);
                    break;
                case "element":
                    tuple.add(elementBuilder.toString());
                    break;
            }
        }

        public List<List<String>> getCsv() {
            return csv;
        }

        public List<String> getTuple() {
            return tuple;
        }
    }


    public List<String> parseTuple(String source) throws UnexpectedSymbolException, ExpectedSymbolsException {
        return parser.parse(source, "tuple")
                .accept(new CsvVisitor())
                .getTuple();
    }

    public List<List<String>> parse(String source) throws UnexpectedSymbolException, ExpectedSymbolsException {
        return parser.parse(source)
                .accept(new CsvVisitor())
                .getCsv();
    }

    public static void main(String[] args) throws ExpectedSymbolsException, UnexpectedSymbolException {
        String csv =    "unquoted-text; \"double quoted \\\" text\"; 12.45\n"
                +       "35; \'single quoted \\' text\'; unquoted-text\n"
                ;

        String tuple =    "\'single quoted \\' text\'; \"double quoted \\\" text\"; 12.45";

        CSVParser csvParser = new CSVParser(';');
        System.out.println(csvParser.parse(csv));
        System.out.println(csvParser.parseTuple(tuple));
    }

}
