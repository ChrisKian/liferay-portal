/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager;

import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Chris Kian
 */
public class ConfigurationKeyReferenceTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testConstructorDefaults() {
		ConfigurationKeyReference configurationKeyReference =
			new ConfigurationKeyReference("com.example.Pid", "myKey");

		Assert.assertEquals(
			CompanyConstants.SYSTEM,
			configurationKeyReference.getCompanyId());
		Assert.assertEquals(
			GroupConstants.DEFAULT_PARENT_GROUP_ID,
			configurationKeyReference.getGroupId());
		Assert.assertEquals(
			"com.example.Pid", configurationKeyReference.getPid());
		Assert.assertEquals("myKey", configurationKeyReference.getKey());
		Assert.assertEquals(
			KeyReference.ANY_PROVIDER,
			configurationKeyReference.getProviderId());
		Assert.assertEquals(
			KeyReference.Type.SECRET, configurationKeyReference.getType());
	}

	@Test
	public void testFromStringInvalid() {
		Assert.assertNull(ConfigurationKeyReference.fromString(null));
		Assert.assertNull(ConfigurationKeyReference.fromString("not-a-ref"));

		// Outer ref valid, inner identifier missing the config: prefix

		Assert.assertNull(
			ConfigurationKeyReference.fromString(
				"${secretRef:*:plain-identifier}"));
	}

	@Test
	public void testFromStringRoundTrip() {
		ConfigurationKeyReference original = new ConfigurationKeyReference(
			"com.example.Pid", "myKey", 42L, 7L);

		ConfigurationKeyReference parsed = ConfigurationKeyReference.fromString(
			original.toString());

		Assert.assertNotNull(parsed);
		Assert.assertEquals("com.example.Pid", parsed.getPid());
		Assert.assertEquals("myKey", parsed.getKey());
		Assert.assertEquals(42L, parsed.getCompanyId());
		Assert.assertEquals(7L, parsed.getGroupId());
	}

	@Test
	public void testIsConfigurationKeyReference() {
		ConfigurationKeyReference configurationKeyReference =
			new ConfigurationKeyReference("com.example.Pid", "myKey");

		Assert.assertTrue(
			ConfigurationKeyReference.isConfigurationKeyReference(
				configurationKeyReference.toString()));
		Assert.assertFalse(
			ConfigurationKeyReference.isConfigurationKeyReference(
				"${secretRef:*:plain}"));
		Assert.assertFalse(
			ConfigurationKeyReference.isConfigurationKeyReference(null));
	}

}
