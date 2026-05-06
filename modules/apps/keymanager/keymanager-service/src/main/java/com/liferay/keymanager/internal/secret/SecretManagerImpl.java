/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.internal.secret;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.internal.audit.KeyManagerAuditEvents;
import com.liferay.keymanager.secret.SecretManager;
import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;
import com.liferay.keymanager.spi.fips.FipsComplianceChecker;
import com.liferay.keymanager.spi.profile.KeyManagerProfile;
import com.liferay.keymanager.spi.profile.ProfileOrchestrator;
import com.liferay.keymanager.spi.secret.SecretVaultProvider;
import com.liferay.keymanager.spi.secret.SecretVaultReader;
import com.liferay.keymanager.spi.secret.SecretVaultWriter;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tomas Polesovsky
 * @author Chris Kian
 */
@Component(service = SecretManager.class)
public class SecretManagerImpl implements SecretManager {

	@Override
	public void deleteSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		_fipsComplianceChecker.check();

		Objects.requireNonNull(keyReference, "No KeyReference provided!");

		String providerId = keyReference.getProviderId();

		try {
			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			_audit(
				KeyManagerAuditEvents.SECRET_DELETE_SECRET, companyId,
				keyReference, providerId, null);

			SecretVaultWriter secretVaultWriter = _getSecretVaultWriter(
				companyId, providerId);

			secretVaultWriter.deleteSecret(
				companyId, keyReference.getIdentifier());
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to delete secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public List<String> getProviders(long companyId)
		throws SecretManagerException {

		_audit(
			KeyManagerAuditEvents.SECRET_GET_PROVIDERS, companyId, null, null,
			null);

		try {
			return _getSecretVaultReaderProviderIds(
				companyId, KeyReference.ANY_PROVIDER);
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get providers: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Override
	public SecureSecret getSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException {

		_fipsComplianceChecker.check();

		Objects.requireNonNull(keyReference, "No KeyReference provided!");

		String providerId = keyReference.getProviderId();

		try {
			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			_audit(
				KeyManagerAuditEvents.SECRET_GET_SECRET, companyId,
				keyReference, providerId, null);

			for (SecretVaultReader reader :
					_getSecretVaultReaders(
						companyId, keyReference.getProviderId())) {

				try {
					SecureSecret secureSecret = reader.getSecret(
						companyId, keyReference.getIdentifier());

					if (secureSecret != null) {
						return secureSecret;
					}
				}
				catch (SecretManagerException secretManagerException) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"Unable to fetch secret from reader",
							secretManagerException);
					}
				}
			}
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}

		return null;
	}

	@Override
	public List<KeyReference> getSecretIdentifiers(
			long companyId, String providerId)
		throws SecretManagerException {

		_audit(
			KeyManagerAuditEvents.SECRET_GET_SECRET_IDENTIFIERS, companyId,
			null, providerId, null);

		List<KeyReference> keyReferences = new ArrayList<>();

		try {
			for (String trackedProviderId :
					_getSecretVaultReaderProviderIds(companyId, providerId)) {

				SecretVaultReader reader = _readerServiceTrackerMap.getService(
					trackedProviderId);

				if (reader == null) {
					continue;
				}

				List<String> identifiers = reader.getSecretIdentifiers(
					companyId);

				for (String identifier : identifiers) {
					keyReferences.add(
						new KeyReference(
							KeyReference.Type.SECRET, trackedProviderId,
							identifier));
				}
			}
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list secret identifiers: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}

		return keyReferences;
	}

	@Override
	public KeyReference putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException {

		_fipsComplianceChecker.check();

		Objects.requireNonNull(secureSecret, "No secureSecret provided!");

		KeyReference keyReference = secureSecret.getKeyReference();

		String providerId = keyReference.getProviderId();

		try {
			if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				providerId = _getSecretVaultProviderId(companyId);
			}

			byte[] inputBytes = null;

			if (!secureSecret.isDestroyed()) {
				inputBytes = secureSecret.getBytes();
			}

			_audit(
				KeyManagerAuditEvents.SECRET_PUT_SECRET, companyId,
				keyReference, providerId, inputBytes);

			SecretVaultWriter secretVaultWriter = _getSecretVaultWriter(
				companyId, providerId);

			secretVaultWriter.putSecret(companyId, secureSecret);

			return new KeyReference(
				KeyReference.Type.SECRET, providerId,
				keyReference.getIdentifier());
		}
		catch (SecretManagerException secretManagerException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to put secret: " +
						secretManagerException.getMessage(),
					secretManagerException);
			}

			throw secretManagerException;
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_readerServiceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, SecretVaultReader.class,
			"(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));

		_writerServiceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, SecretVaultWriter.class,
			"(keymanager.provider.id=*)",
			new PropertyServiceReferenceMapper<>("keymanager.provider.id"));
	}

	@Deactivate
	protected void deactivate() {
		_readerServiceTrackerMap.close();
		_writerServiceTrackerMap.close();
	}

	private void _audit(
		String eventType, long companyId, KeyReference keyReference,
		String resolvedProviderId, byte[] inputBytes) {

		JSONObject additionalInfoJSONObject = _jsonFactory.createJSONObject(
		).put(
			"companyId", companyId
		).put(
			"operation", eventType
		);

		if (resolvedProviderId != null) {
			additionalInfoJSONObject.put("providerId", resolvedProviderId);
		}

		if (keyReference != null) {
			additionalInfoJSONObject.put(
				"identifier", keyReference.getIdentifier()
			).put(
				"keyReferenceType",
				keyReference.getType(
				).name()
			).put(
				"requestedProviderId", keyReference.getProviderId()
			);
		}

		if (inputBytes != null) {
			additionalInfoJSONObject.put(
				"inputByteLength", inputBytes.length
			).put(
				"inputHashSHA256", KeyManagerAuditEvents.hashInput(inputBytes)
			);
		}

		String classPK = "0";

		if (keyReference != null) {
			classPK = keyReference.toString();
		}

		KeyManagerAuditEvents.route(
			_auditRouter, eventType, companyId, _CLASS_NAME, classPK,
			additionalInfoJSONObject);
	}

	private Collection<String> _getProviderIds(
		ServiceTrackerMap<String, ? extends SecretVaultProvider>
			serviceTrackerMap) {

		return serviceTrackerMap.keySet();
	}

	private String _getSecretVaultProviderId(long companyId)
		throws SecretManagerException {

		KeyManagerProfile activeProfile =
			_profileOrchestrator.getActiveProfile();

		if (activeProfile != null) {
			if (companyId == 0L) {
				return activeProfile.getSystemSecretProviderId();
			}

			return activeProfile.getCompanySecretProviderId();
		}

		for (String trackedProviderId :
				_getProviderIds(_writerServiceTrackerMap)) {

			SecretVaultWriter writer = _writerServiceTrackerMap.getService(
				trackedProviderId);

			if (writer.isAllowedCompany(companyId)) {
				return trackedProviderId;
			}
		}

		throw new SecretManagerException(
			StringBundler.concat(
				"No secret vault writer found for ANY provider and company ",
				"ID: ", companyId));
	}

	private List<String> _getSecretVaultReaderProviderIds(
			long companyId, String providerId)
		throws SecretManagerException {

		if (Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
			return Collections.singletonList(
				_getSecretVaultProviderId(companyId));
		}

		return Collections.singletonList(providerId);
	}

	private List<SecretVaultReader> _getSecretVaultReaders(
			long companyId, String providerId)
		throws SecretManagerException {

		List<SecretVaultReader> readers = new ArrayList<>();

		for (String id :
				_getSecretVaultReaderProviderIds(companyId, providerId)) {

			SecretVaultReader reader = _readerServiceTrackerMap.getService(id);

			if ((reader != null) && reader.isAllowedCompany(companyId)) {
				readers.add(reader);
			}
			else if (!Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {
				throw new SecretManagerException(
					StringBundler.concat(
						"No secret vault reader found for ID: ", id,
						" and company ID: ", companyId));
			}
		}

		if (readers.isEmpty() &&
			Objects.equals(providerId, KeyReference.ANY_PROVIDER)) {

			throw new SecretManagerException(
				StringBundler.concat(
					"No secret vault reader found for ANY provider and ",
					"company ID: ", companyId));
		}

		return readers;
	}

	private SecretVaultWriter _getSecretVaultWriter(
			long companyId, String providerId)
		throws SecretManagerException {

		SecretVaultWriter writer = _writerServiceTrackerMap.getService(
			providerId);

		if ((writer != null) && writer.isAllowedCompany(companyId)) {
			return writer;
		}

		throw new SecretManagerException(
			StringBundler.concat(
				"No secret vault writer found for ID: ", providerId,
				" and company ID: ", companyId));
	}

	private static final String _CLASS_NAME = SecretManager.class.getName();

	private static final Log _log = LogFactoryUtil.getLog(
		SecretManagerImpl.class);

	@Reference
	private AuditRouter _auditRouter;

	@Reference
	private FipsComplianceChecker _fipsComplianceChecker;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ProfileOrchestrator _profileOrchestrator;

	private ServiceTrackerMap<String, SecretVaultReader>
		_readerServiceTrackerMap;
	private ServiceTrackerMap<String, SecretVaultWriter>
		_writerServiceTrackerMap;

}