<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
                xmlns:x="http://www.interactive-instruments.de/etf/2.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="x xs etf xsi"
                version="2.0">

    <xsl:output method="text" encoding="UTF-8"/>
    
    <xsl:strip-space elements="*" />
    <xsl:param name="delim" select="';'" />
    <xsl:param name="break" select="'&#xD;&#xA;'" />
    
    
    <xsl:param name="language">en</xsl:param>
    <xsl:variable name="allTemplates" select="//etf:translationTemplateBundles[1]/etf:TranslationTemplateBundle/etf:translationTemplateCollections[1]/etf:LangTranslationTemplateCollection/etf:translationTemplates[1]/etf:TranslationTemplate"/>
    <xsl:variable name="langTemplate" select="($allTemplates[@language = $language], $allTemplates[not(@language = $language) and @language = 'en'], $allTemplates[not(@language = $language or @language = 'en')])"/>
    <xsl:key name="translationKey" match="$langTemplate" use="@name"/>
    <xsl:key name="testAssertionKey"
        match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase/etf:testSteps[1]/etf:TestStep/etf:testAssertions[1]/etf:TestAssertion"
        use="@id"/>


    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="exists(//etf:message[starts-with(./@ref, 'TR.AdV.')])">
                <xsl:value-of select="concat('OA',$delim,'OID', $delim, 'TK-Schlüssel', $delim, 'Fehlermeldung', $delim, 'Ostwert', $delim, 'Nordwert', $break)"/>
                <xsl:call-template name="adv-message">
                    <xsl:with-param name="messages" select = "//etf:message[starts-with(./@ref, 'TR.AdV.') and etf:translationArguments/etf:argument[@token='OID']]" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="exists(//etf:message)">
                <xsl:choose>
                    <xsl:when test="exists(//etf:message[etf:translationArguments/etf:argument[@token='Objektart']])">
                        <xsl:value-of select="concat('Objektart',$delim,'Testkriterium',$delim,'Fehlermeldung', $break)"/>
                    </xsl:when>
                    <xsl:otherwise><xsl:value-of select="concat('Testkriterium',$delim,'Fehlermeldung', $break)"/></xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates select = "//etf:testTaskResults/etf:TestTaskResult/etf:testModuleResults/etf:TestModuleResult/etf:testCaseResults/etf:TestCaseResult/etf:testStepResults/etf:TestStepResult/etf:testAssertionResults/etf:TestAssertionResult[etf:messages]" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('Fehler', $delim, 'Interne Fehler', $break)"/>
                <xsl:value-of select="count(//etf:status[text()='FAILED'])"/>
                <xsl:value-of select="$delim"/>
                <xsl:value-of select="count(//etf:status[text()='INTERNAL_ERROR'])"/>
                <xsl:value-of select="$break"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="adv-message">
        <xsl:param name="messages"/>
        <xsl:for-each select="$messages">
            <xsl:value-of select="etf:non-empty(etf:translationArguments/etf:argument[@token='Objektart'])" />
            <xsl:value-of select="$delim" />
            <xsl:value-of select="etf:non-empty(etf:translationArguments/etf:argument[@token='OID'])" />
            <xsl:value-of select="$delim" />
            <xsl:variable name="schluessel" select="etf:non-empty(substring-after(@ref, 'TR.AdV.'))" />
            <xsl:value-of select="$schluessel" />
            <xsl:value-of select="$delim" />
            <xsl:choose>
                <xsl:when test="exists(./etf:translationArguments)">
                    <xsl:call-template name="translateMessageWithTokens">
                        <xsl:with-param name="templateId" select="./@ref"/>
                        <xsl:with-param name="translationArguments"
                            select="./etf:translationArguments"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="translateSimpleMessage">
                        <xsl:with-param name="templateId" select="./@ref"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$delim" />
            <!-- Ostwert -->
            <xsl:value-of select="etf:with-easting-zone(etf:translationArguments/etf:argument[@token='GeoRefCoord1'],
                etf:translationArguments/etf:argument[@token='SRID'])" />
            <!-- Nordwert -->
            <xsl:value-of select="etf:non-empty(etf:translationArguments/etf:argument[@token='GeoRefCoord2'])" />
            <xsl:value-of select="$break" />
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="etf:TestAssertionResult">
        <xsl:variable name="TestAssertion" select="key('testAssertionKey', ./etf:resultedFrom/@ref)"/>
        <xsl:if test="exists(//etf:message[etf:translationArguments/etf:argument[@token='Objektart']])">
            <xsl:value-of select="etf:non-empty(etf:translationArguments/etf:argument[@token='Objektart'])" />
            <xsl:value-of select="$delim" />
        </xsl:if>

        <xsl:variable name="label" select="$TestAssertion/etf:label" />
        <xsl:for-each select="etf:messages/etf:message">
            <xsl:value-of select="$label"/>
            <xsl:value-of select="$delim" />
            <xsl:choose>
                <xsl:when test="./etf:translationArguments">
                    <xsl:call-template name="translateMessageWithTokens">
                        <xsl:with-param name="templateId" select="./@ref"/>
                        <xsl:with-param name="translationArguments"
                            select="./etf:translationArguments"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="translateSimpleMessage">
                        <xsl:with-param name="templateId" select="./@ref"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$break" />
        </xsl:for-each>
    </xsl:template>
    
    <xsl:function name="etf:non-empty" as="xs:string">
        <xsl:param name="str" as ="xs:string?"/>
        <xsl:sequence select="if (not(normalize-space($str))) then
            'nicht belegt'
            else
            normalize-space($str)"/>
    </xsl:function>

    <xsl:function name="etf:with-easting-zone" as="xs:string">
        <xsl:param name="easting" as ="xs:string?"/>
        <xsl:param name="srid" as ="xs:string?"/>
        <xsl:sequence select="
            if (not(normalize-space($easting))) then
                'nicht belegt'
            else
                if ($srid eq '25832') then concat('32', normalize-space($easting))
                else if ($srid eq '25833') then concat('33', normalize-space($easting))
                else normalize-space($easting)
        "/>
    </xsl:function>


    <xsl:template name="translateSimpleMessage">
        <xsl:param name="templateId" as="xs:string"/>
        <xsl:variable name="template" select="key('translationKey', $templateId)"/>
        <xsl:value-of select="$template[1]/text()" />
    </xsl:template>
    
    <xsl:template name="translateMessageWithTokens">
        <xsl:param name="templateId" as="xs:string"/>
        <xsl:param name="translationArguments" as="node()"/>
        <xsl:variable name="template" select="key('translationKey', $templateId)"/>
        <xsl:variable name="str" select="$template[1]/text()"/>
        <xsl:value-of select="etf:non-empty(etf:replace-multi-tokens($str, $translationArguments[1]/etf:argument))"
        />
    </xsl:template>

    <xsl:function name="etf:replace-multi-tokens" as="xs:string?">
        <xsl:param name="arg" as="xs:string?"/>
        <xsl:param name="arguments" as="element()*"/>
        <!--
			temporary replace backslashes with a dagger &#8224; symbol and a
			$ with a double dagger &#8225; for replace() https://www.w3.org/TR/xpath-functions/#func-replace
		-->
        <xsl:sequence
                select="
            if (count($arguments) &gt; 0) then
            translate(
            etf:replace-multi-tokens(replace($arg, concat('\{', $arguments[1]/@token, '\}'),
            replace(
            translate(etf:if-absent($arguments[1]/text(), ''), '\$', '&#8224;&#8225;'), '([$])', '\\$1')), $arguments[position() &gt; 1])
            , '&#8224;&#8225;' , '\$')
            else
            $arg"
        />
    </xsl:function>
    <xsl:function name="etf:if-absent" as="item()*">
        <xsl:param name="arg" as="item()*"/>
        <xsl:param name="value" as="item()*"/>
        <xsl:sequence select="
            if (exists($arg)) then
            $arg
            else
            $value"/>
    </xsl:function>


</xsl:stylesheet>
