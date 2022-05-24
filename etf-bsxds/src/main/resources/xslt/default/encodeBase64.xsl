<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text"/>

    <xsl:variable name="datamap" select="document('datamap.xml')" />

    <!-- Template to convert the base64 string to ascii representation -->
    <xsl:template name="convertBase64ToAscii">
        <xsl:param name="base64String" />

        <!-- execute if last 2 characters do not contain = character-->
        <xsl:if test="not(contains(substring($base64String, string-length($base64String) - 1), '='))">
            <xsl:variable name="binaryBase64String">
                <xsl:call-template name="base64StringToBinary">
                    <xsl:with-param name="string" select="$base64String"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="$binaryBase64String" />
            </xsl:call-template>
        </xsl:if>

        <!-- extract last two characters -->
        <xsl:variable name="secondLastChar" select="substring($base64String, string-length($base64String) - 1, 1)" />
        <xsl:variable name="lastChar" select="substring($base64String, string-length($base64String), 1)" />

        <!-- execute if 2nd last character is not a =, and last character is = -->
        <xsl:if test="($secondLastChar != '=') and ($lastChar = '=')">
            <xsl:variable name="binaryBase64String">
                <xsl:call-template name="base64StringToBinary">
                    <xsl:with-param name="string" select="substring($base64String, 1, string-length($base64String) - 4)"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="$binaryBase64String" />
            </xsl:call-template>
            <xsl:variable name="partialBinary">
                <xsl:call-template name="base64StringToBinary">
                    <xsl:with-param name="string" select="substring($base64String, string-length($base64String) - 3, 3)" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="substring($partialBinary, 1, 8)" />
            </xsl:call-template>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="substring($partialBinary, 9, 8)" />
            </xsl:call-template>
        </xsl:if>

        <!-- execute if last 2 characters are both = -->
        <xsl:if test="($secondLastChar = '=') and ($lastChar = '=')">
            <xsl:variable name="binaryBase64String">
                <xsl:call-template name="base64StringToBinary">
                    <xsl:with-param name="string" select="substring($base64String, 1, string-length($base64String) - 4)"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="$binaryBase64String" />
            </xsl:call-template>
            <xsl:variable name="partialBinary">
                <xsl:call-template name="base64StringToBinary">
                    <xsl:with-param name="string" select="substring($base64String, string-length($base64String) - 3, 2)" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString" select="substring($partialBinary, 1, 8)" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Template to convert the base64 binary string to ascii representation -->
    <xsl:template name="base64BinaryStringToAscii">
        <xsl:param name="binaryString" />

        <xsl:if test="substring($binaryString, 1, 8) != ''">
            <xsl:variable name="asciiDecimal">
                <xsl:call-template name="binaryToDecimal">
                    <xsl:with-param name="binary" select="substring($binaryString, 1, 8)" />
                    <xsl:with-param name="sum" select="0" />
                    <xsl:with-param name="index" select="0" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="$datamap/datamap/asciidecimal/char[decimal = $asciiDecimal]/ascii" />
            <xsl:call-template name="base64BinaryStringToAscii">
                <xsl:with-param name="binaryString"  select="substring($binaryString, 9)" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Template to convert a binary number to decimal representation; this template calls template pow -->
    <xsl:template name="binaryToDecimal">
        <xsl:param name="binary"/>
        <xsl:param name="sum"/>
        <xsl:param name="index"/>
        <xsl:if test="substring($binary,string-length($binary) - 1) != ''">
            <xsl:variable name="power">
                <xsl:call-template name="pow">
                    <xsl:with-param name="m" select="2"/>
                    <xsl:with-param name="n" select="$index"/>
                    <xsl:with-param name="result" select="1"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="binaryToDecimal">
                <xsl:with-param name="binary" select="substring($binary, 1, string-length($binary) - 1)"/>
                <xsl:with-param name="sum" select="$sum + substring($binary,string-length($binary)) * $power"/>
                <xsl:with-param name="index" select="$index + 1"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="substring($binary,string-length($binary) - 1) = ''">
            <xsl:value-of select="$sum"/>
        </xsl:if>
    </xsl:template>

    <!-- Template to calculate m to the power n -->
    <xsl:template name="pow">
        <xsl:param name="m"/>
        <xsl:param name="n"/>
        <xsl:param name="result"/>
        <xsl:if test="$n &gt;= 1">
            <xsl:call-template name="pow">
                <xsl:with-param name="m" select="$m"/>
                <xsl:with-param name="n" select="$n - 1"/>
                <xsl:with-param name="result" select="$result * $m"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$n = 0">
            <xsl:value-of select="$result"/>
        </xsl:if>
    </xsl:template>

    <!-- Template to convert a base64 string to binary representation; this template calls template decimalToBinary -->
    <xsl:template name="base64StringToBinary">
        <xsl:param name="string" />

        <xsl:if test="substring($string, 1, 1) != ''">
            <xsl:variable name="binary">
                <xsl:call-template name="decimalToBinary">
                    <xsl:with-param name="decimal" select="$datamap/datamap/decimalbase64/char[base64 = substring($string, 1, 1)]/value" />
                    <xsl:with-param name="prev" select="''" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="padZeros">
                <xsl:with-param name="string" select="$binary" />
                <xsl:with-param name="no" select="6 - string-length($binary)" />
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="substring($string, 2) != ''">
            <xsl:call-template name="base64StringToBinary">
                <xsl:with-param name="string" select="substring($string, 2)" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Template to left pad a binary string, with the specified no of 0s, to make it of length 6 -->
    <xsl:template name="padZeros">
        <xsl:param name="string" />
        <xsl:param name="no" />

        <xsl:if test="$no &gt; 0">
            <xsl:call-template name="padZeros">
                <xsl:with-param name="string" select="concat('0', $string)" />
                <xsl:with-param name="no" select="6 - string-length($string) - 1" />
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$no = 0">
            <xsl:value-of select="$string" />
        </xsl:if>
    </xsl:template>

    <!-- Template to convert a decimal number to binary representation -->
    <xsl:template name="decimalToBinary">
        <xsl:param name="decimal"/>
        <xsl:param name="prev"/>

        <xsl:variable name="divresult" select="floor($decimal div 2)"/>
        <xsl:variable name="modresult" select="$decimal mod 2"/>
        <xsl:choose>
            <xsl:when test="$divresult &gt; 1">
                <xsl:call-template name="decimalToBinary">
                    <xsl:with-param name="decimal" select="$divresult"/>
                    <xsl:with-param name="prev" select="concat($modresult, $prev)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$divresult = 0">
                <xsl:value-of select="concat($modresult, $prev)"/>
            </xsl:when>
            <xsl:when test="$divresult = 1">
                <xsl:text>1</xsl:text>
                <xsl:value-of select="concat($modresult, $prev)"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
