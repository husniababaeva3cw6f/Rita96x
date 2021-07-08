package com.lucky_byte.pdf;

import org.xml.sax.Attributes;

public class TextChunk {
	public static final int STYLE_BOLD = 1;
	public static final int STYLE_UNDERLINE = 2;
	public static final int STYLE_ITALIC = 4;
	private String contents;
	private int style;
	private Attributes attr;

	public TextChunk() {
		super();
	}
	public TextChunk(String charas) {
		super();
		this.contents = charas;
	}
	public TextChunk(String charas, int style, Attributes attr) {
		super();
		this.contents = charas;
		this.style = style;
		this.attr = attr;
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
	public Attributes getAttr() {
		return attr;
	}
	public void setAttr(Attributes attr) {
		this.attr = attr;
	}

	public TextChunk clone() {
		TextChunk chunk = new TextChunk();
		chunk.attr = this.attr;
		chunk.contents = new String(this.contents);
		chunk.style = this.style;
		return chunk;
	}
}
