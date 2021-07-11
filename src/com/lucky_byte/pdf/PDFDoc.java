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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.SplitCharacter;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfChunk;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * PDF 操作类
 * 
 * 这个类封装 PDF 相关的操作，包括写入内容、签名、水印，等等。
 */
public class PDFDoc
{
	private final static int BLOCK_TITLE = 1;
	private final static int BLOCK_SECTION = 2;
	private final static int BLOCK_PARA = 3;
	private final static int BLOCK_VSPACE = 4;

	private Object[][] block_types = {
			{ "title", BLOCK_TITLE },
			{ "section", BLOCK_SECTION },
			{ "para", BLOCK_PARA },
			{ "vspace", BLOCK_VSPACE },
	};

	private OutputStream pdf_stream;
	private Document document;
	private PdfWriter writer;

	private SplitCharacter split_character = new SplitCharacter() {
		@Override
		public boolean isSplitCharacter(int start, int current,
				int end, char[] cc, PdfChunk[] chunk) {
			char c;
			if (chunk == null) {
				c = cc[current];
			} else {
				int posi = Math.min(current, chunk.length - 1);
				c = (char) chunk[posi].getUnicodeEquivalent(cc[current]);
			}
			return (c < ' ');
		}
	};

	public PDFDoc(OutputStream pdf_stream) {
		this.pdf_stream = pdf_stream;
	}

	/**
	 * 打开 PDF 文档进行操作
	 * @return 成功或失败
	 */
	public boolean open() {
		try {
			document = new Document(PageSize.A4,50,50,50,50);
			writer = PdfWriter.getInstance(document, pdf_stream);
			writer.setCompressionLevel(0);
			document.open();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 关闭 PDF 文档，关闭后不能继续操作文档
	 */
	public void close() {
		document.close();
	}

	/**
	 * 通过字体文件获取 PDF 字体
	 * @param fname 字体文件名称
	 * @param style 字体样式
	 * @param size 字体大小
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 */
	private Font getFontFromFile(String fname, int style, int size)
			throws DocumentException, IOException {
		int font_style = 0;

		if ((style & TextChunk.STYLE_BOLD) != 0)
			font_style |= Font.BOLD;

		if ((style & TextChunk.STYLE_ITALIC) != 0) {
			font_style |= Font.ITALIC;
		}
		if ((style & TextChunk.STYLE_UNDERLINE) != 0) {
			font_style |= Font.UNDERLINE;
		}

		BaseFont base_font = BaseFont.createFont(fname,
				BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

		return new Font(base_font, size, font_style);
	}

	/**
	 * 根据字体族获取字体
	 * @param family
	 * @param style
	 * @param size
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 */
	private Font getFont(int family, int style, int size)
			throws DocumentException, IOException {
		switch (family) {
		case TextChunk.FONT_FAMILY_HEI:
			return getFontFromFile("resources/SIMHEI.TTF", style, size);
		case TextChunk.FONT_FAMILY_SONG:
			return getFontFromFile("resources/SIMSUN.TTC,0", style, size);
		default:
			return null;
		}
	}

	private void setChunkFont(TextChunk text_chunk, Chunk chunk)
			throws DocumentException, IOException {
		Map<String, String> attrs = text_chunk.getAttrs();

		String value = attrs.get("size");
		if (value != null) {
			try {
				text_chunk.setFontSize(Integer.parseInt(value));
			} catch (Exception ex) {
				System.err.println("Font size '" + value + "' invalid.");
			}
		}

		value = attrs.get("family");
		if (value != null) {
			if (value.equals("heiti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_HEI);
			} else if (value.equals("songti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_SONG);
			} else {
				System.err.println("Font family '" + value + "' unknown!");
			}
		}

		if (value != null) {
			if (value.equals("heiti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_HEI);
			} else if (value.equals("songti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_SONG);
			} else {
				System.err.println("Font family '" + value + "' unknown!");
			}
		}
		Font font = getFont(text_chunk.getFontFamily(),
				text_chunk.getStyle(), text_chunk.getFontSize());
		chunk.setFont(font);
	}

	/**
	 * 根据 TextChunk 熟悉生成 PDF Chunk 对象
	 * @param text_chunk
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 */
	private Chunk formatChunk(TextChunk text_chunk)
			throws DocumentException, IOException {
		Chunk chunk = new Chunk();

		chunk.append(text_chunk.getContents());
		setChunkFont(text_chunk, chunk);
		return chunk;
	}

	/**
	 * 添加一段文字到 PDF 文档
	 * @param chunk_list
	 * @param alignment
	 * @param indent
	 * @param line_space
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void addParagraph(int block_type, List<TextChunk> chunk_list,
			int alignment, float indent)
			throws DocumentException, IOException {
		Paragraph para = new Paragraph();
		para.setAlignment(alignment);
		para.setFirstLineIndent(indent);
		int font_size = 0;

		for(TextChunk text_chunk : chunk_list) {
			Chunk chunk = formatChunk(text_chunk);
			int size = text_chunk.getFontSize();
			if (size > font_size)
				font_size = size;
			chunk.setSplitCharacter(split_character);
			para.add(chunk);
		}

		if (block_type != BLOCK_VSPACE)
			para.setSpacingBefore(font_size / 2);

		document.add(para);
	}

	/**
	 * 添加一块内容到 PDF 文档，块可以为 Title、Section、等等，
	 * 参考类前面的数组定义
	 * @param qName
	 * @param chunk_list
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeBlock(String qName, List<TextChunk> chunk_list)
			throws DocumentException, IOException {
		int block_type = -1;
		for (int i = 0; i < block_types.length; i++) {
			if (qName.equals(block_types[i][0])) {
				block_type = (Integer) block_types[i][1];
				break;
			}
		}
		int font_family = TextChunk.FONT_FAMILY_SONG;
		int font_size = 12;
		int font_style = 0;
		int alignment = Element.ALIGN_LEFT;
		float indent = 0f;

		switch (block_type) {
		case BLOCK_TITLE:
			font_family = TextChunk.FONT_FAMILY_HEI;
			font_size = 18;
			font_style = TextChunk.STYLE_BOLD;
			alignment = Element.TITLE;
			break;
		case BLOCK_SECTION:
			font_size = 16;
			font_style = TextChunk.STYLE_BOLD;
			break;
		case BLOCK_PARA:
			indent = 15f;
			break;
		case BLOCK_VSPACE:
			break;
		default:
			System.out.println("Block element `" + qName + "` unknown!");
			return;
		}

		for(TextChunk text_chunk : chunk_list) {
			text_chunk.setFontFamily(font_family);
			text_chunk.setFontSize(font_size);
			text_chunk.addStyle(font_style);
		}
		this.addParagraph(block_type, chunk_list, alignment, indent);
	}

}
