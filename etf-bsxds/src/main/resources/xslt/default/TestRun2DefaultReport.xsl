<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
	xmlns:x="http://www.interactive-instruments.de/etf/2.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="x xs etf xsi"
	version="2.0">
	<xsl:import href="jsAndCss.xsl"/>
	<xsl:import href="UtilityTemplates.xsl"/>
	<xsl:import href="encodeBase64.xsl"/>
	<xsl:param name="language">en</xsl:param>
	<xsl:param name="baseUrl" select="'https://localhost/etf'"/>
	<xsl:param name="serviceUrl" select="'https://localhost/etf/v2'"/>
	<xsl:param name="forceLocalResLoading">false</xsl:param>
	<xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" encoding="UTF-8"/>
	<xsl:key name="translation" match="x:lang/x:e" use="@key"/>
	<xsl:variable name="allLangs" select="document('ui-text.xml')/*/x:lang"/>
	<xsl:variable name="lang" select="($allLangs[@*:lang = $language], $allLangs[position() =1])[1]"/>
	<xsl:template match="x:lang">
		<xsl:param name="str"/>
		<xsl:variable name="result" select="key('translation', $str)"/>
		<xsl:choose>
			<xsl:when test="$result">
				<xsl:value-of select="$result"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$str"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- VERSION (todo: integrate into build process) yy-mm-dd -->
	<xsl:variable name="reportVersion">2.2.0-b191119</xsl:variable>
	<!-- Create lookup tables for faster id lookups -->
	<xsl:key name="testSuiteKey"
		match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite" use="@id"/>
	<xsl:key name="testModuleKey"
		match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite/etf:testModules[1]/etf:TestModule"
		use="@id"/>
	<xsl:key name="testCaseKey"
		match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase"
		use="@id"/>
	<xsl:key name="testStepKey"
		match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase/etf:testSteps[1]/etf:TestStep"
		use="@id"/>
	<xsl:key name="testAssertionKey"
		match="//etf:executableTestSuites[1]/etf:ExecutableTestSuite/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase/etf:testSteps[1]/etf:TestStep/etf:testAssertions[1]/etf:TestAssertion"
		use="@id"/>
	<xsl:key name="testItemTypeKey" match="//etf:testItemTypes[1]/etf:TestItemType"
		use="@id"/>
	<xsl:key name="testObjectKey" match="//etf:testObjects[1]/etf:TestObject"
		use="@id"/>
	<xsl:key name="testObjectTypeKey"
		match="//etf:testObjectTypes[1]/etf:TestObjectType" use="@id"/>
	<xsl:variable name="allTemplates" select="//etf:translationTemplateBundles[1]/etf:TranslationTemplateBundle/etf:translationTemplateCollections[1]/
		etf:LangTranslationTemplateCollection/etf:translationTemplates[1]/etf:TranslationTemplate"/>
	<xsl:variable name="langTemplate" select="($allTemplates[@language = $language], $allTemplates[not(@language = $language) and @language = 'en'], $allTemplates[not(@language = $language or @language = 'en')])"/>
	<xsl:key name="translationKey" match="$langTemplate" use="@name"/>
	<xsl:key name="testTaskKey"
		match="//etf:testRuns[1]/etf:TestRun/etf:testTasks[1]/etf:TestTask" use="@id"/>
	<xsl:key name="testTaskResultKey"
		match="//etf:testTaskResults[1]/etf:TestTaskResult" use="@id"/>
	<xsl:key name="attachmentsKey"
		match="//etf:testTaskResults[1]/etf:TestTaskResult/etf:attachments[1]/etf:Attachment"
		use="@id"/>
	<!-- get test case result by test case id -->
	<xsl:key name="testCaseResultKey"		
		match="//etf:testTaskResults[1]/etf:TestTaskResult/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult"
		use="etf:resultedFrom/@ref"/>

	<xsl:variable name="testRun" select="//etf:testRuns[1]/etf:TestRun[1]"/>
	<xsl:variable name="testTaskResults"
		select="//etf:testTaskResults[1]/etf:TestTaskResult"/>
	<xsl:variable name="executableTestSuites"
		select="//etf:executableTestSuites[1]/etf:ExecutableTestSuite"/>
	<xsl:variable name="testTasks" select="//etf:testTasks[1]/etf:TestTask"/>
	<xsl:variable name="testObjects"
		select="key('testObjectKey', $testTaskResults/etf:testObject[1]/@ref)"/>
	<xsl:variable name="statisticAttachments"
		select="$testTaskResults/etf:attachments[1]/etf:Attachment[@type = 'StatisticalReport'][1]"/>
	<xsl:variable name="logAttachment" select="$testTaskResults/etf:attachments[1]/etf:Attachment[@type = 'LogFile']"/>
	<xsl:variable name="htmlAttachment" select="$testTaskResults/etf:attachments[1]/etf:Attachment[@type = 'HtmlFile']"/>
	<xsl:variable name="fileAttachments" select="$testTaskResults/etf:attachments[1]/etf:Attachment[@type = 'File']"/>
	
	<xsl:variable name="textAreaJqmClass">ui-input-text ui-shadow-inset ui-body-inherit ui-corner-all ui-mini ui-textinput-autogrow</xsl:variable>
	<xsl:variable name="collapsibleJqmClass">ui-collapsible ui-collapsible-inset ui-corner-all ui-collapsible-collapsed</xsl:variable>
	<xsl:variable name="collapsibleHeadingJqmClass">ui-collapsible-heading-toggle ui-btn ui-btn-icon-left</xsl:variable>
	<xsl:variable name="collapsibleContentJqmClass">ui-collapsible-content ui-collapsible-content-collapsed</xsl:variable>
	
	<!-- Test Report -->
	<!-- ########################################################################################## -->
	<xsl:template match="/*[self::etf:DsResultSet or self::etf:EtfItemCollection]">
		<html lang="{$language}" class="ui-mobile ui-mobile-rendering">
			<head>
				<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
				<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<meta name="web_author" content="interactive instruments GmbH"/>
				<title>
					<xsl:value-of select="$lang/x:e[@key = 'Title']"/>
				</title>
				<!-- Include Styles and Javascript functions -->
				<xsl:call-template name="jsfdeclAndCss"/>
			</head>
			<body class="ui-mobile-viewport ui-overlay-a">
				<div data-role="header">
					<h1>
						<xsl:value-of select="./etf:testRuns[1]/etf:TestRun[1]/etf:label/text()" />
					</h1>
					<a class="ui-link" href="{$baseUrl}/#test-reports" data-ajax="false" data-icon="back"
						data-iconpos="notext"/>
				</div>
				<div data-role="content">
					<div class="ui-grid-b">
						<div class="ui-block-a">
							<xsl:call-template name="reportInfo"/>
						</div>
						<div class="ui-block-b">
							<xsl:call-template name="statistics"/>
						</div>
						<div class="ui-block-c">
							<xsl:call-template name="controls"/>
						</div>
					</div>
					<xsl:apply-templates select="$testTasks[1]//etf:ArgumentList[1]"/>
					<!-- Test object -->
					<xsl:apply-templates select="$testObjects"/>
					<!-- Additional statistics provided by the test project -->
					<xsl:apply-templates select="$statisticAttachments[1]"/>
					<!-- Logging provided by the test project -->
					<xsl:apply-templates select="$logAttachment"/>
					<!-- Html files with additonal information -->
					<xsl:apply-templates select="$htmlAttachment"/>
					<!-- Files with additonal information -->
					<xsl:apply-templates select="$fileAttachments"/>
					<!-- Test Suite Results -->
					<xsl:apply-templates select="$testTaskResults"/>
				</div>
				<xsl:call-template name="footer"/>
			</body>
		</html>
	</xsl:template>
	<!-- General report information-->
	<!-- ########################################################################################## -->
	<xsl:template name="reportInfo">
		<div id="rprtInfo">
			<table>
				<tbody>
				<tr class="ReportDetail">
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'PublicationLocation']"/>
					</td>
					<td>
						<a class="ui-link" href="{$serviceUrl}/TestRuns/{$testRun/@id}.html?lang={$language}"
							data-ajax="false">
							<xsl:value-of select="$lang/x:e[@key = 'PublicationLocationLink']"/>
						</a>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
					</td>
					<td>
						<xsl:value-of select="$lang/x:e[@key = $testRun/@status ]"/>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Started']"/>
					</td>
					<td>
						<xsl:call-template name="formatDate">
							<xsl:with-param name="DateTime"
								select="$testTaskResults/etf:startTimestamp"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
					</td>
					<td>
						<xsl:call-template name="formatDuration">
							<xsl:with-param name="ms"
								select="sum($testTaskResults/etf:duration)"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr class="ReportDetail">
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'ReportVersion']"/>
					</td>
					<td>
						<xsl:value-of select="$reportVersion"/>
					</td>
				</tr>
				<tr class="ReportDetail">
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Log']"/>
					</td>
					<td>
						<a class="ui-link" href="{$serviceUrl}/TestRuns/{substring-after ($testRun/@id, 'EID')}.log"
						   data-ajax="false">
						<xsl:value-of select="$lang/x:e[@key = 'LogLink']"/>
						</a>
					</td>
				</tr>
				</tbody>
			</table>
		</div>
	</xsl:template>
	<!-- Short Statistics -->
	<!-- ########################################################################################## -->
	<xsl:template name="statistics">
		<div id="rprtStatistics">
			<table id="my-table">
				<thead>
					<tr>
						<th/>
						<th>
							<xsl:value-of select="$lang/x:e[@key = 'Count']"/>
						</th>
						<th>
							<xsl:value-of select="$lang/x:e[@key = 'Skipped']"/>
						</th>
						<th>
							<xsl:value-of select="$lang/x:e[@key = 'Failed']"/>
						</th>
						<th>
							<xsl:value-of select="$lang/x:e[@key = 'Warning']"/>
						</th>
						<th>
							<xsl:value-of select="$lang/x:e[@key = 'Manual']"/>
						</th>
					</tr>
				</thead>
				<tbody>
					<!-- TEST SUITE STATS-->
					<xsl:if test="$executableTestSuites[etf:label ne 'IGNORE']">
						<xsl:variable name="results" select="$testTaskResults"/>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'TestSuites']"/>
							</td>
							<td>
								<xsl:value-of select="count($results)"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'SKIPPED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'FAILED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'WARNING'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'PASSED_MANUAL'])"
								/>
							</td>
						</tr>
					</xsl:if>
					<!-- TEST MODULE STATS-->
					<xsl:if
						test="$executableTestSuites/etf:testModules[1]/etf:TestModule[etf:label ne 'IGNORE']">
						<xsl:variable name="results"
							select="$testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult"/>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'TestModules']"/>
							</td>
							<td>
								<xsl:value-of select="count($results)"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'SKIPPED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'FAILED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'WARNING'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'PASSED_MANUAL'])"
								/>
							</td>
						</tr>
					</xsl:if>
					<!-- TEST CASES STATS-->
					<xsl:if
						test="$executableTestSuites/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase[etf:label ne 'IGNORE']">
						<xsl:variable name="results"
							select="$testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult"/>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'TestCases']"/>
							</td>
							<td>
								<xsl:value-of select="count($results)"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'SKIPPED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'FAILED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'WARNING'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'PASSED_MANUAL'])"
								/>
							</td>
						</tr>
					</xsl:if>
					<!-- TEST STEPS STATS-->
					<xsl:if
						test="$executableTestSuites/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase/etf:testSteps[1]/etf:TestStep[etf:label ne 'IGNORE']">
						<xsl:variable name="results"
							select="($testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult[not(/etf:status='SKIPPED')]/etf:testStepResults[1]/etf:TestStepResult[not(etf:resultedFrom/@href)],
							$testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult[not(/etf:status='SKIPPED')]/etf:testStepResults[1]/etf:TestStepResult[not(etf:resultedFrom/@href)]/etf:invokedTests[1]//etf:TestStepResult[not(etf:resultedFrom/@href)])"/>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'TestSteps']"/>
							</td>
							<td>
								<xsl:value-of select="count($results)"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'SKIPPED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'FAILED'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'WARNING'])"/>
							</td>
							<td>
								<xsl:value-of select="count($results[etf:status = 'PASSED_MANUAL'])"
								/>
							</td>
						</tr>
					</xsl:if>
					<!-- TEST ASSERTIONS STATS-->
					<xsl:variable name="results"
						select="($testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult/etf:testStepResults[1]/etf:TestStepResult/etf:testAssertionResults[1]/etf:TestAssertionResult,
							$testTaskResults/etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult[not(/etf:status='SKIPPED')]/etf:testStepResults[1]/etf:TestStepResult/etf:invokedTests[1]//etf:testAssertionResults/etf:TestAssertionResult)"/>
					<tr>
						<td>
							<xsl:value-of select="$lang/x:e[@key = 'TestAssertions']"/>
						</td>
						<td>
							<xsl:value-of select="count($results)"/>
						</td>
						<td>
							<xsl:value-of select="count($results[etf:status = 'SKIPPED'])"/>
						</td>
						<td>
							<xsl:value-of select="count($results[etf:status = 'FAILED'])"/>
						</td>
						<td>
							<xsl:value-of select="count($results[etf:status = 'WARNING'])"/>
						</td>
						<td>
							<xsl:value-of select="count($results[etf:status = 'PASSED_MANUAL'])"/>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</xsl:template>
	<!-- Properties used in test run -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:ArgumentList">
		<xsl:if test="exists(etf:arguments/etf:argument[@name ne ''])">
		<div id="rprtParameters" data-role="collapsible" data-collapsed-icon="info"
			class="ReportDetail">
			<h3>
				<xsl:value-of select="$lang/x:e[@key = 'Parameters']"/>
			</h3>
			<table>
				<tbody>
					<xsl:for-each select="etf:arguments/etf:argument[@name ne '']">
					<xsl:if test="normalize-space(./text())">
						<tr>
							<td>
								<xsl:call-template name="translateParameterName">
									<xsl:with-param name="parameterName" select="./@name"/>
								</xsl:call-template>
							</td>
							<td>
								<xsl:value-of select="./text()"/>
							</td>
						</tr>
					</xsl:if>
				</xsl:for-each>
				</tbody>
			</table>
		</div>
		</xsl:if>
	</xsl:template>
	<!-- TestObject -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestObject">
		<div id="rprtTestobject" data-role="collapsible" data-collapsed-icon="info"
			class="DoNotShowInSimpleView"><xsl:variable name="TestObject" select="."
					/><h3><xsl:value-of select="$lang/x:e[@key = 'TestObject']"/>: <xsl:value-of
					select="$TestObject/etf:label"/></h3>
			<xsl:if
				test="$TestObject/etf:description and normalize-space($TestObject/etf:description/text()) ne ''">
				<p><xsl:value-of select="$TestObject/etf:description/text()" disable-output-escaping="yes"/></p>
			</xsl:if>
			
			<xsl:if test="$TestObject/etf:remoteResource/text() and not($TestObject/etf:remoteResource/text()='http://nowhere')">
				<a target="_blank" href="{$TestObject/etf:remoteResource}"><xsl:value-of select="$TestObject/etf:remoteResource"/></a>
			</xsl:if>
			
			<xsl:if test="$TestObject/etf:Properties/etf:property[@name= 'files']">
				<p><xsl:value-of select="$lang/x:e[@key = 'files']"/>: <xsl:value-of select="$TestObject/etf:Properties/etf:property[@name= 'files']"/></p>
			</xsl:if>
			<xsl:if test="$TestObject/etf:Properties/etf:property[@name= 'sizeHR']">
				<p><xsl:value-of select="$lang/x:e[@key = 'sizeHR']"/>: <xsl:value-of select="$TestObject/etf:Properties/etf:property[@name= 'sizeHR']"/></p>
			</xsl:if>
			
			<p><xsl:value-of select="$lang/x:e[@key = 'TestObjectTypes']"/>: </p>
			<ul>
				<xsl:for-each select="$TestObject/etf:testObjectTypes/etf:testObjectType">
					<xsl:variable name="TestObjectType" select="key('testObjectTypeKey', ./@ref)" />
					<li><xsl:value-of select="$TestObjectType/etf:label/text()"/>  (<xsl:value-of select="$TestObjectType/etf:description/text()" disable-output-escaping="yes"/>)</li>
					<xsl:for-each select="$TestObjectType/etf:subTypes/etf:testObjectType">
						<xsl:variable name="TestObjectSubType" select="key('testObjectTypeKey', ./@ref)[1]" />
						<li><xsl:value-of select="$TestObjectSubType/etf:label/text()"/>  (<xsl:value-of select="$TestObjectSubType/etf:description/text()" disable-output-escaping="yes"/>)</li>
					</xsl:for-each>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>
	
	<!-- Attachments -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:Attachment" priority="1">
		<xsl:variable name="id" select="./@id"/>
		<xsl:choose>
			<xsl:when test="exists(./etf:referencedData/@href)">
				<!--xsl:choose>
					<xsl:when test="unparsed-text-available(./etf:referencedData/@href, 'UTF-8')">
						<xsl:value-of select="unparsed-text(./etf:referencedData/@href, 'UTF-8')"/>
					</xsl:when>
					<xsl:otherwise>
						<pre>Referenced data not available</pre>
					</xsl:otherwise>
				</xsl:choose-->
				<xsl:variable name="testTaskResultId" select="../../@id"/>
				<xsl:value-of select="concat($serviceUrl, '/TestTaskResults/', $testTaskResultId, '/Attachments/', $id)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="convertBase64ToAscii">
					<xsl:with-param name="base64String" select="./etf:embeddedData" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- StatisticalReport (Attachment) -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:Attachment[@type = 'StatisticalReport']" priority="4">
		<xsl:variable name="stat" select="document(./etf:referencedData/@href)/etf:StatisticalReportTable"/>
		<xsl:if test="$stat">
			<xsl:choose>
				<xsl:when test="starts-with($stat/etf:type/@ref, 'file') and unparsed-text-available($stat/etf:type/@ref)">
					<xsl:variable name="tableType" select="parse-xml(unparsed-text($stat/etf:type/@ref))"/>
					<xsl:if test="$tableType and $tableType/etf:StatisticalReportTableType/etf:columnHeaderLabels/etf:label">
						<xsl:variable name="headerLabels" select="$tableType/etf:StatisticalReportTableType/etf:columnHeaderLabels/etf:label"/>
						<div id="rprtAdditionalStatistics" data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView statistical-report-table">
							<h3><xsl:value-of select="./etf:label"/></h3>
							<table>
								<caption><xsl:value-of select="./etf:label"/></caption>
								<tbody>
									<tr>
										<xsl:for-each select="$headerLabels">
											<!-- <xsl:value-of select="$lang/x:e[@key = .]"/> -->
											<th><xsl:value-of select="."/></th>
										</xsl:for-each>
									</tr>
									<xsl:for-each select="$stat/etf:entries/etf:entry">
										<tr>
											<xsl:for-each select="tokenize(text(), ';')">
												<td><xsl:value-of select="normalize-space(.)"/></td>
											</xsl:for-each>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
						</div>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<!-- Legacy -->
					<div id="rprtStatReport" data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView">
						<h3><xsl:value-of select="./etf:label"/></h3>
						<table>
							<tbody>
								<tr>
									<th><xsl:value-of select="$lang/x:e[@key = 'Type']"/></th>
									<th><xsl:value-of select="$lang/x:e[@key = 'Count']"/></th>
								</tr>
								<xsl:for-each select="$stat/etf:entries/etf:entry">
									<tr>
										<td><xsl:value-of select="normalize-space(substring-before(text(),';'))"/></td>
										<td><xsl:value-of select="normalize-space(substring-after(text(),';'))"/></td>
									</tr>
								</xsl:for-each>
							</tbody>
						</table>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<!-- ETS LogFile (Attachment) -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:Attachment[@type = 'LogFile']" priority="4">
		<xsl:choose>
			<xsl:when test="unparsed-text-available(./etf:referencedData/@href, 'UTF-8')">
				<xsl:variable name="log" select="unparsed-text(./etf:referencedData/@href, 'UTF-8')"/>	
				<xsl:if test="$log">
					<xsl:variable name="TestSuite" select="key('testSuiteKey', ../../etf:resultedFrom/@ref)"/>
					<div id="rprtLogFile" data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView">
						<h3><xsl:value-of select="$lang/x:e[@key = 'LogFile']"/><xsl:if test="$TestSuite">: <xsl:value-of select="$TestSuite/etf:label"/></xsl:if></h3>
						<pre><xsl:value-of select="$log" disable-output-escaping="yes"/></pre>
					</div>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="TestSuite" select="key('testSuiteKey', ../../etf:resultedFrom/@ref)"/>
				<div id="rprtLogFile" data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView">
					<h3><xsl:value-of select="$lang/x:e[@key = 'LogFile']"/><xsl:if test="$TestSuite">: <xsl:value-of select="$TestSuite/etf:label"/></xsl:if></h3>
					<pre>Log path not available</pre>
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- HTML file (Attachment) -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:Attachment[@type = 'HtmlFile']" priority="4">
		<xsl:if test="exists(./etf:referencedData/@href)">
			<xsl:variable name="id" select="./@id"/>
			<xsl:variable name="testTaskResultId" select="../../@id"/>
			<xsl:variable name="href" select="concat($serviceUrl, '/TestTaskResults/', $testTaskResultId, '/Attachments/', $id)"/>
			<div data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView">
				<h3><xsl:value-of select="./etf:label"/></h3>
				<a target="_blank" rel="noopener noreferrer" href="{$href}"><xsl:value-of select="$lang/x:e[@key = 'OpenInNewTab']"/></a>
			</div>
		</xsl:if>
	</xsl:template>

	<!-- File (Attachment) -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:Attachment[@type = 'File']" priority="4">
		<xsl:if test="exists(./etf:referencedData/@href)">
			<xsl:variable name="id" select="./@id"/>
			<xsl:variable name="testTaskResultId" select="../../@id"/>
			<xsl:variable name="href" select="concat($serviceUrl, '/TestTaskResults/', $testTaskResultId, '/Attachments/', $id)"/>
			<xsl:variable name="label" select="./etf:label" />
			<div data-role="collapsible" data-collapsed-icon="info" class="DoNotShowInSimpleView">
				<h3><xsl:value-of select="$label"/></h3>
				<xsl:choose>
					<xsl:when test="./etf:mimeType='text/csv'">
						<xsl:variable name="testRunLabel" select="$testRun/etf:testRuns[1]/etf:TestRun[1]/etf:label/text()" />
						<xsl:variable name="downloadLabel" select="translate( concat($testRunLabel, '_Attachment_', $label, '.csv'), '&quot;&amp;!ßäöüÄÖÜ/\\?%*:|,', '_')"/>
						<a download="{$downloadLabel}" href="{$href}"><xsl:value-of select="$lang/x:e[@key = 'DownloadFile']"/></a>
					</xsl:when>
					<xsl:otherwise>
						<a href="{$href}"><xsl:value-of select="$lang/x:e[@key = 'DownloadFile']"/></a>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		</xsl:if>
	</xsl:template>
	
	<!-- Test suite result information -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestTaskResult">
		<xsl:variable name="TestSuite" select="key('testSuiteKey', ./etf:resultedFrom/@ref)"/>
		<xsl:if test="not($TestSuite)">
			<xsl:message terminate="yes">ERROR: Executable Test Suite <xsl:value-of select="./etf:resultedFrom/@ref"/> not found</xsl:message>
		</xsl:if>
		<xsl:variable name="resultItem" select="."/>
		<xsl:variable name="internalError" select="exists($resultItem/etf:errorMessage)"/>
		
		<!-- Order by TestSuites -->
		<div class="TestSuite" data-role="collapsible" data-theme="e" data-content-theme="e">
			<xsl:attribute name="data-theme">
				<xsl:choose>
					<xsl:when test="$internalError">e</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED'">h</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'">j</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'FAILED'">i</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'WARNING'">j</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INFO'">j</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'SKIPPED'">j</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'">f</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'">e</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INTERNAL_ERROR'">e</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:attribute name="data-content-theme">
				<xsl:choose>
					<xsl:when test="$internalError">e</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED'">h</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'">g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'FAILED'">i</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'WARNING'">g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INFO'">g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'SKIPPED'">g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'">f</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'">e</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INTERNAL_ERROR'">e</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<h2>
				<xsl:value-of select="$TestSuite/etf:label"/>
				<div class="ui-li-count">
					<xsl:variable name="FailedCount">
						<xsl:choose>
							<xsl:when
								test="$TestSuite/etf:testModules/etf:TestModule[etf:label ne 'IGNORE']">
								<xsl:value-of
									select="count(./etf:testModuleResults[1]/etf:TestModuleResult[etf:status = 'FAILED'])"
								/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="count(./etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult[etf:status = 'FAILED'])"
								/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:variable name="Count">
						<xsl:choose>
							<xsl:when
								test="$TestSuite/etf:testModules/etf:TestModule[etf:label ne 'IGNORE']">
								<xsl:value-of
									select="count(./etf:testModuleResults[1]/etf:TestModuleResult)"
								/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="count(./etf:testModuleResults[1]/etf:TestModuleResult/etf:testCaseResults[1]/etf:TestCaseResult)"
								/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:if test="$FailedCount &gt; 0"><xsl:value-of
							select="$lang/x:e[@key = 'FAILED']"/>: <xsl:value-of
							select="$FailedCount"/> / </xsl:if>
					<xsl:value-of select="$Count"/>
				</div>
			</h2>
			<xsl:if
				test="$TestSuite/etf:description and normalize-space($TestSuite/etf:description/text()[1]) ne ''">
				<xsl:value-of select="$TestSuite/etf:description/text()"
					disable-output-escaping="yes"/>
				<br/>
			</xsl:if>
			<br/>
			<!-- General data about test result and test case -->
			<table>
				<tbody>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
					</td>
					<td>
						<xsl:value-of select="$lang/x:e[@key = $resultItem/etf:status]"/>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
					</td>
					<td>
						<xsl:call-template name="formatDuration">
							<xsl:with-param name="ms" select="./etf:duration[1]/text()"/>
						</xsl:call-template>
					</td>
				</tr>
				<tr class="ReportDetail">
					<td><xsl:value-of select="$lang/x:e[@key = 'TestSuite']"/> ID</td>
					<td>
						<xsl:value-of select="$TestSuite/@id"/>
					</td>
				</tr>
				<xsl:call-template name="itemData">
					<xsl:with-param name="Node" select="$TestSuite"/>
				</xsl:call-template>
				<xsl:call-template name="properties">
					<xsl:with-param name="Node" select="$TestSuite"/>
				</xsl:call-template>
				</tbody>
			</table>
			<br/>
			
			<xsl:choose>
				<xsl:when test="$internalError">
					<h3>The Test Suite was not executed because the Test Driver returned an error.</h3>
					<label for="error.{$resultItem/@id}">Error message</label>
					<textarea id="error.{$resultItem/@id}" data-mini="true" readonly="readonly" class="{$textAreaJqmClass}">
						<xsl:value-of select="$resultItem/etf:errorMessage"/>
					</textarea>
					<p>Please contact the system administrator if the problem persists.</p>
				</xsl:when>
				<xsl:otherwise>
					<!-- TestModule result information -->
					<xsl:apply-templates select="./etf:testModuleResults[1]/etf:TestModuleResult"/>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>
	<!-- Test module result information -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestModuleResult">
		<xsl:variable name="TestModule" select="key('testModuleKey', ./etf:resultedFrom/@ref)"/>
		<xsl:variable name="resultItem" select="."/>
		<xsl:choose>
			<xsl:when test="$TestModule/etf:label[1]/text() = 'IGNORE'">
				<div class="TestModulePlaceHolder">
					<xsl:apply-templates select="./etf:testCaseResults[1]/etf:TestCaseResult"/>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<!-- Order by TestModules -->
				<div data-role="collapsible" data-theme="e"
					data-content-theme="e">
					<xsl:attribute name="data-theme">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'">h</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'">j</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'">i</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'">j</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'">j</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'">j</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'">f</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'">e</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:attribute name="data-content-theme">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'">h</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'">g</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'">i</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'">g</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'">g</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'">g</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'">f</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'">e</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'">TestModule SuccessfulTestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'">TestModule ManualTestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'">TestModule FailedTestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'">TestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'">TestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'">TestModule SkippedTestModule</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'">TestModule SuccesfulTestModule DoNotShowInSimpleView</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'">TestModule</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:variable name="FailedTestCaseCount"
						select="count(./etf:testCaseResults[1]/etf:TestCaseResult[etf:status = 'FAILED'])"/>
					<h2>
						<xsl:value-of select="$TestModule/etf:label"/>
						<div class="ui-li-count">
							<xsl:if test="$FailedTestCaseCount &gt; 0"><xsl:value-of
									select="$lang/x:e[@key = 'FAILED']"/>: <xsl:value-of
									select="$FailedTestCaseCount"/> / </xsl:if>
							<xsl:value-of
								select="count(./etf:testCaseResults[1]/etf:TestCaseResult)"/>
						</div>
					</h2>
					<xsl:if
						test="$TestModule/etf:description and normalize-space($TestModule/etf:description/text()) ne ''">
						<xsl:value-of select="$TestModule/etf:description/text()"
							disable-output-escaping="yes"/>
						<br/>
					</xsl:if>
					<br/>
					<table>
						<tbody>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
							</td>
							<td>
								<xsl:value-of select="$lang/x:e[@key = $resultItem/etf:status[1]]"/>
							</td>
						</tr>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
							</td>
							<td>
								<xsl:call-template name="formatDuration">
									<xsl:with-param name="ms" select="./etf:duration[1]/text()"/>
								</xsl:call-template>
							</td>
						</tr>
						<tr class="ReportDetail">
							<td><xsl:value-of select="$lang/x:e[@key = 'TestModule']"/> ID</td>
							<td>
								<xsl:value-of select="$TestModule/@id"/>
							</td>
						</tr>
						</tbody>
					</table>
					<br/>
					<!-- TestCase result information -->
					<xsl:apply-templates select="./etf:testCaseResults[1]/etf:TestCaseResult"/>
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Test case result information -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestCaseResult">
		<xsl:variable name="TestCase" select="key('testCaseKey', ./etf:resultedFrom/@ref)"/>
		<xsl:variable name="resultItem" select="."/>
		
		<div data-role="collapsible" data-enhanced="true" id="{$TestCase/@id}" data-mini="true">
			<xsl:variable name="skipped" select="./etf:status[1]/text() = 'SKIPPED'" as="xs:boolean"/>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SuccessfulTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase ManualTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase FailedTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SuccessfulTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SuccessfulTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SkippedTestCase</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SuccessfulTestCase DoNotShowInSimpleView</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleJqmClass"/> TestCase SuccessfulTestCase DoNotShowInSimpleView</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			
			<h3 class="ui-collapsible-heading ui-collapsible-heading-collapsed">
				<a href="#">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-h ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-eye ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-i ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-info ui-btn-j ui-mini </xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-info ui-btn-j ui-mini </xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-alert ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-f ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-f ui-mini</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:variable name="label">
						<xsl:call-template name="string-replace">
							<xsl:with-param name="text" select="$TestCase/etf:label"/>
							<xsl:with-param name="replace" select="'(disabled)'"/>
							<xsl:with-param name="with" select="''"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of select="$label"/>
					<div class="ui-li-count">
						<xsl:variable name="FailedCount">
							<xsl:choose>
								<xsl:when
									test="$TestCase/etf:testSteps/etf:TestStep[etf:label ne 'IGNORE']">
									<!-- TODO detect technical test steps through href in resultedFrom -->
									<xsl:value-of
										select="count((./etf:testStepResults[1]/etf:TestStepResult[etf:status = 'FAILED' and not(etf:resultedFrom/@href)], 
										./etf:testStepResults[1]/etf:TestStepResult[etf:status = 'FAILED' and not(etf:resultedFrom/@href)]/etf:invokedTests[1]//etf:TestStepResult[etf:status = 'FAILED' and not(etf:resultedFrom/@href)] ))"
									/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of
										select="count(./etf:testStepResults[1]/etf:TestStepResult/etf:testAssertionResults[1]/etf:TestAssertionResult[etf:status = 'FAILED'])"
									/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="Count">
							<xsl:choose>
								<xsl:when
									test="$TestCase/etf:testSteps/etf:TestStep[etf:label ne 'IGNORE']">
									<xsl:value-of
										select="count((./etf:testStepResults[1]/etf:TestStepResult[not(etf:resultedFrom/@href)],
										./etf:testStepResults[1]/etf:TestStepResult[not(etf:resultedFrom/@href)]/etf:invokedTests[1]//etf:TestStepResult[not(etf:resultedFrom/@href)]))"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of
										select="count(./etf:testStepResults[1]/etf:TestStepResult/etf:testAssertionResults[1]/etf:TestAssertionResult)"
									/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:if test="$FailedCount &gt; 0"><xsl:value-of
							select="$lang/x:e[@key = 'FAILED']"/>: <xsl:value-of
								select="$FailedCount"/> / </xsl:if>
						<xsl:value-of select="$Count"/>
					</div>
					<span class="ui-collapsible-heading-status"> click to expand contents</span>
				</a>
			</h3>
			<div aria-hidden="true">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-h</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-i</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-f</xsl:when>
				</xsl:choose>
			</xsl:attribute>

			<xsl:if test="$skipped">
				<h3>The test case was skipped because it depends on other test cases that also failed or were skipped. Check the cause of the problem by checking failed test cases on which this test case depends.</h3>
			</xsl:if>
			<xsl:if
				test="$TestCase/etf:description and normalize-space($TestCase/etf:description/text()) ne ''">
				<xsl:value-of select="$TestCase/etf:description/text()"
					disable-output-escaping="yes"/>
				<br/>
			</xsl:if>
			<br/>
			<!-- General data about test result and test case -->
			<table>
				<tbody>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
					</td>
					<td>
						<xsl:value-of select="$lang/x:e[@key = $resultItem/etf:status]"/>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
					</td>
					<td>
						<xsl:call-template name="formatDuration">
							<xsl:with-param name="ms" select="./etf:duration[1]/text()"/>
						</xsl:call-template>
					</td>
				</tr>
				<xsl:for-each select="$TestCase/etf:dependencies/etf:testCase/@ref">
					<xsl:variable name="DepTestCase" select="key('testCaseKey', .)"/>
					<tr>
						<xsl:attribute name="class">
							<xsl:if test="not($skipped)">DoNotShowInSimpleView</xsl:if>
						</xsl:attribute>
						<td>
							<xsl:value-of select="$lang/x:e[@key = 'Dependency']"/>
						</td>
						<td>
							<xsl:variable name="depTestCaseId" select="$DepTestCase/@id"/>
							<a class="ui-link" href="{$serviceUrl}/TestRuns/{$testRun/@id}.html?lang={$language}#{$depTestCaseId}"
								data-ajax="false"
								onclick="event.preventDefault(); jumpToAnchor('{$depTestCaseId}'); return false;">
								<xsl:value-of select="$DepTestCase/etf:label/text()"/>
							</a>
						</td>
					</tr>
				</xsl:for-each>				
				<tr class="ReportDetail">
					<td><xsl:value-of select="$lang/x:e[@key = 'TestCase']"/> ID</td>
					<td>
						<xsl:value-of select="$TestCase/@id"/>
					</td>
				</tr>
				<xsl:call-template name="itemData">
					<xsl:with-param name="Node" select="$TestCase"/>
				</xsl:call-template>
				<xsl:call-template name="properties">
					<xsl:with-param name="Node" select="$TestCase"/>
				</xsl:call-template>
				</tbody>
			</table>
			<br/>
			<!--Add test step results and information about the teststeps -->
				<xsl:if test="not($skipped)">
					<xsl:apply-templates select="./etf:testStepResults[1]/etf:TestStepResult"/>
				</xsl:if>
			</div>
		</div>
	</xsl:template>
	<!-- Test Step Results -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestStepResult">
		<!-- Information from referenced Test Step -->
		<xsl:variable name="TestStep" select="key('testStepKey', ./etf:resultedFrom/@ref)"/>
		<xsl:variable name="resultItem" select="."/>
		<xsl:choose>
			<xsl:when test="not($TestStep) or $TestStep/etf:label[1]/text() = 'IGNORE'">
				<div class="TestStepPlaceHolder">
					<xsl:apply-templates
						select="./etf:testAssertionResults[1]/etf:TestAssertionResult"/>
				</div>
			</xsl:when>
			<xsl:otherwise>
				
				<xsl:variable name="testStepResultId" select="$resultItem/@id"/>
				
				<div data-role="collapsible" data-enhanced="true" id="{$testStepResultId}" data-mini="true">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SuccessfulTestStep </xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep ManualTestStep</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep FailedTestStep</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SuccessfulTestStep</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SuccessfulTestStep</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SkippedTestStep</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SuccessfulTestStep DoNotShowInSimpleView</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleJqmClass"/> TestStep SuccessfulTestStep DoNotShowInSimpleView</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:variable name="id" select="./@id"/>
					<xsl:variable name="FailedAssertionCount"
						select="count(./etf:testAssertionResults[1]/etf:TestAssertionResult[etf:status[1]/text() = 'FAILED'])"/>
					<h4 class="ui-collapsible-heading ui-collapsible-heading-collapsed">
						<a href="#">
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-h ui-mini</xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-j ui-mini</xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-i ui-mini</xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-j ui-mini </xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-j ui-mini </xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-j ui-mini</xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-f ui-mini</xsl:when>
									<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-plus ui-btn-f ui-mini</xsl:when>
								</xsl:choose>
							</xsl:attribute>
							<xsl:variable name="label">
								<xsl:call-template name="string-replace">
									<xsl:with-param name="text" select="$TestStep/etf:label[1]/text()"/>
									<xsl:with-param name="replace" select="'(disabled)'"/>
									<xsl:with-param name="with" select="''"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:variable name="relabel" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[@type='RELABEL']"/>
							<xsl:choose>
								<xsl:when test="$relabel">
									<xsl:apply-templates select="$relabel"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$label"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:if test="./etf:testAssertionResults[1]/etf:TestAssertionResult">
								<div class="ui-li-count">
									<xsl:if test="$FailedAssertionCount &gt; 0"><xsl:value-of
										select="$lang/x:e[@key = 'FAILED']"/>: <xsl:value-of
											select="$FailedAssertionCount"/> / </xsl:if>
									<xsl:value-of
										select="count(./etf:testAssertionResults[1]/etf:TestAssertionResult)"
									/>
								</div>
							</xsl:if>
							<span class="ui-collapsible-heading-status"> click to expand contents</span>
						</a>
					</h4>
					
					
					<div aria-hidden="true">
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-h</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-i</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
								<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-f</xsl:when>
							</xsl:choose>
						</xsl:attribute>
					<xsl:if
						test="$TestStep/etf:description and normalize-space($TestStep/etf:description/text()) ne ''">
						<xsl:value-of select="$TestStep/etf:description/text()"
							disable-output-escaping="yes"/>
						<br/>
					</xsl:if>
					<br/>
					<table>
						<tbody>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
							</td>
							<td>
								<xsl:value-of select="$lang/x:e[@key = $resultItem/etf:status]"/>
							</td>
						</tr>
						<tr class="DoNotShowInSimpleView">
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Started']"/>
							</td>
							<td>
								<xsl:call-template name="formatDate">
									<xsl:with-param name="DateTime"
										select="./etf:startTimestamp[1]/text()"/>
								</xsl:call-template>
							</td>
						</tr>
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
							</td>
							<td>
								<xsl:call-template name="formatDuration">
									<xsl:with-param name="ms" select="./etf:duration[1]/text()"/>
								</xsl:call-template>
							</td>
						</tr>
						<tr class="ReportDetail">
							<td><xsl:value-of select="$lang/x:e[@key = 'TestStep']"/> ID</td>
							<td>
								<xsl:value-of select="$TestStep/@id"/>
							</td>
						</tr>
						<tr class="DoNotShowInSimpleView">
							<td><xsl:value-of select="$lang/x:e[@key = 'TestStepLocation']"/></td>
							<td>
								<a class="ui-link" href="{$serviceUrl}/TestRuns/{$testRun/@id}.html?lang={$language}#{$testStepResultId}"
									data-ajax="false"
									onclick="event.preventDefault(); jumpToAnchor('{$testStepResultId}'); return false;">
									<xsl:value-of select="$lang/x:e[@key = 'PublicationLocationLink']"/>
								</a>
							</td>
						</tr>
						
						
						<!-- Invoked Tests -->
						<xsl:if test="$resultItem/etf:invokedTests/etf:TestStepResult">
							<xsl:for-each select="$resultItem/etf:invokedTests/etf:TestStepResult">
								<xsl:variable name="DepTestStep" select="key('testStepKey', ./etf:resultedFrom/@ref)"/>
								<tr>
									<!--xsl:attribute name="class">
										<xsl:if test="not($failedOrSkipped)">DoNotShowInSimpleView</xsl:if>
									</xsl:attribute-->
									<td>
										<xsl:value-of select="$lang/x:e[@key = 'Dependency']"/>
									</td>
									<td>
										<xsl:variable name="depTestStepId" select="./@id"/>
										<a class="ui-link" href="{$serviceUrl}/TestRuns/{$testRun/@id}.html?lang={$language}#{$depTestStepId}"
											data-ajax="false"
											onclick="event.preventDefault(); jumpToAnchor('{$depTestStepId}'); return false;">
											<xsl:variable name="status" select="./etf:status/text()"/>
											<xsl:value-of select="$DepTestStep/etf:label/text()"/> (<xsl:value-of select="$lang/x:e[@key = $status]"/>) 
										</a>
									</td>
								</tr>
							</xsl:for-each>
						</xsl:if>
						</tbody>
					</table>
					<br/>
					
					<xsl:variable name="endpoint" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[@type='ServiceEndpoint']"/>
					<xsl:if test="$endpoint">
						<p><a target="_blank" data-ajax="false">
							<xsl:attribute name="href">
								<xsl:apply-templates select="$endpoint"/>
							</xsl:attribute>
							Endpoint
						</a></p>
					</xsl:if>
					
					<xsl:variable name="parameter" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[@type='GetParameter']"/>
					<xsl:if test="$parameter">
						<label for="attachment.{$parameter/@id}">Request</label>
						<textarea id="attachment.{$parameter/@id}" data-mini="true" readonly="readonly" class="{$textAreaJqmClass}">
							<xsl:apply-templates select="$parameter"/>
						</textarea>
					</xsl:if>
						
					<!-- PostData, deprecated type: PostParameter -->
					<xsl:variable name="post" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[@type='PostData' or @type='PostParameter']"/>
					<xsl:if test="$post">
						<p><a target="_blank" data-ajax="false">
							<xsl:attribute name="href">
								<xsl:apply-templates select="$post"/>
							</xsl:attribute>
							Open saved POST request data
						</a></p>
					</xsl:if>
					
					<xsl:variable name="response" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[@type='ServiceResponse']"/>
					<xsl:if test="$response">
						<p><a target="_blank" data-ajax="false">
							<xsl:attribute name="href">
								<xsl:apply-templates select="$response"/>
							</xsl:attribute>
							<xsl:value-of select="$lang/x:e[@key = 'OpenSavedResponse']"/>
						</a></p>
					</xsl:if>
					
					
					<!-- Execution Statement -->
					<xsl:if test="$TestStep/etf:statementForExecution[1] and 
						not($TestStep/etf:statementForExecution[1]='NOT_APPLICABLE')">
						<div class="Request">
							<xsl:if test="$FailedAssertionCount = 0">
								<xsl:attribute name="class">DoNotShowInSimpleView</xsl:attribute>
							</xsl:if>
							<xsl:apply-templates select="$TestStep/etf:statementForExecution[1]"/>
						</div>
					</xsl:if>
					
					<xsl:variable name="attachments" select="key('attachmentsKey', ./etf:attachments[1]/etf:attachment/@ref)[
						not(@type='ServiceEndpoint' or @type='ServiceResponse' or @type='GetParameter' or @type='PostData' or @type='PostParameter' or @type='RELABEL')]"/>
					<xsl:if test="$attachments">
						<div class="Attachments DoNotShowInSimpleView">
							<h4>Attachments</h4>
							<xsl:if test="$attachments[etf:referencedData]">
							<ul>
								<xsl:for-each select="$attachments[etf:referencedData]">
									<li><a target="_blank" data-ajax="false">
										<xsl:attribute name="href">
											<xsl:apply-templates select="."/>
										</xsl:attribute>
										<xsl:choose>
											<xsl:when test="./etf:referencedData/@size">
												<xsl:value-of select="concat(./etf:label, ' (', ./etf:referencedData/@size, 'bytes )')"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="./etf:label"/>
											</xsl:otherwise>
										</xsl:choose>
									</a></li>
								</xsl:for-each>
							</ul>
							</xsl:if>
							<xsl:if test="$attachments[etf:embeddedData]">
								<textarea data-mini="true" readonly="readonly" class="{$textAreaJqmClass}">
									<xsl:for-each select="$attachments[etf:embeddedData]">
											<xsl:apply-templates select="."/>
									</xsl:for-each>
								</textarea>
							</xsl:if>
						</div>
					</xsl:if>
					
					<!-- Get Assertion results -->
					<xsl:if test="./etf:testAssertionResults[1]/etf:TestAssertionResult">
						<div class="AssertionsContainer">
							<h4><xsl:value-of select="$lang/x:e[@key = 'TestAssertions']"/></h4>
							<xsl:apply-templates
								select="./etf:testAssertionResults[1]/etf:TestAssertionResult"/>
						</div>
					</xsl:if>
					
					<xsl:if test="etf:messages[1]/*">
						<xsl:apply-templates select="etf:messages[1]"/>
					</xsl:if>
					
				</div>
				</div>
			</xsl:otherwise>
			
		</xsl:choose>
		
		<!-- Call invoked Test Steps -->
		<xsl:apply-templates select="$resultItem/etf:invokedTests[1]/etf:TestStepResult"/>
		
	</xsl:template>
	<!-- Messages -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:statementForExecution">
		<div class="ExecutionStatement">
			<xsl:variable name="id" select="generate-id(.)"/>
			<label for="{$id}">
				<xsl:value-of select="$lang/x:e[@key = 'ExecutionStatement']"/>
			</label>
			<textarea id="{$id}.executionStatement" data-mini="true" readonly="readonly" class="{$textAreaJqmClass}">
				<xsl:value-of select="text()"/>
			</textarea>
		</div>
	</xsl:template>
	<!-- Assertion results -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:TestAssertionResult">
		<xsl:variable name="resultItem" select="."/>
		<xsl:variable name="TestAssertion" select="key('testAssertionKey', ./etf:resultedFrom/@ref)"/>
		
		<xsl:variable name="assertionResultId" select="$resultItem/@id"/>
		
		<div data-role="collapsible" data-enhanced="true" id="{$assertionResultId}" data-mini="true">
			<!-- Assertion Styling: Set attributes do indicate the status -->
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion SuccessfulAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion ManualAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion FailedAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion SuccessfulAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion SuccessfulAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion FailedAssertion</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion SuccessfulAssertion DoNotShowInSimpleView</xsl:when>
					<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleJqmClass"/> Assertion SuccessfulAssertion DoNotShowInSimpleView</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:variable name="id" select="./@id"/>
			<!-- Information from referenced Assertion -->
			<h5 class="ui-collapsible-heading ui-collapsible-heading-collapsed">
				<a href="#">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-check ui-btn-h ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-eye ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-alert ui-btn-i ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-info ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-info ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-alert ui-btn-j ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-forbidden ui-btn-f ui-mini</xsl:when>
							<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleHeadingJqmClass"/> ui-icon-forbidden ui-btn-f ui-mini</xsl:when>
						</xsl:choose>
					</xsl:attribute><xsl:value-of select="$TestAssertion/etf:label"/><span class="ui-collapsible-heading-status"> click to expand contents</span></a>
			</h5>
			<div aria-hidden="true">
				<xsl:attribute name="class">
					<xsl:choose>
						<xsl:when test="./etf:status[1]/text() = 'PASSED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-h</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'PASSED_MANUAL'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'FAILED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-i</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'WARNING'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'INFO'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'SKIPPED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'NOT_APPLICABLE'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-g</xsl:when>
						<xsl:when test="./etf:status[1]/text() = 'UNDEFINED'"><xsl:value-of select="$collapsibleContentJqmClass"/> ui-body-f</xsl:when>
					</xsl:choose>
				</xsl:attribute>
			<xsl:if
				test="$TestAssertion/etf:description and normalize-space($TestAssertion/etf:description/text()[1]) ne ''">
				<xsl:value-of select="$TestAssertion/etf:description/text()"
					disable-output-escaping="yes"/>
				<br/>
			</xsl:if>
			<br/>
			<table>
				<tbody>
					<tr>
						<td>
							<xsl:value-of select="$lang/x:e[@key = 'Status']"/>
						</td>
						<td>
							<xsl:value-of select="$lang/x:e[@key = $resultItem/etf:status]"/>
						</td>
					</tr>
					<xsl:if test="./etf:duration[1]/text()">
						<tr>
							<td>
								<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
							</td>
							<td>
								<xsl:call-template name="formatDuration">
									<xsl:with-param name="ms" select="./etf:duration[1]/text()"/>
								</xsl:call-template>
							</td>
						</tr>
					</xsl:if>
					<tr class="ReportDetail">
						<td><xsl:value-of select="$lang/x:e[@key = 'TestAssertion']"/> ID</td>
						<td>
							<xsl:value-of select="$TestAssertion/@id"/>
						</td>
					</tr>
					<tr class="DoNotShowInSimpleView">
						<td><xsl:value-of select="$lang/x:e[@key = 'AssertionLocation']"/></td>
						<td>
							<a class="ui-link" href="{$serviceUrl}/TestRuns/{$testRun/@id}.html?lang={$language}#{$assertionResultId}"
								data-ajax="false"
								onclick="event.preventDefault(); jumpToAnchor('{$assertionResultId}'); return false;">
								<xsl:value-of select="$lang/x:e[@key = 'AssertionLocationLink']"/>
							</a>
						</td>
					</tr>
					<xsl:call-template name="properties">
						<xsl:with-param name="Node" select="$TestAssertion"/>
					</xsl:call-template>
				</tbody>
			</table>
			<br/>
			<xsl:if
				test="$TestAssertion/etf:expression and not($TestAssertion/etf:expression = ('','NOT_APPLICABLE', '''PASSED'''))">
				<div class="ReportDetail Expression">
					<label for="{$id}.expression"><xsl:value-of
							select="$lang/x:e[@key = 'Expression']"/>:</label>
					<textarea id="{$id}.expression" class="Expression {$textAreaJqmClass}" data-mini="true" readonly="readonly">
						<xsl:value-of select="$TestAssertion/etf:expression"/>
					</textarea>
				</div>
			</xsl:if>
			<xsl:if test="$TestAssertion/etf:expectedResult and not(normalize-space($TestAssertion/etf:expectedResult) = ('','NOT_APPLICABLE'))">
				<div class="ReportDetail ExpectedResult">
					<label for="{$id}.expectedResult"><xsl:value-of
							select="$lang/x:e[@key = 'ExpectedResult']"/>:</label>
					<textarea id="{$id}.expectedResult" class="ExpectedResult {$textAreaJqmClass}" data-mini="true" readonly="readonly">
						<xsl:value-of select="$TestAssertion/etf:expectedResult"/>
					</textarea>
				</div>
			</xsl:if>
			<xsl:if test="etf:messages[1]/*">
				<xsl:apply-templates select="etf:messages[1]"/>
			</xsl:if>
		</div>
		</div>
	</xsl:template>
	<!-- Item data information without label -->
	<!-- ########################################################################################## -->
	<xsl:template name="itemData">
		<xsl:param name="Node"/>
		<xsl:if test="$Node/etf:startTimestamp/text()">
			<tr class="DoNotShowInSimpleView">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Started']"/>
				</td>
				<td>
					<xsl:call-template name="formatDate">
						<xsl:with-param name="DateTime" select="$Node/etf:startTimestamp"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:duration/text()">
			<tr>
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Duration']"/>
				</td>
				<td>
					<xsl:call-template name="formatDuration">
						<xsl:with-param name="ms" select="$Node/etf:duration[1]/text()"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
		<!-- Version Data -->
		<xsl:if test="$Node/etf:author/text()">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Author']"/>
				</td>
				<td>
					<xsl:value-of select="$Node/etf:author"/>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:creationDate/text()">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'DateCreated']"/>
				</td>
				<td>
					<xsl:call-template name="formatDate">
						<xsl:with-param name="DateTime" select="$Node/etf:creationDate"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:version/text()">
			<tr>
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Version']"/>
				</td>
				<td>
					<xsl:value-of select="$Node/etf:version"/>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:lastEditor/text()">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'LastEditor']"/>
				</td>
				<td>
					<xsl:value-of select="$Node/etf:lastEditor"/>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:lastUpdateDate/text()">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'LastUpdated']"/>
				</td>
				<td>
					<xsl:call-template name="formatDate">
						<xsl:with-param name="DateTime" select="$Node/etf:lastUpdateDate"/>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
		<xsl:variable name="remoteResource" select="$Node/etf:remoteResource/text()"/>
		<xsl:if test="$remoteResource and $remoteResource ne 'http://none'">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Reference']"/>
				</td>
				<td>
					<xsl:choose>
						<xsl:when test="starts-with($remoteResource, 'http')">
							<a class="ui-link" href="{$remoteResource}">Link</a>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$remoteResource"/>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="$Node/etf:itemHash/text()">
			<tr class="ReportDetail">
				<td>
					<xsl:value-of select="$lang/x:e[@key = 'Hash']"/>
				</td>
				<td>
					<xsl:value-of select="$Node/etf:itemHash"/>
				</td>
			</tr>
		</xsl:if>		
	</xsl:template>
	<!-- ########################################################################################## -->
	<xsl:template name="properties">
		<xsl:param name="Node"/>
		<xsl:if test="$Node/etf:Properties/etf:property">
			<xsl:for-each select="$Node/etf:Properties/etf:property">
				<tr class="ReportDetail">
					<td><xsl:value-of select="./@name"/></td>
					<td><xsl:value-of select="./text()"/></td>
				</tr>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<!-- Messages -->
	<!-- ########################################################################################## -->
	<xsl:template match="etf:messages">
		<div class="FailureMessage">
			<xsl:if test="./etf:message">
				<xsl:variable name="id" select="generate-id(.)"/>
				<label for="{$id}">
					<xsl:value-of select="$lang/x:e[@key = 'Messages']"/>
				</label>
				<textarea id="{$id}.failureMessages" data-mini="true" readonly="readonly" class="{$textAreaJqmClass}">
					<xsl:for-each select="./etf:message">
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
					</xsl:for-each>
				</textarea>
			</xsl:if>
		</div>
	</xsl:template>
	
	<xsl:template name="translateSimpleMessage">
		<xsl:param name="templateId" as="xs:string"/>
		<xsl:variable name="template" select="key('translationKey', $templateId)"/>
		<xsl:variable name="str" select="$template[1]/text()"/>
		<xsl:if test="not(normalize-space($str))">
			<xsl:message terminate="yes">ERROR: Translation template for ID <xsl:value-of
				select="$templateId"/> not found</xsl:message>
		</xsl:if>
		<xsl:value-of select="concat($str, '&#13;&#10;')" />
	</xsl:template>
	
	<xsl:template name="translateMessageWithTokens">
		<xsl:param name="templateId" as="xs:string"/>
		<xsl:param name="translationArguments" as="node()"/>
		<xsl:variable name="template" select="key('translationKey', $templateId)"/>
		<xsl:variable name="str" select="$template[1]/text()"/>
		<xsl:if test="not(normalize-space($str))">
			<p>ERROR: Translation template for ID <xsl:value-of select="$templateId"/> not found.</p>
			<p>Please contact the system administrator if the problem persists.</p>
			<xsl:message terminate="yes">ERROR: Translation template for ID <xsl:value-of
					select="$templateId"/> not found</xsl:message>
		</xsl:if>
		<xsl:value-of
			select="concat(etf:replace-multi-tokens($str, $translationArguments[1]/etf:argument), '&#13;&#10;')"
		/>
	</xsl:template>
	
	<xsl:template name="translateParameterName">
		<xsl:param name="parameterName" as="xs:string"/>
		<xsl:variable name="template" select="key('translationKey', concat('TR.parameter.name.', $parameterName))"/>
		<xsl:variable name="str" select="$template[1]/text()"/>
		<xsl:choose>
			<xsl:when test="normalize-space($str)">
				<xsl:value-of select="etf:replace-multi-tokens($str, ())" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$parameterName"/>
			</xsl:otherwise>
		</xsl:choose>
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
	<!-- ChangeFailureMessage -->
	<!-- ########################################################################################## -->
	<xsl:function name="etf:unwrapMessage">
		<!--
   By default the whole expression of a XQuery will be written out in error messages.
   In this template function the error message will be "beautified".
   To use this functionality the xquery shall be in the following form:
   <result>
   {
     XQUERY Functions
     if( xyz=FAILURE ) then return 'failure message foo bar... '
     else ''
   </result>
  -->
		<xsl:param name="message"/>
		<xsl:variable name="unwrapped" select="substring-before(substring-after($message,'result&gt;'), '&lt;/result')" />
		<xsl:choose>
			<xsl:when test="normalize-space($unwrapped)">
				<xsl:value-of select="$unwrapped"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$message"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<!-- Footer -->
	<!-- ########################################################################################## -->
	<xsl:template name="footer">
		<div data-role="footer">
			<h1>Report generated by ETF</h1>
		</div>
		<xsl:call-template name="footerScripts"/>
	</xsl:template>
</xsl:stylesheet>
