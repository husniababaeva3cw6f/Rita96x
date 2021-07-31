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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.SplitCharacter;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfChunk;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * 一个类用于保存块默认属性
 */
class PDFBlockDefault
{
	int block_type;
	int font_family;
	int font_size;
	int font_style;
	int alignment;
	float indent;
	float line_space_before;
	float line_space_after;

	public PDFBlockDefault(int block_type, int family,
			int size, int style, int alignment, float indent,
			float line_space_before, float line_space_after) {
		this.block_type = block_type;
		this.font_family = family;
		this.font_size = size;
		this.font_style = style;
		this.alignment = alignment;
		this.indent = indent;
		this.line_space_before = line_space_before;
		this.line_space_after = line_space_after;
	}
}

/**
 * PDF 操作类
 * 
 * 这个类封装 PDF 文档相关的操作，通过 iText 实现。
 * 如果需要水印、印章等特殊效果，请参考 PDFProcess 类。
 */
public class PDFDoc
{
	public final static int BLOCK_TITLE = 1;
	public final static int BLOCK_SECTION = 2;
	public final static int BLOCK_PARA = 3;

	private Object[][] block_types = {
			{ "title", BLOCK_TITLE },
			{ "section", BLOCK_SECTION },
			{ "para", BLOCK_PARA },
	};
	private List<PDFBlockDefault> block_defaults;

	private Rectangle page_size = PageSize.A4;
	private int page_margin_left = 50;
	private int page_margin_right = 50;
	private int page_margin_top = 50;
	private int page_margin_bottom = 50;

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
		
		block_defaults = new ArrayList<PDFBlockDefault>();

		// 默认的块属性，应用程序可以通过 setBlockDefault() 来修改这些属性
		block_defaults.add(new PDFBlockDefault(BLOCK_TITLE,
				TextChunk.FONT_FAMILY_HEI, 18, TextChunk.STYLE_BOLD,
				Element.ALIGN_CENTER, 0.0f, 0.0f, 16.0f));
		block_defaults.add(new PDFBlockDefault(BLOCK_SECTION,
				TextChunk.FONT_FAMILY_SONG, 16, TextChunk.STYLE_BOLD,
				Element.ALIGN_LEFT, 0.0f, 13.0f, 0.0f));
		block_defaults.add(new PDFBlockDefault(BLOCK_PARA,
				TextChunk.FONT_FAMILY_SONG, 12, 0,
				Element.ALIGN_LEFT, 22.0f, 6.0f, 0.0f));
	}

	/**
	 * 打开 PDF 文档进行操作
	 * @return 成功或失败
	 */
	public boolean open() {
		try {
			document = new Document(page_size, page_margin_left,
					page_margin_right, page_margin_top, page_margin_bottom);
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
	 * 测试文档是否已打开
	 * @return
	 */
	public boolean isOpen() {
		if (document == null)
			return false;
		return document.isOpen();
	}

	/**
	 * 设置页面大小
	 * @param page_size
	 */
	public void setPageSize(Rectangle page_size) {
		this.page_size = page_size;
	}

	/**
	 * 设置页面边距
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public void setPageMargin(int left, int right, int top, int bottom) {
		page_margin_left = left;
		page_margin_right = right;
		page_margin_top = top;
		page_margin_bottom = bottom;
	}

	/**
	 * 设置块默认属性
	 * 这个函数一次性设置所有的块默认属性，如果需要单独设置某一个属性，
	 * 请使用下面的 setBlockDefaultXXX() 函数。
	 * @param block_type 块类型
	 * @param font_family 字体家族
	 * @param font_size 字体大小
	 * @param font_style 字体风格
	 * @param alignment 对齐方式
	 * @param indent 首行缩进距离
	 */
	public void setBlockDefault(int block_type, int font_family,
			int font_size, int font_style, int alignment, float indent,
			float line_space_before, float line_space_after) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.font_family = font_family;
				block.font_size = font_size;
				block.font_style = font_style;
				block.alignment = alignment;
				block.indent = indent;
				block.line_space_before = line_space_before;
				block.line_space_after = line_space_after;
				break;
			}
		}
	}

	/**
	 * 设置块的默认字体家族
	 * @param block_type
	 * @param font_family
	 */
	public void setBlockDefaultFontFamily(int block_type, int font_family) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.font_family = font_family;
				break;
			}
		}
	}

	/**
	 * 设置块的默认字体大小
	 * @param block_type
	 * @param font_size
	 */
	public void setBlockDefaultFontSize(int block_type, int font_size) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.font_size = font_size;
				break;
			}
		}
	}

	/**
	 * 设置块的默认字体风格
	 * @param block_type
	 * @param font_style
	 */
	public void setBlockDefaultFontStyle(int block_type, int font_style) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.font_style = font_style;
				break;
			}
		}
	}

	/**
	 * 设置块的默认对齐方式
	 * @param block_type
	 * @param alignment
	 */
	public void setBlockDefaultAlignment(int block_type, int alignment) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.alignment = alignment;
				break;
			}
		}
	}

	/** 设置块的默认首行缩进距离
	 * @param block_type
	 * @param indent
	 */
	public void setBlockDefaultIndent(int block_type, float indent) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.indent = indent;
				break;
			}
		}
	}

	/**
	 * 设置段前空间
	 * @param block_type
	 * @param line_space_before
	 */
	public void setBlockDefaultLineSpaceBefore(int block_type,
			float line_space_before) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.line_space_before = line_space_before;
				break;
			}
		}
	}

	/**
	 * 设置段后空间
	 * @param block_type
	 * @param line_space_after
	 */
	public void setBlockDefaultLineSpaceAfter(int block_type,
			float line_space_after) {
		for (PDFBlockDefault block : block_defaults) {
			if (block.block_type == block_type) {
				block.line_space_after = line_space_after;
				break;
			}
		}
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

		// 设置字体样式
		if ((style & TextChunk.STYLE_BOLD) != 0) {
			font_style |= Font.BOLD;
		}
		if ((style & TextChunk.STYLE_ITALIC) != 0) {
			font_style |= Font.ITALIC;
		}
		if ((style & TextChunk.STYLE_UNDERLINE) != 0) {
			font_style |= Font.UNDERLINE;
		}

		BaseFont base_font = BaseFont.createFont(fname,
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

		return new Font(base_font, size, font_style);
	}

	/**
	 * 根据字体族获取字体
	 * @param family 字体族
	 * @param style 字体风格
	 * @param size 字体大小
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

	/**
	 * 根据 TextChunk 中字体相关的属性来设置 Chunk 的字体，字体包括：
	 * 家族(黑体或宋体)、大小、修饰(粗体、斜体、下划线等等)。
	 * @param text_chunk TextChunk 对象，保存了字体的属性
	 * @param chunk PDF Chunk 对象
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void setChunkFont(TextChunk text_chunk, Chunk chunk)
			throws DocumentException, IOException {
		Map<String, String> attrs = text_chunk.getAttrs();

		// 设置字体族
		String value = attrs.get("family");
		if (value != null) {
			if (value.equalsIgnoreCase("heiti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_HEI);
			} else if (value.equalsIgnoreCase("songti")) {
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_SONG);
			} else {
				System.err.println("Font family '" + value + "' unknown!");
				text_chunk.setFontFamily(TextChunk.FONT_FAMILY_SONG);
			}
		}

		// 设置字体大小
		value = attrs.get("size");
		if (value != null) {
			try {
				text_chunk.setFontSize(Integer.parseInt(value));
			} catch (Exception ex) {
				System.err.println("Font size '" + value + "' invalid.");
				text_chunk.setFontSize(12);
			}
		}

		chunk.setFont(getFont(text_chunk.getFontFamily(),
				text_chunk.getStyle(), text_chunk.getFontSize()));
	}

	/**
	 * 根据 TextChunk 的属性生成 PDF Chunk 对象
	 * @param text_chunk TextChunk 对象
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

	private void formatParagraph(Paragraph para, List<TextChunk> chunk_list) {
		// 用第一个节点来设置块的段落属性
		TextChunk text_chunk = chunk_list.get(0);
		if (text_chunk != null) {
			Map<String, String> attrs = text_chunk.getAttrs();

			// 设置段落对齐方式
			String value = attrs.get("align");
			if (value != null) {
				if (value.equalsIgnoreCase("left")) {
					para.setAlignment(Element.ALIGN_LEFT);
				} else if (value.equalsIgnoreCase("center")) {
					para.setAlignment(Element.ALIGN_CENTER);
				} else if (value.equalsIgnoreCase("right")) {
					para.setAlignment(Element.ALIGN_RIGHT);
				} else {
					System.err.println("Block alignment type '"
							+ value + "' unknown.");
				}
			}

			// 设置段落缩进
			value = attrs.get("indent");
			if (value != null) {
				try {
					float indent = Float.parseFloat(value);
					para.setFirstLineIndent(indent);
				} catch (Exception ex) {
					System.err.println("Indent attribute must has a float value");
				}
			}

			// 设置段落前空间
			value = attrs.get("space-before");
			if (value != null) {
				try {
					float space = Float.parseFloat(value);
					para.setSpacingBefore(space);
				} catch (Exception ex) {
					System.err.println("space-before attribute must has a float value");
				}
			}

			// 设置段落后空间
			value = attrs.get("space-after");
			if (value != null) {
				try {
					float space = Float.parseFloat(value);
					para.setSpacingAfter(space);
				} catch (Exception ex) {
					System.err.println("space-after attribute must has a float value");
				}
			}
		}
	}

	/**
	 * 添加一段文字到 PDF 文档
	 * @param chunk_list chunks 列表
	 * @param alignment 对齐方式
	 * @param indent 首行缩进空间
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void addParagraph(int block_type, List<TextChunk> chunk_list,
			PDFBlockDefault block_default)
			throws DocumentException, IOException {
		Paragraph para = new Paragraph();

		for(TextChunk text_chunk : chunk_list) {
			text_chunk.setFontFamily(block_default.font_family);
			text_chunk.setFontSize(block_default.font_size);
			text_chunk.addStyle(block_default.font_style);

			Chunk chunk = formatChunk(text_chunk);
			chunk.setSplitCharacter(split_character);
			para.add(chunk);
		}
		para.setSpacingBefore(block_default.line_space_before);
		para.setSpacingAfter(block_default.line_space_after);
		para.setAlignment(block_default.alignment);
		para.setFirstLineIndent(block_default.indent);

		formatParagraph(para, chunk_list);
		document.add(para);
	}

	/**
	 * 添加一块内容到 PDF 文档，块可以为 Title、Section、等等，
	 * 参考类前面的数组定义
	 * @param block_name 块类型名，例如 title, section 等等
	 * @param chunk_list 本块的内容，一个块包含多个 chunk，它们通过列表保存
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeBlock(String block_name, List<TextChunk> chunk_list)
			throws DocumentException, IOException {
		if (block_name == null ||
				chunk_list == null || chunk_list.size() == 0) {
			return;
		}

		int block_type = -1;

		// 将块名称映射到内部的整数表示
		for (int i = 0; i < block_types.length; i++) {
			if (block_name.equalsIgnoreCase((String) block_types[i][0])) {
				block_type = (Integer) block_types[i][1];
				break;
			}
		}
		if (block_type == -1) {
			System.err.println("Block type '" + block_name + "' unknown!");
			return;
		}

		for (PDFBlockDefault block_default : block_defaults) {
			if (block_default.block_type == block_type) {
				addParagraph(block_type, chunk_list, block_default);
				break;
			}
		}
	}

}
