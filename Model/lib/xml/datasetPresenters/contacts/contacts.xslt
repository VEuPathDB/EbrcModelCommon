<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
    <h2>All Contacts</h2>
    <table border="1">
      <tr bgcolor="#9acd32">
        <th>Unique ID</th>
        <th>Name</th>
         <th>Institution</th>
         <th>Address</th>
         <th>email</th>
      </tr>
      <xsl:for-each select="contacts/contact">
      <xsl:sort select="contactId"/>
      <tr>
        <td><xsl:value-of select="contactId"/></td>
        <td><xsl:value-of select="name"/></td>
        
        <td><xsl:value-of select="institution"/></td>
        <td><xsl:value-of select="address"/> &#160;
        <xsl:value-of select="city"/> &#160;
        <xsl:value-of select="state"/> &#160;
        <xsl:value-of select="zip"/> &#160;
        <xsl:value-of select="country"/>
        </td>
        <td><xsl:value-of select="email"/></td>
      </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>
