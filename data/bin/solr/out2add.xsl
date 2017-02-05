<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- 
    Трансформирует XML в формате выдачи Solr в формат UpdateXmlMessages
-->

<xsl:output method="xml" indent="yes"/>

<xsl:template match="text()|@*"></xsl:template>

<xsl:template match="/response/result">
  <add overwrite="true">
    <xsl:apply-templates/>
  </add>
</xsl:template>

<xsl:template match="doc">
  <doc>
    <xsl:apply-templates />
  </doc>
</xsl:template>

<!-- arrays -->
<xsl:template match="doc/arr">
    <xsl:for-each select="*">
        <field>
            <xsl:attribute name="name">
                <xsl:value-of select="../@name" />
            </xsl:attribute>
            <xsl:value-of select="text()" />
        </field>
    </xsl:for-each>
</xsl:template>


<xsl:template match="doc/int">
  <field>
    <xsl:attribute name="name">
        <xsl:value-of select="@name" />
    </xsl:attribute>
    <xsl:value-of select="text()" />
    <xsl:apply-templates />
  </field>
</xsl:template>

<xsl:template match="doc/long">
  <field>
    <xsl:attribute name="name">
        <xsl:value-of select="@name" />
    </xsl:attribute>
    <xsl:value-of select="text()" />
    <xsl:apply-templates />
  </field>
</xsl:template>

<xsl:template match="doc/str">
  <field>
    <xsl:attribute name="name">
        <xsl:value-of select="@name" />
    </xsl:attribute>
    <xsl:value-of select="text()" />
    <xsl:apply-templates />
  </field>
</xsl:template>

<xsl:template match="doc/date">
  <field>
    <xsl:attribute name="name">
        <xsl:value-of select="@name" />
    </xsl:attribute>
    <xsl:value-of select="text()" />
    <xsl:apply-templates />
  </field>
</xsl:template>


</xsl:stylesheet>
