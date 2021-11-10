package com.lucky_byte.pdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
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

			List<URL> css_urls = new ArrayList<URL>();
			css_urls.add(new URL(new URL("file:"), "textpdf.css"));

			List<URL> js_urls = new ArrayList<URL>();
			js_urls.add(new URL(new URL("file:"), "jquery-1.11.3.min.js"));
			js_urls.add(new URL(new URL("file:"), "textpdf.js"));

			TextParser parser = new TextParser(
					new FileInputStream(xmlfile),
					new FileInputStream(jsonfile),
					new FileOutputStream(htmlfile));
			parser.setCSSURLs(css_urls);
			parser.setJSURLs(js_urls);
			parser.genHTML();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
