<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<html>
			<body>
				<h1>Data Source Attributions</h1>


				<hr></hr>
				<xsl:for-each select="datasetPresenters/datasetPresenter">
					<xsl:variable name="relativeNode" select="." />

					<h3>
						<xsl:value-of select="displayName"
							disable-output-escaping="yes" />
					</h3>
					<dl>
						
                           <dt><b>Short Display Name:</b></dt>
                           <dd>
                            <xsl:value-of select="shortDisplayName"
                                disable-output-escaping="yes" />
                            </dd>
      
      
							<dt><b>Description:</b></dt>
						
                        <dd>
                            <xsl:value-of select="description"
                                disable-output-escaping="yes" />
                        </dd>

						
							<dt><b>Short Description:</b></dt>
						
						<dd>
							<xsl:value-of select="summary"
								disable-output-escaping="yes" />
						</dd>

						
							<dt><b>Display Category (Optional):</b></dt>
						
						<dd>
							<xsl:value-of select="displayCategory"
								disable-output-escaping="yes" />
						</dd>

							<dt><b>Website Usage:</b></dt>
						
						<dd>
							<xsl:value-of select="usage"
								disable-output-escaping="yes" />
						</dd>

							<dt><b>Protocol:</b></dt>
						
						<dd>
							<xsl:value-of select="protocol"
								disable-output-escaping="yes" />
						</dd>

						
							<dt><b>Caveat:</b></dt>
						
						<dd>
							<xsl:value-of select="caveat"
								disable-output-escaping="yes" />
						</dd>

						
							<dt><b>Acknowledgement:</b></dt>
						
						<dd>
							<xsl:value-of select="acknowledgement"
								disable-output-escaping="yes" />
						</dd>

						
							<dt><b>ReleasePolicy:</b></dt>
						
						<dd>
							<xsl:value-of select="releasePolicy"
								disable-output-escaping="yes" />
						</dd>







						
							<dt><b>Pubmed IDs:</b></dt>
						
						<dd>
							<xsl:for-each select="./pubmedId">
       
<a>
<xsl:attribute name="href">
http://www.ncbi.nlm.nih.gov/pubmed/?term=<xsl:value-of select="." disable-output-escaping="yes" />
</xsl:attribute>
<xsl:value-of select="." disable-output-escaping="yes" />
</a>


								<br />
							</xsl:for-each>
						</dd>



							<dt><b>Contacts:</b></dt>
						
						<dd>
      
                                <xsl:for-each select="./primaryContactId">

        <xsl:variable name="primaryContactId">                     
        <xsl:value-of select="." disable-output-escaping="yes" />
        </xsl:variable>
<b>
        <xsl:value-of select="document('contacts/contacts.xml')/contacts/contact[contactId=$primaryContactId]/name"/> (<xsl:value-of select="document('contacts/contacts.xml')/contacts/contact[contactId=$primaryContactId]/institution"/>)
                              </b>
                              
                                <br />        
                            </xsl:for-each>
      
      
							<xsl:for-each select="./contactId">

		<xsl:variable name="contactId">						
        <xsl:value-of select="." disable-output-escaping="yes" />
        </xsl:variable>

        <xsl:value-of select="document('contacts/contacts.xml')/contacts/contact[contactId=$contactId]/name"/> (<xsl:value-of select="document('contacts/contacts.xml')/contacts/contact[contactId=$contactId]/institution"/>)
                                <br />        
							</xsl:for-each>
						</dd>
                            <dt><b>Links:</b></dt>
                        <dd>
                            <xsl:for-each select="./link">
       
<a>
<xsl:attribute name="href">
<xsl:value-of select="url" disable-output-escaping="yes" />
</xsl:attribute>

<xsl:choose>
  <xsl:when test="text != ''">
   <xsl:value-of select="text" disable-output-escaping="yes" />
    </xsl:when>
    <xsl:otherwise>
     <xsl:value-of select="url" disable-output-escaping="yes" />
    </xsl:otherwise>
    </xsl:choose>

</a>


                                <br />
                            </xsl:for-each>
                        </dd>
                        
                        
                        <dt><b>References</b></dt>
                        
                        <dd>
                        <xsl:for-each select="./wdkReference">

                           addWdkReference("<xsl:value-of select="@recordClass"/>", "<xsl:value-of select="@type"/>", "<xsl:value-of select="@name"/>"); <br />
                        
                        
                        

                                                </xsl:for-each>
                        
                        </dd>
                        

					</dl>


					<hr></hr>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
