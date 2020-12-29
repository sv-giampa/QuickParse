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

package quickparse.semantics;

import quickparse.parsing.syntaxtree.SyntaxTree;
import quickparse.parsing.syntaxtree.SyntaxTreeVisitor;
import quickparse.parsing.syntaxtree.ConstructNode;
import quickparse.parsing.syntaxtree.TokenNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a function computable on a {@link SyntaxTree}.<br>
 * The computation of each node, produces an object.
 *
 */
public abstract class SyntaxTreeFunction {

    private class FunctionVisitor implements SyntaxTreeVisitor {

        private Object result;
        private LinkedList<List<Object>> stack = new LinkedList<>();

        FunctionVisitor(){}

        @Override
        public void token(TokenNode node) {
            Object result = SyntaxTreeFunction.this.token(node);
            if(result != null)
                stack.peek().add(result);
        }

        @Override
        public void enterConstruct(ConstructNode node) {
            stack.push(new ArrayList<>());
        }

        @Override
        public void exitConstruct(ConstructNode node) {
            List<Object> objects = stack.pop();
            Object constructResult = construct(node, objects);
            if(constructResult != null)
                if(stack.size() > 0)
                    stack.peek().add(constructResult);
                else
                    result = constructResult;
        }

        public Object getResult(){
            return result;
        }
    }

    public final Object apply(SyntaxTree syntaxTree){
        FunctionVisitor functionVisitor = new FunctionVisitor();
        syntaxTree.accept(functionVisitor);
        return functionVisitor.getResult();
    }

    /**
     * Computes an object from the syntax node of a token.
     *
     * @param node    the syntax node of the token
     * @return the object produced by the function from the token
     */
    protected abstract Object token(TokenNode node);

    /**
     * Computes an object from the syntax node of a construct.
     *
     * @param node            the syntax node of the construct
     * @param childrenResults list of the objects produced by computing this same function on the children nodes
     * @return the object produced by the function from the construct
     */
    protected abstract Object construct(ConstructNode node, List<Object> childrenResults);


}
