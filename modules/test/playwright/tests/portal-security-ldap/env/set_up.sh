#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}

source ${CURRENT_DIR_NAME}/../../../env/common.sh

function simple_ldap_set_up {

	# These steps will no longer be needed with next CI release
	# Find slapd.ldif file
#	local slapdLdif="/usr/local/etc/openldap/slapd.ldif"
#
#	# Check if exists
#	if [ -e ${slapdLdif} ]
#	then
#		echo "CI's slapd.ldif:"
#		echo "$(<${slapdLdif})"
#	else
#		echo "slapd.ldif does not exist"
#	fi
#
#	# Append our DB definition
#	local databaseDefinition="${CURRENT_DIR_NAME}/slapd.ldif"
#
#	# Check if exists
#	if [ -e ${databaseDefinition} ]
#	then
#		echo "database definition:"
#		echo "$(<${databaseDefinition})"
#	else
#		echo "database definition does not exist"
#	fi
#
#	# Compare final line of both files and append only if necessary
#	test `tail -1 "${slapdLdif}"` == `tail -1 "${databaseDefinition}"`
#
#	if [ $? -ne 0 ]; then
#		echo "files are different, appending our DB def"
#		echo ${databaseDefinition} >> ${slapdLdif}
#	else
#		echo "DB def already appended, skipping"
#	fi

	# Make life easy and just copy the entire correct file
	local slapdLdif="/usr/local/etc/openldap/slapd.ldif"
	local slapdWithDatabaseDefinitionLdif="${CURRENT_DIR_NAME}/slapdWithDatabaseDefinition.ldif"

	cp ${slapdWithDatabaseDefinitionLdif} ${slapdLdif}


	# Delete slapd.d dir if it exists
	if [ -d /usr/local/etc/slapd.d ]
	then
		echo "deleting slapd.d"
		rm -rf /usr/local/etc/slapd.d
	else
		echo "slapd.d does not exist"
	fi

	mkdir /usr/local/etc/slapd.d

	# Check if slapd.d now exists
	if [ -d /usr/local/etc/slapd.d ]
	then
		echo "slapd.d exists"
	else
		echo "slapd.d still does not exist"
	fi

	# Import configuration database

	echo "Performing slapadd:"
	/usr/local/sbin/slapadd -n 0 -F /usr/local/etc/slapd.d -l ${slapdLdif}

	# These steps will always be needed

	# Test 1
	echo "LDAP Test 1:"

	# Start SLAPD
	 /usr/local/libexec/slapd -F /usr/local/etc/slapd.d

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi

	echo "LDAP Test 1.5:"

	ldapsearch -x -b '' -s base '(objectclass=*)' namingContexts

	# Add user via ldif file
	# Check if exists
	local ldifFile="${CURRENT_DIR_NAME}/simple.ldif"

	if [ -e ${ldifFile} ]
	then
		echo "simple.ldif:"
		echo "$(<${ldifFile})"
	else
		echo "simple.ldif does not exist"
	fi

	ldapadd -x -D "cn=admin,dc=example,dc=com" -w "secret" -f ${ldifFile}

	# Test 2
	echo "LDAP Test 2:"

	# May need -w arg here too, but not sure
	ldapsearch -x -b 'dc=example,dc=com' '(objectclass=*)'

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