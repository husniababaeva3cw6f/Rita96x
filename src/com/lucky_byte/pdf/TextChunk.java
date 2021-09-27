/* TextPDF - generate PDF dynamically
 * 
 * Copyright (c) 2015 Lucky Byte, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.lucky_byte.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

public class TextChunk
{
	private String contents;
	private Map<String, String> attrs;
	private boolean is_value;

	public TextChunk() {
		attrs = new HashMap<String, String>();
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String charas) {
		this.contents = charas;
	}

	public Map<String, String> getAttrs() {
		return attrs;
	}

	public void addAttrs(Attributes attrs) {
		for (int i = 0; i < attrs.getLength(); i++) {
			String name = attrs.getQName(i);
			String value = attrs.getValue(i);
			this.attrs.put(name, value);
		}
	}

	public void addAttrs(Map<String, String> attrs) {
		Set<String> keys = attrs.keySet();
		for (String key : keys) {
			this.attrs.put(new String(key), new String(attrs.get(key)));
		}
	}

	public void addAttr(String key, String value) {
		if (key != null && value != null) {
			attrs.put(key, value);
		}
	}

	public boolean isValue() {
		return is_value;
	}

	public void setIsValue(boolean is_value) {
		this.is_value = is_value;
	}

	public TextChunk clone() {
		TextChunk chunk = new TextChunk();

		Set<String> keys = this.attrs.keySet();
		for (String key : keys) {
			chunk.attrs.put(new String(key),
					new String(this.attrs.get(key)));
		}
		chunk.contents = new String(this.contents);
		chunk.is_value = this.is_value;
		return chunk;
	}

}
