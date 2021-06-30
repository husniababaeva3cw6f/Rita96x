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

import java.io.OutputStream;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * PDF 操作类
 * 
 * 这个类封装 PDF 相关的操作，包括写入内容、签名、水印，等等。
 */
public class PDFDoc
{
	private OutputStream pdf_stream;
	private Document document;
	private PdfWriter writer;

	public PDFDoc(OutputStream pdf_stream) {
		this.pdf_stream = pdf_stream;
	}

	/**
	 * 打开 PDF 文档进行操作
	 * @return 成功或失败
	 */
	public boolean open() {
		try {
			document = new Document();
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

	public void writePara(String qName, List<TextChunk> chunk_list) {
		if (qName.equals("title")) {
			writeTitle(chunk_list);
		} else if (qName.equals("section")) {
			writeSection(chunk_list);
		} else if (qName.equals("para")) {
			writeParagraph(chunk_list);
		}
	}

	private void writeTitle(List<TextChunk> chunk_list) {
		TextChunk chunk = chunk_list.get(0);
		System.out.println("write title: " + chunk.getContents());
	}

	private void writeSection(List<TextChunk> chunk_list) {
		TextChunk chunk = chunk_list.get(0);
		System.out.println("write section: " + chunk.getContents());
	}

	private void writeParagraph(List<TextChunk> chunk_list) {
		TextChunk chunk = chunk_list.get(0);
		System.out.println("write para: " + chunk.getContents());
	}

}
