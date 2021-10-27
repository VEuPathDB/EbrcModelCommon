<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>
<xsl:template match="/">
<xsl:for-each select="datasetPresenters/datasetPresenter/history/@buildNumber/../..">

<xsl:value-of select="@projectName"/><xsl:text>&#x9;</xsl:text>
<xsl:value-of select="@name"/><xsl:text>&#x9;</xsl:text>

<xsl:for-each select="history">|<xsl:value-of select="@buildNumber"/>|</xsl:for-each><xsl:text>&#x9;</xsl:text>

<xsl:value-of select="templateInjector/@className"/><xsl:text>&#x9;</xsl:text>
<xsl:text>&#10;</xsl:text>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
