<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs">
    
    <xsl:output indent="yes"/>
    
    <xsl:template match="/">
<assembly>
    <id><xsl:value-of select="/config/id"/></id>

    <xsl:if test="/config/id = 'dev'">
        <includeBaseDirectory>false</includeBaseDirectory>
    </xsl:if>
    
    <formats>
        <format><xsl:value-of select="/config/output-format"/></format>
    </formats>

    <fileSets>
        <fileSet>
            <directory><xsl:value-of select="/config/karaf/distribution/@base"/></directory>
            <outputDirectory>/framework</outputDirectory>
            <excludes>
                <exclude>**/demos/**</exclude>
                <exclude>bin/**</exclude>
                <exclude>etc/custom.properties</exclude>
                <exclude>etc/system.properties</exclude>
                <exclude>etc/users.properties</exclude>
                <exclude>etc/org.ops4j.pax.url.mvn.cfg</exclude>
                <exclude>etc/org.apache.karaf.features.cfg</exclude>
                <exclude>etc/org.apache.felix.fileinstall-deploy.cfg</exclude>
                <exclude>etc/org.ops4j.pax.logging.cfg</exclude>
                <exclude>etc/jre.properties</exclude>
                <exclude>LICENSE</exclude>
                <exclude>NOTICE</exclude>
                <exclude>README</exclude>
                <exclude>RELEASE-NOTES</exclude>
                <exclude>karaf-manual*.html</exclude>
                <exclude>karaf-manual*.pdf</exclude>
            </excludes>
        </fileSet>

        <!-- Copy over bin/* separately to get the correct file mode -->
        <fileSet>
            <directory>target/dependencies/launcher</directory>
            <outputDirectory>/framework</outputDirectory>
            <includes>
                <xsl:for-each select="scripts/script">
                    <include><xsl:value-of select="."/></include>
                </xsl:for-each>
            </includes>
            <excludes>
                <exclude>*.jar</exclude>
            </excludes>
            <fileMode>0755</fileMode>
        </fileSet>

        <fileSet>
            <directory>src/main/bin</directory>
            <outputDirectory>/</outputDirectory>
            <lineEnding><xsl:value-of select="/config/line-ending"/></lineEnding>
            <fileMode>0755</fileMode>
            <includes>
                <xsl:for-each select="/config/cytoscape/scripts/script">
                    <include><xsl:value-of select="."/></include>
                </xsl:for-each>
            </includes>
        </fileSet>

        <fileSet>
            <directory>target/dependencies/branding</directory>
            <includes>
                <include>*.jar</include>
            </includes>
            <outputDirectory>/framework/lib</outputDirectory>
        </fileSet>

        <fileSet>
            <directory>target/dependencies/launcher</directory>
            <includes>
                <include>*.jar</include>
            </includes>
            <outputDirectory>/framework/lib</outputDirectory>
        </fileSet>

        <fileSet>
            <directory>target/dependencies/splash-launcher/images</directory>
            <includes>
                <include>CytoscapeSplashScreen.png</include>
            </includes>
            <outputDirectory>/framework</outputDirectory>
        </fileSet>

        <fileSet>
            <directory>src/main/resources</directory>
            <includes>
                <include>*.png</include>
            </includes>
            <outputDirectory>/framework</outputDirectory>
        </fileSet>
        
        <fileSet>
            <directory>src/main/distribution</directory>
            <outputDirectory>/framework</outputDirectory>
            <fileMode>0644</fileMode>
        </fileSet>
        <fileSet>
            <directory>target/classes/etc</directory>
            <outputDirectory>/framework/etc/</outputDirectory>
            <lineEnding><xsl:value-of select="/config/line-ending"/></lineEnding>
            <fileMode>0644</fileMode>
            <excludes>
                <exclude>**/*.formatted</exclude>
            </excludes>
        </fileSet>

        <fileSet>
            <directory><xsl:value-of select="/config/karaf/feature-repository"/></directory>
            <outputDirectory>/framework/system</outputDirectory>
        </fileSet>
        
        <fileSet>
            <directory>src/main/resources/sampleData</directory>
            <includes>
                <include>**/*</include>
            </includes>
            <outputDirectory>/sampleData</outputDirectory>
        </fileSet>

    </fileSets>
</assembly>

</xsl:template>
</xsl:stylesheet>