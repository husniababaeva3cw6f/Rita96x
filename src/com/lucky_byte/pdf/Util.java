/* TextPDF - generate PDF dynamically
 * 
 * Copyright (c) 2015 Lucky Byte, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.lucky_byte.pdf;

/**
 * 通用工具类
 */
public class Util
{
	private static final Object[][] xml_escape_chars = {
			{ '"', "&#x0022;" }, { '&', "&#x0026;" }, { '\'', "&#x0027;" },
			{ '<', "&#x003C;" }, { '>', "&#x003E;" },
	};

	private static final Object[][] html_escape_chars = {
			{ '"', "&#x0022;" }, { '&', "&#x0026;" }, { '\'', "&#x0027;" },
			{ '<', "&#x003C;" }, { '>', "&#x003E;" }, { ' ', "&#x00A0;" },
			{ '¡', "&#x00A1;" }, { '¢', "&#x00A2;" }, { '£', "&#x00A3;" },
			{ '¤', "&#x00A4;" }, { '¥', "&#x00A5;" }, { '¦', "&#x00A6;" },
			{ '§', "&#x00A7;" }, { '¨', "&#x00A8;" }, { '©', "&#x00A9;" },
			{ 'ª', "&#x00AA;" }, { '«', "&#x00AB;" }, { '¬', "&#x00AC;" },
			{ '®', "&#x00AE;" }, { '¯', "&#x00AF;" }, { '°', "&#x00B0;" },
			{ '±', "&#x00B1;" }, { '²', "&#x00B2;" }, { '³', "&#x00B3;" },
			{ '´', "&#x00B4;" }, { 'µ', "&#x00B5;" }, { '¶', "&#x00B6;" },
			{ '·', "&#x00B7;" }, { '¸', "&#x00B8;" }, { '¹', "&#x00B9;" },
			{ 'º', "&#x00BA;" }, { '»', "&#x00BB;" }, { '¼', "&#x00BC;" },
			{ '½', "&#x00BD;" }, { '¾', "&#x00BE;" }, { '¿', "&#x00BF;" },
			{ 'À', "&#x00C0;" }, { 'Á', "&#x00C1;" }, { 'Â', "&#x00C2;" },
			{ 'Ã', "&#x00C3;" }, { 'Ä', "&#x00C4;" }, { 'Å', "&#x00C5;" },
			{ 'Æ', "&#x00C6;" }, { 'Ç', "&#x00C7;" }, { 'È', "&#x00C8;" },
			{ 'É', "&#x00C9;" }, { 'Ê', "&#x00CA;" }, { 'Ë', "&#x00CB;" },
			{ 'Ì', "&#x00CC;" }, { 'Í', "&#x00CD;" }, { 'Î', "&#x00CE;" },
			{ 'Ï', "&#x00CF;" }, { 'Ð', "&#x00D0;" }, { 'Ñ', "&#x00D1;" },
			{ 'Ò', "&#x00D2;" }, { 'Ó', "&#x00D3;" }, { 'Ô', "&#x00D4;" },
			{ 'Õ', "&#x00D5;" }, { 'Ö', "&#x00D6;" }, { '×', "&#x00D7;" },
			{ 'Ø', "&#x00D8;" }, { 'Ù', "&#x00D9;" }, { 'Ú', "&#x00DA;" },
			{ 'Û', "&#x00DB;" }, { 'Ü', "&#x00DC;" }, { 'Ý', "&#x00DD;" },
			{ 'Þ', "&#x00DE;" }, { 'ß', "&#x00DF;" }, { 'à', "&#x00E0;" },
			{ 'á', "&#x00E1;" }, { 'â', "&#x00E2;" }, { 'ã', "&#x00E3;" },
			{ 'ä', "&#x00E4;" }, { 'å', "&#x00E5;" }, { 'æ', "&#x00E6;" },
			{ 'ç', "&#x00E7;" }, { 'è', "&#x00E8;" }, { 'é', "&#x00E9;" },
			{ 'ê', "&#x00EA;" }, { 'ë', "&#x00EB;" }, { 'ì', "&#x00EC;" },
			{ 'í', "&#x00ED;" }, { 'î', "&#x00EE;" }, { 'ï', "&#x00EF;" },
			{ 'ð', "&#x00F0;" }, { 'ñ', "&#x00F1;" }, { 'ò', "&#x00F2;" },
			{ 'ó', "&#x00F3;" }, { 'ô', "&#x00F4;" }, { 'õ', "&#x00F5;" },
			{ 'ö', "&#x00F6;" }, { '÷', "&#x00F7;" }, { 'ø', "&#x00F8;" },
			{ 'ù', "&#x00F9;" }, { 'ú', "&#x00FA;" }, { 'û', "&#x00FB;" },
			{ 'ü', "&#x00FC;" }, { 'ý', "&#x00FD;" }, { 'þ', "&#x00FE;" },
			{ 'ÿ', "&#x00FF;" }, { 'Œ', "&#x0152;" }, { 'œ', "&#x0153;" },
			{ 'Š', "&#x0160;" }, { 'š', "&#x0161;" }, { 'Ÿ', "&#x0178;" },
			{ 'ƒ', "&#x0192;" }, { 'ˆ', "&#x02C6;" }, { '˜', "&#x02DC;" },
			{ 'Α', "&#x0391;" }, { 'Β', "&#x0392;" }, { 'Γ', "&#x0393;" },
			{ 'Δ', "&#x0394;" }, { 'Ε', "&#x0395;" }, { 'Ζ', "&#x0396;" },
			{ 'Η', "&#x0397;" }, { 'Θ', "&#x0398;" }, { 'Ι', "&#x0399;" },
			{ 'Κ', "&#x039A;" }, { 'Λ', "&#x039B;" }, { 'Μ', "&#x039C;" },
			{ 'Ν', "&#x039D;" }, { 'Ξ', "&#x039E;" }, { 'Ο', "&#x039F;" },
			{ 'Π', "&#x03A0;" }, { 'Ρ', "&#x03A1;" }, { 'Σ', "&#x03A3;" },
			{ 'Τ', "&#x03A4;" }, { 'Υ', "&#x03A5;" }, { 'Φ', "&#x03A6;" },
			{ 'Χ', "&#x03A7;" }, { 'Ψ', "&#x03A8;" }, { 'Ω', "&#x03A9;" },
			{ 'α', "&#x03B1;" }, { 'β', "&#x03B2;" }, { 'γ', "&#x03B3;" },
			{ 'δ', "&#x03B4;" }, { 'ε', "&#x03B5;" }, { 'ζ', "&#x03B6;" },
			{ 'η', "&#x03B7;" }, { 'θ', "&#x03B8;" }, { 'ι', "&#x03B9;" },
			{ 'κ', "&#x03BA;" }, { 'λ', "&#x03BB;" }, { 'μ', "&#x03BC;" },
			{ 'ν', "&#x03BD;" }, { 'ξ', "&#x03BE;" }, { 'ο', "&#x03BF;" },
			{ 'π', "&#x03C0;" }, { 'ρ', "&#x03C1;" }, { 'ς', "&#x03C2;" },
			{ 'σ', "&#x03C3;" }, { 'τ', "&#x03C4;" }, { 'υ', "&#x03C5;" },
			{ 'φ', "&#x03C6;" }, { 'χ', "&#x03C7;" }, { 'ψ', "&#x03C8;" },
			{ 'ω', "&#x03C9;" }, { 'ϑ', "&#x03D1;" }, { 'ϒ', "&#x03D2;" },
			{ 'ϖ', "&#x03D6;" }, { ' ', "&#x2002;" }, { '–', "&#x2013;" },
			{ '—', "&#x2014;" }, { '‘', "&#x2018;" }, { '’', "&#x2019;" },
			{ '‚', "&#x201A;" }, { '“', "&#x201C;" }, { '”', "&#x201D;" },
			{ '„', "&#x201E;" }, { '†', "&#x2020;" }, { '‡', "&#x2021;" },
			{ '•', "&#x2022;" }, { '…', "&#x2026;" }, { '‰', "&#x2030;" },
			{ '′', "&#x2032;" }, { '″', "&#x2033;" }, { '‹', "&#x2039;" },
			{ '›', "&#x203A;" }, { '‾', "&#x203E;" }, { '⁄', "&#x2044;" },
			{ '€', "&#x20AC;" }, { 'ℑ', "&#x2111;" }, { '℘', "&#x2118;" },
			{ 'ℜ', "&#x211C;" }, { '™', "&#x2122;" }, { 'ℵ', "&#x2135;" },
			{ '←', "&#x2190;" }, { '↑', "&#x2191;" }, { '→', "&#x2192;" },
			{ '↓', "&#x2193;" }, { '↔', "&#x2194;" }, { '↵', "&#x21B5;" },
			{ '⇐', "&#x21D0;" }, { '⇑', "&#x21D1;" }, { '⇒', "&#x21D2;" },
			{ '⇓', "&#x21D3;" }, { '⇔', "&#x21D4;" }, { '∀', "&#x2200;" },
			{ '∂', "&#x2202;" }, { '∃', "&#x2203;" }, { '∅', "&#x2205;" },
			{ '∇', "&#x2207;" }, { '∈', "&#x2208;" }, { '∉', "&#x2209;" },
			{ '∋', "&#x220B;" }, { '∏', "&#x220F;" }, { '∑', "&#x2211;" },
			{ '−', "&#x2212;" }, { '∗', "&#x2217;" }, { '√', "&#x221A;" },
			{ '∝', "&#x221D;" }, { '∞', "&#x221E;" }, { '∠', "&#x2220;" },
			{ '∧', "&#x2227;" }, { '∨', "&#x2228;" }, { '∩', "&#x2229;" },
			{ '∪', "&#x222A;" }, { '∫', "&#x222B;" }, { '∴', "&#x2234;" },
			{ '∼', "&#x223C;" }, { '≅', "&#x2245;" }, { '≈', "&#x2248;" },
			{ '≠', "&#x2260;" }, { '≡', "&#x2261;" }, { '≤', "&#x2264;" },
			{ '≥', "&#x2265;" }, { '⊂', "&#x2282;" }, { '⊃', "&#x2283;" },
			{ '⊄', "&#x2284;" }, { '⊆', "&#x2286;" }, { '⊇', "&#x2287;" },
			{ '⊕', "&#x2295;" }, { '⊗', "&#x2297;" }, { '⊥', "&#x22A5;" },
			{ '⋅', "&#x22C5;" }, { '⌈', "&#x2308;" }, { '⌉', "&#x2309;" },
			{ '⌊', "&#x230A;" }, { '⌋', "&#x230B;" }, { '〈', "&#x2329;" },
			{ '〉', "&#x232A;" }, { '◊', "&#x25CA;" }, { '♠', "&#x2660;" },
			{ '♣', "&#x2663;" }, { '♥', "&#x2665;" }, { '♦', "&#x2666;" },
	};

	public static String escapeXMLChars(char ch) {
		for (int i = 0; i < xml_escape_chars.length; i++) {
			if ((Character) xml_escape_chars[i][0] == ch) {
				return (String) xml_escape_chars[i][1];
			}
		}
		return null;
	}


	public static String escapeHTMLChars(char ch) {
		for (int i = 0; i < html_escape_chars.length; i++) {
			if ((Character) html_escape_chars[i][0] == ch) {
				return (String) html_escape_chars[i][1];
			}
		}
		return null;
	}

	public static String escapeHTMLString(String string) {
		if (string == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			String escape = escapeHTMLChars(string.charAt(i));
			if (escape != null) {
				builder.append(escape);
			} else {
				builder.append(string.charAt(i));
			}
		}
		return builder.toString();
	}
}
