package com.lucky_byte.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import org.json.simple.JSONObject;
import com.itextpdf.text.DocumentException;

public class HTMLDoc extends TextDoc
{
	private JSONObject json_object;
	private URL css_url, js_url;

	private String html_open = "<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "  <head>\n"
			+ "    <title>__TITLE__</title>\n"
			+ "    <meta name=\"author\" content=\"Lucky Byte, Inc.\"/>\n"
			+ "    __CSS_URL__\n"
			+ "    __JS_URL__\n"
			+ "  </head>\n"
			+ "  <body>\n";

	private String html_close = "  </body>\n</html>\n";

	public HTMLDoc(OutputStream out_stream) {
		super(out_stream);
	}

	public void setJSONObject(JSONObject json_object) {
		this.json_object = json_object;
	}

	public void setURL(URL css_url, URL js_url) {
		this.css_url = css_url;
		this.js_url = js_url;
	}

	private boolean writeHtmlStream(String string) {
		try {
			out_stream.write(string.getBytes("UTF-8"));
			return true;
		} catch (UnsupportedEncodingException e) {
			System.err.println("Unsupported encoding.");
			return false;
		} catch (IOException e) {
			System.err.println("Write to html stream failed.");
			return false;
		}
	}

	@Override
	public boolean open() {
		if (json_object != null) {
			if (json_object.containsKey("title")) {
				Object value = json_object.get("title");
				if (value instanceof String) {
					html_open = html_open.replace("__TITLE__",
							(CharSequence) value);
				}
			}
		}
		if (css_url != null) {
			html_open = html_open.replace("__CSS_URL__",
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
							css_url.getPath() + "\" />");
		}
		if (js_url != null) {
			html_open = html_open.replace("__JS_URL__",
					"<script src=\"" +
							js_url.getPath() + "\"></script>");
		}
		return writeHtmlStream(html_open);
	}

	@Override
	public void close() {
		if (out_stream != null) {
			writeHtmlStream(html_close);
		}
	}

	@Override
	public boolean isOpen() {
		return out_stream != null;
	}

	@Override
	public void writeBlock(String block_name, List<TextChunk> chunk_list)
			throws DocumentException, IOException {

	}

	@Override
	public void newPage() {
	}

}
