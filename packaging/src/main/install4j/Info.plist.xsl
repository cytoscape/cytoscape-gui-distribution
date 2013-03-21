<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/plist/dict">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
            
            <!-- Ticket #1496: Add keys to enable high resolution mode -->
            <key>NSHighResolutionCapable</key>
            <true/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>