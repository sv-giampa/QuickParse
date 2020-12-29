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

package quickparse.semantics.interpreters;

import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.parsing.syntaxtree.TokenNode;
import quickparse.semantics.SyntaxTreeFunction;
import quickparse.semantics.interpreters.exception.SemanticsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SimpleInterpreter<T> implements Interpreter<T>{
    public interface TokenRule{
        Object apply(TokenNode node);
    }

    public interface ConstructRule {
        Object apply(ConstructNode node, List<Object> list);
    }

    private Map<String, TokenRule> tokenRules = new HashMap<>();
    private Map<String, ConstructRule> constructRules = new HashMap<>();

    protected SimpleInterpreter(){}

    private SyntaxTreeFunction syntaxTreeFunction = new SyntaxTreeFunction(){
        @Override
        public Object token(TokenNode node) {
            if(node.name.equals(""))
                return null;
            if(tokenRules.containsKey(node.name))
                return tokenRules.get(node.name).apply(node);
            return node.value;
        }

        @Override
        public Object construct(ConstructNode node, List<Object> childrenResults) {
            if(constructRules.containsKey(node.name))
                return constructRules.get(node.name).apply(node, childrenResults);
            return childrenResults;
        }
    };

    @Override
    public T analyze(SyntaxTree syntaxTree) throws SemanticsException {
        try {
            @SuppressWarnings("unchecked")
            T res = (T) syntaxTreeFunction.apply(syntaxTree);
            return res;
        }catch(Exception e){
            throw new SemanticsException(e);
        }
    }

    public static Builder create(){
        return new SimpleInterpreter.Builder();
    }

    public static class Builder{
        SimpleInterpreter<?> interpreter = new SimpleInterpreter<>();

        public Builder token(String token, TokenRule tokenrule){
            interpreter.tokenRules.put(token, tokenrule);
            return this;
        }

        public Builder construct(String construct, ConstructRule constructRule){
            interpreter.constructRules.put(construct, constructRule);
            return this;
        }

        public <T> SimpleInterpreter<T> build(){
            try {
                @SuppressWarnings("unchecked")
                SimpleInterpreter<T> res = (SimpleInterpreter<T>) interpreter;
                return res;
            } finally{
                interpreter = new SimpleInterpreter<>();
            }
        }
    }

}
