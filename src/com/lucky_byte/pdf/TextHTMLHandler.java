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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TextHTMLHandler extends DefaultHandler
{
	private TextParser parser;
	private OutputStream html_stream;
	private StringBuilder text_builder;
	private JSONObject json_object;
	private URL css_url;
	private URL js_url;

	private String html_open = "<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>__TITLE__</title>\n"
			+ "    <meta name=\"author\" content=\"Lucky Byte, Inc.\"/>\n"
			+ "    __CSS_URL__\n"
			+ "    __JS_URL__\n"
			+ "  </head>\n"
			+ "  <body>\n";

	private String html_close = "  </body>\n</html>\n";

	public TextHTMLHandler(TextParser parser, URL css_url, URL js_url) {
		this.parser = parser;
		this.html_stream = parser.out_stream;
		this.css_url = css_url;
		this.js_url = js_url;
		this.text_builder = new StringBuilder();
	}

	private void writeHtmlStream(String string) throws SAXException {
		try {
			html_stream.write(string.getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SAXException("Write to stream failed.");
		}
	}

	/**
	 * 文档开始解析时回调
	 */
	@Override
	public void startDocument() throws SAXException {
		if (parser.json_stream != null) {
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
		if (json_object != null) {
			if (json_object.containsKey("title")) {
				Object value = json_object.get("title");
				if (value instanceof String) {
					html_open = html_open.replace("__TITLE__",
							(CharSequence) value);
				}
			}
		}
		if (css_url != null) {
			html_open = html_open.replace("__CSS_URL__",
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
							css_url.getPath() + "\" />");
		}
		if (js_url != null) {
			html_open = html_open.replace("__JS_URL__",
					"<script src=\"" +
							js_url.getPath() + "\"></script>");
		}
		writeHtmlStream(html_open);
	}

	/**
	 * 文档解析结束时回调
	 */
	@Override
	public void endDocument() throws SAXException {
		writeHtmlStream(html_close);
	}

	/**
	 * 元素开始时回调
	 */
	@Override
	public void startElement(String namespaceURI,
			String localName, String qName, Attributes attrs)
					throws SAXException {
	}

	/**
	 * 元素结束时回调
	 */
	@Override
	public void endElement(String namespaceURI,
			String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("title")) {
			writeHtmlStream("    <h1>");
			writeHtmlStream(text_builder.toString());
			writeHtmlStream("    </h1>\n");
			text_builder.setLength(0);
		} else if (qName.equalsIgnoreCase("section")) {
			writeHtmlStream("    <h2>");
			writeHtmlStream(text_builder.toString());
			writeHtmlStream("    </h2>\n");
			text_builder.setLength(0);
		} else if (qName.equalsIgnoreCase("para")) {
			writeHtmlStream("    <p>");
			writeHtmlStream(text_builder.toString());
			writeHtmlStream("    </p>\n");
			text_builder.setLength(0);
		}
	}

	/**
	 * 标签字符串处理
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String contents = new String(ch, start, length);
		text_builder.append(contents.trim());
	}

}
