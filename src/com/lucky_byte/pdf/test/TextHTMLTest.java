package com.lucky_byte.pdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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

			List<String> css_urls = new ArrayList<String>();
			css_urls.add("textpdf.css");

			List<String> js_urls = new ArrayList<String>();
			js_urls.add("jquery-1.11.3.min.js");
			js_urls.add("textpdf.js");

			TextParser parser = new TextParser(
					new FileInputStream(xmlfile),
					new FileInputStream(jsonfile),
					new FileOutputStream(htmlfile));
			parser.setCSSLinks(css_urls);
			parser.setJSLinks(js_urls);
			parser.setOutputEncoding("GB18030");
			parser.genHTML();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
