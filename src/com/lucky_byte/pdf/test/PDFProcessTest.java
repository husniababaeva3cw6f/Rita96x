package com.lucky_byte.pdf.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.lucky_byte.pdf.PDFProcess;

public class PDFProcessTest
{
	@Test
	public void testPDFProcess() throws DocumentException, IOException {
		PDFProcess pdfProcess =new PDFProcess(
				new FileInputStream("tests/test.pdf"),
				new FileOutputStream("tests/test2.pdf"));
		pdfProcess.addTextMarker("这是一个测试水印", 0.4f, 45, 18,
				PDFProcess.MARKER_STYLE_FULL);
		pdfProcess.addImgMarker("tests/logo-32.png", -32, 0, 32, 32, 1.0f, true);
		pdfProcess.finish();
	}
}
