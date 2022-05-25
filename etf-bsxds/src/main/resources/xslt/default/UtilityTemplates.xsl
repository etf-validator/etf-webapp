<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" encoding="UTF-8"/>
	<xsl:template name="formatDuration">
		<xsl:param name="ms"/>
		<xsl:variable name="hours" select="floor($ms div 3600000)"/>
		<xsl:variable name="minutes" select="floor($ms div 60000) mod 60"/>
		<xsl:variable name="seconds" select="floor($ms div 1000) mod 60"/>
		<xsl:variable name="milliseconds" select="$ms mod 1000"/>
		<xsl:if test="$hours gt 0">
			<xsl:value-of select="$hours"/>
			<xsl:value-of select="'h '"/>
		</xsl:if>
		<xsl:if test="$hours gt 0 or $minutes gt 0">
			<xsl:value-of select="$minutes"/>
			<xsl:value-of select="' min '"/>
		</xsl:if>
		<xsl:if test="$hours eq 0">
			<xsl:choose>
				<xsl:when test="$minutes eq 0 and $seconds eq 0 and $milliseconds eq 0">
					<!--xsl:value-of select="'few nanoseconds'"/-->
					<xsl:value-of select="'0.001 s'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$seconds"/>
					<xsl:if
						test="$minutes eq 0 and $seconds le 2 and $milliseconds gt 0">
						<xsl:value-of select="'.'"/>
						<xsl:value-of select="format-number($milliseconds, '000')"/>
					</xsl:if>
					<xsl:value-of select="' s'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	<!-- converts a date string from 2014-01-15T16:21:47.099+01:00 or 15.1.2014T16:21:47.099+01:00to 15/01/2014 16:21:47 -->
	<xsl:template name="formatDate">
		<xsl:param name="DateTime"/>
		<!-- Based on John Workmans formatDate -->
		<!-- DATE -->
		<xsl:variable name="date">
			<xsl:choose>
				<xsl:when test="contains($DateTime, 'T')">
					<xsl:value-of select="substring-before($DateTime, 'T')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$DateTime"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($date, '-')">
				<!-- Format 2014-01-15 -->
				<xsl:variable name="year" select="substring($date, 1, 4)"/>
				<xsl:variable name="month-temp" select="substring-after($date, '-')"/>
				<xsl:variable name="month" select="substring-before($month-temp, '-')"/>
				<xsl:variable name="day-temp" select="substring-after($month-temp, '-')"/>
				<xsl:variable name="day" select="substring($day-temp, 1, 2)"/>
				<xsl:value-of select="$day"/>
				<xsl:value-of select="'/'"/>
				<xsl:value-of select="$month"/>
				<xsl:value-of select="'/'"/>
				<xsl:value-of select="$year"/>
			</xsl:when>
			<xsl:when test="contains($date, '.')">
				<!-- Format 15.1.2015 -->
				<xsl:variable name="day-temp" select="substring-after($date, '.')"/>
				<xsl:value-of select="substring-before($date, '.')"/>
				<xsl:value-of select="'/'"/>
				<xsl:variable name="month-temp" select="substring-after($date, '.')"/>
				<xsl:value-of select="substring-before($month-temp, '.')"/>
				<xsl:value-of select="'/'"/>
				<xsl:variable name="year-temp" select="substring-after($date, '.')"/>
				<xsl:value-of select="substring-before($year-temp, '.')"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- Unknown -->
				<xsl:value-of select="'unknown'"/>
			</xsl:otherwise>
		</xsl:choose>
		<!-- TIME -->
		<xsl:if test="contains($DateTime, 'T')">
			<xsl:variable name="time" select="substring-after($DateTime, 'T')"/>
			<xsl:variable name="hh" select="substring($time, 1, 2)"/>
			<xsl:variable name="mm" select="substring($time, 4, 2)"/>
			<xsl:variable name="ss" select="substring($time, 7, 2)"/>
			<xsl:variable name="ms" select="substring($time, 10, 3)"/>
			<xsl:variable name="tz" select="substring($time, 14, 5)"/>
			<xsl:value-of select="' '"/>
			<xsl:value-of select="$hh"/>
			<xsl:value-of select="':'"/>
			<xsl:value-of select="$mm"/>
			<xsl:value-of select="':'"/>
			<xsl:value-of select="$ss"/>
			<xsl:value-of select="' GMT'"/>
		</xsl:if>
	</xsl:template>
	<!-- XSLT Cookbook, 2nd Edition -->
	<xsl:template name="substring-after-last">
		<xsl:param name="input"/>
		<xsl:param name="substr"/>
		<!-- Extract the string which comes after the first occurrence -->
		<xsl:variable name="temp" select="substring-after($input, $substr)"/>
		<xsl:choose>
			<!-- If it still contains the search string the recursively process -->
			<xsl:when test="$substr and contains($temp, $substr)">
				<xsl:call-template name="substring-after-last">
					<xsl:with-param name="input" select="$temp"/>
					<xsl:with-param name="substr" select="$substr"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$temp"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="string-replace">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="with"/>
		<xsl:choose>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text, $replace)"/>
				<xsl:value-of select="$with"/>
				<xsl:call-template name="string-replace">
					<xsl:with-param name="text" select="substring-after($text, $replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="with" select="$with"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
