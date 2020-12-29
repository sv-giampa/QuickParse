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
import java.util.regex.Pattern;

public class TokenNode extends SyntaxTree {
    public final Pattern pattern;

    public TokenNode(CharSequence source, int start, int end, String name, Pattern pattern) {
        super(source, start, end, name);
        this.pattern = pattern;
    }

    @Override
    public <STV extends SyntaxTreeVisitor> STV accept(STV syntaxTreeVisitor) {
        syntaxTreeVisitor.token(this);
        return syntaxTreeVisitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TokenNode tokenNode = (TokenNode) o;
        return Objects.equals(pattern, tokenNode.pattern) && Objects.equals(value, tokenNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pattern, value);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++)
            sb.append("|--");
        sb.append(
                String.format("+ Token [name=%s, value={%s}, pattern={%s}, start=%d, end=%d]\n",
                        name, value, pattern, start, end));
        return sb.toString();
    }
}
