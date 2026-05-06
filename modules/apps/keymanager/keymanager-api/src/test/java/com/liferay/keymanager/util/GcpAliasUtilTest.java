/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tomas Polesovsky
 */
public class GcpAliasUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		new LiferayUnitTestRule();

	@Test
	public void testNormalize() {

		// Dots and spaces become hyphens

		Assert.assertEquals(
			"jdbc-default-password",
			GcpAliasUtil.normalize("jdbc.default.password"));
		Assert.assertEquals(
			"my-secret-key", GcpAliasUtil.normalize("my secret key"));

		// Other punctuation becomes underscore; runs are preserved

		Assert.assertEquals(
			"my---secret---key___",
			GcpAliasUtil.normalize("my---secret...key!!!"));

		// Whitespace at the boundaries is mapped just like elsewhere

		Assert.assertEquals(
			"--leading-spaces--", GcpAliasUtil.normalize("  leading.spaces  "));

		// Null in -> null out

		Assert.assertNull(GcpAliasUtil.normalize(null));

		// Already-conforming input passes through

		Assert.assertEquals(
			"NormalString123", GcpAliasUtil.normalize("NormalString123"));
	}

}
