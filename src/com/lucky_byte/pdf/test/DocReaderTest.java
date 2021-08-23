package com.lucky_byte.pdf.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.junit.Test;

import com.lucky_byte.pdf.DocReader;

public class DocReaderTest
{
	@Test
	public void test() {
		try {
			InputStream doc_stream =
					new FileInputStream("tests/test.doc");
			OutputStream xml_stream =
					new FileOutputStream("tests/test.xml");
			
			DocReader reader = new DocReader();
			reader.setXSLUrl(new URL(new URL("file:"),
					"../src/xsl/textpdf.xsl"));
			reader.read(doc_stream, xml_stream);

			doc_stream.close();
			xml_stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
