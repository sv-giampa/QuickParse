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

import quickparse.grammar.Rule;

import java.util.List;
import java.util.Objects;

public class ConstructNode extends SyntaxTree {
    public final List<SyntaxTree> children;
    public final Rule rule;

    public ConstructNode(CharSequence source, int start, int end, String name, List<SyntaxTree> children, Rule rule) {
        super(source, start, end, name);
        this.children = children;
        this.rule = rule;
    }

    @Override
    public <STV extends SyntaxTreeVisitor> STV accept(STV syntaxTreeVisitor) {
        syntaxTreeVisitor.enterConstruct(this);

        for (SyntaxTree child : children)
            child.accept(syntaxTreeVisitor);

        syntaxTreeVisitor.exitConstruct(this);
        return syntaxTreeVisitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConstructNode that = (ConstructNode) o;
        return Objects.equals(children, that.children) && Objects.equals(rule, that.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), children, rule);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++)
            sb.append("|--");
        sb.append(String.format("+ Construct [name=%s, rule={%s}, start=%d, end=%d]\n",
                        name, rule, start, end));
        for (SyntaxTree child : children) {
            sb.append(child.toString(level + 1));
        }
        return sb.toString();
    }

}
