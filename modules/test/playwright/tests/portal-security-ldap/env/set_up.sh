#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}

source ${CURRENT_DIR_NAME}/../../../env/common.sh

function ldap_set_up {

	local ldifFile="${CURRENT_DIR_NAME}/ldif.ldif"

	#Apache DS
	/bin/bash -c ldapadd -c -D uid=admin,ou=system -f ${ldifFile} -H ldap://0.0.0.0:10389 -v -w secret -x

	#OpenLDAP
	/bin/bash -c ldapadd -c -D cn=admin,ou=test -f ${ldifFile} -H ldap://0.0.0.0:389 -v -w secret -x
}

function main {
	default_set_up

	ldap_set_up
}

main "${@}"