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
import java.util.List;

import org.xml.sax.Attributes;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

/**
 * 输出文档抽象类
 */
public abstract class TextDoc
{
	protected OutputStream out_stream;
	protected Rectangle page_size = PageSize.A4;
	protected int page_margin_left = 50;
	protected int page_margin_right = 50;
	protected int page_margin_top = 50;
	protected int page_margin_bottom = 50;
	protected String encoding = "UTF-8";

	public TextDoc(OutputStream out_stream) {
		this.out_stream = out_stream;
	}

	/**
	 * 设置页面大小
	 * @param page_size
	 */
	public void setPageSize(Rectangle page_size) {
		this.page_size = page_size;
	}

	/**
	 * 设置页面边距
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public void setPageMargin(int left, int right, int top, int bottom) {
		page_margin_left = left;
		page_margin_right = right;
		page_margin_top = top;
		page_margin_bottom = bottom;
	}

	/**
	 * 设置输出文件编码
	 * @param enc 编码
	 */
	public void setEncoding(String enc) {
		this.encoding = enc;
	}

	abstract public boolean open();
	abstract public void close();
	abstract public boolean isOpen();
	abstract public void writeBlock(String block_name,
			List<TextChunk> chunk_list)
					throws DocumentException, IOException;
	abstract public void newPage();
	abstract public void addHRule(Attributes attrs);
	abstract public void addImage(Attributes attrs);
}
