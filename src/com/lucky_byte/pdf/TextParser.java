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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
 */
public class TextParser
{
	InputStream xml_stream;
	InputStream json_stream;
	OutputStream pdf_stream;
	PDFDoc pdfdoc;

	public TextParser(InputStream xml_stream,
			InputStream json_stream, OutputStream pdf_stream)
					throws FileNotFoundException {
		this.xml_stream = xml_stream;
		this.json_stream = json_stream;
		this.pdf_stream = pdf_stream;

		pdfdoc = new PDFDoc(pdf_stream);
	}

	/**
	 * 解析 XML 模板并生成 PDF 文档
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws DocumentException 
	 * @throws ParseException 
	 */
	public void parse() throws ParserConfigurationException,
			SAXException, IOException, ParseException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();
		XMLFileHandler handler = new XMLFileHandler(this);
		parser.parse(xml_stream, handler);
	}
}


/**
 * 解析 XML 模板，并生成 PDF 文件
 *
 */
class XMLFileHandler extends DefaultHandler
{
	private TextParser parser;
	private List<TextChunk> chunk_list;
	private Stack<TextChunk> chunk_stack;
	private StringBuilder contents_builder;
	private JSONObject json_object;
	
	private String[] block_labels = {
			"title", "section", "para",
	};

	public XMLFileHandler(TextParser parser)
			throws IOException, ParseException {
		chunk_list = new ArrayList<TextChunk>();
		chunk_stack = new Stack<TextChunk>();
		contents_builder = new StringBuilder();

		this.parser = parser;
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

	private Object[][] page_size_map = {
			{ "a0", PageSize.A0 },
			{ "a1", PageSize.A1 },
			{ "a2", PageSize.A2 },
			{ "a3", PageSize.A3 },
			{ "a4", PageSize.A4 },
			{ "a5", PageSize.A5 },
			{ "a6", PageSize.A6 },
			{ "a7", PageSize.A7 },
			{ "a8", PageSize.A8 },
			{ "a9", PageSize.A9 },
			{ "a10", PageSize.A10 },

			{ "b0", PageSize.B0 },
			{ "b1", PageSize.B1 },
			{ "b2", PageSize.B2 },
			{ "b3", PageSize.B3 },
			{ "b4", PageSize.B4 },
			{ "b5", PageSize.B5 },
			{ "b6", PageSize.B6 },
			{ "b7", PageSize.B7 },
			{ "b8", PageSize.B8 },
			{ "b9", PageSize.B9 },
			{ "b10", PageSize.B10 },
	};

	private void setupPage(Attributes attrs) {
		// 页面大小
		String value = attrs.getValue("page-size");
		if (value != null) {
			for (Object[] item : page_size_map) {
				if (value.equalsIgnoreCase((String) item[0])) {
					parser.pdfdoc.setPageSize((Rectangle) item[1]);
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
					parser.pdfdoc.setPageMargin(
							Integer.parseInt(array[0]),
							Integer.parseInt(array[1]),
							Integer.parseInt(array[2]),
							Integer.parseInt(array[3]));
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
			if (parser.pdfdoc.isOpen()) {
				throw new SAXException("'textpdf' must be root element.");
			}

			// 必须先设置页面属性再打开文档
			setupPage(attrs);

			if (!parser.pdfdoc.open()) {
				throw new SAXException("Open document failed.");
			}
			return;
		}

		if (!parser.pdfdoc.isOpen()) {
			throw new SAXException("Document unopen yet. "
					+ "check your xml root element is 'textpdf'");
		}

		try{
			prev_chunk = chunk_stack.peek();
			String contents = contents_builder.toString();
			if (contents.length() > 0) {
				prev_chunk.setContents(contents);
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

		if (prev_chunk != null) {
			chunk.setStyle(prev_chunk.getStyle());
		}

		if (qName.equalsIgnoreCase("b")) {
			chunk.addStyle(TextChunk.STYLE_BOLD);
		} else if (qName.equalsIgnoreCase("u")) {
			chunk.addStyle(TextChunk.STYLE_UNDERLINE);
		} else if (qName.equalsIgnoreCase("i")) {
			chunk.addStyle(TextChunk.STYLE_ITALIC);
		} else if (qName.equalsIgnoreCase("value")) {
			String id = attrs.getValue("id");
			if (id == null) {
				System.err.println("Value element missing 'id' attribute.");
			} else {
				if (!json_object.containsKey(id)) {
					System.err.println("JSON data missing key '" + id
							+ "', please check!");
				} else {
					Object value = json_object.get(id);
					if (!(value instanceof String)) {
						System.err.println("JSON key '" + id
								+ "' must has a string value.");
					} else {
						contents_builder.append(value);
						chunk.addStyle(TextChunk.STYLE_BOLD);
						chunk.addStyle(TextChunk.STYLE_UNDERLINE);
					}
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
		super.characters(ch, start, length);

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
			parser.pdfdoc.close();
			return;
		}

		TextChunk chunk = chunk_stack.pop();

		String contents = contents_builder.toString();
		if (contents.length() > 0) {
			chunk.setContents(contents);
			contents_builder.setLength(0);
			chunk_list.add(chunk.clone());
		}

		for (String label : block_labels) {
			if (label.equalsIgnoreCase(qName)) {
				try {
					parser.pdfdoc.writeBlock(qName, chunk_list);
				} catch (Exception e) {
					e.printStackTrace();
					throw new SAXException("Write to PDF failed.");
				} finally {
					chunk_list.clear();
				}
			}
		}
	}
	
}
