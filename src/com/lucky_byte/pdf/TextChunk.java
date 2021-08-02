package com.lucky_byte.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

public class TextChunk
{
	private String contents;
	private Map<String, String> attrs;

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

	public TextChunk clone() {
		TextChunk chunk = new TextChunk();

		Set<String> keys = this.attrs.keySet();
		for (String key : keys) {
			chunk.attrs.put(new String(key),
					new String(this.attrs.get(key)));
		}
		chunk.contents = new String(this.contents);
		return chunk;
	}
}
