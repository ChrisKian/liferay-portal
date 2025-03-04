#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}

source ${CURRENT_DIR_NAME}/../../../env/common.sh

function main {
	default_tear_down

	simple_ldap_tear_down
}

function simple_ldap_tear_down {

	# Remove Groups and Users (may not be needed if slapd.d removal takes care of it)

	local deleteGroupsAndUsersLdif="${CURRENT_DIR_NAME}/deleteGroupsAndUsers.ldif"

	ldapdelete -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${deleteGroupsAndUsersLdif}

	# These steps will no longer be needed with next CI release
	# Remove DB changes

	# Find slapd.ldif file and count it's lines
	local slapdLdif="/usr/local/etc/openldap/slapd.ldif"
	local originalSlapdLdif="${CURRENT_DIR_NAME}/slapd.ldif"
#	local slapdwc=$(grep -c "^" ${slapdLdif})
#
#	# Count our DB definition lines
#	local databaseDefinition="${CURRENT_DIR_NAME}/slapd.ldif"
#	local ddwc=$(grep -c "^" ${databaseDefinition})
#	local diffwc=$(echo $((${slapdwc}-${ddwc})))
#
#	# Remove trailing database definition from slapd.ldif
#	head -n ${diffwc} ${slapdLdif} > temp.ldif && mv temp.ldif ${slapdLdif}

	cp ${slapdLdif} ${originalSlapdLdif}

	echo "CI's slapd.ldif after tear_down:"
	echo "$(<${slapdLdif})"

	# Test since dir might be different
	#echo "LDAP Test 3:"

	# Stop slapd service
	kill -INT `cat /usr/local/var/run/slapd.pid`

	# Not needed since above is correct dir
	#if [ $? -ne 0 ]; then
	#	echo "Command failed with exit status $?"
	#	kill -INT `cat /usr/local/var/slapd.pid`
	#else
	#	echo "Command succeeded"
	#fi

	# Delete slapd.d dir (will need to be modified instead after release,
	# or maybe just a file or two removed, not sure yet)

	rm -rf /usr/local/etc/slapd.d
}

main "${@}"