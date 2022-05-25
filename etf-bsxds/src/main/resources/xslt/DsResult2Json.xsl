<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.interactive-instruments.de/etf/2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
                xmlns:etfAppinfo="http://www.interactive-instruments.de/etf/appinfo/1.0"
                xmlns:json="http://json.org/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="xs etfAppinfo" version="2.0" xsi:schemaLocation="http://json.org/ ">
    
    <xsl:import href="DsResult2Xml.xsl"/>
    <xsl:import href="xml-to-json.xsl"/>

    <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="serviceUrl" select="'https://localhost/etf-webapp/v2'"/>
    
    <!-- Overwrite template defaults -->
    <xsl:param name="skip-root" as="xs:boolean" select="true()"/>
    <xsl:param name="includeRefType" select="false()"/>
    <xsl:param name="hrefTypeEnding" select="'.json'"/>

    <xsl:template match="/etf:DsResultSet">
        
        <xsl:variable name="collection">
        <xsl:element name="EtfItemCollection">
            <!-- Collection attributes -->
            <xsl:attribute name="version">2.0</xsl:attribute>
            
            <xsl:variable name="subSet" select="*[./*[1]/local-name() = $selection]"/>
            <xsl:variable name="returnedItems" select="count($subSet/*)"/>
            <xsl:attribute name="returnedItems" select="$returnedItems"/>

            <xsl:if test="number($limit) gt 0">
                <xsl:attribute name="position" select="format-number($offset div $limit, '#')"/>
            </xsl:if>
            <xsl:element name="etf:ref">
                <xsl:choose>
                    <xsl:when test="number($limit) gt 0">
                        <xsl:value-of select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset, 'limit', $limit, 'fields', $fieldsParam))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- Set reference to the single included item -->
                        <xsl:value-of select="etf:createUrl(concat($serviceUrl, '/', $selection, 's/', substring-after($subSet/*[1]/@id, 'EID'), $hrefTypeEnding ), ('fields', $fieldsParam))"
                        />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="number($limit) gt 0">
                <xsl:if test="number($offset - $limit) ge 0">
                    <xsl:element name="etf:previous">
                        <xsl:value-of select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset - $limit, 'limit', $limit, 'fields', $fieldsParam))"
                        />
                    </xsl:element>
                </xsl:if>
                <xsl:if test="$returnedItems gt 0 and number($returnedItems) eq number($limit)">
                    <xsl:element name="etf:next">
                        <!-- Unknown if there are any items -->
                        <xsl:value-of select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset + $limit, 'limit', $limit, 'fields', $fieldsParam))"
                        />
                    </xsl:element>
                </xsl:if>
            </xsl:if>

            <xsl:apply-templates select="$subSet"/>

            <xsl:if test="$returnedItems gt 0 and count(*[not(./*[1]/local-name() = $selection)]) gt 0">
                <!-- additional referencedItems -->
                <xsl:element name="referencedItems">
                    <xsl:apply-templates select="*[not(./*[1]/local-name() = $selection)]"/>
                </xsl:element>
            </xsl:if>
        </xsl:element>
        </xsl:variable>
        
        <!-- Thanks Bram -->
        <xsl:value-of select="json:generate( $collection )"/>
    </xsl:template>


</xsl:stylesheet>
