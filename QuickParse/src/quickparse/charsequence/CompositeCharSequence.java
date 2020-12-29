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

package quickparse.charsequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class allows to compose more {@link CharSequence} instances.
 * This class aims to preserve the efficiency of each {@link CharSequence} implementation passed to its constructor.
 */
public class CompositeCharSequence implements CharSequence {

	private ArrayList<CharSequence> sequences;
	private ArrayList<Integer> indices;

	private int start;
	private int end;

	/**
	 * Creates a new {@link CompositeCharSequence}
	 * @param charSequences the {@link CharSequence char sequencies} to compose
	 */
	public CompositeCharSequence(CharSequence... charSequences) {
		this(Arrays.asList(charSequences));
	}


	/**
	 * Creates a new {@link CompositeCharSequence}
	 * @param charSequences the {@link CharSequence char sequencies} to compose
	 */
	public CompositeCharSequence(List<CharSequence> charSequences) {
		sequences = new ArrayList<>(charSequences);
		indices = new ArrayList<>(charSequences.size());

		int index = 0;
		for (CharSequence s : sequences) {
			indices.add(index);
			index += s.length();
		}

		end = index;
	}


	// used to create a sub-sequence of a CompositeCharSequence
	private CompositeCharSequence(CompositeCharSequence parent, int start, int end) {
		this.start = parent.start + start;
		this.end = parent.start + end;
		this.sequences = parent.sequences;
		this.indices = parent.indices;
	}

	@Override
	public char charAt(int index) {
		if (index >= end || index < start)
			throw new IndexOutOfBoundsException();
		index = start + index;
		int seqIndex = getSequenceIndex(index);
		int indexInSeq = (seqIndex == 0) ? index : index - indices.get(seqIndex);
		return sequences.get(seqIndex)
				.charAt(indexInSeq);
	}

	@Override
	public int length() {
		return end - start;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (start < 0 || end < 0 || end < start || end > this.end || start > this.end)
			throw new IndexOutOfBoundsException(
					String.format("start: %s, end: %s, sequence-length: %s", start, end, this.end - this.start));
		return new CompositeCharSequence(this, start, end);
	}

	@Override
	public String toString() {
		int startSeqIndex = getSequenceIndex(start);
		int startIndexInSeq = (startSeqIndex == 0) ? start : start - indices.get(startSeqIndex);
		int endSeqIndex = getSequenceIndex(end);
		int endIndexInSeq = (endSeqIndex == 0) ? end : end - indices.get(endSeqIndex);

		StringBuilder sb = new StringBuilder(end - start);

		for (int seqIndex = startSeqIndex; seqIndex <= endSeqIndex; seqIndex++) {
			CharSequence sequence = sequences.get(seqIndex);
			int start = 0, end = sequence.length();
			boolean subSequence = false;
			if (seqIndex == startSeqIndex) {
				start = startIndexInSeq;
				subSequence = true;
			}
			if (seqIndex == endSeqIndex) {
				end = endIndexInSeq;
				subSequence = true;
			}
			if (subSequence)
				sb.append(sequence.subSequence(start, end)
						.toString());
			else
				sb.append(sequence.toString());
		}

		return sb.toString();
	}

	private int getSequenceIndex(int charIndex) {
		int seqIndex = Collections.binarySearch(indices, Integer.valueOf(charIndex));
		if (seqIndex < 0)
			seqIndex = -(seqIndex + 1) - 1;

		return seqIndex;
	}

	public static void main(String[] args) {

		CharSequence world = new CompositeCharSequence("worl", "d");
		CharSequence helloWorld = new CompositeCharSequence("he", "llo ", world, "!!!");
		CharSequence heSaysHelloWorld = new CompositeCharSequence("he s", "ays", ": ", helloWorld);
		System.out.println(heSaysHelloWorld);
		System.out.println("charAt(5): " + heSaysHelloWorld.charAt(5));
		System.out.println("charAt(12): " + heSaysHelloWorld.charAt(12));
		System.out.println("charAt(14): " + heSaysHelloWorld.charAt(15));
		System.out.println("subSequence(12, 15): " + heSaysHelloWorld.subSequence(11, 17));
	}

}
