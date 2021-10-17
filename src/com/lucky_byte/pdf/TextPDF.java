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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
	static public void gen(File xmlfile, File jsonfile, File pdffile)
			throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		if (xmlfile == null || jsonfile == null || pdffile == null) {
			throw new IllegalArgumentException();
		}
		TextParser parser = new TextParser();
		parser.genPDF(new FileInputStream(xmlfile),
				new FileInputStream(jsonfile),
				new FileOutputStream(pdffile));
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
	static public void gen(String xmlstr, String jsonstr, File pdffile)
			throws ParserConfigurationException,
					SAXException, IOException, ParseException {
		if (xmlstr == null || jsonstr == null || pdffile == null) {
			throw new IllegalArgumentException();
		}
		byte[] xml_bytes = xmlstr.getBytes(StandardCharsets.UTF_8);
		byte[] json_bytes = jsonstr.getBytes(StandardCharsets.UTF_8);
		TextParser parser = new TextParser();
		parser.genPDF(new ByteArrayInputStream(xml_bytes),
				new ByteArrayInputStream(json_bytes),
				new FileOutputStream(pdffile));
	}

	/**
	 * 命令行程序入口
	 * @param args 命令行参数
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		List<String> args2 = new ArrayList<String>();
		String pdf_fname = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-o")) {
				if (i >= args.length - 1) {
					System.err.println("'-o' option require a argument");
					return;
				}
				pdf_fname = args[i + 1];
				i++;
				continue;
			} else {
				args2.add(args[i]);
			}
		}

		if (args2.size() < 2) {
			System.err.println("Argument missing...");
			System.err.println();
			System.err.println("Usage:");
			System.err.println("  java -jar textpdf.jar [OPTION] <xmlfile|docfile> [jsonfile]");
			System.err.println("\nOptions:");
			System.err.println("  -o filename:  Output pdf file name");
			System.err.println();
			return;
		}
		File xmlfile = new File(args2.get(0));
		if (!xmlfile.exists()) {
			System.err.println(xmlfile.getAbsolutePath() + " not found.");
			return;
		}
		File jsonfile = new File(args2.get(1));
		if (!jsonfile.exists()) {
			System.err.println(jsonfile.getAbsolutePath() + " not found.");
			return;
		}
		if (pdf_fname == null) {
			pdf_fname = args2.get(0) + ".pdf";
		}
		File pdffile = new File(pdf_fname);
		if (pdffile.exists()) {
			System.err.println(pdffile.getAbsolutePath() + " already exists.");
			return;
		}

		try {
			if (args2.get(0).endsWith(".doc")) {
				File tmpfile = File.createTempFile("textpdf-", ".xml");

				DocReader reader = new DocReader();
				reader.setAutoTitle(true);
				reader.ignoreBlankPara(true);
				InputStream doc_stream =
						new FileInputStream(xmlfile);
				OutputStream xml_stream =
						new FileOutputStream(tmpfile);
				reader.read(doc_stream, xml_stream, null);
				xmlfile = tmpfile;
			}
			TextPDF.gen(xmlfile, jsonfile, pdffile);

			if (args2.get(0).endsWith(".doc")) {	// 删除临时文件
				xmlfile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
