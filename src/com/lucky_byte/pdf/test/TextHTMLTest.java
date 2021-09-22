package com.lucky_byte.pdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import org.junit.Test;

import com.lucky_byte.pdf.TextParser;

public class TextHTMLTest
{
	@Test
	public void test() {
		try {
			File xmlfile = new File("tests/test.xml");
			File jsonfile = new File("tests/test.json");
			File htmlfile = new File("tests/test.html");
			URL css_url = new URL(new URL("file:"), "textpdf.css");
			URL js_url = new URL(new URL("file:"), "textpdf.js");

			TextParser parser = new TextParser();
			parser.genHTML(new FileInputStream(xmlfile),
					new FileInputStream(jsonfile),
					new FileOutputStream(htmlfile),
					css_url, js_url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
