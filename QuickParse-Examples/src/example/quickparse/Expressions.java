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
import quickparse.parsing.exception.ExpectedSymbolsException;
import quickparse.semantics.interpreters.exception.SemanticsException;
import quickparse.semantics.interpreters.typed.TypedInterpreter;
import quickparse.semantics.interpreters.typed.annotation.SyntaxConstruct;
import quickparse.semantics.interpreters.typed.annotation.SyntaxToken;
import quickparse.parsing.RecursiveDescentParser;
import quickparse.parsing.Parser;
import quickparse.parsing.exception.UnexpectedSymbolException;

import java.util.List;

public class Expressions {

    private static Grammar grammar = Grammar.create()
            .ignorePatterns("\\s") // ignore white space characters
            .addRule(Rule.head("expression").produces("level1"))

            // level 1 of operator precedence: addition, subtraction
            .addRule(Rule.head("level1").produces("level1-tail"))
            .addRule(Rule.head("level1-tail").produces("level2", "level1-operator:[\\+\\-]", "level1"))
            .addRule(Rule.head("level1-tail").produces("level2"))

            // level 2 of operator precedence: multiplication, division
            .addRule(Rule.head("level2").produces("level2-tail"))
            .addRule(Rule.head("level2-tail").produces("level-final", "level2-operator:[\\*\\/]", "level2"))
            .addRule(Rule.head("level2-tail").produces("level-final"))

            // final level of operator precedence
            .addRule(Rule.head("level-final").produces("term"))
            .addRule(Rule.head("level-final").produces("negative"))
            .addRule(Rule.head("level-final").produces("positive"))

            // unary operators
            .addRule(Rule.head("negative").produces(":\\-", "term"))
            .addRule(Rule.head("positive").produces(":\\+", "term"))

            // expression term definition
            .addRule(Rule.head("term").produces("number:[\\+\\-]?[0-9]*\\.?[0-9]+([eE][\\+\\-]?[0-9]+)?"))
            .addRule(Rule.head("term").produces(":\\(", "expression", ":\\)"))

            .build();

    private static TypedInterpreter<Double> interpreter = new TypedInterpreter<Double>(grammar) {

        @SyntaxToken("number")
        private double number(CharSequence value){return Double.parseDouble(value.toString());}

        @SyntaxConstruct("negative")
        private double negative(double term){return -term;}

        @SyntaxConstruct("term")
        @SyntaxConstruct("positive")
        @SyntaxConstruct("expression")
        @SyntaxConstruct("level-final")
        private double term(double term){return term;}

        @SyntaxConstruct("level1")
        @SyntaxConstruct("level2")
        //'elements' list contains numbers and operators in an alternate form, something such as [number, operator, number, operator, ...]
        private double operation(List<Object> elements){
            double value = (double) elements.get(0);
            for(int i=1; i<elements.size(); i++){
                String operator = elements.get(i).toString();
                // operators precedence is encoded in the grammar:
                // executes the operations in the order they are presented
                switch(operator){
                    case "+":
                        value += (double) elements.get(++i);
                        break;
                    case "-":
                        value -= (double) elements.get(++i);
                        break;
                    case "*":
                        value *= (double) elements.get(++i);
                        break;
                    case "/":
                        value /= (double) elements.get(++i);
                        break;
                }
            }
            return value;
        }
    };

    private static Parser parser = new RecursiveDescentParser(grammar);

    public static void main(String[] args) throws UnexpectedSymbolException, ExpectedSymbolsException, SemanticsException {
        evaluateAndPrint("1+2*3+4");
        evaluateAndPrint("(1+2)*3+4");
        evaluateAndPrint("1+2*(3+4)");
        evaluateAndPrint("(1+2)*(3+4)");
        evaluateAndPrint("(2*((1+2)*3+6)-5)*8");
    }

    public static void evaluateAndPrint(String expression) throws UnexpectedSymbolException, ExpectedSymbolsException, SemanticsException {
        double result = interpreter.analyze(parser.parse(expression));
        System.out.printf("%s = %s\n", expression, result);
    }
}
