<?xml version="1.0" encoding="UTF-8"?>
<!-- To register our own module, which contains the protocol mapper, it is not enough to just
add the jar file to the jboss server. We have to change the jboss xml file too. This is done
by this xslt file -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="//ds:subsystem/ds:providers" xmlns:ds="urn:jboss:domain:keycloak-server:1.1">
        <ds:providers>
            <!-- We add our own module, which contains the protocol mapper, to the providers here.
             Without that, we would be unable to configure keycloak to use our protocol mapper. It wouldn't
             be visible in the list of available protocol mappers. The name of the module must be the
             same as in the module.xml -->
            <ds:provider>module:hamburg.schwartau.keycloak-custom-protocol-mapper-example</ds:provider>
            <ds:provider>
                classpath:${jboss.home.dir}/providers/*
            </ds:provider>
        </ds:providers>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

