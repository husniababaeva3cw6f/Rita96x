package com.lucky_byte.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

public class TextTable
{
	private Map<String, String> attrs;
	private List<TextChunk> cells;

	public TextTable() {
		cells = new ArrayList<TextChunk>();
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
			this.attrs.put(key, attrs.get(key));
		}
	}

	public void addAttr(String key, String value) {
		if (key != null && value != null) {
			attrs.put(key, value);
		}
	}

	public List<TextChunk> getCells() {
		return cells;
	}

	public void addCell(TextChunk chunk) {
		cells.add(chunk);
	}

	public TextChunk lastCell() {
		int last = cells.size() - 1;
		return cells.get(last);
	}

}
