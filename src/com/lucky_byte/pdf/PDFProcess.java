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
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

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
	public final static int MARKER_STYLE_ALL = 2;
	
	public final static int FONT_FAMILY_HEI  = 1;
	public final static int FONT_FAMILY_SONG = 2;
	
	private	int img_marker_style;
	private int text_marker_Style;
	private	int font_family;
	private	int font_size;
	private	float img_fill_opacity;
	private	float text_fill_opacity;
	private	float img_x;
	private	float img_y;
	private int imgsize_x;
	private int imgsize_y;
	private	float text_x;
	private	float text_y;
	private int textsize_x;
	private int textsize_y;
	private BaseColor color;
	private int angle;
	
	public PDFProcess() {
		this.img_marker_style = MARKER_STYLE_CENTER;
		this.text_marker_Style = MARKER_STYLE_ALL;
		this.font_family = FONT_FAMILY_SONG;
		this.font_size = 20;
		this.img_fill_opacity = 0.8f;
		this.text_fill_opacity = 0.5f;
		this.img_x = 400;
		this.img_y = 480;
		this.text_x = 200;
		this.text_y = 120;
		this.color = BaseColor.GRAY;
		this.angle = 45;
		this.imgsize_x = 100;
		this.imgsize_y = 100;
		this.textsize_x = 100;
		this.textsize_y = 100;
	}	
	
	private BaseFont getBaseFont(int font_family) 
			throws DocumentException, IOException{
		BaseFont base_font = null;
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
		return base_font;
	}

	/**
	 * 修改文本水印风格：
	 * 1: 居中 位于 pdf 页面中心
	 * 2: 平铺 将 pdf 页面铺满
	 * @param text_marker_style
	 */
	public void setTextMarkerStyle(int text_marker_style ){
		this.text_marker_Style = text_marker_style;
	}

	/**
	 * 修改文本文字格式
	 * @param font_family
	 */
	public void setTextFontFamily(int font_family){
		this.font_family = font_family;
	}

	/**
	 * 修改文本字号
	 * @param font_size
	 */
	public void setTextFontSize(int font_size){
		this.font_size = font_size;
	}

	/**
	 * 修改文本颜色
	 * @param color
	 */
	public void setTextColor(BaseColor color){
		this.color = color;
	}

	/**
	 * 修改文本倾斜角度
	 * @param angle
	 */
	public void setTextAngle(int angle){
		this.angle = angle;
	}

	/**
	 * 修改图片透明度
	 * @param img_fill_opacity
	 */
	public void setImgFillOpacity(float img_fill_opacity){
		this.img_fill_opacity = img_fill_opacity;
	}

	/**
	 * 修改文本透明度
	 * @param text_fill_opacity
	 */
	public void setTextFillOpacity(float text_fill_opacity){
		this.text_fill_opacity = text_fill_opacity;
	}

	/**
	 * 添加文字和图片水印
	 * @param inputFile
	 * @param outputFile
	 * @param textFile
	 * @param imageFile
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void addMarker(String inputFile, String outputFile,
			String textFile,String imageFile)
					throws DocumentException, IOException{
		PdfReader reader = new PdfReader(inputFile);
		PdfStamper stamper = new PdfStamper(reader,
				new FileOutputStream(outputFile));
		int total = reader.getNumberOfPages() + 1;
		PdfContentByte content;  
		Image image = null; 
		Rectangle pageRect = null;
		PdfGState gs = new PdfGState();
		if (imageFile != null) {
			image = Image.getInstance(imageFile);  
			image.setAbsolutePosition(img_x,img_y);
			image.scaleToFit(imgsize_x, imgsize_y);
		} 
		for (int i = 1; i < total; i++) {  
			pageRect = stamper.getReader().getPageSizeWithRotation(i);
			float size_x = pageRect.getWidth();
			float size_y = pageRect.getHeight();
			content = stamper.getUnderContent(i); 
			gs.setFillOpacity(img_fill_opacity);
			content.setGState(gs);
			content.addImage(image);
			if (textFile !=null&&textFile.length()!=0) {
				content.beginText();  
				gs.setFillOpacity(text_fill_opacity);
				content.setGState(gs);
				content.setColorFill(color);  
				content.setFontAndSize(getBaseFont(font_family), font_size);
				content.setTextMatrix(text_x, text_y);  
				if(text_marker_Style == MARKER_STYLE_CENTER ){
					content.showTextAligned(Element.ALIGN_JUSTIFIED_ALL, 
							textFile,size_x/2, size_y/2, angle); 
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append(textFile+"       ");
					sb.append(textFile+"       ");
					sb.append(textFile+"       ");
					sb.append(textFile+"       ");
					for(int x = 0; x<size_x;x += textsize_x*sb.length()){
						for(int y = (int)size_y;
								y > -(int) size_y; y -= textsize_y) {
							content.showTextAligned(Element.ALIGN_JUSTIFIED_ALL,
									sb.toString(),x, y, angle); 
						}
					}
				}
				content.endText();
			}
		}
		stamper.close();  
	}

	/**
	 * 方法重载
	 * @param inputFile
	 * @param outputFile
	 * @param file
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void addMarker(String inputFile, String outputFile, String file)
			throws DocumentException, IOException {
		PdfReader reader = new PdfReader(inputFile);
		PdfStamper stamper = new PdfStamper(reader,
				new FileOutputStream(outputFile));
		int total = reader.getNumberOfPages() + 1;
		PdfContentByte content;
		Rectangle pageRect = null;
		PdfGState gs = new PdfGState();
		if (!isImage(new File(file))) {
			for (int i = 1; i < total; i++) {
				pageRect = stamper.getReader().getPageSizeWithRotation(i);
				float size_x = pageRect.getWidth();
				float size_y = pageRect.getHeight();
				content = stamper.getUnderContent(i);
				if (file != null && file.length() != 0) {
					content.beginText();
					gs.setFillOpacity(text_fill_opacity);
					content.setGState(gs);
					content.setColorFill(color);
					content.setFontAndSize(getBaseFont(font_family), font_size);
					content.setTextMatrix(text_x, text_y);

					if (text_marker_Style == MARKER_STYLE_CENTER) {
						content.showTextAligned(Element.ALIGN_CENTER,
								file, size_x / 2, size_y / 2, angle);
					} else {
						StringBuffer sb = new StringBuffer();
						sb.append(file + "       ");
						sb.append(file + "       ");
						sb.append(file + "       ");
						sb.append(file + "       ");
						for(int x = 0; x<size_x;x += textsize_x*sb.length()){
							for(int y = (int)size_y;
									y>-(int) size_y;y -= textsize_y){
								content.showTextAligned(Element.ALIGN_CENTER,
										sb.toString(),x, y, angle);
							}
						}
					}
					content.endText();
				}
			}
		} else {
			Image image = null;
			if (file != null) {
				image = Image.getInstance(file);
				image.setAbsolutePosition(img_x, img_y);
				image.scaleToFit(imgsize_x, imgsize_y);
			}
			for (int i = 1; i < total; i++) {
				pageRect = stamper.getReader().getPageSizeWithRotation(i);
				content = stamper.getUnderContent(i);
				gs.setFillOpacity(img_fill_opacity);
				content.setGState(gs);
				content.addImage(image);
			}
		}
		stamper.close();
	}
	
	/**
	 * 判断输入的字符串是否是图片
	 * @param file
	 * @return
	 */
	private boolean isImage(File file) {
		boolean flag = false;
		try {
			ImageInputStream is = ImageIO.createImageInputStream(file);
			if (null == is) {
				return flag;
			}
			is.close();
			flag = true;
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return flag;
	}
}
