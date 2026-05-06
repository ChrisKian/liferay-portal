/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.fips;

import com.liferay.keymanager.spi.fips.FipsComplianceChecker;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.security.Provider;
import java.security.Security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tomas Polesovsky
 */
public class FipsComplianceCheckerImplTest {

	@Before
	public void setUp() {
		_fipsComplianceChecker = new FipsComplianceCheckerImpl() {

			@Override
			public boolean isFipsEnforced() {
				return _enforced;
			}

		};
	}

	@Test
	public void testCheckPassesWhenBcfipsFirst() {
		_enforced = true;

		Provider originalFirst = null;

		try {
			Provider[] providers = Security.getProviders();

			if (providers.length > 0) {
				originalFirst = providers[0];

				Security.removeProvider(originalFirst.getName());
			}

			Security.insertProviderAt(
				new Provider("BCFIPS", 1.0, "Dummy BCFIPS") {
				},
				1);

			_fipsComplianceChecker.check();
		}
		finally {
			Security.removeProvider("BCFIPS");

			if (originalFirst != null) {
				Security.insertProviderAt(originalFirst, 1);
			}
		}
	}

	@Test
	public void testCheckSilentWhenNotEnforced() {
		_enforced = false;

		_fipsComplianceChecker.check();
	}

	@Test
	public void testCheckThrowsWhenBcfipsMissing() {
		_enforced = true;

		Provider originalFirst = null;

		try {
			Provider[] providers = Security.getProviders();

			if (providers.length > 0) {
				originalFirst = providers[0];

				Security.removeProvider(originalFirst.getName());
			}

			Security.insertProviderAt(
				new Provider("Dummy", 1.0, "Dummy") {
				},
				1);

			try {
				_fipsComplianceChecker.check();

				Assert.fail("Expected RuntimeException");
			}
			catch (RuntimeException runtimeException) {
				Assert.assertTrue(
					runtimeException.getMessage(
					).contains(
						"BCFIPS must be the first security provider"
					));
			}
		}
		finally {
			Security.removeProvider("Dummy");

			if (originalFirst != null) {
				Security.insertProviderAt(originalFirst, 1);
			}
		}
	}

	@Rule
	public final LiferayUnitTestRule liferayUnitTestRule =
		new LiferayUnitTestRule();

	private boolean _enforced;
	private FipsComplianceChecker _fipsComplianceChecker;

}