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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Audit-event helpers for the key-manager subsystem.
 *
 * <p>
 * Centralizes the event-type constants and the {@link AuditMessage} builder
 * helpers the manager implementations call before delegating to a provider.
 * Audit failures (router missing, {@link AuditException}) are swallowed with
 * a warn-log so they do not block crypto / secret operations.
 * </p>
 *
 * <p>
 * Bypasses
 * <code>com.liferay.portal.security.audit.event.generators.util.AuditMessageBuilder</code>
 * because that helper reads <code>CompanyThreadLocal</code> internally; the
 * keymanager API takes <code>companyId</code> as an explicit parameter, and
 * the manager must not consult the thread-local.
 * </p>
 *
 * @author Chris Kian
 */
public class KeyManagerAuditEvents {

	public static final String CRYPTO_DECRYPT = "keymanager.crypto.decrypt";

	public static final String CRYPTO_DELETE_KEY =
		"keymanager.crypto.deleteKey";

	public static final String CRYPTO_ENCRYPT = "keymanager.crypto.encrypt";

	public static final String CRYPTO_GENERATE_ASYMMETRIC_KEY_PAIR =
		"keymanager.crypto.generateAsymmetricKeyPair";

	public static final String CRYPTO_GENERATE_SECRET_KEY =
		"keymanager.crypto.generateSecretKey";

	public static final String CRYPTO_GET_KEY_IDENTIFIERS =
		"keymanager.crypto.getKeyIdentifiers";

	public static final String CRYPTO_GET_KEY_METADATA =
		"keymanager.crypto.getKeyMetadata";

	public static final String CRYPTO_GET_PROVIDERS =
		"keymanager.crypto.getProviders";

	public static final String CRYPTO_IMPORT_SECRET_KEY =
		"keymanager.crypto.importSecretKey";

	public static final String CRYPTO_UNWRAP = "keymanager.crypto.unwrap";

	public static final String CRYPTO_WRAP = "keymanager.crypto.wrap";

	public static final String SECRET_DELETE_SECRET =
		"keymanager.secret.deleteSecret";

	public static final String SECRET_GET_PROVIDERS =
		"keymanager.secret.getProviders";

	public static final String SECRET_GET_SECRET =
		"keymanager.secret.getSecret";

	public static final String SECRET_GET_SECRET_IDENTIFIERS =
		"keymanager.secret.getSecretIdentifiers";

	public static final String SECRET_PUT_SECRET =
		"keymanager.secret.putSecret";

	/**
	 * Returns the hex-encoded SHA-256 of <code>input</code>, or
	 * <code>null</code> if <code>input</code> is <code>null</code>. The
	 * digest is computed in a single pass — the input is not retained or
	 * copied.
	 */
	public static String hashInput(byte[] input) {
		if (input == null) {
			return null;
		}

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

			return StringUtil.bytesToHexString(messageDigest.digest(input));
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"SHA-256 not available; skipping audit input hash",
					noSuchAlgorithmException);
			}

			return null;
		}
	}

	/**
	 * Builds an {@link AuditMessage} for a key-manager operation and routes
	 * it via <code>auditRouter</code>. Passes <code>companyId</code>
	 * explicitly — does not read <code>CompanyThreadLocal</code>.
	 *
	 * <p>
	 * <code>className</code> is the API interface name (for example,
	 * <code>CryptoManager.class.getName()</code>); <code>classPK</code> is
	 * the {@link KeyReference#toString()} when present, or
	 * <code>"0"</code>; <code>additionalInfo</code> carries the per-operation
	 * payload (provider id, identifier, input hash, ...).
	 * </p>
	 *
	 * <p>
	 * Audit failures are swallowed with a warn-log so the caller can proceed
	 * to the underlying crypto / secret operation.
	 * </p>
	 */
	public static void route(
		AuditRouter auditRouter, String eventType, long companyId,
		String className, String classPK, JSONObject additionalInfoJSONObject) {

		if (auditRouter == null) {
			return;
		}

		long userId = 0;

		String principalName = PrincipalThreadLocal.getName();

		if (principalName != null) {
			userId = GetterUtil.getLong(principalName);
		}

		if (additionalInfoJSONObject == null) {
			additionalInfoJSONObject = JSONFactoryUtil.createJSONObject();
		}

		AuditMessage auditMessage = new AuditMessage(
			eventType, companyId, 0, userId, null, className, classPK, null,
			null, additionalInfoJSONObject);

		try {
			auditRouter.route(auditMessage);
		}
		catch (AuditException auditException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to route key manager audit message",
					auditException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		KeyManagerAuditEvents.class);

}