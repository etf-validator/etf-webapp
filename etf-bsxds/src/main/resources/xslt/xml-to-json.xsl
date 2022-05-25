<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:json="http://json.org/">

    <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <!--
       XSLTJSON v1.0.93.

       You can use these parameters to control the output by supplying them to
       stylesheet. Consult the manual of your XSLT processor for instructions
       on how to pass parameters to a stylesheet.

       * debug           -  Enable or disable the output of the temporary
                            XML tree used to generate JSON output.
       * skip-root       -  Skip the root XML element.
       * jsonp           -  Enable JSONP; the JSON output will be prepended with
                            the value of the jsonp parameter and wrapped in parentheses.

       Credits:
         Chick Markley (chick@diglib.org) - Octal number & numbers with terminating period.
         Torben Schreiter (Torben.Schreiter@inubit.com) - Suggestions for skip root and node list.
         Michael Nilsson - Bug report and unit tests for json:force-array feature.
         Frank Schwichtenberg - Namespace prefix name bug.
         Wilson Cheung - Bug report and fix for invalid number serialization.
         Danny Cohn - Bug report and fix for invalid floating point number serialization.

       Copyright:
        2006-2014, Bram Stein

        Licensed under the new BSD License.
        All rights reserved.
    -->
    <xsl:param name="debug" as="xs:boolean" select="false()"/>
    <xsl:param name="skip-root" as="xs:boolean" select="false()"/>

    <!--
      If you import or include the stylesheet in your own stylesheet you
      can use this function to transform any XML node to JSON.
    -->
    <xsl:function name="json:generate" as="xs:string">
        <xsl:param name="input" as="node()"/>

        <xsl:variable name="json-tree">
            <json:object>
                <xsl:copy-of select="json:create-node($input, false())"/>
            </json:object>
        </xsl:variable>

        <xsl:variable name="json-mtree">
            <xsl:choose>
                <xsl:when test="$skip-root">
                    <xsl:copy-of select="$json-tree/json:object/json:member/json:value/child::node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$json-tree"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="output">
            <xsl:text/><xsl:apply-templates select="$json-mtree" mode="json"/><xsl:text/>
        </xsl:variable>

        <xsl:sequence select="$output"/>
    </xsl:function>

    <!--
      Template to match the root node so that the stylesheet can also
      be used on the command line.
    -->
    <!--xsl:template match="/*">
        <xsl:choose>
            <xsl:when test="$debug">
                <xsl:variable name="json-tree">
                    <json:object>
                        <xsl:copy-of select="json:create-simple-node(.)"/>
                    </json:object>
                </xsl:variable>

                <debug>
                    <xsl:copy-of select="$json-tree"/>
                </debug>
                <xsl:apply-templates select="$json-tree" mode="json"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="json:generate(.)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template-->

    <!--
      All methods below are private methods and should not be used
      standalone.
    -->
    <xsl:template name="json:build-tree">
        <xsl:param name="input" as="node()"/>
        <json:object>
            <xsl:copy-of select="json:create-simple-node($input/child::node())"/>
        </json:object>
    </xsl:template>

    <xsl:function name="json:create-simple-node-member" as="node()">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="value"/>
        <json:member>
            <json:name><xsl:value-of select="$type"/></json:name>
            <json:value><xsl:copy-of select="$value"/></json:value>
        </json:member>
    </xsl:function>

    <xsl:function name="json:create-simple-node" as="node()*">
        <xsl:param name="node" as="node()"/>

        <xsl:copy-of select="json:create-simple-node-member('#name', $node/local-name())"/>
        <xsl:copy-of select="json:create-simple-node-member('#text', $node/child::text())"/>

        <xsl:variable name="empty-array">
            <json:array/>
        </xsl:variable>

        <xsl:variable name="children">
            <json:array>
                <xsl:for-each select="$node/@*">
                    <json:array-value>
                        <json:value>
                            <json:object>
                                <xsl:copy-of select="json:create-simple-node-member('#name', concat('@',./local-name()))"/>
                                <xsl:copy-of select="json:create-simple-node-member('#text', string(.))"/>
                                <xsl:copy-of select="json:create-simple-node-member('#children', $empty-array)"/>
                            </json:object>
                        </json:value>
                    </json:array-value>
                </xsl:for-each>
                <xsl:for-each select="$node/child::element()">
                    <json:array-value>
                        <json:value>
                            <json:object>
                                <xsl:copy-of select="json:create-simple-node(.)"/>
                            </json:object>
                        </json:value>
                    </json:array-value>
                </xsl:for-each>
            </json:array>
        </xsl:variable>
        <xsl:copy-of select="json:create-simple-node-member('#children', $children)"/>
    </xsl:function>

    <xsl:function name="json:create-node" as="node()">
        <xsl:param name="node" as="node()"/>
        <xsl:param name="in-array" as="xs:boolean"/>
        <xsl:choose>
            <xsl:when test="$in-array">
                <json:array-value>
                    <json:value>
                        <xsl:copy-of select="json:create-children($node)"/>
                    </json:value>
                </json:array-value>
            </xsl:when>
            <xsl:otherwise>
                <json:member>
                    <xsl:copy-of select="json:create-string($node)"/>
                    <json:value>
                        <xsl:copy-of select="json:create-children($node)"/>
                    </json:value>
                </json:member>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="json:create-children">
        <xsl:param name="node" as="node()"/>
        <xsl:choose>
            <xsl:when test="exists($node/child::text()) and count($node/child::node()) eq 1">
                <xsl:choose>
                    <xsl:when test="count($node/@*[not(../@json:force-array) or count(.|../@json:force-array)=2]) gt 0">
                        <json:object>
                            <xsl:copy-of select="json:create-attributes($node)"/>
                            <json:member>
                                <json:name>$</json:name>
                                <json:value><xsl:value-of select="$node"/></json:value>
                            </json:member>
                        </json:object>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$node"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="exists($node/child::text())">
                <xsl:choose>
                    <xsl:when test="count($node/@*[not(../@json:force-array) or count(.|../@json:force-array)=2]) gt 0">
                        <json:object>
                            <xsl:copy-of select="json:create-attributes($node)"/>
                            <json:member>
                                <json:name>$</json:name>
                                <json:value>
                                    <xsl:copy-of select="json:create-mixed-array($node)"/>
                                </json:value>
                            </json:member>
                        </json:object>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="json:create-mixed-array($node)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="exists($node/child::node()) or (count($node/@*[not(../@json:force-array) or count(.|../@json:force-array)=2]) gt 0)">
                <json:object>
                    <xsl:copy-of select="json:create-attributes($node)"/>
                    <xsl:for-each-group select="$node/child::node()" group-adjacent="local-name()">
                        <xsl:choose>
                            <xsl:when test="count(current-group()) eq 1 and (not(exists(./@json:force-array)) or ./@json:force-array eq 'false')">
                                <xsl:copy-of select="json:create-node(current-group()[1], false())"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <json:member>
                                    <json:name><xsl:value-of select="current-group()[1]/local-name()"/></json:name>
                                    <json:value>
                                        <json:array>
                                            <xsl:for-each select="current-group()">
                                                <xsl:copy-of select="json:create-node(.,true())"/>
                                            </xsl:for-each>
                                        </json:array>
                                    </json:value>
                                </json:member>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </json:object>
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="json:create-mixed-array" as="node()">
        <xsl:param name="node" as="node()"/>
        <json:array>
            <xsl:for-each select="$node/child::node()">
                <json:array-value>
                    <json:value>
                        <xsl:choose>
                            <xsl:when test="self::text()">
                                <xsl:value-of select="$node"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <json:object>
                                    <xsl:copy-of select="json:create-node(.,false())"/>
                                </json:object>
                            </xsl:otherwise>
                        </xsl:choose>
                    </json:value>
                </json:array-value>
            </xsl:for-each>
        </json:array>
    </xsl:function>

    <xsl:function name="json:create-string" as="node()">
        <xsl:param name="node" as="node()"/>
        <json:name><xsl:value-of select="$node/local-name()"/></json:name>
    </xsl:function>

    <xsl:function name="json:create-attributes" as="node()*">
        <xsl:param name="node" as="node()"/>
        <xsl:for-each select="$node/@*[not(../@json:force-array) or count(.|../@json:force-array)=2]">
            <json:member>
                <json:name><xsl:value-of select="local-name()"/></json:name>
                <json:value><xsl:value-of select="."/></json:value>
            </json:member>
        </xsl:for-each>
    </xsl:function>

    <!--
      These are output functions that transform the temporary tree
      to JSON.
    -->
    <xsl:template match="json:parameter" mode="json">
        <xsl:variable name="parameters"><xsl:apply-templates mode="json"/></xsl:variable>
        <xsl:value-of select="string-join($parameters/parameter, ', ')"/>
    </xsl:template>

    <xsl:template match="json:object" mode="json">
        <xsl:variable name="members"><xsl:apply-templates mode="json"/></xsl:variable>
        <parameter>
            <xsl:text/>{<xsl:text/>
            <xsl:value-of select="string-join($members/member,',')"/>
            <xsl:text/>}<xsl:text/>
        </parameter>
    </xsl:template>

    <xsl:template match="json:member" mode="json">
        <xsl:text/><member><xsl:apply-templates mode="json"/></member><xsl:text/>
    </xsl:template>
    
    <xsl:function name="json:encode-string" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:value-of>
            <xsl:for-each select="string-to-codepoints($in)">
                <xsl:choose>
                    <xsl:when test=". gt 65535">
                        <xsl:value-of select="concat('\u', json:hex4((. - 65536) idiv 1024 + 55296))"/>
                        <xsl:value-of select="concat('\u', json:hex4((. - 65536) mod 1024 + 56320))"/>
                    </xsl:when>
                    <xsl:when test=". = 34">\"</xsl:when>
                    <xsl:when test=". = 92">\\</xsl:when>
                    <xsl:when test=". = 08">\b</xsl:when>
                    <xsl:when test=". = 09">\t</xsl:when>
                    <xsl:when test=". = 10">\n</xsl:when>
                    <xsl:when test=". = 12">\f</xsl:when>
                    <xsl:when test=". = 13">\r</xsl:when>
                    <xsl:when test=". lt 32 or (. ge 127 and . le 160)">
                        <xsl:value-of select="concat('\u', json:hex4(.))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="codepoints-to-string(.)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:value-of>
    </xsl:function>
    
    <xsl:function name="json:hex4" as="xs:string">
        <xsl:param name="ch" as="xs:integer"/>
        <xsl:variable name="hex" select="'0123456789abcdef'"/>
        <xsl:value-of>
            <xsl:value-of select="substring($hex, $ch idiv 4096 + 1, 1)"/>
            <xsl:value-of select="substring($hex, $ch idiv 256 mod 16 + 1, 1)"/>
            <xsl:value-of select="substring($hex, $ch idiv 16 mod 16 + 1, 1)"/>
            <xsl:value-of select="substring($hex, $ch mod 16 + 1, 1)"/>
        </xsl:value-of>
    </xsl:function>

    <xsl:template match="json:name" mode="json">
        <xsl:text/>"<xsl:value-of select="."/>":<xsl:text/>
    </xsl:template>

    <xsl:template match="json:value" mode="json">
        <xsl:choose>
            <xsl:when test="node() and not(text())">
                <xsl:apply-templates mode="json"/>
            </xsl:when>
            <xsl:when test="text()">
                <xsl:choose>
                    <!-- Handle invalid JSON numbers like '01', '+1', '-01', '1.', and '.5' -->
                    <xsl:when test="normalize-space(.) ne . or not((string(.) castable as xs:integer and 
                        not(starts-with(string(.),'+') or starts-with(string(.),'-')) and not(starts-with(string(.),'0') and not(. = '0'))) 
                        or (string(.) castable as xs:decimal and not(starts-with(string(.),'+')) and not(starts-with(.,'-.')) and 
                        not(starts-with(.,'.')) and not(starts-with(.,'-0') and not(starts-with(.,'-0.'))) and not(ends-with(.,'.')) and 
                        not(starts-with(.,'0') and not(starts-with(.,'0.'))))) and not(. = 'false') and not(. = 'true') and not(. = 'null')">
                        <xsl:text/>"<xsl:value-of select="json:encode-string(.)"/>"<xsl:text/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text/><xsl:value-of select="."/><xsl:text/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text/>null<xsl:text/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="json:array-value" mode="json">
        <xsl:text/><value><xsl:apply-templates mode="json"/></value><xsl:text/>
    </xsl:template>

    <xsl:template match="json:array" mode="json">
        <xsl:variable name="values">
            <xsl:apply-templates mode="json"/>
        </xsl:variable>
        <xsl:text/>[<xsl:text/>
        <xsl:value-of select="string-join($values/value,',')"/>
        <xsl:text/>]<xsl:text/>
    </xsl:template>
</xsl:stylesheet>
