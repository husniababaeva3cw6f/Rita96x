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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

/**
 * 解析 XML 模板
 * 
 * 这个类负责解析 XML 模板，并组合 JSON 数据，然后调用 PDFDoc
 * 类提供的功能生成 PDF 文件。
 * 
 * 版本 0.2 增加生成 HTML 的能力，主要的原因是 XSL 用起来太恼火
 */
public class TextParser
{
	static final public int DOC_TYPE_PDF  = 1;
	static final public int DOC_TYPE_HTML = 2;

	InputStream xml_stream;
	InputStream json_stream;
	OutputStream out_stream;
	URL css_url, js_url;

	public static final String[] BLOCK_ELEMENTS = {
			"title", "section", "para", "pagebreak",
	};

	/**
	 * 解析 XML 模板并生成输出文档
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws DocumentException 
	 * @throws ParseException 
	 */
	public void genDoc(int doc_type, InputStream xml_stream,
			InputStream json_stream, OutputStream pdf_stream,
			URL css_url, URL js_url)
				throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		this.xml_stream = xml_stream;
		this.json_stream = json_stream;
		this.out_stream = pdf_stream;
		this.css_url = css_url;
		this.js_url = js_url;

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(xml_stream, new TextDocHandler(this, doc_type));
	}

	/**
	 * 解析 XML 模板并生成 PDF 文档
	 * @param xml_stream
	 * @param json_stream
	 * @param pdf_stream
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void genPDF(InputStream xml_stream,
			InputStream json_stream, OutputStream pdf_stream)
				throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		genDoc(DOC_TYPE_PDF, xml_stream, json_stream,
				pdf_stream, null, null);
	}

	/**
	 * 解析 XML 模板并生成 HTML 文档
	 * @param xml_stream
	 * @param html_stream
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParseException 
	 */
	public void genHTML(InputStream xml_stream,
			InputStream json_stream, OutputStream html_stream,
			URL css_url, URL js_url)
				throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		genDoc(DOC_TYPE_HTML, xml_stream, json_stream,
				html_stream, css_url, js_url);
	}
}


/**
 * 解析 XML 模板，并生成 PDF 文件
 *
 */
class TextDocHandler extends DefaultHandler
{
	private TextParser parser;
	private TextDoc text_doc;
	private List<TextChunk> chunk_list;
	private Stack<TextChunk> chunk_stack;
	private StringBuilder contents_builder;
	private JSONObject json_object;
	private JSONObject json_data;
	
	public TextDocHandler(TextParser parser, int doc_type)
			throws IOException, ParseException {
		chunk_list = new ArrayList<TextChunk>();
		chunk_stack = new Stack<TextChunk>();
		contents_builder = new StringBuilder();

		this.parser = parser;

		switch(doc_type) {
		case TextParser.DOC_TYPE_PDF:
			text_doc = new PDFDoc(parser.out_stream);
			break;
		case TextParser.DOC_TYPE_HTML:
			text_doc = new HTMLDoc(parser.out_stream);
			((HTMLDoc) text_doc).setURL(parser.css_url, parser.js_url);
			break;
		default:
			throw new IOException("Document type unsupported.");
		}
	}

	/**
	 * 文档开始解析时回调
	 */
	@Override
	public void startDocument() throws SAXException {
		try {
			InputStreamReader reader =
					new InputStreamReader(parser.json_stream,
							StandardCharsets.UTF_8);
			JSONParser json_parser = new JSONParser();
			json_object = (JSONObject) json_parser.parse(
					new BufferedReader(reader));

			if (text_doc instanceof PDFDoc) {
				if (!json_object.containsKey("data")) {
					System.err.println(
							"JSON source missing 'data' key, please check!");
				} else {
					Object value = json_object.get("data");
					if (!(value instanceof JSONObject)) {
						System.err.println("JSON 'data' must be a object.");
					} else {
						json_data = (JSONObject) value;
					}
				}
			} else if (text_doc instanceof HTMLDoc) {
				((HTMLDoc) text_doc).setJSONObject(json_object);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SAXException("Failed to parse JSON stream");
		}
	}

	/**
	 * 文档解析结束时回调
	 */
	@Override
	public void endDocument() throws SAXException {
	}

	// 页面大小常数定义
	private Object[][] page_size_map = {
			{ "a0", PageSize.A0 }, { "a1", PageSize.A1 },
			{ "a2", PageSize.A2 }, { "a3", PageSize.A3 },
			{ "a4", PageSize.A4 }, { "a5", PageSize.A5 },
			{ "a6", PageSize.A6 }, { "a7", PageSize.A7 },
			{ "a8", PageSize.A8 }, { "a9", PageSize.A9 },
			{ "a10", PageSize.A10 },

			{ "b0", PageSize.B0 }, { "b1", PageSize.B1 },
			{ "b2", PageSize.B2 }, { "b3", PageSize.B3 },
			{ "b4", PageSize.B4 }, { "b5", PageSize.B5 },
			{ "b6", PageSize.B6 }, { "b7", PageSize.B7 },
			{ "b8", PageSize.B8 }, { "b9", PageSize.B9 },
			{ "b10", PageSize.B10 },
	};

	private void setupPage(Attributes attrs) {
		// 页面大小
		String value = attrs.getValue("page-size");
		if (value != null) {
			for (Object[] item : page_size_map) {
				if (value.equalsIgnoreCase((String) item[0])) {
					text_doc.setPageSize((Rectangle) item[1]);
					break;
				}
			}
		}

		// 页面边距
		value = attrs.getValue("page-margin");
		if (value != null) {
			String[] array = value.split(",");
			if (array.length < 4) {
				System.err.println("Page margin format error.");
			} else {
				try {
					text_doc.setPageMargin(
							Integer.parseInt(array[0].trim()),
							Integer.parseInt(array[1].trim()),
							Integer.parseInt(array[2].trim()),
							Integer.parseInt(array[3].trim()));
				} catch (Exception ex) {
					System.err.println("Page margin format error.");
				}
			}
		}
	}

	/**
	 * 元素开始时回调
	 */
	@Override
	public void startElement(String namespaceURI,
			String localName, String qName, Attributes attrs)
					throws SAXException {
		TextChunk prev_chunk = null;
		
		if (qName.equalsIgnoreCase("textpdf")) {
			if (text_doc.isOpen()) {
				throw new SAXException("'textpdf' must be root element.");
			}

			// 必须先设置页面属性再打开文档
			setupPage(attrs);
			if (!text_doc.open()) {
				throw new SAXException("Open document failed.");
			}
			return;
		}

		if (!text_doc.isOpen()) {
			throw new SAXException("Document unopen yet. "
					+ "check your xml root element is 'textpdf'");
		}

		// Block 元素不可嵌套
		for (String label : TextParser.BLOCK_ELEMENTS) {
			if (label.equalsIgnoreCase(qName)) {
				chunk_list.clear();
				break;
			}
		}

		try{
			prev_chunk = chunk_stack.peek();
			String contents = contents_builder.toString();
			if (contents.length() > 0) {
				prev_chunk.setContents(
						contents.replaceAll("[ \t\f]*\n+[ \t\f]*", ""));
				contents_builder.setLength(0);
				chunk_list.add(prev_chunk.clone());
			}
		} catch (EmptyStackException ese) {
		}

		TextChunk chunk = new TextChunk();
		if (prev_chunk != null) {
			chunk.addAttrs(prev_chunk.getAttrs());
		}
		chunk.addAttrs(attrs);

		if (qName.equalsIgnoreCase("value")) {
			chunk.setIsValue(true);

			String id = attrs.getValue("id");
			if (id == null) {
				System.err.println("Value element missing 'id' attribute.");
			} else {
				if (text_doc instanceof PDFDoc) {
					if (json_data != null) {
						if (!json_data.containsKey(id)) {
							System.err.println("JSON data key '" + id
									+ "' not found!");
						} else {
							Object value = json_data.get(id);
							if (!(value instanceof String)) {
								System.err.println("JSON  data key '" + id
										+ "' must has a string value.");
							} else {
								contents_builder.append(value);
								if (attrs.getValue("font-style") == null) {
									chunk.addAttr("font-style", "bold,underline");
								}
							}
						}
					}
				}
			}
		} else if (qName.equalsIgnoreCase("hspace")) {
			String value = attrs.getValue("size");
			if (value == null || value.length() == 0) {
				System.err.println("hspace need a size attribute.");
			} else {
				try {
					int size = Integer.parseInt(value);
					for (int i = 0; i < size; i++) {
						contents_builder.append(' ');
					}
				} catch (Exception ex) {
					System.err.println("size attribute need a integer value");
				}
			}
		}
		chunk_stack.push(chunk);
	}

	/**
	 * 标签字符串处理
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String contents = new String(ch, start, length);
		contents_builder.append(contents.trim());
	}

	/**
	 * 元素结束时回调
	 */
	@Override
	public void endElement(String namespaceURI,
			String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("textpdf")){
			text_doc.close();
			return;
		}
		if (qName.equalsIgnoreCase("pagebreak")) {
			text_doc.newPage();
			return;
		}
		if (qName.equalsIgnoreCase("break")) {
			contents_builder.append("\n");
			return;
		}

		TextChunk chunk = chunk_stack.pop();

		String contents = contents_builder.toString();
		if (contents.length() > 0 ||
				qName.equalsIgnoreCase("value") ||
				qName.equalsIgnoreCase("hspace")) {
			chunk.setContents(contents.replaceAll("[ \t\f]*\n+[ \t\f]*", ""));
			contents_builder.setLength(0);
			chunk_list.add(chunk.clone());
		}

		for (String label : TextParser.BLOCK_ELEMENTS) {
			// 空段落，需要增加一个空 TextChunk 对象去模拟空段落
			if (chunk_list.size() == 0 && label.equalsIgnoreCase("para")) {
				chunk.setContents(" ");
				chunk_list.add(chunk.clone());
			}

			if (chunk_list.size() > 0) {
				if (label.equalsIgnoreCase(qName)) {
					try {
						text_doc.writeBlock(qName, chunk_list);
					} catch (Exception e) {
						e.printStackTrace();
						throw new SAXException("Write to PDF failed.");
					} finally {
						chunk_list.clear();
					}
					break;
				}
			}
		}
	}
	
}
