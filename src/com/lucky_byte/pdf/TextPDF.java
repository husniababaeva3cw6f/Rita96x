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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

/**
 * 文本转 PDF
 * 
 * 这个类提供命令行程序，以及高级 API
 */
public class TextPDF
{
	/**
	 * 生成 PDF 文件
	 * @param xmlfile XML 模板文件
	 * @param jsonfile JSON 数据文件
	 * @param pdffile 输出 PDF 文件
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws ParseException 
	 */
	static void gen(File xmlfile, File jsonfile, File pdffile)
			throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		if (xmlfile == null || jsonfile == null || pdffile == null) {
			throw new IllegalArgumentException();
		}
		TextParser parser =
			new TextParser(new FileInputStream(xmlfile),
				new FileInputStream(jsonfile),
				new FileOutputStream(pdffile));
		parser.parse();
	}

	/**
	 * 和上面的函数类似，只不过不是用字符串内容代替文件内容
	 * @param xmlstr XML 模板字符串
	 * @param jsonstr JSON 数据字符串
	 * @param pdffile 输出 PDF 文件
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws ParseException 
	 */
	static void gen(String xmlstr, String jsonstr, File pdffile)
			throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		if (xmlstr == null || jsonstr == null || pdffile == null) {
			throw new IllegalArgumentException();
		}
		byte[] xml_bytes = xmlstr.getBytes(StandardCharsets.UTF_8);
		byte[] json_bytes = jsonstr.getBytes(StandardCharsets.UTF_8);
		TextParser parser =
			new TextParser(new ByteArrayInputStream(xml_bytes),
				new ByteArrayInputStream(json_bytes),
				new FileOutputStream(pdffile));
		parser.parse();
	}

	/**
	 * 命令行程序入口
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Argument missing...");
			return;
		}
		File xmlfile = new File(args[0]);
		if (!xmlfile.exists()) {
			System.err.println(xmlfile.getAbsolutePath() +
					" not found.");
			return;
		}
		File jsonfile = new File(args[1]);
		if (!jsonfile.exists()) {
			System.err.println(jsonfile.getAbsolutePath() +
					" not found.");
			return;
		}

		File pdffile = new File(args[0] + ".pdf");
		if (pdffile.exists()) {
			System.err.println(pdffile.getAbsolutePath() +
					" already exists.");
			return;
		}

		try {
			TextPDF.gen(xmlfile, jsonfile, pdffile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
