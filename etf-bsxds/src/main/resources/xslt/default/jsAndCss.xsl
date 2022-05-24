<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.interactive-instruments.de/etf/2.0" version="1.0" exclude-result-prefixes="x">
	<xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" encoding="UTF-8"/>
	<xsl:variable name="defaultStyleResourcePath">
		<xsl:text>http://services.interactive-instruments.de/etf/css</xsl:text>
	</xsl:variable>
	<xsl:param name="stylePath" select="$defaultStyleResourcePath"/>
	<!-- JQuery Mobile and Styling includes-->
	<!-- ########################################################################################## -->
	<xsl:template name="jsfdeclAndCss">
		<xsl:choose>
			<xsl:when test="$forceLocalResLoading='true'">
				<link rel="stylesheet" href="{$stylePath}/de.interactive-instruments.min.css"/>
				<link rel="stylesheet" href="{$stylePath}/de.interactive-instruments.rep.css"/>
			</xsl:when>
			<xsl:otherwise>
				<link rel="stylesheet" href="https://resources.etf-validator.net/report/v2/css/de.interactive-instruments.min.css?v2"/>
				<link rel="stylesheet" href="https://resources.etf-validator.net/report/v2/css/de.interactive-instruments.rep.css?v2"/>
			</xsl:otherwise>
		</xsl:choose>
		<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jquerymobile/1.4.5/jquery.mobile.min.css"/>
		<script src="https://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.11.3.min.js"
				integrity="sha256-rsPUGdUPBXgalvIj4YKJrrUlmLXbOb6Cp7cdxn1qeUc=" crossorigin="anonymous"/>
		<script src="https://ajax.googleapis.com/ajax/libs/jquerymobile/1.4.5/jquery.mobile.min.js"
				integrity="sha256-MkfSkbXhZoQ1CyPwjC30mPfLF8iKF5n564n9WvCLX4E=" crossorigin="anonymous"/>
	</xsl:template>
	<!-- Report controls-->
	<!-- ########################################################################################## -->
	<xsl:template name="controls">
		<div id="rprtControl">
			<fieldset id="controlgroupLOD" data-role="controlgroup" data-mini="true">
				<legend>
					<xsl:value-of select="$lang/x:e[@key='LevelOfDetail']"/>
				</legend>
				
				<label for="cntrlAllDetails">
					<xsl:value-of select="$lang/x:e[@key='AllDetails']"/>
				</label>
				<input type="radio" name="radio-lod" id="cntrlAllDetails" value="cntrlAllDetails"/>
				
				<label for="cntrlLessInformation">
					<xsl:value-of select="$lang/x:e[@key='LessInformation']"/>
				</label>
				<input type="radio" name="radio-lod" id="cntrlLessInformation" value="cntrlLessInformation"/>
				
				<label for="cntrlSimplified">
					<xsl:value-of select="$lang/x:e[@key='Simplified']"/>
				</label>
				<input type="radio" name="radio-lod" id="cntrlSimplified" value="cntrlSimplified" checked="checked"/>
			</fieldset>
			<fieldset id="controlgroupShow" data-role="controlgroup" data-mini="true">
				<legend>
					<xsl:value-of select="$lang/x:e[@key='Show']"/>
				</legend>
				
				<label for="cntrlShowAll">
					<xsl:value-of select="$lang/x:e[@key='All']"/>
				</label>
				<input type="radio" name="radio-filter" id="cntrlShowAll" value="cntrlShowAll" checked="checked"/>
				
				<label for="cntrlShowOnlyFailed">
					<xsl:value-of select="$lang/x:e[@key='OnlyFailed']"/>
				</label>
				<input type="radio" name="radio-filter" id="cntrlShowOnlyFailed" value="cntrlShowOnlyFailed"/>
				
				<label for="cntrlShowOnlyManual">
					<xsl:value-of select="$lang/x:e[@key='OnlyManual']"/>
				</label>
				<input type="radio" name="radio-filter" id="cntrlShowOnlyManual" value="cntrlShowOnlyManual"/>
				
			</fieldset>
		</div>
	</xsl:template>
	<!-- Script tags in the footer-->
	<!-- ########################################################################################## -->
	<xsl:template name="footerScripts">
		
		<div class="ui-field-contain" id="lodFadinMenu" style="display: none; width: 200px; position: fixed; top: 10px; right: 5px;" data-role="controlgroup">
			<label for="select-Show"> <xsl:value-of select="$lang/x:e[@key='LevelOfDetail']"/> </label>	
			<select name="select-Show" id="lodFadinMenuSelect" data-mini="true">
				<option value="cntrlAllDetails"><xsl:value-of select="$lang/x:e[@key='AllDetails']"/></option>
				<option value="cntrlLessInformation"><xsl:value-of select="$lang/x:e[@key='LessInformation']"/></option>
				<option value="cntrlSimplified" selected="selected"><xsl:value-of select="$lang/x:e[@key='Simplified']"/></option>
			</select>
		</div>
		
		<script>
			
			function SelectorCache() {
				var cache = {};
				function getFromCache( selector ) {
					if ( undefined === cache[ selector ] ) {
						cache[ selector ] = $( selector );
					}
					return cache[ selector ];
				}
				return { get: getFromCache };
			}
			var cache = new SelectorCache();

			function hide(elements) {
				elements.css({ display: 'none' });
			}

			function show(elements) {
				elements.css({ display: '' });
			}

			function updateLod(cntrl) {
				if(cntrl=="cntrlSimplified")
				{
					hide(cache.get('.ReportDetail, .DoNotShowInSimpleView, .XQueryContainsAssertion'));
				}
				else if(cntrl=="cntrlLessInformation")
				{
					hide(cache.get('.ReportDetail'));
					show(cache.get('.DoNotShowInSimpleView'));
				}
				else if(cntrl=="cntrlAllDetails")
				{
					show(cache.get('.ReportDetail, .DoNotShowInSimpleView'));
				}
			}
			
			<!-- Controls for filtering -->
			cache.get( "input[name=radio-filter]" ).on( "click", function() {
			
			var cntrl = $( "input[name=radio-filter]:checked" ).val();
				if(cntrl=="cntrlShowOnlyFailed")
				{
					hide(cache.get('.SuccessfulTestModule, .ManualTestModule, .SkippedTestModule, .SuccessfulTestCase, .ManualTestCase, .SuccessfulTestStep, .ManualTestStep, .SuccessfulAssertion, .ManualAssertion, .SkippedTestCase, .SkippedTestStep, .SkippedAssertion'));					
					show(cache.get('.FailedTestCase, .FailedTestStep, .FailedAssertion'));
					
					if(cache.get('.TestStep, .Assertion').length &lt; 3500) {
						cache.get('.TestSuite').collapsible('expand');
						cache.get('.FailedTestModule').collapsible('expand');
						cache.get('.FailedTestCase').collapsible('expand');
						cache.get('.FailedTestStep').collapsible('expand');
						cache.get('.FailedAssertion').collapsible('expand');
					}
				}
				else if(cntrl=="cntrlShowOnlyManual")
				{
					hide(cache.get('.SuccessfulTestModule, .FailedTestModule, .SkippedTestModule, .SuccessfulTestCase, .FailedTestCase, .SkippedTestCase, .SkippedTestStep, .SkippedAssertion, .SuccessfulTestStep, .FailedTestStep, .SuccessfulAssertion, .FailedAssertion'));
					show(cache.get('.ManualTestModule, .ManualTestCase, .ManualTestStep, .ManualAssertion'));
					
					if(cache.get('.TestStep, .Assertion').length &lt; 3500) {
						cache.get('.TestSuite').collapsible('expand');
						cache.get('.ManualTestModule').collapsible('expand');
						cache.get('.ManualTestCase').collapsible('expand');
						cache.get('.ManualTestStep').collapsible('expand');
						cache.get('.ManualAssertion').collapsible('expand');
					}
				}
				else if(cntrl=="cntrlShowAll")
				{
					show(cache.get('.SuccessfulTestModule, .FailedTestModule, .ManualTestModule, .SkippedTestModule, .SuccessfulTestCase, .SuccessfulTestStep, .SuccessfulAssertion, .SkippedTestCase, .SkippedTestStep, .SkippedAssertion, .ManualTestCase, .ManualTestStep, .ManualAssertion, .FailedTestCase, .FailedTestStep, .FailedAssertion'));
				}
			});
			
			$.fn.exists = function(){ return this.length > 0; }
			
			$.fn.getParentWithClass = function(className) {
				var p = this.parent();
				if(p.exists()) {
					return p.hasClass(className) ? p : p.getParentWithClass(className);
				}
				console.warn('Parent class '+className+' not found');
				return null;
			}
			
			<!-- Jump to element with ID -->
			function jumpToAnchor(anchorId) {
				var anchorElement = $('#'+anchorId);
				if(anchorId!="" &amp;&amp; anchorElement.exists()) {
					console.log("Scrolling to anchor: "+anchorId);
					// Expand parent model items

					anchorElement.collapsible('expand').getParentWithClass("TestSuite").collapsible('expand');
					var testModulePar = anchorElement.collapsible('expand').getParentWithClass("TestModule")
					if(testModulePar) {
						testModulePar.collapsible('expand');
					}
					var testCasePar = anchorElement.collapsible('expand').getParentWithClass("TestCase");
					if(testCasePar) {
						testCasePar.collapsible('expand');
					}
					var testStepPar = anchorElement.collapsible('expand').getParentWithClass("TestStep");
					if(testStepPar) {
						testStepPar.collapsible('expand');
					}
					var position = $(anchorElement).offset().top;
					$("html, body").stop().animate({ scrollTop: position });
					window.history.pushState(null,"", "#"+anchorId);
				}
			}			
			<!-- Controls for switching the level of detail -->
			$("#controlgroupLOD input").on( "click", function() {
				var cntrl = $( "#controlgroupLOD input:checked" ).val();
				$('#lodFadinMenuSelect').val(cntrl).selectmenu('refresh');
				updateLod(cntrl);
			});
			$("#lodFadinMenuSelect").on( "change", function() {
				var cntrl = $( "#lodFadinMenuSelect option:selected").val()
				$("#cntrlAllDetails").prop('checked', false).checkboxradio("refresh");
				$("#cntrlLessInformation").prop('checked', false).checkboxradio("refresh");
				$("#cntrlSimplified").prop('checked', false).checkboxradio("refresh");
				$("#"+cntrl).prop('checked', true).checkboxradio("refresh");
				updateLod(cntrl);
			});	
			$(document).scroll(function () {
				var y = $(this).scrollTop();
				if (y > 370) {
					cache.get('#lodFadinMenu').fadeIn();
				} else {
					cache.get('#lodFadinMenu').fadeOut();
				}
			});
			
			<!-- Init page -->
			$(document).one('pagebeforeshow', function() {				
				<!-- Jump to element with ID on page initialization -->
				var url = decodeURIComponent(window.location.href);
				var anchorIdx = url.indexOf("#");
				var anchorId = anchorIdx != -1 ? url.substring(anchorIdx+1) : "";
				var anchorElement = $('#'+anchorId);
				if(anchorId!="" &amp;&amp; anchorElement.exists()) {
					cache.get('.DoNotShowInSimpleView').show();
					jumpToAnchor(anchorId);
					$("body").one("pagecontainershow", function () {
						var position = cache.get(anchorElement).offset().top;
						// ... but it works...
						setTimeout( function() {
							cache.get("html, body").stop().animate({ scrollTop: position });
							},150);
						});
				}
				
				<!-- Hide checkbox if there are no manual assertions -->
				if ( cache.get('.ManualTestCase, .ManualTestStep, .ManualAssertion').length==0) {
					cache.get('#cntrlShowOnlyManual').checkboxradio();
					cache.get('#cntrlShowOnlyManual').checkboxradio('refresh');
					cache.get('#cntrlShowOnlyManual').checkboxradio('disable').checkboxradio('refresh');
				}
				
				<!-- Default: simplified -->
				hide(cache.get('.ReportDetail, .DoNotShowInSimpleView, .XQueryContainsAssertion'));
				
			});
			
		</script>
	</xsl:template>
</xsl:stylesheet>
