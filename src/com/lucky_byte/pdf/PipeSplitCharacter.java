package com.lucky_byte.pdf;

import com.itextpdf.text.SplitCharacter;
import com.itextpdf.text.pdf.PdfChunk;

public class PipeSplitCharacter implements SplitCharacter  {

	@Override
	public boolean isSplitCharacter(
	    int start, int current, int end, char[] cc,
	    PdfChunk[] ck) {
	    char c;
	    if (ck == null)
	      c = cc[current];
	    else
	      c = (char)ck[Math.min(current, ck.length - 1)]
	                  .getUnicodeEquivalent(cc[current]);
	    return (c == '|' || c <= ' ' || c == '-');

	}

}
