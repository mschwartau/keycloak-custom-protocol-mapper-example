#!/bin/sh

if [ "${POPULATE_TEST_DATA}" = "true" ]; then
  eval "/opt/keycloak/populate-data.sh | tee populate-data.log &"
fi

exec /opt/keycloak/bin/kc.sh "$@"