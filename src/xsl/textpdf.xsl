<?xml version="1.0" encoding="utf-8"?>

<!-- (c) 2015 Lucky Byte, Inc. -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:textpdf="http://www.lucky-byte.com/XSL/TextPDF">

  <xsl:template match="/">
    <html>
      <head>
        <title>bi</title>
        <style>
          body {
          font-size:16px;
          margin: 40px;
          line-height: 150%;
          }
          .title {
          font-size: 22px;
          }
          input {
          margin-right: 5px;
          color: blue;
          font-size: 16px;
          }
        </style>
      </head>

      <body>
        <xsl:apply-templates select="textpdf" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="textpdf">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="para">
    <p><xsl:apply-templates /></p>
  </xsl:template>

  <xsl:template match="title">
    <p class="title"><xsl:value-of select="." /></p>
  </xsl:template>

  <xsl:template match="value">
    <input>
      <xsl:attribute name="id">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <xsl:attribute name="name">
        <xsl:value-of select="@id" />
      </xsl:attribute>
      <!-- only add size attr when minlen attr present -->
      <xsl:if test="@minlen">
        <xsl:attribute name="size">
          <xsl:value-of select="@minlen" />
        </xsl:attribute>
      </xsl:if>
    </input>
  </xsl:template>

</xsl:stylesheet>
