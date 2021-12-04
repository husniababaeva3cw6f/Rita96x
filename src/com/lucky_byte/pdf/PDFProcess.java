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
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


/**
 * PDF 后期处理
 */
public class PDFProcess
{
	public final static int MARKER_STYLE_CENTER = 1;
	public final static int MARKER_STYLE_FULL = 2;
	
	public final static int FONT_FAMILY_HEI = 1;
	public final static int FONT_FAMILY_SONG = 2;

	private PdfReader reader;
	private PdfStamper stamper;

	private	int font_family;
	private BaseColor color;
	
	public PDFProcess(InputStream pdf_in_stream,
			OutputStream pdf_out_stream) throws IOException {

		try {
			reader = new PdfReader(pdf_in_stream);
			stamper = new PdfStamper(reader, pdf_out_stream);
		} catch (DocumentException e) {
			throw new IOException(e);
		}

		this.font_family = FONT_FAMILY_SONG;
		this.color = BaseColor.GRAY;
	}	

	private BaseFont getBaseFont(int font_family) throws IOException{
		BaseFont base_font = null;
		try {
			switch (font_family) {
			case FONT_FAMILY_HEI:
					base_font = BaseFont.createFont("resources/SIMHEI.TTF",
							BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				break;
			case FONT_FAMILY_SONG:
				base_font = BaseFont.createFont("resources/SIMSUN.TTC,0",
						BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				break;
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		}
		
		return base_font;
	}

	public void finish() throws IOException {
		try {
			this.stamper.close();
		} catch (DocumentException e) {
			throw new IOException(e);
		}
	}

	public void addTextMarker(String text, float opacity,
			int angle, int font_size, int style) throws IOException {
		int total_pages = reader.getNumberOfPages();
		Rectangle page_rect;

		if (text == null || text.length() == 0)
			return;

		for (int i = 1; i <= total_pages; i++) {  
			page_rect = reader.getPageSizeWithRotation(i);
			float width = page_rect.getWidth();
			float height = page_rect.getHeight();
			float text_width = font_size * text.length();

			PdfGState gs = new PdfGState();
			gs.setFillOpacity(opacity);

			PdfContentByte content = stamper.getUnderContent(i); 
			content.beginText();
			content.setGState(gs);
			content.setColorFill(color);
			content.setFontAndSize(getBaseFont(font_family), font_size);
			content.setTextMatrix(10, 10);

			switch (style) {
			case MARKER_STYLE_CENTER:
				content.showTextAligned(Element.ALIGN_JUSTIFIED_ALL, 
						text, width / 2 - text_width / 2, height / 2, angle); 
				break;
			case MARKER_STYLE_FULL:
				for (float y = height - 20; y > -height + 20; y -= 100) {
					for (float x = 10; x < width - 10; x += text_width) {
						content.showTextAligned(Element.ALIGN_JUSTIFIED_ALL,
								text, x, y, angle); 
					}
				}
				break;
			}
			content.endText();
		}
	}

	public void addImgMarker(String img_filename, float x, float y,
			float width, float height, float opacity,
			boolean only_first_page) throws IOException {
		if (img_filename == null) {
			return;
		}
		Image image = null;
		Rectangle page_rect;
		int total_pages = reader.getNumberOfPages();

		try {
			image = Image.getInstance(img_filename);
			image.scaleToFit(width, height);
		} catch (BadElementException e) {
			throw new IOException(e);
		}
		PdfGState gs = new PdfGState();
		gs.setFillOpacity(opacity);

		if (only_first_page) {
			total_pages = 1;
		}
		for (int i = 1; i <= total_pages; i++) {
			page_rect = reader.getPageSizeWithRotation(i);
			PdfContentByte content = stamper.getUnderContent(i);
			if (x < 0) {
				x = page_rect.getWidth() + x;
			}
			image.setAbsolutePosition(x, page_rect.getHeight() - y - height);
			content.setGState(gs);
			try {
				content.addImage(image);
			} catch (DocumentException e) {
				throw new IOException(e);
			}
		}
	}

}
