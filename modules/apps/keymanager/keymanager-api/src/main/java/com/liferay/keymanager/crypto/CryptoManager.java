/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.crypto;

import com.liferay.keymanager.KeyReference;

import java.security.Key;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Public facade for cryptographic operations. Routes calls to a
 * {@link com.liferay.keymanager.spi.crypto.CryptoVaultProvider} based on the
 * provider id encoded in the {@link KeyReference}.
 *
 * <p>
 * If the {@link KeyReference} carries the {@link KeyReference#ANY_PROVIDER}
 * wildcard, the manager resolves the actual provider from the active
 * <code>KeyManagerProfile</code> (system role for
 * <code>companyId == 0</code>, company role otherwise).
 * </p>
 *
 * <p>
 * Every operation takes <code>companyId</code> explicitly. The implementation
 * does not read <code>CompanyThreadLocal</code>.
 * </p>
 *
 * <p>
 * The implementation emits a Liferay <code>AuditMessage</code> via
 * <code>AuditRouter</code> before delegating to the provider. The audit
 * payload tags the operation, alias, calling principal, and a SHA-256 hash
 * of any byte input — never the input itself.
 * </p>
 *
 * <p>
 * If no provider is registered for the resolved provider id (or for
 * <code>companyId</code>), the manager throws
 * {@link CryptoManagerException} with a message naming the requested provider
 * id and company.
 * </p>
 *
 * @author Tomas Polesovsky
 */
@ProviderType
public interface CryptoManager {

	/**
	 * Decrypts <code>ciphertext</code> using the key addressed by
	 * <code>keyReference</code>. The key material is not retained by the
	 * manager.
	 */
	public byte[] decrypt(
			long companyId, KeyReference keyReference, byte[] ciphertext)
		throws CryptoManagerException;

	/**
	 * Removes the key addressed by <code>keyReference</code> from its
	 * provider.
	 */
	public void deleteKey(long companyId, KeyReference keyReference)
		throws CryptoManagerException;

	/**
	 * Encrypts <code>plaintext</code> using the key addressed by
	 * <code>keyReference</code>. Plaintext bytes pass through to the provider
	 * without being copied or retained.
	 */
	public byte[] encrypt(
			long companyId, KeyReference keyReference, byte[] plaintext)
		throws CryptoManagerException;

	/**
	 * Generates a new asymmetric key pair (private + matching public) inside
	 * the named provider and returns the {@link KeyReference} addressing it.
	 *
	 * @param  algorithmSpec the algorithm specification understood by the
	 *         provider (for example, <code>RSA</code>,
	 *         <code>EC/secp256r1</code>); format is provider-defined
	 */
	public KeyReference generateAsymmetricKeyPair(
			long companyId, String providerId, String identifier,
			String algorithmSpec)
		throws CryptoManagerException;

	/**
	 * Generates a new symmetric secret key inside the named provider and
	 * returns the {@link KeyReference} addressing it.
	 */
	public KeyReference generateSecretKey(
			long companyId, String providerId, String identifier,
			String algorithmSpec)
		throws CryptoManagerException;

	/**
	 * Lists every key addressed by the named provider for the given company.
	 */
	public List<KeyReference> getKeyIdentifiers(
			long companyId, String providerId)
		throws CryptoManagerException;

	/**
	 * Returns metadata (algorithm, cipher spec, creation date) for the key
	 * addressed by <code>keyReference</code>, without unwrapping its material.
	 */
	public CryptoKey getKeyMetadata(long companyId, KeyReference keyReference)
		throws CryptoManagerException;

	/**
	 * Returns the ids of every {@link
	 * com.liferay.keymanager.spi.crypto.CryptoVaultProvider} that is allowed
	 * to serve <code>companyId</code>.
	 */
	public List<String> getProviders(long companyId)
		throws CryptoManagerException;

	/**
	 * Imports raw key material into the named provider and returns the
	 * {@link KeyReference} addressing the new key.
	 *
	 * <p>
	 * The implementation zeros the supplied <code>rawKeyMaterial</code> array
	 * with <code>Arrays.fill(_, (byte) 0)</code> after the provider returns
	 * (or throws), so the caller need not re-zero it.
	 * </p>
	 */
	public KeyReference importSecretKey(
			long companyId, String providerId, String identifier,
			byte[] rawKeyMaterial, String algorithmSpec)
		throws CryptoManagerException;

	/**
	 * Decrypts a wrapped key using the wrapping key addressed by
	 * <code>masterKeyReference</code> and reconstructs the inner {@link Key}
	 * with the named algorithm and cipher type
	 * (<code>{@link javax.crypto.Cipher#SECRET_KEY}</code> /
	 * <code>{@link javax.crypto.Cipher#PRIVATE_KEY}</code> /
	 * <code>{@link javax.crypto.Cipher#PUBLIC_KEY}</code>).
	 */
	public Key unwrap(
			long companyId, KeyReference masterKeyReference,
			byte[] wrappedKeyBytes, String wrappedKeyAlgorithm,
			int wrappedKeyCipherType)
		throws CryptoManagerException;

	/**
	 * Encrypts (wraps) the encoded form of <code>keyToWrap</code> with the
	 * wrapping key addressed by <code>masterKeyReference</code>.
	 */
	public byte[] wrap(
			long companyId, KeyReference masterKeyReference, Key keyToWrap)
		throws CryptoManagerException;

}
