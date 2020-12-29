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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class defines a wrapper around a text file that is used to efficiently read
 * its content without fully loading it in memory. This implementation uses a
 * strict LRU paging policy. Note that the {@link #charAt(int)} method does not
 * support long (64 bit) indexing, so that the maximum valid file size is equal to
 * {@link Integer#MAX_VALUE}.
 * 
 * @author Salvatore Giampa'
 *
 */
public class FileCharSequence implements CharSequence {
	/**
	 * Default page size
	 */
	public static final int DEFAULT_PAGE_SIZE = 1 * 1024 * 1024; // 1 MB for single page

	/**
	 * Default pages count
	 */
	public static final int DEFAULT_PAGES_COUNT = 200; // 200 MB in total

	/**
	 * Use unicode as default encoding
	 */
	public static final boolean DEFAULT_IS_UNICODE = false;

	/**
	 * Define a internal utility class that represents the cache that is shared
	 * across the current instance and all its sons created by the
	 * {@link FileCharSequence#subSequence(int, int)} method.
	 * 
	 * @author Salvatore Giampa'
	 *
	 */
	private static class LruCache extends LinkedHashMap<Integer, byte[]> {
		private static final long serialVersionUID = -5984381395399427428L;
		private final List<byte[]> freePages;
		private int pagesCount, pageSize;

		public LruCache(int pagesCount, int pageSize) {
			super(pagesCount, 0.75f, true);
			this.pagesCount = pagesCount;
			this.pageSize = pageSize;
			this.freePages = Collections.synchronizedList(new LinkedList<>());
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
			if (size() > pagesCount) {
				freePages.add(eldest.getValue());
				return true;
			}
			return false;
		}

		public byte[] getFreePage(int pageStart) {
			byte[] page;
			synchronized (freePages) {
				if (freePages.size() > 0)
					page = freePages.remove(0);
				else
					page = new byte[pageSize];
				put(pageStart, page);
			}
			return page;
		}
	}

	private boolean isUnicode;
	
	private byte[] lastPage;
	
	private int lastPageStart = -1;

	private LruCache cache;

	private RandomAccessFile raf;
	
	private int start, end;

	/**
	 * As {@link FileCharSequence#FileCharSequence(File, int, int, boolean)},
	 * setting default parameters:<br>
	 * - pages count: {@value #DEFAULT_PAGES_COUNT};<br>
	 * - page size: {@value #DEFAULT_PAGE_SIZE};<br>
	 * - is unicode: {@value #DEFAULT_IS_UNICODE};<br>
	 * 
	 * @param file the file around which this wrapper would be built
	 * @throws IOException if an I/O error occurs
	 */
	public FileCharSequence(File file) throws IOException {
		this(file, DEFAULT_PAGES_COUNT, DEFAULT_PAGE_SIZE, DEFAULT_IS_UNICODE);
	}

	/**
	 * 
	 * Create a wrapper CharSequence around the specified file
	 * 
	 * @param file       the file around which this wrapper would be built
	 * @param pagesCount the number of caching pages
	 * @param pageSize   the size of a cache size
	 * @param isUnicode  indicates that the file is encoded on 16-bit Unicode
	 *                   alphabet
	 * @throws IOException if an I/O error occurs, or if the file is larger than {@link Integer#MAX_VALUE}, because it cannot be indexed through the {@link #charAt(int)} method.
	 */
	public FileCharSequence(File file, int pagesCount, int pageSize, boolean isUnicode) throws IOException {
		if (isUnicode && pageSize % 2 != 0)
			pageSize--;
		raf = new RandomAccessFile(file, "r");
		if (raf.length() > Integer.MAX_VALUE) {
			raf.close();
			throw new IOException("The file is too long for CharSequence");
		}
		this.isUnicode = isUnicode;
		start = 0;
		end = (int) raf.length();
		cache = new LruCache(pagesCount, pageSize);
	}

	/**
	 * Constructor used to create sub-sequences
	 * @param parent the parent that generated the sub sequence
	 * @param start the start of the new sub-sequence, inclusive
	 * @param end the end of the new sub-sequence, exclusive
	 */
	private FileCharSequence(FileCharSequence parent, int start, int end) {
		this.start = parent.start + start;
		this.end = parent.start + end;
		this.isUnicode = parent.isUnicode;
		this.cache = parent.cache;
		this.raf = parent.raf;
	}

	@Override
	public char charAt(int index) {
		index = start+index;
		if (index >= end || index < start)
			throw new IndexOutOfBoundsException();

		int pageStart = cache.pageSize * (index / cache.pageSize);
		byte[] page;
		if(lastPageStart == pageStart)
			page = lastPage;
		else {
			Integer pageKey = Integer.valueOf(pageStart);
			if (cache.containsKey(pageKey)) {
				page = cache.get(pageKey);
			} else {
				page = cache.getFreePage(pageStart);
				synchronized (raf) {
					try {
						raf.seek(start + index);
						raf.read(page);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			lastPage = page;
			lastPageStart = pageStart;
		}

		if (isUnicode) {
			int inPageIndex = 2 * index % cache.pageSize;
			return (char) (page[inPageIndex] << 8 | page[inPageIndex + 1]);
		} else {
			int inPageIndex = index % cache.pageSize;
			return (char) page[inPageIndex];
		}
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
		return new FileCharSequence(this, start, end);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<length(); i++)
			sb.append(charAt(i));
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + ((raf == null) ? 0 : raf.hashCode());
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileCharSequence other = (FileCharSequence) obj;
		if (end != other.end)
			return false;
		if (raf == null) {
			if (other.raf != null)
				return false;
		} else if (!raf.equals(other.raf))
			return false;
		if (start != other.start)
			return false;
		return true;
	}
	
	public static void main(String[] args) throws IOException {
		CharSequence fcs = new FileCharSequence(new File("./README.md"));
		System.out.println(fcs.subSequence(500, 1000));
	}

}
