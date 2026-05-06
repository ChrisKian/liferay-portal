/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.crypto;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.crypto.CryptoManager;
import com.liferay.keymanager.crypto.CryptoManagerException;
import com.liferay.keymanager.spi.crypto.CryptoVaultProvider;
import com.liferay.keymanager.spi.fips.FipsComplianceChecker;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.portal.kernel.audit.AuditException;
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
public class CryptoManagerImplTest {

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		_cryptoManagerImpl = new CryptoManagerImpl();

		_inject("_auditRouter", _auditRouter);
		_inject("_fipsComplianceChecker", _fipsComplianceChecker);
		_inject("_profileOrchestrator", _profileOrchestrator);
		_inject("_serviceTrackerMap", _serviceTrackerMap);
	}

	@Test
	public void testAuditEmittedBeforeProviderDelegation() throws Exception {
		_stubResolveAndAllow("db", true);

		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.eq(7L), Mockito.eq("k"), Mockito.any(byte[].class))
		).thenReturn(
			new byte[] {9}
		);

		_cryptoManagerImpl.encrypt(
			7L, KeyReference.fromString("${keyRef:db:k}"), new byte[] {1});

		InOrder inOrder = Mockito.inOrder(_auditRouter, _cryptoVaultProvider);

		inOrder.verify(
			_auditRouter
		).route(
			Mockito.any(AuditMessage.class)
		);
		inOrder.verify(
			_cryptoVaultProvider
		).encrypt(
			Mockito.anyLong(), Mockito.anyString(), Mockito.any(byte[].class));
	}

	@Test
	public void testAuditFailureDoesNotBlockOperation() throws Exception {
		_stubResolveAndAllow("db", true);

		Mockito.doThrow(
			new AuditException("simulated")
		).when(
			_auditRouter
		).route(
			Mockito.any(AuditMessage.class)
		);

		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.eq(7L), Mockito.eq("k"), Mockito.any(byte[].class))
		).thenReturn(
			new byte[] {9}
		);

		byte[] result = _cryptoManagerImpl.encrypt(
			7L, KeyReference.fromString("${keyRef:db:k}"), new byte[] {1});

		Assert.assertArrayEquals(new byte[] {9}, result);
	}

	@Test
	public void testEncryptAuditShapeContainsHash() throws Exception {
		_stubResolveAndAllow("db", true);

		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.eq(7L), Mockito.eq("k"), Mockito.any(byte[].class))
		).thenReturn(
			new byte[0]
		);

		_cryptoManagerImpl.encrypt(
			7L, KeyReference.fromString("${keyRef:db:k}"), new byte[] {1, 2});

		ArgumentCaptor<AuditMessage> argumentCaptor = ArgumentCaptor.forClass(
			AuditMessage.class);

		Mockito.verify(
			_auditRouter
		).route(
			argumentCaptor.capture()
		);

		AuditMessage auditMessage = argumentCaptor.getValue();

		Assert.assertEquals(
			"keymanager.crypto.encrypt", auditMessage.getEventType());
		Assert.assertEquals(7L, auditMessage.getCompanyId());
		Assert.assertEquals("${keyRef:db:k}", auditMessage.getClassPK());

		Assert.assertEquals(
			CryptoManager.class.getName(), auditMessage.getClassName());

		JSONObject additionalInfo = auditMessage.getAdditionalInfo();

		Assert.assertEquals("db", additionalInfo.getString("providerId"));
		Assert.assertEquals("k", additionalInfo.getString("identifier"));
		Assert.assertEquals(
			2, additionalInfo.getInt("inputByteLength"));
		Assert.assertNotNull(additionalInfo.getString("inputHashSHA256"));
	}

	@Test(expected = CryptoManagerException.class)
	public void testEncryptThrowsWhenNoProviderRegistered() throws Exception {
		Mockito.when(
			_serviceTrackerMap.getService("db")
		).thenReturn(
			null
		);

		_cryptoManagerImpl.encrypt(
			7L, KeyReference.fromString("${keyRef:db:k}"), new byte[] {1});
	}

	@Test
	public void testFipsCheckBeforeEncrypt() throws Exception {
		_stubResolveAndAllow("db", true);

		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(byte[].class))
		).thenReturn(
			new byte[0]
		);

		_cryptoManagerImpl.encrypt(
			7L, KeyReference.fromString("${keyRef:db:k}"), new byte[] {1});

		InOrder inOrder = Mockito.inOrder(
			_fipsComplianceChecker, _cryptoVaultProvider);

		inOrder.verify(_fipsComplianceChecker).check();
		inOrder.verify(
			_cryptoVaultProvider
		).encrypt(
			Mockito.anyLong(), Mockito.anyString(), Mockito.any(byte[].class));
	}

	@Test
	public void testImportSecretKeyZerosRawKeyMaterialEvenOnFailure()
		throws Exception {

		_stubResolveAndAllow("db", true);

		Mockito.when(
			_cryptoVaultProvider.importSecretKey(
				Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(byte[].class), Mockito.anyString())
		).thenThrow(
			new CryptoManagerException("provider boom")
		);

		byte[] rawKeyMaterial = {1, 2, 3, 4};

		try {
			_cryptoManagerImpl.importSecretKey(
				7L, "db", "k", rawKeyMaterial, "AES");

			Assert.fail();
		}
		catch (CryptoManagerException cryptoManagerException) {
			for (byte b : rawKeyMaterial) {
				Assert.assertEquals(0, b);
			}
		}
	}

	@Test
	public void testWildcardSystemRoutesToProfileSystemDek() throws Exception {
		Mockito.when(
			_profileOrchestrator.getActiveProfile()
		).thenReturn(
			_keyManagerProfile
		);
		Mockito.when(
			_keyManagerProfile.getSystemDekProviderId()
		).thenReturn(
			"system-db"
		);
		Mockito.when(
			_serviceTrackerMap.getService("system-db")
		).thenReturn(
			_cryptoVaultProvider
		);
		Mockito.when(
			_cryptoVaultProvider.isAllowedCompany(0L)
		).thenReturn(
			true
		);
		Mockito.when(
			_cryptoVaultProvider.encrypt(
				Mockito.eq(0L), Mockito.eq("k"), Mockito.any(byte[].class))
		).thenReturn(
			new byte[0]
		);

		_cryptoManagerImpl.encrypt(
			0L, KeyReference.fromString("${keyRef:*:k}"), new byte[] {1});

		Mockito.verify(
			_keyManagerProfile, Mockito.atLeastOnce()
		).getSystemDekProviderId();
	}

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	private void _inject(String fieldName, Object value) {
		try {
			Field field = CryptoManagerImpl.class.getDeclaredField(fieldName);

			field.setAccessible(true);
			field.set(_cryptoManagerImpl, value);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private void _stubResolveAndAllow(String providerId, boolean allowed) {
		Mockito.when(
			_serviceTrackerMap.getService(providerId)
		).thenReturn(
			_cryptoVaultProvider
		);
		Mockito.when(
			_cryptoVaultProvider.isAllowedCompany(Mockito.anyLong())
		).thenReturn(
			allowed
		);
	}

	@Mock
	private AuditRouter _auditRouter;

	private CryptoManagerImpl _cryptoManagerImpl;

	@Mock
	private CryptoVaultProvider _cryptoVaultProvider;

	@Mock
	private FipsComplianceChecker _fipsComplianceChecker;

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private ProfileOrchestrator _profileOrchestrator;

	@Mock
	private ServiceTrackerMap<String, CryptoVaultProvider> _serviceTrackerMap;

}
