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

package quickparse.parsing.syntaxtree;

/**
 * Defines a SyntaxTreeVisitor for a {@link SyntaxTree syntax tree}
 */
public interface SyntaxTreeVisitor {

    /**
     * Visits the syntax node of a token.
     *
     * @param node    the syntax node relative to the token
     */
    void token(TokenNode node);

    /**
     * Enters into the syntax node of a construct.
     *
     * @param node            the syntax node relative to the construct
     */
    void enterConstruct(ConstructNode node);

    /**
     * Exits from the syntax node of a construct.
     *
     * @param node            the syntax node relative to the construct
     */
    void exitConstruct(ConstructNode node);
}
