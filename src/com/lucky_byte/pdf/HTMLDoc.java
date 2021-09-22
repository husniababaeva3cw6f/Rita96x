package com.lucky_byte.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.itextpdf.text.DocumentException;

public class HTMLDoc extends TextDoc {

	public HTMLDoc(OutputStream out_stream) {
		super(out_stream);
	}

	@Override
	public boolean open() {
		return true;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void writeBlock(String block_name, List<TextChunk> chunk_list)
			throws DocumentException, IOException {

	}

	@Override
	public void newPage() {

	}

}
