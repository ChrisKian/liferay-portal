#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}

source ${CURRENT_DIR_NAME}/../../../env/common.sh

function simple_ldap_set_up {

	# These steps will no longer be needed with next CI release
	# Find slapd.ldif file
	local slapdLdif="/usr/local/etc/openldap/slapd.ldif"

	# Append our DB definition
	local databaseDefinition="${CURRENT_DIR_NAME}/slapd.ldif"

	echo ${databaseDefinition} >> ${slapdLdif}

	# Create slapd.d dir

	mkdir /usr/local/etc/slapd.d

	# Import configuration database

	/usr/local/sbin/slapadd -n 0 -F /usr/local/etc/slapd.d -l ${slapdLdif}

	# These steps will always be needed

	# Start SLAPD
	 /usr/local/libexec/slapd -F /usr/local/etc/slapd.d

	# Test 1
	echo "LDAP Test 1:"
	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi

	# Add user via ldif file
	local ldifFile="${CURRENT_DIR_NAME}/simple.ldif"

	ldapadd -x -D "cn=admin,dc=example,dc=com" -W -f ${ldifFile}

	# Test 2
	ldapsearch -x -b 'dc=example,dc=com' '(objectclass=*)'

	echo "LDAP Test 2:"
	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi
}

function main {
	default_set_up

	simple_ldap_set_up
}

main "${@}"