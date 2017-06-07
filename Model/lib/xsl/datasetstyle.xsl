<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:output method="html" doctype-system="about:legacy-compat" />

<!-- / forward slash is used to denote a patern that matches the root node of the XML document -->
<xsl:template match ="/" >
  <html>
    <head>
      <title> Dataset Classes </title>
      <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/themes/smoothness/jquery-ui.css"/>
      <style type="text/css">
        body {
          min-width: 900px;
          font-family: helvetica, arial, sans;
          font-size: 95%;
          margin: 0px;
        }
        .banner {
          background-color: lightgoldenrodyellow;
          font-size: 85%;
          text-align: center;
          border-bottom: 1px solid wheat;
          padding: 2px;
          color: #666;
        }
        code, .code {
          font-family: monospace;
        }
        h2 {
          border-top: 4px solid red;
          border-bottom: 3px solid orange;
          padding-top: 4px;
          padding-bottom: 4px;
        }
        h3, h4 {
          margin-bottom: 0px;
        }
        h3, dt {
          color: purple;
        }
        table {
          margin-top: 1em;
        }
        #categories, #classes, #content {
          overflow-y: scroll;
        }
        #toc {
          float: left;
          width: 280px;
          padding-left: 8px;
          padding-right: 10px;
          overflow-x: auto;
          font-size: 90%;
        }
        #toc ul {
          list-style: none;
          padding-left: 10px;
          margin: 0;
        }
        #toc li {
          padding: 1px 0;
        }
        #toc a, #toc a:visited {
          text-decoration: none;
          color: rgb(17,79,192);
          display: inline-block;
          width: 100%;
        }
        #toc a:hover {
          color: #bb7a2a;
          background-color: whitesmoke;
        }
        #toc-handle {
          border-right: 1px solid #ccc;
          border-left: 1px solid #ccc;
          width: 4px;
          right: 0px;
          cursor: ew-resize;
        }
        #categories {
          height: 30%;
        }
        #categories ul {
          padding-bottom: 8px;
        }
        #categories li.active {
          background: #ccc;
        }
        #categories-handle {
          border-top: 1px solid #ccc;
          border-bottom: 1px solid #ccc;
          height: 4px;
          bottom: 0px;
          cursor: ns-resize;
        }
        .ui-resizable-handle {
          background-color: white;
        }
        .ui-resizable-handle:hover {
          background-color: #ccc;
          background-color: whitesmoke;
        }
        #content {
          margin-left: 290px;
          padding-left: 10px;
          padding-right: 8px;
        }
        #content .section > div {
          padding-left: 1em;
          overflow-x: auto;
        }
        #content h3.inline, #content h3.inline + div {
          display: inline-block;
          vertical-align: bottom;
        }
        #content ul {
          padding-left: 20px;
          margin-left: 0;
        }
        th, td {
          padding: 4px;
          text-align: left;
        }
        .ui-accordion .ui-accordion-header {
          font-size: small;
          background: none;
        }
      </style>
    </head>
    <body>
      <div class="banner">If you want to see the raw classes.xml file, use your browser's View Source tool (or type Ctrl-u)</div>
      <div class="main">
        <div id="toc">
          <div id="categories">
            <h3>Categories</h3>
            <xsl:apply-templates mode="categories"/>
            <div id="categories-handle" class="ui-resizable-handle ui-resizable-s"></div>
          </div>
          <div id="classes">
            <h3>Classes</h3>
            <xsl:apply-templates mode="classes"/>
          </div>
          <div id="toc-handle" class="ui-resizable-handle ui-resizable-e"></div>
        </div>
        <div id="content">
          <xsl:apply-templates />
        </div>
      </div>
      <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
      <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
      <script>
        <![CDATA[
        jQuery(function($) {
          function setHeights() {
            $("#toc").height($(window).height() - 20);
            $("#classes").height($("#toc").height() - $("#categories").height());
            $("#content").height($(window).height() - 20);
          }
          $("#toc").resizable({ handles: { e: "#toc-handle" } });
          $("#categories").resizable({
            handles: { e: "#categories-handle" },
            resize: function(e, ui) {
              $("#classes").height($("#toc").height() - ui.size.height);
            }
          }).on("scroll", function() {
            var negScrollTop = -$(this).scrollTop();
            $("#categories-handle").css("bottom", negScrollTop);
          });
          setHeights();
          $(window).on("resize", setHeights);
        });

        $("#categories").on("click", ".type-filter", function(e) {
          e.preventDefault();
          var type = $(this).attr("href").slice(1);
          $("#categories li").removeClass("active");
          if (type) {
            $(this).parent().addClass("active");
            $(".datasetClass").show().not("." + type).hide();
            $(".classes li").show().not("." + type).hide();
          } else {
            $(".datasetClass").show();
            $(".classes li").show();
          }
          $("#content").scrollTop(0);
        });

        $(".accordion").accordion({
          collapsible: true,
          active: false
          }).on( "accordionactivate", function( event, ui ) {
            $(this).accordion("refresh");
          } );

        // var manualBaseUrl = location.href.replace("ApiCommonModel",
        //     "ManualDeliveryExample").replace(/Model.*/, "")

        // $(".manual-example").each(function(idx, node) {
        //   var $node = $(node);
        //   $node.attr("href", manualBaseUrl + $node.data("dir"));
        // });
        ]]>
      </script>
    </body>
  </html>
</xsl:template>

<xsl:template match="datasetClasses" mode="categories">
  <!--
  <ul class="categories">
    <li><a class="type-filter" href="#all">All categories</a></li>
    <xsl:for-each select="datasetClass/datasetLoader/@type[not(.=preceding::datasetClass/datasetLoader/@type)]">
      <li><a class="type-filter" data-type="{.}" href="#{.}"><xsl:value-of select="."/></a></li>
    </xsl:for-each>
    <xsl:for-each select="datasetClass/@category[not(.=preceding::datasetClass/@category or .='' or .=preceding::datasetClass/datasetLoader/@type)]">
      <li><a class="type-filter" data-type="{.}" href="#{.}"><xsl:value-of select="."/></a></li>
    </xsl:for-each>
  </ul>
  -->
  <ul class="categories">
    <li><a class="type-filter" href="#">All categories</a></li>
    <!-- choose category if present, otherwise choose type -->
    <xsl:for-each select="
      datasetClass/@category[not(.=preceding::datasetClass/@category or .='' or .=preceding::datasetClass/datasetLoader/@type)] |
      datasetClass/datasetLoader/@type[../../@category='' and not(.=preceding::datasetClass/datasetLoader/@type)]
      ">
      <xsl:sort/>
      <li><a class="type-filter" href="#{.}"><xsl:value-of select="."/></a></li>
    </xsl:for-each>
  </ul>
</xsl:template>

<!-- found at www.jasinskionline.com/TechnicalWiki/XSL-to-Display-Raw-XML.ashx?AspxAutoDetectCookieSupport=1 -->
<xsl:template name="raw">
    &lt;<xsl:value-of select="name()"/> <xsl:for-each select="@*">
      <xsl:text> </xsl:text><xsl:value-of select="name()"/>="<xsl:value-of select='.'/>"</xsl:for-each>&gt;
    <xsl:for-each select="*">
      <div style="padding:0em 2em">
        <xsl:call-template name="raw"/>
      </div>
    </xsl:for-each>
    <xsl:if test="name() != 'prop'">
      <xsl:value-of select="text()"/>
    </xsl:if>
    &lt;/<xsl:value-of select="name()"/>&gt;
</xsl:template>

<xsl:template match="datasetClasses" mode="classes">
  <ul class="classes">
    <xsl:for-each select="datasetClass">
      <xsl:sort select="@class"/>
      <xsl:variable name="type">
        <xsl:choose>
          <xsl:when test="@category and @category != ''">
            <xsl:value-of select="@category" />
          </xsl:when>
          <xsl:when test="datasetLoader/@type and datasetLoader/@type != ''">
            <xsl:value-of select="datasetLoader/@type" />
          </xsl:when>
        </xsl:choose>
      </xsl:variable>

      <li class="{$type}"><a href="#{@class}"><xsl:value-of select="@class"/></a></li>
    </xsl:for-each>
  </ul>
</xsl:template>

<xsl:template match="datasetClasses" >

    <xsl:for-each select="datasetClass" >

      <xsl:variable name="type">
        <xsl:choose>
          <xsl:when test="@category and @category != ''">
            <xsl:value-of select="@category" />
          </xsl:when>
          <xsl:when test="datasetLoader/@type and datasetLoader/@type != ''">
            <xsl:value-of select="datasetLoader/@type" />
          </xsl:when>
        </xsl:choose>
      </xsl:variable>

      <div class="datasetClass {$type}">
        <a name="{@class}"/>
        <h2>Class: <xsl:value-of select="@class" /> </h2>

          <xsl:if test="@datasetFileHint and @datasetFileHint != ''">
            <div class="section">
              <h3 class="inline">Target Dataset File:</h3>
              <div><xsl:value-of select="@datasetFileHint"/> XML file</div>
            </div>
          </xsl:if>

          <div class="section">
            <h3 class="inline">Category:</h3>
            <div> <xsl:value-of select="$type"/> </div>
          </div>

          <div class="section">
            <h3 class="inline">GraphTemplateFile:</h3>
            <div> <xsl:value-of select="graphTemplateFile/@name" /> </div>
          </div>

          <xsl:if test="purpose and purpose != ''">
            <div class="section">
              <h3>Purpose</h3>
              <div> <xsl:value-of select="purpose" /> </div>
            </div>
          </xsl:if>

          <xsl:if test="qaNotes">
            <div class="section">
              <h3>QA Notes</h3>
              <div><xsl:value-of select="qaNotes"/></div>
            </div>
          </xsl:if>

          <div class="section">
            <h3>Properties</h3>
            <div>
              <table border="1">
               <tr><th>Property</th><th>Description</th></tr>
               <xsl:for-each select="prop" >
                 <tr>
                   <td><xsl:value-of select="@name" /></td>
                   <td><xsl:value-of select="." /></td>
                 </tr>
               </xsl:for-each>
             </table>

             <br/>
             <div class="accordion">
               <h3>XML template (paste this template into your dataset file)</h3>
               <div class="code">
                 &#160;&#160;&lt;dataset class="<xsl:value-of select="@class"/>"&gt;
                 <xsl:for-each select="prop" >
                   <div>
                     &#160;&#160;&#160;&#160;<xsl:call-template name="raw"/>
                   </div>
                 </xsl:for-each>
                 &#160;&#160;&lt;/dataset&gt;
               </div>
             </div>
           </div>
         </div>

         <xsl:if test="datasetLoader/manualGet">
           <div class="section">
             <h3>Manual Delivery</h3>
             <div>
               <xsl:for-each select="datasetLoader/manualGet">
                 <p>
                   <b>File location: </b>
                   <code><xsl:value-of select="@fileOrDir"/></code>
                 </p>

                 <xsl:if test="descriptionOfFinal">
                   <div class="section">
                     <h4>Requirements for <code>final/</code> dir:</h4>
                     <div><xsl:value-of select="descriptionOfFinal"/></div>
                   </div>
                 </xsl:if>

                 <xsl:if test="example">
                   <table border="1">
                     <tr><th>Example</th><th>Notes</th></tr>
                     <xsl:for-each select="example">
                       <!-- this href is a challenge.  it should be https://cbilsvn.pmacs.upenn.edu/svn/apidb/ManualDeliveryExample/XXXX/the_dir -->
                       <!-- where XXXX is either trunk or branches/some_branch_number, depending on where this .xsl file is in svn -->
                       <!-- and the_dir is the value of @dir -->
                       <tr><td><a class="manual-example" data-dir="{@dir}" href="http://devtools.eupathdb.org/datasetExamples/{@dir}" target="_blank"><xsl:value-of select="@dir"/></a></td><td><xsl:value-of select="."/></td></tr>
                     </xsl:for-each>
                   </table>
                 </xsl:if>
               </xsl:for-each>
             </div>
           </div>
         </xsl:if>

          <xsl:if test="datasetLoader">
            <br/>
            <div class="section">
              <h3>Dataset loader</h3>
              <div>
                <!--
                <table border="1">
                  <tr>
                    <th>DatasetName</th>
                    <th>Version</th>
                    <th>Scope</th>
                    <th>OrgAbbrev</th>
                  </tr>
                  <tr>
                    <td><code><xsl:value-of select="datasetLoader/@datasetName" /></code> </td>
                    <td><code><xsl:value-of select="datasetLoader/@version" /></code> </td>
                    <td><code><xsl:value-of select="datasetLoader/@scope" /></code> </td>
                    <td><code><xsl:value-of select="datasetLoader/@organismAbbrev" /></code> </td>
                  </tr>
                  <tr>
                    <th>manualGet: </th>
                    <td colspan="3"><code><xsl:value-of select="datasetLoader/manualGet/@fileOrDir" /></code></td>
                  </tr>
                  <tr>
                    <th>plugin: </th>
                    <td colspan="3"><code><xsl:value-of select="datasetLoader/@plugin" /></code></td>
                  </tr>
                  <tr>
                    <th>pluginArgs: </th>
                    <td colspan="3"><code><xsl:value-of select="datasetLoader/pluginArgs" /></code></td>
                  </tr>
                </table>
                -->
                <xsl:for-each select="datasetLoader">
                  <div class="accordion">
                    <h3>Dataset loader XML</h3>
                    <div class="code" style="padding-top:4px">
                      <xsl:call-template name="raw"/>
                    </div>
                  </div>
                </xsl:for-each>
              </div>
            </div>
          </xsl:if>

         <xsl:if test="templateInjector">
           <div class="section">
             <h3>Template Injectors</h3>
             <div>
               <xsl:for-each select="templateInjector">
                 <table border="1">
                   <tr><th>Name</th><th>Notes</th></tr>
                   <tr><td><xsl:value-of select="@name"/></td><td><xsl:value-of select="@notes"/></td></tr>
                 </table>
               </xsl:for-each>
             </div>
           </div>
         </xsl:if>

         <br/>
         <br/>
      </div>
    </xsl:for-each>
  </xsl:template >
</xsl:stylesheet >
  
  

