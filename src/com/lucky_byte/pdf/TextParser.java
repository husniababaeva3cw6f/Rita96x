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

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
	private JSONParser json_parser;
	private List<TextChunk> chunk_list;
	private Stack<TextChunk> chunk_stack;
	private StringBuilder contents_builder;
	
	public XMLFileHandler(TextParser parser)
			throws IOException, ParseException {
		chunk_list = new ArrayList<TextChunk>();
		chunk_stack = new Stack<TextChunk>();
		contents_builder = new StringBuilder();

		this.parser = parser;
		assert(parser != null);
		assert(parser.json_stream != null);

		InputStreamReader reader =
				new InputStreamReader(parser.json_stream,
						StandardCharsets.UTF_8);
		assert(reader != null);
		
		try {
		json_parser = new JSONParser();
		json_parser.parse(new BufferedReader(reader));
		} catch (Exception ex) {
			
		}
	}

	/**
	 * 文档开始解析时回调
	 */
	@Override
	public void startDocument() throws SAXException {
		if (!parser.pdfdoc.open()) {
			throw new SAXException("Open document failed.");
		}
	}

	/**
	 * 文档解析结束时回调
	 */
	@Override
	public void endDocument() throws SAXException {
		parser.pdfdoc.close();
	}

	/**
	 * 元素开始时回调
	 */
	@Override
	public void startElement(String namespaceURI,
			String localName, String qName, Attributes attrs)
					throws SAXException {
		TextChunk textchunk;
		
		try{
			textchunk = chunk_stack.peek();
			textchunk.setContents(contents_builder.toString());
		} catch (EmptyStackException ese) {
			textchunk = new TextChunk();
			textchunk.setAttr(attrs);

			if (qName.equals("b")) {
				textchunk.setStyle(TextChunk.STYLE_BOLD);
			} else if (qName.equals("u")) {
				textchunk.setStyle(TextChunk.STYLE_UNDERLINE);
			} else if (qName.equals("i")) {
				textchunk.setStyle(TextChunk.STYLE_ITALIC);
			}
			chunk_stack.push(textchunk);
		}
	}

	/**
	 * 标签字符串处理
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);

		String contents = new String(ch, start, length);
		contents_builder.append(contents.trim());			
	}
	
	/**
	 * 元素结束时回调
	 */
	@Override
	public void endElement(String namespaceURI,
			String localName, String qName) {
		try {
			TextChunk chunk = chunk_stack.pop();
			chunk_list.add(chunk);
			
			if (qName.equals("title") ||
					qName.equals("section") ||
					qName.equals("para")) {
				parser.pdfdoc.writePara(qName, chunk_list);
				chunk_list.clear();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
