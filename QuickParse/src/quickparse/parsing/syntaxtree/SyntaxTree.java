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

import java.util.Objects;

/**
 * Represents a generic node of a syntax tree, which is the result of a parsing process. A syntax tree node represents a {@link CharSequence char sequence} matched by a grammar rule.<br>
 * A generic node of a syntax tree has four properties:
 * <ul>
 *     <li>Source: the source {@link CharSequence char sequence} that has been parsed;</li>
 *     <li>Start index: the position in the Source at which the matched {@link CharSequence char sequence} starts;</li>
 *     <li>End index: the position in the Source at which the matched {@link CharSequence char sequence} ends;</li>
 *     <li>Name: the name of the Construct or Token matched (i.e., the head symbol of the grammar rule matched);</li>
 * </ul>
 *
 */
public abstract class SyntaxTree {
    public final CharSequence source;
    public final int start;
    public final int end;
    public final String name;
    public final CharSequence value;

    protected SyntaxTree(CharSequence source, int start, int end, String name) {
        this.source = source;
        this.start = start;
        this.end = end;
        this.name = name;
        this.value = source.subSequence(start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyntaxTree that = (SyntaxTree) o;
        return start == that.start && end == that.end && Objects.equals(source, that.source) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, name);
    }

    /**
     * Encodes this structure and its hierarchy in a string.<br>
     * @return a {@link String} object
     * @see Object#toString() 
     * @see #toString(int)
     */
    @Override
    public String toString(){
        return "+ Syntax tree [source={"+source.subSequence(start, end)+"}]\n"+toString(1);
    }

    /**
     * Encodes this structure and its hierarchy in a string.<br>
     * @param level The deep level in the hierarchy.
     * @return a {@link String} object
     * @see #toString()
     */
    public abstract String toString(int level);

    /**
     * Accepts a {@link SyntaxTreeVisitor} and uses it to visit this node and its eventually present children.
     * @param <STV> the sub-type of {@link SyntaxTreeVisitor}
     * @param syntaxTreeVisitor the {@link SyntaxTreeVisitor visitor} to accept
     * @return the same {@link SyntaxTreeVisitor visitor} given as input
     */
    public abstract <STV extends SyntaxTreeVisitor> STV accept(STV syntaxTreeVisitor);

}
