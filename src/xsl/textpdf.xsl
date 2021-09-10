<?xml version="1.0" encoding="utf-8"?>

<!-- (c) 2015 Lucky Byte, Inc. -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" encoding="utf=8" indent="yes"
    doctype-system="about:legacy-compat"/>

  <xsl:template match="/">

    <html>
      <head>
        <title><xsl:value-of select="textpdf/title" /></title>
        <meta name="author" content="Lucky Byte, Inc."/>
        <meta name="generator" content="TextPDF" />
        <meta name="description" content="TextPDF Template Editor" />
        <meta name="keywords" content="TextPDF, PDF, Template" />
        <link rel="stylesheet" href="textpdf.css" />
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
      <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
      <xsl:attribute name="name"><xsl:value-of select="@id" /></xsl:attribute>
      <!-- only add size attr when minlen attr present -->
      <xsl:if test="@minlen">
        <xsl:attribute name="size"><xsl:value-of select="@minlen" /></xsl:attribute>
      </xsl:if>
    </input>
  </xsl:template>

  <xsl:template match="span">
    <xsl:value-of select="." />
  </xsl:template>

</xsl:stylesheet>
