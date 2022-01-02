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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.json.simple.JSONObject;

/**
 * 读取 .doc 文件，并转换为 TextPDF 可识别的模板格式
 */
public class DocReader
{
	private URL xsl_url = null;
	private boolean auto_title = false;
	private boolean ignore_blank_para = false;
	private Map<String, Object> json_object;
	private Map<String, String> json_data;

	/**
	 * 如果指定，将在文件中增加 XSL 风格页的引用
	 * @param url XSL stylesheet URL
	 */
	public void setXSLUrl(URL url) {
		this.xsl_url = url;
	}

	/**
	 * 自动识别标题行，默认关闭，可以通过这个函数开启
	 * @param auto_title 是/否
	 */
	public void setAutoTitle(boolean auto_title) {
		this.auto_title = auto_title;
	}

	/**
	 * 是否忽略空白段落
	 * @param ignore
	 */
	public void ignoreBlankPara(boolean ignore) {
		this.ignore_blank_para = ignore;
	}

	private int getTitleIndex(Range range) {
		int index = 0;
		int max_font_size = 0;
		boolean center = false;

		// 从头 3 段中找标题
		int n_paras = Math.min(3, range.numParagraphs());
		for (int i = 0; i < n_paras; i++) {
			Paragraph para = range.getParagraph(i);

			// 找到这一段中最大的字体
			int font_size = 0;
			for (int j = 0; j < para.numCharacterRuns(); j++) {
				CharacterRun run = para.getCharacterRun(j);
				font_size = Math.max(font_size, run.getFontSize());
			}

			// 如果字体比之前的都大，则认为是标题
			if (font_size > max_font_size) {
				index = i;
				max_font_size = font_size;
			} else if (font_size == max_font_size) {
				if (!center && para.getJustification() == 1) {
					index = i;
					center = true;
				}
			}
		}
		return index;
	}

	private void appendParaAttrs(StringBuilder builder, Paragraph para) {
		switch(para.getJustification()) {
		case 1:
			builder.append(" align=\"center\"");
			break;
		case 2:
			builder.append(" align=\"right\"");
			break;
		case 3:	// left 对齐是默认的，不写入模板中
			break;
		}
	}

	private void appendRunAttrs(StringBuilder builder,
			CharacterRun run, boolean is_span) {
		StringBuilder style = new StringBuilder();

		if (is_span) {
			if (run.isBold()) {
				style.append("bold");
			}
			if (run.isItalic()) {
				if (style.length() > 0) style.append(",");
				style.append("italic");
			}
			if (run.getUnderlineCode() == 1) {
				if (style.length() > 0) style.append(",");
				style.append("underline");
			}
			if (style.length() > 0) {
				builder.append(" font-style=\"" + style + "\"");
			}
		}

		builder.append(" font-size=\"");
		builder.append((int)(run.getFontSize() / 2));
		builder.append("\"");
	}

	private String textEscape(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			String escape = Util.escapeXMLChars(text.charAt(i));
			if (escape != null) {
				builder.append(escape);
			} else {
				builder.append(text.charAt(i));
			}
		}
		return builder.toString();
	}

	private void readCharacterRuns(Paragraph para, int para_index,
			StringBuilder builder, boolean is_title) {
		StringBuilder all_text = null;
		if (is_title && json_object != null) {
			all_text = new StringBuilder();
		}
		for (int j = 0; j < para.numCharacterRuns(); j++) {
			CharacterRun run = para.getCharacterRun(j);
			String text = run.text().replaceAll("[\u0000-\u001f]", "");

//			System.out.println("run text: " + text + " >i=" + para_index);
//			System.out.println("vanished: " + run.isVanished());
//			System.out.println("special: " + run.isSpecialCharacter());

			// 忽略特殊字符
			if (run.isSpecialCharacter()) {
				continue;
			}
			// 忽略级链接
			if (text.matches(" HYPERLINK .+")) {
				continue;
			}

			// \u3000: IDEOGRAPHIC SPACE
			if (text.matches("^[\\s\u3000]+$")) {
				if (run.getUnderlineCode() == 1) {
					String vid = "vid_" + para_index + "_" + j;
					builder.append("    <value id=\"");
					builder.append(vid);
					builder.append("\" minlen=\"");
					builder.append(text.length());
					builder.append("\"");
					appendRunAttrs(builder, run, false);
					builder.append(" />\n");
					if (json_data != null) {
						json_data.put(vid, "");
					}
				} else {
					builder.append("    <hspace");
					builder.append(" size=\"");
					builder.append(text.length());
					builder.append("\"");
					appendRunAttrs(builder, run, false);
					builder.append(" />\n");
				}
			} else if (text.matches("^_+$")) {
				String vid = "vid_" + para_index + "_" + j;
				builder.append("    <value id=\"");
				builder.append(vid);
				builder.append("\" minlen=\"");
				builder.append(text.length());
				builder.append("\"");
				appendRunAttrs(builder, run, false);
				builder.append(" />\n");
				if (json_data != null) {
					json_data.put(vid, "");
				}
			} else if (text.length() > 0) {
				builder.append("    <span");
				appendRunAttrs(builder, run, true);
				builder.append(">");
				builder.append(textEscape(text));
				builder.append("</span>\n");

				if (all_text != null) {
					all_text.append(text);
				}
			}
		}
		if (is_title && json_object != null) {
			json_object.put("title", all_text.toString());
		}
	}

	/**
	 * 转换 .doc 文件
	 * @param doc_stream .doc 数据流
	 * @param xml_stream .xml 输出流，用于保存转换后结果
	 * @throws IOException
	 */
	public void read(InputStream doc_stream, OutputStream xml_stream,
			OutputStream json_stream)
					throws IOException {
		if (doc_stream == null || xml_stream == null) {
			System.err.println("Invalid argument");
			return;
		}
		if (json_stream != null) {
			json_object = new HashMap<String, Object>();
			json_data = new HashMap<String, String>();
		}
		HWPFDocument document = new HWPFDocument(doc_stream);
		Range range = document.getRange();
		StringBuilder builder = new StringBuilder();

		builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		if (xsl_url != null) {
			builder.append("<?xml-stylesheet type=\"text/xsl\" href=\"" +
					xsl_url.getPath() + "\"?>\n");
		}
		builder.append("\n<!-- Automatic generated by TextPDF DocReader -->\n");
		builder.append("\n<textpdf>\n");

		xml_stream.write(builder.toString().getBytes("UTF-8"));
		builder.setLength(0);

		int title_index = 0;
		if (auto_title) {
			title_index = getTitleIndex(range);
		}

		Table table = null;

		for (int i = 0; i < range.numParagraphs(); i++) {
			Paragraph para = range.getParagraph(i);
			boolean is_title = false;

			if (para.pageBreakBefore()) {	// 换页符
				builder.append("  <pagebreak />\n");
			}

			if (para.isInTable()) {		// 表格
				if (table == null) {
					table = range.getTable(para);
					int cols = table.numParagraphs() / table.numRows();
					StringBuilder columns = new StringBuilder();
					columns.append("1");
					for (int n = 1; n < cols; n++) {
						if (n == cols - 1) {
							columns.append(",0");
						} else {
							columns.append(",1");
						}
					}
					builder.append("  <table columns=\"" +
							columns.toString() + "\">\n");
				}
				builder.append("    <cell>");
				builder.append(para.text().replaceAll("[\u0000-\u001f]", ""));
				builder.append("</cell>\n");
				continue;
			} else {
				if (table != null) {
					builder.append("  </table>\n");
					table = null;
				}
			}

			if (ignore_blank_para && para.numCharacterRuns() == 0) {
				continue;
			}
			if (auto_title && i == title_index) {
				builder.append("  <title");
				is_title = true;
			} else {
				builder.append("  <para");
			}
			appendParaAttrs(builder, para);
			builder.append(">\n");

			readCharacterRuns(para, i, builder, is_title);

			if (auto_title && i == title_index) {
				builder.append("  </title>\n");
			} else {
				builder.append("  </para>\n");
			}
			xml_stream.write(builder.toString().getBytes("UTF-8"));
			builder.setLength(0);
		}
		xml_stream.write("</textpdf>\n".getBytes());

		// 输出 JSON 数据模板
		if (json_stream != null) {
			json_object.put("data", json_data);
			String json_string = JSONObject.toJSONString(json_object);
			json_stream.write(json_string.getBytes("UTF-8"));
		}
	}

}
