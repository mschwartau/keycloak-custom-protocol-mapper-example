FROM jboss/keycloak:4.5.0.Final

# Add own mapper to keycloak
# Idea is based on https://github.com/arielcarrera/keycloak-docker-oracle
ADD ./protocol-mapper/src/main/module/changeProvider.xsl /opt/jboss/keycloak/
RUN java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone.xml -xsl:/opt/jboss/keycloak/changeProvider.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone.xml; java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml -xsl:/opt/jboss/keycloak/changeProvider.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml; rm /opt/jboss/keycloak/changeProvider.xsl

RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/hamburg/schwartau/keycloak-custom-protocol-mapper-example/main
ADD ./protocol-mapper/target/keycloak-custom-protocol-mapper-example.jar /opt/jboss/keycloak/modules/system/layers/base/hamburg/schwartau/keycloak-custom-protocol-mapper-example/main/keycloak-custom-protocol-mapper-example.jar
ADD ./protocol-mapper/src/main/module/module.xml /opt/jboss/keycloak/modules/system/layers/base/hamburg/schwartau/keycloak-custom-protocol-mapper-example/main