package com.lucky_byte.pdf.test;

import java.io.File;
import org.junit.Test;

import com.lucky_byte.pdf.TextPDF;

public class TextPDFTest {

	@Test
	public void test() {
		try {
			File xmlfile = new File("tests/test.xml");
			File jsonfile = new File("tests/test.json");
			File pdffile = new File("tests/test.pdf");
			TextPDF.gen(xmlfile, jsonfile, pdffile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
