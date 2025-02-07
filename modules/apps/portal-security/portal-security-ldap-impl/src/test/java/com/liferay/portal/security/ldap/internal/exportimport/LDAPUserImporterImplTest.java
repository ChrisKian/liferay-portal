/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.exportimport;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.security.ldap.SafeLdapContext;
import com.liferay.portal.security.ldap.SafePortalLDAP;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import javax.naming.InvalidNameException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jorge Díaz
 */
@RunWith(Arquillian.class)
public class LDAPUserImporterImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testApacheDS() throws Exception {
		String baseProviderURL = "ldap://localhost:10389";
		String principal = "uid=admin,ou=system";
		String credentials = "secret";

		SafeLdapContext safeLdapContext = _safePortalLDAP.getSafeLdapContext(
			TestPropsValues.getCompanyId(), baseProviderURL, principal,
			credentials);

		if (safeLdapContext != null) {
			Assert.fail("Found the correct mapping!");
		}

		baseProviderURL = "ldap://0.0.0.0:10389";

		safeLdapContext = _safePortalLDAP.getSafeLdapContext(
			TestPropsValues.getCompanyId(), baseProviderURL, principal,
			credentials);

		if (safeLdapContext != null) {
			Assert.fail("Found the correct mapping!");
		}
	}

	@Test
	public void testBindingInNamespaceEscape() throws InvalidNameException {
		Assert.assertEquals(
			"cn=User\\\\,with\\\\,commas,ou=users,dc=example,dc=com",
			escapeLDAPName(
				"cn=User\\,with\\,commas,ou=users,dc=example,dc=com"));
		Assert.assertEquals(
			"cn=User\\\\2cwith\\\\2ccommas,ou=users,dc=example,dc=com",
			escapeLDAPName(
				"cn=User\\2cwith\\2ccommas,ou=users,dc=example,dc=com"));
	}

	@Test
	public void testOpenLDAP() throws Exception {
		String baseProviderURL = "ldap://localhost:389";
		String principal = "cn=admin,ou=test";
		String credentials = "secret";

		SafeLdapContext safeLdapContext = _safePortalLDAP.getSafeLdapContext(
			TestPropsValues.getCompanyId(), baseProviderURL, principal,
			credentials);

		if (safeLdapContext != null) {
			Assert.fail("Found the correct mapping!");
		}

		baseProviderURL = "ldap://0.0.0.0:389";

		safeLdapContext = _safePortalLDAP.getSafeLdapContext(
			TestPropsValues.getCompanyId(), baseProviderURL, principal,
			credentials);

		if (safeLdapContext != null) {
			Assert.fail("Found the correct mapping!");
		}
	}

	protected String escapeLDAPName(String query) {
		return _ldapUserImporterImpl.escapeLDAPName(query);
	}

	private static final LDAPUserImporterImpl _ldapUserImporterImpl =
		new LDAPUserImporterImpl();

	@Inject
	private volatile SafePortalLDAP _safePortalLDAP;

}