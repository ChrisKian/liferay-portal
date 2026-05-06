/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.secret;

import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;

/**
 * Write-side contract for opaque-secret backends.
 *
 * <p>
 * Implementations must not retain references to the cleartext bytes carried
 * by {@link SecureSecret}. The caller (the manager and ultimately the
 * application code) owns the {@link SecureSecret} lifecycle; the provider
 * reads {@link SecureSecret#getBytes()} synchronously inside
 * {@link #putSecret} and must let the cleartext go out of scope before
 * returning.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface SecretVaultWriter extends SecretVaultProvider {

	public void deleteSecret(long companyId, String identifier)
		throws SecretManagerException;

	public void putSecret(long companyId, SecureSecret secureSecret)
		throws SecretManagerException;

}