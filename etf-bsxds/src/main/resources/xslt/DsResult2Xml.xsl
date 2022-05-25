<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.interactive-instruments.de/etf/2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
    xmlns:etfAppinfo="http://www.interactive-instruments.de/etf/appinfo/1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="xs etfAppinfo" version="2.0">

    <xsl:output method="xml"/>

    <xsl:param name="serviceUrl" select="'https://localhost/etf-webapp/v2'"/>
    <xsl:param name="selection"/>
    <xsl:param name="offset" select="0"/>
    <xsl:param name="limit" select="-1"/>
    <xsl:param name="fields" as="xs:string" select="'*'"/>
    <xsl:variable name="fieldsParam" select="if (empty($fields) or $fields='*') then '' else $fields"/>
    <xsl:param name="maskSecrets" select="true()"/>
    
    <!-- Overwrite template defaults -->
    <xsl:param name="includeRefType" select="true()"/>
    <xsl:param name="hrefTypeEnding" select="'.xml'"/>

    <xsl:key name="ids" match="/etf:DsResultSet//*" use="@id"/>

    <xsl:key name="translationNames"
        match="/etf:DsResultSet/etf:translationTemplateBundles/etf:TranslationTemplateBundle/etf:translationTemplateCollections/etf:LangTranslationTemplateCollection"
        use="@name"/>

    <!-- =============================================================== -->
    <xsl:template match="/etf:DsResultSet">
        <xsl:element name="EtfItemCollection">
            
            <xsl:variable name="subSet" select="*[./*[1]/local-name() = $selection]"/>
            <xsl:variable name="returnedItems" select="count($subSet/*)"/>
            
            <!-- Collection attributes -->
            <xsl:attribute name="version">2.0</xsl:attribute>
            <xsl:if test="$fieldsParam=''">
                <xsl:attribute name="xsi:schemaLocation">http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/service/service.xsd</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="returnedItems" select="$returnedItems"/>
            <!-- page position -->
            <xsl:if test="number($limit) gt 0">
                <xsl:attribute name="position" select="format-number($offset div $limit, '#')"/>
            </xsl:if>
            
            <xsl:if test="not($fieldsParam='')">
                <xsl:comment>This is a partial response that contains only the filtered fields and may not validate against the service.xsd schema.</xsl:comment>
            </xsl:if>
            
            <!-- reference to this page -->
            <xsl:element name="etf:ref">
                <xsl:choose>
                    <xsl:when test="number($limit) gt 0">
                        <xsl:attribute name="href"
                            select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset, 'limit', $limit, 'fields', $fieldsParam))"
                        />
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- Set reference to the single included item -->
                        <xsl:attribute name="href"
                            select="etf:createUrl(concat($serviceUrl, '/', $selection, 's/', substring-after($subSet/*[1]/@id, 'EID'), $hrefTypeEnding ), ('fields', $fieldsParam))"
                        />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            
            <xsl:if test="number($limit) gt 0">
                <!-- reference to previous page -->
                <xsl:if test="number($offset - $limit) ge 0">
                    <xsl:element name="etf:previous">
                        <xsl:attribute name="href"
                            select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset - $limit, 'limit', $limit, 'fields', $fieldsParam))" 
                        />
                    </xsl:element>
                </xsl:if>
                <!-- reference to next page -->
                <xsl:if test="$returnedItems gt 0 and number($returnedItems) eq number($limit)">
                    <xsl:element name="etf:next">
                        <!-- Unknown if there are any items -->
                        <xsl:attribute name="href"
                            select="etf:createUrl(concat($serviceUrl, '/', $selection, 's', $hrefTypeEnding), ('offset', $offset + $limit, 'limit', $limit, 'fields', $fieldsParam))" 
                        />
                    </xsl:element>
                </xsl:if>
            </xsl:if>

            <xsl:if test="$returnedItems gt 0">
                <xsl:apply-templates select="$subSet"/>

                <!-- additional referencedItems -->
                <xsl:element name="referencedItems">
                    <xsl:apply-templates select="*[not(./*[1]/local-name() = $selection)]"/>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>
    
    <xsl:function name="etf:createUrl">
        <xsl:param name="baseUrl"/>
        <xsl:param name="keyValuePairs"/>
        <xsl:if test="(count($keyValuePairs) mod 2) ne 0">
            <xsl:message terminate="yes">Invalid number of arguments</xsl:message>
        </xsl:if>
        <xsl:variable name="params" select="string-join(for $i in 0 to count($keyValuePairs) div 2 
            return if (not(string($keyValuePairs[$i*2])='')) then 
                ('&amp;', string($keyValuePairs[($i*2)-1]), '=', string($keyValuePairs[$i*2])) else (), '')"/>
        <xsl:choose>
            <xsl:when test="not($params='')"><xsl:value-of select="concat($baseUrl, '?', $params)"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$baseUrl"/></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- =============================================================== -->
    <xsl:template name="etf:createReference">
        <xsl:param name="reference" as="xs:string"/>
        <xsl:param name="type" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="key('ids', $reference)">
                <xsl:if test="$includeRefType">
                    <xsl:attribute name="xsi:type">loc</xsl:attribute>
                </xsl:if>
                <xsl:attribute name="ref">
                    <xsl:value-of select="$reference"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$includeRefType">
                    <xsl:attribute name="xsi:type">ext</xsl:attribute>
                </xsl:if>
                <xsl:attribute name="href">
                    <xsl:value-of
                            select="concat($serviceUrl, '/', $type, 's/', substring-after($reference, 'EID'), $hrefTypeEnding)"
                    />
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
   

    <!-- =============================================================== -->
    <!-- filter etf:itemHash and etf:localPath -->
    <xsl:template priority="4"
        match="@* | /etf:DsResultSet//node()[not(self::etf:itemHash or self::etf:localPath)]">
        <xsl:copy>
            <xsl:apply-templates
                select="@* | node()[not(self::etf:itemHash or self::etf:localPath)]"/>
        </xsl:copy>
    </xsl:template>

    <!-- =============================================================== -->
    <xsl:template priority="7"
        match="*/etf:tag | */etf:testObject | */etf:testObjectType | */etf:testTaskResult | */etf:executableTestSuite | */etf:translationTemplateBundle">
        <xsl:element name="{name()}">
            <xsl:variable name="reference" select="@ref"/>
            <xsl:variable name="type"
                select="concat(upper-case(substring(local-name(), 1, 1)), substring(local-name(), 2))"/>
            <xsl:call-template name="etf:createReference">
                <xsl:with-param name="reference" select="$reference"/>
                <xsl:with-param name="type" select="$type"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <!-- =============================================================== -->
    <xsl:template priority="8"
        match="*/etf:translationTemplate | */etf:dependencies/etf:testCase | */etf:attachments/etf:attachment">
        <!-- The referenced items shall be included in the result -->
        <xsl:element name="{name()}">
            <xsl:variable name="local_reference" select="@ref"/>
            <xsl:if test="$includeRefType">
                <xsl:attribute name="xsi:type">loc</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="ref">
                <xsl:value-of select="$local_reference"/>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>

    <!-- =============================================================== -->
    <xsl:template match="*/etf:Attachment/etf:referencedData" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="testTaskResultId" select="../../../@id"/>
            <xsl:variable name="attachmentId" select="../@id"/>
            <xsl:attribute name="href">
                <xsl:value-of
                    select="concat($serviceUrl, '/TestTaskResults/', $testTaskResultId, '/Attachments/', $attachmentId)"
                />
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    
    <!-- =============================================================== -->
    <xsl:template match="*/etf:TestRun/etf:logPath" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="testRunId" select="../@id"/>
            <xsl:value-of
                select="concat($serviceUrl, '/TestRuns/', $testRunId, '/log')"
            />
        </xsl:element>
    </xsl:template>
    
    <!-- =============================================================== -->
    <xsl:template match="*/etf:TestObject/etf:ResourceCollection/etf:resource/@href" priority="8">
        <xsl:attribute name="{name()}" 
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:choose>
                <xsl:when test="starts-with(., 'file://')">
                    <xsl:variable name="testObjectId" select="../../../@id"/>
                    <xsl:value-of
                        select="concat($serviceUrl, '/TestObjects/', $testObjectId, '/data')"
                    />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
    </xsl:template>
    
    <!-- =============================================================== -->
    <xsl:template match="*/etf:testDriver" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="reference" select="@ref"/>
            <xsl:call-template name="etf:createReference">
                <xsl:with-param name="reference" select="$reference"/>
                <xsl:with-param name="type" select="'Component'"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <!-- =============================================================== -->
    <xsl:template match="*/etf:testItemType" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="reference" select="@ref"/>
            <xsl:call-template name="etf:createReference">
                <xsl:with-param name="reference" select="$reference"/>
                <xsl:with-param name="type" select="'TestItemType'"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:variable name="parentMapping">
        <entry key="TestModule">ExecutableTestSuite</entry>
        <entry key="TestCase">TestModule</entry>
        <entry key="TestStep">TestCase</entry>
        <entry key="TestAssertion">TestStep</entry>
        <entry key="TestModuleResult">TestTaskResult</entry>
        <entry key="TestCaseResult">TestModuleResult</entry>
        <entry key="TestStepResult">TestCaseResult</entry>
        <entry key="TestAssertionResult">TestStepResult</entry>
        <entry key="TestObjectType">TestObjectType</entry>
        <entry key="TranslationTemplateBundle">TranslationTemplateBundle</entry>
        <entry key="TestTask">TestRun</entry>
        <entry key="TestTaskResult">TestRun</entry>
    </xsl:variable>

    <!-- =============================================================== -->
    <xsl:template match="*/etf:parent" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="reference" select="@ref"/>
            <xsl:variable name="parent" select="../local-name()"/>
            <xsl:variable name="type" select="$parentMapping/*[@key = $parent]"/>
            <xsl:call-template name="etf:createReference">
                <xsl:with-param name="reference" select="$reference"/>
                <xsl:with-param name="type" select="$type"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>


    <xsl:variable name="resultedFromMapping">
        <entry key="TestTaskResult">ExecutableTestSuite</entry>
        <entry key="TestModuleResult">TestModule</entry>
        <entry key="TestCaseResult">TestCase</entry>
        <entry key="TestStepResult">TestStep</entry>
        <entry key="TestAssertionResult">TestAssertion</entry>
    </xsl:variable>

    <!-- =============================================================== -->
    <xsl:template match="*/etf:resultedFrom" priority="8">
        <xsl:element name="{name()}"
            xpath-default-namespace="http://www.interactive-instruments.de/etf/2.0">
            <xsl:variable name="reference" select="@ref"/>
            <xsl:variable name="parent" select="../local-name()"/>
            <xsl:variable name="type" select="$resultedFromMapping/*[@key = $parent]"/>
            <xsl:call-template name="etf:createReference">
                <xsl:with-param name="reference" select="$reference"/>
                <xsl:with-param name="type" select="$type"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
