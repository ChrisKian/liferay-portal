/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.audit;

import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Chris Kian
 */
public class KeyManagerAuditEventsTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testAuditExceptionSwallowed() throws Exception {
		Mockito.doThrow(
			new AuditException("simulated")
		).when(
			_auditRouter
		).route(
			Mockito.any(AuditMessage.class)
		);

		KeyManagerAuditEvents.route(
			_auditRouter, "keymanager.crypto.encrypt", 1L, "Class", "0", null);
	}

	@Test
	public void testHashInputNullReturnsNull() {
		Assert.assertNull(KeyManagerAuditEvents.hashInput(null));
	}

	@Test
	public void testHashInputProducesHexSha256() {
		String hex = KeyManagerAuditEvents.hashInput(new byte[] {0});

		Assert.assertEquals(64, hex.length());
		Assert.assertTrue(hex.matches("[0-9a-fA-F]+"));
	}

	@Test
	public void testNullRouterIsNoOp() {
		KeyManagerAuditEvents.route(
			null, "keymanager.crypto.encrypt", 1L, "Class", "0", null);
	}

	@Test
	public void testRouteUsesExplicitCompanyId() throws Exception {
		KeyManagerAuditEvents.route(
			_auditRouter, "keymanager.crypto.encrypt", 42L,
			"com.liferay.keymanager.crypto.CryptoManager", "${keyRef:db:k}",
			JSONFactoryUtil.createJSONObject(
			).put(
				"operation", "keymanager.crypto.encrypt"
			));

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
		Assert.assertEquals(42L, auditMessage.getCompanyId());
		Assert.assertEquals(0L, auditMessage.getGroupId());
		Assert.assertEquals(
			"com.liferay.keymanager.crypto.CryptoManager",
			auditMessage.getClassName());
		Assert.assertEquals("${keyRef:db:k}", auditMessage.getClassPK());

		JSONObject capturedAdditionalInfoJSONObject =
			auditMessage.getAdditionalInfo();

		Assert.assertEquals(
			"keymanager.crypto.encrypt",
			capturedAdditionalInfoJSONObject.getString("operation"));
	}

	@Mock
	private AuditRouter _auditRouter;

}