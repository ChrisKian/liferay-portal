/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.configuration.persistence.listener;

import com.liferay.keymanager.spi.fips.FipsComplianceChecker;
import com.liferay.keymanager.spi.fips.FipsReport;
import com.liferay.keymanager.spi.fips.FipsValidator;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Tomas Polesovsky
 */
public class KeyManagerConfigurationModelListenerTest {

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		_keyManagerConfigurationModelListener =
			new KeyManagerConfigurationModelListener();

		_injectField(
			_keyManagerConfigurationModelListener, "_fipsComplianceChecker",
			_fipsComplianceChecker);
		_injectField(
			_keyManagerConfigurationModelListener, "_serviceTrackerMap",
			_serviceTrackerMap);
	}

	@Test
	public void testCompliantPassesUnchanged() throws Exception {
		String pid = "com.example.Pid";

		Mockito.when(
			_serviceTrackerMap.getService(pid)
		).thenReturn(
			_fipsValidator
		);

		Mockito.when(
			_fipsValidator.validate(Mockito.anyMap())
		).thenReturn(
			FipsReport.compliant()
		);

		_keyManagerConfigurationModelListener.onBeforeSave(
			pid, _properties("key", "value"));

		Mockito.verifyNoInteractions(_fipsComplianceChecker);
	}

	@Test
	public void testNoncompliantStandardModeWarnsAndPasses() throws Exception {
		String pid = "com.example.Pid";

		Mockito.when(
			_serviceTrackerMap.getService(pid)
		).thenReturn(
			_fipsValidator
		);

		Mockito.when(
			_fipsValidator.validate(Mockito.anyMap())
		).thenReturn(
			FipsReport.noncompliant("Algorithm not allowed")
		);

		Mockito.when(
			_fipsComplianceChecker.isFipsEnforced()
		).thenReturn(
			false
		);

		// Should not throw

		_keyManagerConfigurationModelListener.onBeforeSave(
			pid, _properties("k", "v"));
	}

	@Test
	public void testNoncompliantStrictModeRejects() {
		String pid =
			"com.liferay.keymanager.provider.gcp.internal.configuration." +
				"GcpKmsCompanyCryptoVaultProviderConfiguration";

		Mockito.when(
			_fipsComplianceChecker.isFipsEnforced()
		).thenReturn(
			true
		);

		Mockito.when(
			_serviceTrackerMap.getService(pid)
		).thenReturn(
			_fipsValidator
		);

		Mockito.when(
			_fipsValidator.validate(Mockito.anyMap())
		).thenReturn(
			FipsReport.noncompliant(
				"SOFTWARE protection level is not allowed when FIPS is " +
					"enforced.")
		);

		try {
			_keyManagerConfigurationModelListener.onBeforeSave(
				pid, _properties("newKeyProtectionLevel", "SOFTWARE"));

			Assert.fail("Expected ConfigurationModelListenerException");
		}
		catch (ConfigurationModelListenerException
					configurationModelListenerException) {

			Assert.assertTrue(
				configurationModelListenerException.getMessage(
				).contains(
					"SOFTWARE protection level is not allowed"
				));
		}
	}

	@Test
	public void testNonkeymanagerPidWithValidatorIsValidated()
		throws Exception {

		// Hardening: drop the prefix filter — any PID with a registered
		// FipsValidator is validated, regardless of namespace.

		String pid = "com.foreign.bundle.Configuration";

		Mockito.when(
			_serviceTrackerMap.getService(pid)
		).thenReturn(
			_fipsValidator
		);

		Mockito.when(
			_fipsValidator.validate(Mockito.anyMap())
		).thenReturn(
			FipsReport.compliant()
		);

		_keyManagerConfigurationModelListener.onBeforeSave(
			pid, _properties("k", "v"));

		Mockito.verify(
			_fipsValidator
		).validate(
			Mockito.anyMap()
		);
	}

	@Test
	public void testNoValidatorRegisteredIsNoOp() throws Exception {
		Mockito.when(
			_serviceTrackerMap.getService(Mockito.anyString())
		).thenReturn(
			null
		);

		_keyManagerConfigurationModelListener.onBeforeSave(
			"any.pid", _properties("k", "v"));

		Mockito.verifyNoInteractions(_fipsComplianceChecker, _fipsValidator);
	}

	@Rule
	public final LiferayUnitTestRule liferayUnitTestRule =
		new LiferayUnitTestRule();

	private void _injectField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass(
			).getDeclaredField(
				fieldName
			);

			field.setAccessible(true);
			field.set(target, value);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private Dictionary<String, Object> _properties(String key, Object value) {
		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put(key, value);

		return properties;
	}

	@Mock
	private FipsComplianceChecker _fipsComplianceChecker;

	@Mock
	private FipsValidator _fipsValidator;

	private KeyManagerConfigurationModelListener
		_keyManagerConfigurationModelListener;

	@Mock
	private ServiceTrackerMap<String, FipsValidator> _serviceTrackerMap;

}