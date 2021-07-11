package com.lucky_byte.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

public class TextChunk {
	public static final int STYLE_BOLD = 1;
	public static final int STYLE_UNDERLINE = 2;
	public static final int STYLE_ITALIC = 4;
	public static final int FONT_FAMILY_HEI = 1;
	public static final int FONT_FAMILY_SONG = 2;

	private String contents;
	private int style;
	private Map<String, String> attrs;
	private int font_size;
	private int font_family;

	public TextChunk() {
		super();
		attrs = new HashMap<String, String>();
	}

	public TextChunk(String charas, int style, Attributes attrs) {
		super();
		this.contents = charas;
		this.style = style;
		this.attrs = new HashMap<String, String>();
		for (int i = 0; i < attrs.getLength(); i++) {
			String name = attrs.getQName(i);
			String value = attrs.getValue(i);
			this.attrs.put(name, value);
		}
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String charas) {
		this.contents = charas;
	}
	public int getStyle() {
		return style;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	public void addStyle(int style) {
		this.style |= style;
	}
	public void delStyle(int style) {
		this.style &= ~style;
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

	public int getFontSize() {
		return font_size;
	}
	public void setFontSize(int font_size) {
		this.font_size = font_size;
	}
	public int getFontFamily() {
		return font_family;
	}
	public void setFontFamily(int font_family) {
		this.font_family = font_family;
	}

	public TextChunk clone() {
		TextChunk chunk = new TextChunk();

		Set<String> keys = this.attrs.keySet();
		for (String key : keys) {
			chunk.attrs.put(new String(key),
					new String(this.attrs.get(key)));
		}
		chunk.contents = new String(this.contents);
		chunk.style = this.style;
		chunk.font_family = this.font_family;
		chunk.font_size = this.font_size;
		return chunk;
	}
}
