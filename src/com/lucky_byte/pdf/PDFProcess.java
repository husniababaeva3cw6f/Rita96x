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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

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

	/**
	 * 结束添加内容
	 * @throws IOException
	 */
	public void finish() throws IOException {
		try {
			this.stamper.close();
		} catch (DocumentException e) {
			throw new IOException(e);
		}
	}

	/**
	 * 添加一段文字水印
	 * @param text
	 * @param opacity
	 * @param angle
	 * @param font_size
	 * @param style
	 * @throws IOException
	 */
	public void addTextMarker(String text, float opacity,
			int angle, int font_size, int style) throws IOException {
		if (text == null || text.length() == 0)
			return;

		int total_pages = reader.getNumberOfPages();

		for (int i = 1; i <= total_pages; i++) {
			Rectangle page_rect = reader.getPageSizeWithRotation(i);
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

	/**
	 * 添加一个图片
	 * @param img_filename
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param opacity
	 * @param only_first_page
	 * @throws IOException
	 */
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

	/**
	 * 生成二维码图片
	 * @param text
	 * @throws IOException 
	 */
	private void createQRCode(String contents, int width,
			int height, File img_file) throws IOException {
		try {
			Hashtable<EncodeHintType, Object> hints =
					new Hashtable<EncodeHintType, Object>();
	
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

			Writer writer = new MultiFormatWriter();
			BitMatrix bitMatrix = writer.encode(contents,
					BarcodeFormat.QR_CODE, width, height, hints);

			MatrixToImageWriter.writeToPath(bitMatrix, "png", img_file.toPath());
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * 添加一个二维码到第一页
	 * @param contents
	 * @throws IOException
	 */
	public void addQRCode(String contents) throws IOException {
		int width = 128, height = 128;
		File tmpfile = File.createTempFile("qrcode", ".png");
		createQRCode(contents, width, height, tmpfile);
		addImgMarker(tmpfile.getAbsolutePath(),
				-width, 0, width, height, 1.0f, true);
	}

	/**
	 * 添加页眉
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public void addHeader(String text) throws IOException {
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			Rectangle page_size = reader.getPageSize(i);
			PdfPTable table = new PdfPTable(1);
			table.setTotalWidth(400);
			table.setLockedWidth(true);
			table.getDefaultCell().setFixedHeight(20);
			table.getDefaultCell().setBorder(Rectangle.BOTTOM);
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
			table.addCell(text);
			table.writeSelectedRows(0, -1, 50, page_size.getHeight() - 20,
					stamper.getOverContent(i));
		}
	}

	public void addPageNum() throws IOException {
		int total_pages = reader.getNumberOfPages();

		PdfGState gs = new PdfGState();
		gs.setFillOpacity(1.0f);

		for (int i = 1; i <= total_pages; i++) {
			PdfContentByte content = stamper.getUnderContent(i);
			content.beginText();
			content.setGState(gs);
			content.setColorFill(BaseColor.BLACK);
			content.setFontAndSize(getBaseFont(font_family), 11);

			Rectangle page_rect = reader.getPageSizeWithRotation(i);
			String text = String.format("- 第 %d 页 共 %d 页 -", i, total_pages);
			content.showTextAligned(Element.ALIGN_CENTER,
					text, page_rect.getWidth() / 2, 30, 0);
			content.endText();
		}
	}

	public static final int ALLOW_PRINTING = PdfWriter.ALLOW_PRINTING;
	public static final int ALLOW_DEGRADED_PRINTING = PdfWriter.ALLOW_DEGRADED_PRINTING;
	public static final int ALLOW_MODIFY_CONTENTS = PdfWriter.ALLOW_MODIFY_CONTENTS;
	public static final int ALLOW_ASSEMBLY = PdfWriter.ALLOW_ASSEMBLY;
	public static final int ALLOW_COPY = PdfWriter.ALLOW_COPY;
	public static final int ALLOW_SCREENREADERS = PdfWriter.ALLOW_SCREENREADERS;
	public static final int ALLOW_MODIFY_ANNOTATIONS = PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
	public static final int ALLOW_FILL_IN = PdfWriter.ALLOW_FILL_IN;

	public void encrypt(String user_passwd, String owner_passwd,
			int permissions) throws Exception {
		if (owner_passwd == null) {
			owner_passwd = "LuckyByte.TextPdf.default";
		}
		try {
			stamper.setEncryption(PdfWriter.ENCRYPTION_AES_128 |
					PdfWriter.DO_NOT_ENCRYPT_METADATA,
					user_passwd, owner_passwd, permissions);
		} catch (DocumentException ex) {
			throw new Exception(ex);
		}
	}

}
