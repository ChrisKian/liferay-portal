/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.secret;

import com.liferay.keymanager.KeyReference;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Public facade for opaque secret storage. Routes calls to a
 * {@link com.liferay.keymanager.spi.secret.SecretVaultReader} (read paths)
 * or {@link com.liferay.keymanager.spi.secret.SecretVaultWriter} (write
 * paths) based on the provider id encoded in the {@link KeyReference}.
 *
 * <p>
 * If the {@link KeyReference} carries the {@link KeyReference#ANY_PROVIDER}
 * wildcard, the manager resolves the actual provider from the active
 * <code>KeyManagerProfile</code>.
 * </p>
 *
 * <p>
 * Every operation takes <code>companyId</code> explicitly. The implementation
 * does not read <code>CompanyThreadLocal</code>.
 * </p>
 *
 * <p>
 * The implementation emits a Liferay <code>AuditMessage</code> via
 * <code>AuditRouter</code> before delegating to the provider. Audit
 * payload: operation, alias, calling principal, and a SHA-256 hash of any
 * byte input.
 * </p>
 *
 * <p>
 * If no provider is registered for the resolved provider id, the manager
 * throws {@link SecretManagerException} with a message naming the requested
 * provider id and company.
 * </p>
 *
 * @author Tomas Polesovsky
 */
@ProviderType
public interface SecretManager {

	/**
	 * Removes the secret addressed by <code>keyReference</code> from its
	 * provider.
	 */
	public void deleteSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException;

	/**
	 * Returns the ids of every {@link
	 * com.liferay.keymanager.spi.secret.SecretVaultProvider} that is allowed
	 * to serve <code>companyId</code>.
	 */
	public List<String> getProviders(long companyId)
		throws SecretManagerException;

	/**
	 * Returns the secret addressed by <code>keyReference</code>.
	 *
	 * <p>
	 * The returned {@link SecureSecret} is owned by the caller. Wrap the call
	 * in a try-with-resources block so the cleartext is zeroed when consumption
	 * ends.
	 * </p>
	 */
	public SecureSecret getSecret(long companyId, KeyReference keyReference)
		throws SecretManagerException;

	/**
	 * Lists every secret addressed by the named provider for the given
	 * company.
	 */
	public List<KeyReference> getSecretIdentifiers(
			long companyId, String providerId)
		throws SecretManagerException;

	/**
	 * Stores or replaces the secret carried by <code>secureSecret</code> in
	 * its provider, returning the {@link KeyReference} addressing the result.
	 *
	 * <p>
	 * The provider receives the cleartext via
	 * {@link SecureSecret#getBytes()} and must not retain it. The caller
	 * remains responsible for closing the supplied {@link SecureSecret}.
	 * </p>
	 */
	public KeyReference putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException;

}