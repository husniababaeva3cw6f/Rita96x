package com.lucky_byte.pdf.test;

import java.io.IOException;

import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.lucky_byte.pdf.PDFProcess;

public class PDFProcessTest
{
	@Test
	public void testPDFProcess() throws DocumentException, IOException {
		String inputFlie ="tests/test.pdf";
		String outputFlie ="tests/test.pdf";
		String textFile = "这是一个测试水印";
		String imgFile= "tests/logo-32.png";
		PDFProcess pdfProcess =new PDFProcess();
		pdfProcess.setTextAngle(45);
		pdfProcess.setImgFillOpacity(0.1f);
		pdfProcess.setTextFillOpacity(0.1f);
		//pdfProcess.setTextFontSize(50);
		//pdfProcess.waterMark(inputFlie,outputFlie,waterMarkName,permission);
		//pdfProcess.setTextMarkerStyle(1);
		pdfProcess.addMarker(inputFlie, outputFlie, textFile, null);
	}
}
