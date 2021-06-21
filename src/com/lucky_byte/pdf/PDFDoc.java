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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * PDF 操作类
 * 
 * 这个类封装 PDF 相关的操作，包括写入内容、签名、水印，等等。
 */
public class PDFDoc
{
	private File pdffile;
	private Document document;

	public PDFDoc(File pdffile) {
		this.pdffile = pdffile;
	}

	/**
	 * 返回 PDF 的文件对象
	 * @return File 对象
	 */
	public File getFile() {
		return pdffile;
	}

	/**
	 * 打开 PDF 文档进行操作
	 * @return 成功或失败
	 * @throws DocumentException 
	 * @throws FileNotFoundException 
	 */
	public boolean open()
			throws FileNotFoundException, DocumentException {
		if (pdffile.exists()) {
			throw new FileNotFoundException("File exists.");
		}
		document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(pdffile));
		document.open();
		return false;
	}

	/**
	 * 关闭 PDF 文档，关闭后不能继续操作文档
	 */
	public void close() {
		document.close();
	}

}
