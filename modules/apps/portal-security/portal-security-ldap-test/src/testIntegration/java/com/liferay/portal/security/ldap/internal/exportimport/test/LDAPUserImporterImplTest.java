/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.security.ldap.SafeLdapContext;
import com.liferay.portal.security.ldap.SafePortalLDAP;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class LDAPUserImporterImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testOpenLDAP() throws Exception {
		String baseProviderURL = "ldap://localhost:389";
		String principal = "cn=admin,dc=example,dc=com";
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

		baseProviderURL = "ldap://127.0.0.1:389";

		safeLdapContext = _safePortalLDAP.getSafeLdapContext(
			TestPropsValues.getCompanyId(), baseProviderURL, principal,
			credentials);

		if (safeLdapContext != null) {
			Assert.fail("Found the correct mapping!");
		}
	}

	@Inject
	private volatile SafePortalLDAP _safePortalLDAP;

}