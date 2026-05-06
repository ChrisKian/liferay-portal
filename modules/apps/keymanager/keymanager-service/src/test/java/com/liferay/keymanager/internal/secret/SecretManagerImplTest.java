/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.secret;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.secret.SecretManager;
import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;
import com.liferay.keymanager.spi.fips.FipsComplianceChecker;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.keymanager.spi.secret.SecretVaultReader;
import com.liferay.keymanager.spi.secret.SecretVaultWriter;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Chris Kian
 */
public class SecretManagerImplTest {

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		_secretManagerImpl = new SecretManagerImpl();

		_inject("_auditRouter", _auditRouter);
		_inject("_fipsComplianceChecker", _fipsComplianceChecker);
		_inject("_profileOrchestrator", _profileOrchestrator);
		_inject(
			"_readerServiceTrackerMap", _readerServiceTrackerMap);
		_inject(
			"_writerServiceTrackerMap", _writerServiceTrackerMap);
	}

	@Test(expected = SecretManagerException.class)
	public void testDeleteThrowsWhenNoWriterRegistered() throws Exception {
		Mockito.when(
			_writerServiceTrackerMap.getService("db")
		).thenReturn(
			null
		);

		_secretManagerImpl.deleteSecret(
			7L, KeyReference.fromString("${secretRef:db:k}"));
	}

	@Test
	public void testGetSecretAuditEmittedBeforeReader() throws Exception {
		Mockito.when(
			_readerServiceTrackerMap.getService("db")
		).thenReturn(
			_secretVaultReader
		);
		Mockito.when(
			_secretVaultReader.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);
		Mockito.when(
			_secretVaultReader.getSecret(7L, "k")
		).thenReturn(
			new SecureSecret(
				KeyReference.fromString("${secretRef:db:k}"), new byte[] {0})
		);

		_secretManagerImpl.getSecret(
			7L, KeyReference.fromString("${secretRef:db:k}"));

		InOrder inOrder = Mockito.inOrder(_auditRouter, _secretVaultReader);

		inOrder.verify(
			_auditRouter
		).route(
			Mockito.any(AuditMessage.class)
		);
		inOrder.verify(
			_secretVaultReader
		).getSecret(
			7L, "k"
		);
	}

	@Test
	public void testPutSecretRoutesToWriterAndAudits() throws Exception {
		Mockito.when(
			_writerServiceTrackerMap.getService("db")
		).thenReturn(
			_secretVaultWriter
		);
		Mockito.when(
			_secretVaultWriter.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			true
		);

		KeyReference keyReference = KeyReference.fromString(
			"${secretRef:db:my-secret}");

		try (SecureSecret secureSecret = new SecureSecret(
				keyReference, "password")) {

			_secretManagerImpl.putSecret(7L, secureSecret);
		}

		ArgumentCaptor<AuditMessage> argumentCaptor = ArgumentCaptor.forClass(
			AuditMessage.class);

		Mockito.verify(
			_auditRouter
		).route(
			argumentCaptor.capture()
		);

		AuditMessage auditMessage = argumentCaptor.getValue();

		Assert.assertEquals(
			"keymanager.secret.putSecret", auditMessage.getEventType());
		Assert.assertEquals(7L, auditMessage.getCompanyId());
		Assert.assertEquals(
			SecretManager.class.getName(), auditMessage.getClassName());

		JSONObject additionalInfo = auditMessage.getAdditionalInfo();

		Assert.assertEquals("db", additionalInfo.getString("providerId"));
		Assert.assertEquals(
			"my-secret", additionalInfo.getString("identifier"));
		Assert.assertNotNull(additionalInfo.getString("inputHashSHA256"));

		Mockito.verify(
			_secretVaultWriter
		).putSecret(
			Mockito.eq(7L), Mockito.any(SecureSecret.class)
		);
	}

	@Test
	public void testWildcardCompanyRoutesToProfileCompanySecret()
		throws Exception {

		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			_keyManagerProfile
		);
		Mockito.when(
			_keyManagerProfile.getCompanySecretProviderId()
		).thenReturn(
			"company-db"
		);
		Mockito.when(
			_writerServiceTrackerMap.getService("company-db")
		).thenReturn(
			_secretVaultWriter
		);
		Mockito.when(
			_secretVaultWriter.isAllowedCompany(7L)
		).thenReturn(
			true
		);

		KeyReference keyReference = KeyReference.fromString(
			"${secretRef:*:my-secret}");

		try (SecureSecret secureSecret = new SecureSecret(
				keyReference, "password")) {

			_secretManagerImpl.putSecret(7L, secureSecret);
		}

		Mockito.verify(_keyManagerProfile).getCompanySecretProviderId();
		Mockito.verify(
			_secretVaultWriter
		).putSecret(
			Mockito.eq(7L), Mockito.any(SecureSecret.class)
		);
	}

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	private void _inject(String fieldName, Object value) {
		try {
			Field field = SecretManagerImpl.class.getDeclaredField(fieldName);

			field.setAccessible(true);
			field.set(_secretManagerImpl, value);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Mock
	private AuditRouter _auditRouter;

	@Mock
	private FipsComplianceChecker _fipsComplianceChecker;

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private ProfileOrchestrator _profileOrchestrator;

	@Mock
	private ServiceTrackerMap<String, SecretVaultReader>
		_readerServiceTrackerMap;

	private SecretManagerImpl _secretManagerImpl;

	@Mock
	private SecretVaultReader _secretVaultReader;

	@Mock
	private SecretVaultWriter _secretVaultWriter;

	@Mock
	private ServiceTrackerMap<String, SecretVaultWriter>
		_writerServiceTrackerMap;

}
