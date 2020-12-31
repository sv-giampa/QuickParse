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
import quickparse.parsing.syntaxtree.SyntaxTreeVisitor;
import quickparse.parsing.syntaxtree.TokenNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExpressionsWithVisitor {
    private static Grammar grammar = Grammar.create()
            .ignorePatterns("\\s") // ignore white space characters
            .addRule(Rule.head("expression").produces("level1"))

            // level 1 of operator precedence: addition, subtraction
            .addRule(Rule.head("level1").produces("level1-tail"))
            .addRule(Rule.head("level1-tail").produces("level2", "level1-operator:[\\+\\-]", "level1-tail"))
            .addRule(Rule.head("level1-tail").produces("level2"))

            // level 2 of operator precedence: multiplication, division
            .addRule(Rule.head("level2").produces("level2-tail"))
            .addRule(Rule.head("level2-tail").produces("level-final", "level2-operator:[\\*\\/]", "level2-tail"))
            .addRule(Rule.head("level2-tail").produces("level-final"))

            // final level of operator precedence
            .addRule(Rule.head("level-final").produces("term"))

            // expression term definition
            .addRule(Rule.head("term").produces("number:[\\+\\-]?[0-9]*\\.?[0-9]+([eE][\\+\\-]?[0-9]+)?"))
            .addRule(Rule.head("term").produces(":\\(", "expression", ":\\)"))
            .addRule(Rule.head("term").produces("unary-operation"))

            // unary operations
            .addRule(Rule.head("unary-operation").produces("negative"))
            .addRule(Rule.head("unary-operation").produces("positive"))
            .addRule(Rule.head("negative").produces(":\\-", "term"))
            .addRule(Rule.head("positive").produces(":\\+", "term"))

            .build();

    private static Parser parser = new RecursiveDescentParser(grammar);

    private static class ExpressionVisitor implements SyntaxTreeVisitor{
        LinkedList<List<Object>> stack = new LinkedList<>();
        List<Object> currentLevel = new ArrayList<>();
        double value;

        public double getValue() {
            return value;
        }

        @Override
        public void token(TokenNode node) {
            switch (node.name){
                case "number":
                    value = Double.parseDouble(node.value.toString());
                case "level1-operator":
                case "level2-operator":
                    currentLevel.add(value);
                    currentLevel.add(node.value.toString());
                    break;
            }
        }

        @Override
        public void enterConstruct(ConstructNode node) {
            switch (node.name){
                case "level1":
                case "level2":
                    stack.push(currentLevel);
                    currentLevel = new ArrayList<>();
                    break;
            }
        }

        @Override
        public void exitConstruct(ConstructNode node) {
            switch (node.name){
                case "negative":
                    value = -value;
                    break;
                case "level1":
                case "level2":
                    currentLevel.add(value);
                    value = (double) currentLevel.get(0);
                    for(int i = 1; i< currentLevel.size(); i++){
                        String operator = currentLevel.get(i).toString();
                        // operators precedence is encoded in the grammar:
                        // executes the operations in the order they are presented
                        switch(operator){
                            case "+":
                                value += (double) currentLevel.get(++i);
                                break;
                            case "-":
                                value -= (double) currentLevel.get(++i);
                                break;
                            case "*":
                                value *= (double) currentLevel.get(++i);
                                break;
                            case "/":
                                value /= (double) currentLevel.get(++i);
                                break;
                        }
                    }
                    currentLevel = stack.pop();
                    break;
            }
        }
    }

    private static Double evaluate(CharSequence expression) throws UnexpectedSymbolException, ExpectedSymbolsException {
        return parser.parse(expression)
                .accept(new ExpressionVisitor())
                .getValue();
    }

    public static void main(String[] args) throws ExpectedSymbolsException, UnexpectedSymbolException {
        String expression = "2+3*(4+2)*2";
        System.out.println(expression + " = " + evaluate(expression));
    }
}
