package com.lucky_byte.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import com.itextpdf.text.DocumentException;

/**
 * 输出 HTML 文档
 */
public class HTMLDoc extends TextDoc
{
	private boolean is_open = false;
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

	private boolean writeStream(String string) {
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
		if (out_stream == null)
			return false;

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
		is_open = true;
		return writeStream(html_open);
	}

	@Override
	public void close() {
		if (is_open && out_stream != null) {
			writeStream(html_close);
		}
	}

	@Override
	public boolean isOpen() {
		return is_open;
	}

	private String[][] block_labels = {
			{ "title",   "h1" },
			{ "chapter", "h2" },
			{ "section", "h3" },
			{ "para",    "p" },
	};

	private String getHtmlLabel(String block_name) {
		for (int i = 0; i < block_labels.length; i++) {
			if (block_name.equalsIgnoreCase(block_labels[i][0])) {
				return block_labels[i][1];
			}
		}
		return null;
	}

	private Map<String, String> getHtmlAttrs(TextChunk chunk,
			boolean block_element) {
		Map<String, String> chunk_attrs = chunk.getAttrs();
		Map<String, String> html_attrs = new HashMap<String, String>();
		StringBuilder style_string = new StringBuilder();

		for (String key : chunk_attrs.keySet()) {
			if (key.equalsIgnoreCase("font-style")) {
				String[] styles = chunk_attrs.get(key).split(",");
				for (int i = 0; i < styles.length; i++) {
					String style_name = styles[i].trim();
					if (style_name.equalsIgnoreCase("bold")) {
						style_string.append("font-weight: bold;");
					} else if (style_name.equalsIgnoreCase("italic")) {
						style_string.append("font-style: italic;");
					} else if (style_name.equalsIgnoreCase("underline")) {
						style_string.append("font-decoration: underline;");
					}
				}
			} else if (block_element && key.equalsIgnoreCase("indent")) {
				style_string.append("text-indent: " + chunk_attrs.get(key) + "px;");
			} else if (block_element && key.equalsIgnoreCase("align")) {
				style_string.append("text-align: " + chunk_attrs.get(key) + ";");
			}
		}
		if (style_string.toString().length() > 0) {
			html_attrs.put("style", style_string.toString());
		}
		return html_attrs;
	}

	private String htmlCharEscape(String contents) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < contents.length(); i++) {
			char ch = contents.charAt(i);
			switch (ch) {
			case ' ':
				builder.append("&nbsp;");
				break;
			case '\t':
				builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				break;
			case '&':
				builder.append("&amp;");
				break;
			case '<':
				builder.append("&lt;");
				break;
			case '>':
				builder.append("&gt;");
				break;
			case '\n':
				builder.append("<br/>");
			default:
				builder.append(ch);
			}
		}
		return builder.toString();
	}

	private void writeValue(TextChunk chunk) {
		Map<String, String> attrs = chunk.getAttrs();
		writeStream("<input type=\"text\"");
		String value = attrs.get("id");
		if (value != null && value.length() > 0) {
			writeStream("id=\"" + value + "\" name=\"" + value + "\"");
		}
		value = attrs.get("minlen");
		if (value != null && value.length() > 0) {
			writeStream(" size=\"" + value + "\"");
		}
		writeStream(" />");
	}

	@Override
	public void writeBlock(String block_name, List<TextChunk> chunk_list)
			throws DocumentException, IOException {
		if (out_stream == null || chunk_list.size() == 0)
			return;

		String label = getHtmlLabel(block_name);
		if (label == null) {
			System.err.println("unable map block name '"
					+ block_name + "'to html label.");
			return;
		}

		for (int i = 0; i < chunk_list.size(); i++) {
			TextChunk chunk = chunk_list.get(i);
			if (chunk.isValue()) {
				writeValue(chunk);
				continue;
			}
			if (i == 0) {
				writeStream("    <" + label +
						" class=\"" + block_name + "\"");
			} else {
				writeStream("<span");
			}
			Map<String, String> html_attrs = getHtmlAttrs(chunk, i == 0);
			for (String key : html_attrs.keySet()) {
				writeStream(" " + key + "=\"" + html_attrs.get(key) + "\"");
			}
			writeStream(">");
			writeStream(htmlCharEscape(chunk.getContents()));
			if (i > 0) {
				writeStream("</span>");
			}
		}
		writeStream("</" + label + ">\n");
	}

	@Override
	public void newPage() {
		writeStream("<hr/>");
	}

}
