/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.crypto;

import com.liferay.keymanager.crypto.CryptoKey;
import com.liferay.keymanager.crypto.CryptoManagerException;

import java.security.Key;

import java.util.List;

/**
 * SPI implemented by every cryptographic backend (database, GCP KMS,
 * AWS KMS, KMIP, local keystore, ...).
 *
 * <p>
 * Implementations register as OSGi services of this type with the
 * <code>keymanager.provider.id</code> component property — for example
 * <code>property = "keymanager.provider.id=db-system-crypto"</code>. The
 * <code>CryptoManager</code> resolves the {@link
 * com.liferay.keymanager.KeyReference} provider id (or the wildcard, via the
 * active profile) to the matching service.
 * </p>
 *
 * <p>
 * {@link #isAllowedCompany(long)} gates whether the provider serves a given
 * <code>companyId</code>. The manager invokes it before delegating; a
 * <code>false</code> answer is treated as "no provider for this scope" and
 * surfaces as a {@link CryptoManagerException} naming the requested provider
 * id.
 * </p>
 *
 * <p>
 * Memory hygiene: implementations must zero any plaintext byte[] they
 * allocate as soon as they finish using it
 * (<code>Arrays.fill(_, (byte) 0)</code>). The <code>importSecretKey</code>
 * <code>rawKeyMaterial</code> array is zeroed by the manager after the
 * provider returns; providers that copy the input must zero their own copy.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface CryptoVaultProvider {

	public byte[] decrypt(long companyId, String identifier, byte[] ciphertext)
		throws CryptoManagerException;

	public void deleteKey(long companyId, String identifier)
		throws CryptoManagerException;

	public byte[] encrypt(long companyId, String identifier, byte[] plaintext)
		throws CryptoManagerException;

	public String generateAsymmetricKeyPair(
			long companyId, String identifier, String algorithmSpec)
		throws CryptoManagerException;

	public String generateSecretKey(
			long companyId, String identifier, String algorithmSpec)
		throws CryptoManagerException;

	public List<String> getKeyIdentifiers(long companyId)
		throws CryptoManagerException;

	public CryptoKey getKeyMetadata(long companyId, String identifier)
		throws CryptoManagerException;

	public String importSecretKey(
			long companyId, String identifier, byte[] rawKeyMaterial,
			String algorithmSpec)
		throws CryptoManagerException;

	/**
	 * Returns whether this provider serves the given company. Used by the
	 * manager to gate routing — a <code>false</code> answer is equivalent to
	 * "no provider registered for this scope".
	 */
	public boolean isAllowedCompany(long companyId);

	public Key unwrap(
			long companyId, String identifier, byte[] wrappedKeyBytes,
			String wrappedKeyAlgorithm, int wrappedKeyCipherType)
		throws CryptoManagerException;

	public byte[] wrap(long companyId, String identifier, Key keyToWrap)
		throws CryptoManagerException;

}