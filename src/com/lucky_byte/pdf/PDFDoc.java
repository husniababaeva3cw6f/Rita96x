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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.SplitCharacter;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfChunk;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;


/**
 * ????????????????????????????????????
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
 * PDF ?????????
 * 
 * ??????????????? PDF ?????????????????????????????? iText ?????????
 * ?????????????????????????????????????????????????????? PDFProcess ??????
 */
public class PDFDoc extends TextDoc
{
	public final static int BLOCK_TITLE = 1;
	public final static int BLOCK_CHAPTER = 2;
	public final static int BLOCK_SECTION = 3;
	public final static int BLOCK_PARA = 4;

	public final static int FONT_FAMILY_HEI = 1;
	public final static int FONT_FAMILY_SONG = 2;

	public static final int FONT_STYLE_BOLD = 1;
	public static final int FONT_STYLE_UNDERLINE = 2;
	public static final int FONT_STYLE_ITALIC = 4;

	private Object[][] block_types = {
			{ "title",   BLOCK_TITLE },
			{ "chapter", BLOCK_CHAPTER },
			{ "section", BLOCK_SECTION },
			{ "para",    BLOCK_PARA },
	};
	private List<PDFBlockDefault> block_defaults;

	private Document document;
	private PdfWriter writer;
	private Map<String, Image> images;

	private SplitCharacter split_character = new SplitCharacter() {
		@Override
		public boolean isSplitCharacter(int start, int current,
				int end, char[] cc, PdfChunk[] chunk) {
			return true;
		}
	};

	public PDFDoc(OutputStream pdf_stream) {
		super(pdf_stream);

		block_defaults = new ArrayList<PDFBlockDefault>();
		images = new HashMap<String, Image>();

		// ????????????????????????????????????????????? setBlockDefault() ?????????????????????
		block_defaults.add(new PDFBlockDefault(BLOCK_TITLE,
				FONT_FAMILY_HEI, 18, FONT_STYLE_BOLD,
				Element.ALIGN_CENTER, 0.0f, 0.0f, 16.0f));
		block_defaults.add(new PDFBlockDefault(BLOCK_CHAPTER,
				FONT_FAMILY_SONG, 16, FONT_STYLE_BOLD,
				Element.ALIGN_LEFT, 0.0f, 14.0f, 0.0f));
		block_defaults.add(new PDFBlockDefault(BLOCK_SECTION,
				FONT_FAMILY_SONG, 14, FONT_STYLE_BOLD,
				Element.ALIGN_LEFT, 0.0f, 12.0f, 0.0f));
		block_defaults.add(new PDFBlockDefault(BLOCK_PARA,
				FONT_FAMILY_SONG, 12, 0,
				Element.ALIGN_LEFT, 22.0f, 6.0f, 0.0f));
	}

	private void addMetaInfo() {
		document.addTitle("TextPdf ??????");
		document.addSubject("???????????????????????????????????????????????????");
		document.addAuthor("Lucky Byte, Inc.(??????)");
		document.addKeywords("TextPdf, PDF, Lucky Byte Inc., ??????");
		document.addCreator("TextPdf ?????? " + Version.VERSION +
				" - http://git.oschina.net/lucky-byte/textpdf");
	}

	/**
	 * ?????? PDF ??????????????????
	 * @return ???????????????
	 */
	@Override
	public boolean open() {
		try {
			document = new Document();
			document.setMargins(page_margin_left, page_margin_right,
					page_margin_top, page_margin_bottom);
			writer = PdfWriter.getInstance(document, out_stream);
			// writer.setFullCompression();	// ?????? PDF 1.5
			writer.setCompressionLevel(9);
			addMetaInfo();
			document.open();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ?????? PDF ??????????????????????????????????????????
	 */
	@Override
	public void close() {
		document.close();
	}

	/**
	 * ???????????????????????????
	 * @return
	 */
	@Override
	public boolean isOpen() {
		if (document == null)
			return false;
		return document.isOpen();
	}

	@Override
	public void setPageSize(Rectangle page_size) {
		super.setPageSize(page_size);
		if (isOpen()) {
			document.setPageSize(page_size);
		}
	}

	@Override
	public void setPageMargin(int left, int right, int top, int bottom) {
		super.setPageMargin(left, right, top, bottom);
		if (isOpen()) {
			document.setMargins(page_margin_left, page_margin_right,
					page_margin_top, page_margin_bottom);
		}
	}

	/**
	 * ?????????????????????
	 * ????????????????????????????????????????????????????????????????????????????????????????????????
	 * ?????????????????? setBlockDefaultXXX() ?????????
	 * @param block_type ?????????
	 * @param font_family ????????????
	 * @param font_size ????????????
	 * @param font_style ????????????
	 * @param alignment ????????????
	 * @param indent ??????????????????
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
	 * ??????????????????????????????
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
	 * ??????????????????????????????
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
	 * ??????????????????????????????
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
	 * ??????????????????????????????
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

	/** ????????????????????????????????????
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
	 * ??????????????????
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
	 * ??????????????????
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
	 * ?????? TextChunk ????????????????????????????????? Chunk ???????????????????????????
	 * ??????(???????????????)??????????????????(?????????????????????????????????)???
	 * @param text_chunk TextChunk ?????????????????????????????????
	 * @param chunk PDF Chunk ??????
	 * @param block_default 
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void setChunkFont(TextChunk text_chunk, Chunk chunk,
			PDFBlockDefault block_default)
					throws DocumentException, IOException {
		Map<String, String> attrs = text_chunk.getAttrs();

		int font_family = block_default.font_family;
		int font_size = block_default.font_size;
		int font_style = block_default.font_style;
		BaseFont base_font = null;

		String value = attrs.get("font-family");
		if (value != null) {
			if (value.equalsIgnoreCase("heiti") ||
					value.equalsIgnoreCase("hei")) {
				font_family = FONT_FAMILY_HEI;
			} else if (value.equalsIgnoreCase("songti") ||
					value.equalsIgnoreCase("song")) {
				font_family = FONT_FAMILY_SONG;
			} else {
				System.err.println("Font family '" + value + "' unknown!");
			}
		}

		value = attrs.get("font-size");
		if (value != null) {
			try {
				font_size = Integer.parseInt(value);
			} catch (Exception ex) {
				System.err.println("Font size '" + value + "' invalid.");
			}
		}

		value = attrs.get("font-style");
		if (value != null) {
			font_style = 0;
			String[] styles = value.split(",");
			for (int i = 0; i < styles.length; i++) {
				String label = styles[i].trim();
				if (label.equalsIgnoreCase("bold")) {
					font_style |= Font.BOLD;
				} else if (label.equalsIgnoreCase("italic")) {
					font_style |= Font.ITALIC;
				} else if (label.equalsIgnoreCase("underline")) {
					font_style |= Font.UNDERLINE;
				}
			}
		}

		switch (font_family) {
		case FONT_FAMILY_HEI:
			base_font = BaseFont.createFont("resources/SIMHEI.TTF",
					BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			break;
		case FONT_FAMILY_SONG:
			base_font = BaseFont.createFont("resources/SIMSUN.TTC,0",
					BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			break;
		}
		chunk.setFont(new Font(base_font, font_size, font_style));
	}

	/**
	 * ?????? TextChunk ??????????????? PDF Chunk ??????
	 * @param text_chunk TextChunk ??????
	 * @param block_default 
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 */
	private Chunk formatChunk(TextChunk text_chunk,
			PDFBlockDefault block_default)
					throws DocumentException, IOException {
		Chunk chunk = new Chunk();
		Map<String, String> attrs = text_chunk.getAttrs();

		String value = attrs.get("super");
		if (value != null && value.equalsIgnoreCase("true")) {
			chunk.setTextRise(6.0f);
			if (!attrs.containsKey("font-size")) {
				attrs.put("font-size", "8");
			}
		}
		value = attrs.get("sub");
		if (value != null && value.equalsIgnoreCase("true")) {
			chunk.setTextRise(-3.0f);
			if (!attrs.containsKey("font-size")) {
				attrs.put("font-size", "8");
			}
		}

		String contents = text_chunk.getContents();

		value = text_chunk.getAttrs().get("minlen");
		if (value != null && value.length() > 0) {
			if (contents.length() == 0) {
				chunk.setUnderline(1.0f, -4.0f);
			}
			try {
				int minlen = Integer.parseInt(value);
				int currlen = 0;
				for (int i = 0; i < contents.length(); i++) {
					char ch = contents.charAt(i);
					if (ch < 127) {
						currlen += 1;
					} else {
						currlen += 2;
					}
				}
				if (currlen < minlen) {
					StringBuilder builder = new StringBuilder(contents);
					for (; currlen < minlen; currlen++) {
						builder.append(' ');
					}
					contents = builder.toString();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("minlen need a integer value.");
			}
		}
		chunk.append(contents);
		setChunkFont(text_chunk, chunk, block_default);
		return chunk;
	}

	private void formatParagraph(Paragraph para, List<TextChunk> chunk_list) {
		// ?????????????????????????????????????????????
		TextChunk text_chunk = chunk_list.get(0);
		if (text_chunk != null) {
			Map<String, String> attrs = text_chunk.getAttrs();

			// ????????????????????????
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
			// ??????????????????
			value = attrs.get("indent");
			if (value != null) {
				try {
					float indent = Float.parseFloat(value);
					para.setFirstLineIndent(indent);
				} catch (Exception ex) {
					System.err.println(
							"Indent attribute must has a float value");
				}
			}
			// ?????????????????????
			value = attrs.get("space-before");
			if (value != null) {
				try {
					float space = Float.parseFloat(value);
					para.setSpacingBefore(space);
				} catch (Exception ex) {
					System.err.println(
							"space-before attribute must has a float value");
				}
			}
			// ?????????????????????
			value = attrs.get("space-after");
			if (value != null) {
				try {
					float space = Float.parseFloat(value);
					para.setSpacingAfter(space);
				} catch (Exception ex) {
					System.err.println(
							"space-after attribute must has a float value");
				}
			}
		}
	}

	/**
	 * ????????????????????? PDF ??????
	 * @param chunk_list chunks ??????
	 * @param alignment ????????????
	 * @param indent ??????????????????
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void addParagraph(int block_type, List<TextChunk> chunk_list,
			PDFBlockDefault block_default)
			throws DocumentException, IOException {
		Paragraph para = new Paragraph();

		for(TextChunk text_chunk : chunk_list) {
			Chunk chunk = formatChunk(text_chunk, block_default);
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
	 * ????????????????????? PDF ????????????????????? Title???Section????????????
	 * ??????????????????????????????
	 * @param block_name ????????????????????? title, section ??????
	 * @param chunk_list ??????????????????????????????????????? chunk???????????????????????????
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeBlock(String block_name, List<TextChunk> chunk_list)
			throws IOException {
		if (block_name == null ||
				chunk_list == null || chunk_list.size() == 0) {
			return;
		}
		if (document == null && !document.isOpen()) {
			System.err.println("Document unopen yet, please open it first.");
			return;
		}

		int block_type = -1;

		// ??????????????????????????????????????????
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
				try {
					addParagraph(block_type, chunk_list, block_default);
				} catch (DocumentException e) {
					throw new IOException(e);
				}
				break;
			}
		}
	}

	/**
	 * ??????
	 */
	@Override
	public void newPage() {
		if (document == null && !document.isOpen()) {
			System.err.println("Document unopen yet, please open it first.");
			return;
		}
		document.newPage();
	}

	@Override
	public void addHRule(Attributes attrs) {
		try {
			int width = 1;
			int percent = 100;
			String value = attrs.getValue("width");
			if (value != null) {
				try {
					width = Integer.parseInt(value);
				} catch (Exception ex) {
					width = 1;
				}
			}
			value = attrs.getValue("percent");
			if (value != null) {
				try {
					percent = Integer.parseInt(value);
				} catch (Exception ex) {
					percent = 100;
				}
			}
			LineSeparator line = new LineSeparator(width, percent, null,
					Element.ALIGN_CENTER, 0f);
			document.add(Chunk.NEWLINE);
			document.add(line);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * ??????????????????
	 * @param attrs
	 */
	@Override
	public void addImage(Attributes attrs) {
		try {
			String src = attrs.getValue("src");
			if (src == null) {
				System.err.println("img missing src attribute.");
				return;
			}
			Image img = images.get(src);
			if (img == null) {
				img = Image.getInstance(src);
				images.put(src, img);
			}
			if (img != null) {
				document.add(img);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PdfPTable createTable(Map<String, String> attrs)
			throws DocumentException {
		float width = 100;
		int[] columns = null;
		PdfPTable table;

		String value = attrs.get("columns");
		if (value != null) {
			try {
				String[] array = value.split(",");
				columns = new int[array.length];
				for (int i = 0; i < array.length; i++) {
					columns[i] = Integer.parseInt(array[i]);
				}
			} catch (Exception ex) {
				System.err.println("column must has a integer value");
			}
		}
		if (columns == null) {
			table = new PdfPTable(1);
		} else {
			table = new PdfPTable(columns.length);
			table.setWidths(columns);
		}

		value = attrs.get("width");
		if (value != null) {
			try {
				width = Float.parseFloat(value);
			} catch (Exception ex) {
				System.err.println("width must has a float value");
			}
		}
		table.setWidthPercentage(width);
		table.setLockedWidth(false);

		return table;
	}

	private PdfPCell createTableCell(TextChunk text_chunk,
			PDFBlockDefault block_default)
					throws DocumentException, IOException {
		Chunk chunk = formatChunk(text_chunk, block_default);
		Phrase phrase = new Phrase();
		phrase.add(chunk);

		PdfPCell cell = new PdfPCell(phrase);
		cell.setVerticalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5);

		Map<String, String> attrs = text_chunk.getAttrs();
		String value = attrs.get("colspan");
		if (value != null) {
			try {
				cell.setColspan(Integer.parseInt(value));
			} catch (Exception ex) {
				System.err.println("colspan must has a integer value");
			}
		}
		value = attrs.get("align");
		if (value != null) {
			if (value.equalsIgnoreCase("left")) {
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			} else if (value.equalsIgnoreCase("center")) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else if (value.equalsIgnoreCase("right")) {
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
		}
		return cell;
	}

	@Override
	public void writeTable(TextTable table) throws IOException {
		if (!isOpen() || table == null) {
			return;
		}
		PDFBlockDefault block_default = null;
		for (PDFBlockDefault def : this.block_defaults) {
			if (def.block_type == BLOCK_PARA) {
				block_default = def;
				break;
			}
		}
		PdfPTable pdf_table;

		try {
			pdf_table = createTable(table.getAttrs());
			if (pdf_table == null) {
				return;
			}
		} catch (DocumentException e1) {
			throw new IOException(e1);
		}

		for (TextChunk text_chunk : table.getCells()) {
			try {
				pdf_table.addCell(createTableCell(text_chunk, block_default));
			} catch (DocumentException e) {
				throw new IOException(e);
			}
		}
		try {
			pdf_table.completeRow();
			document.add(pdf_table);
		} catch (DocumentException e) {
			throw new IOException(e);
		}
	}

}


/**
 * ?????? PDF ??????
 */
class PDFDocPageEvent extends PdfPageEventHelper
{
	int page_num;
	PdfTemplate total;

	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		total = writer.getDirectContent().createTemplate(30, 16);
	}

	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {
		ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
				new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
	}

	@Override
	public void onStartPage(PdfWriter writer, Document document) {
		page_num++;
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfPTable table = new PdfPTable(3);
		try {
			table.setWidths(new int[]{24, 24, 2});
			table.setTotalWidth(527);
			table.setLockedWidth(true);
			table.getDefaultCell().setFixedHeight(20);
			table.getDefaultCell().setBorder(Rectangle.BOTTOM);
			table.addCell("??????");
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(String.format("Page %d of", writer.getPageNumber()));
			PdfPCell cell = new PdfPCell(Image.getInstance(total));
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);
			table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
		} catch(DocumentException de) {
			de.printStackTrace();
		}
	}

	@Override
	public void onGenericTag(PdfWriter writer, Document pdfDocument,
			Rectangle rect, String text) {
		
	}

	@Override
	public void onParagraph(
			PdfWriter writer, Document pdfDocument,
			float paragraphPosition) {
		
	}

	@Override
	public void onParagraphEnd(
			PdfWriter writer, Document pdfDocument,
			float paragraphPosition) {
	}

	@Override
	public void onChapter(
			PdfWriter writer, Document document,
			float position, Paragraph title) {
		
	}

	@Override
	public void onChapterEnd(
			PdfWriter writer, Document document, float position) {

	}

	@Override
	public void onSection(
			PdfWriter writer, Document document,
			float position, int depth, Paragraph title) {
	}

	@Override
	public void onSectionEnd(
			PdfWriter writer, Document document, float position) {
		
	}

}
