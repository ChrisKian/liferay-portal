/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.secret;

import com.liferay.keymanager.secret.SecretManagerException;
import com.liferay.keymanager.secret.SecureSecret;

import java.util.List;

/**
 * Read-side contract for opaque-secret backends.
 *
 * <p>
 * Backends that ship as read-only (for example, a vendored vault snapshot)
 * implement this interface but not {@link SecretVaultWriter}. Backends that
 * support both reads and writes implement both. Register as an OSGi service
 * with the <code>keymanager.provider.id</code> component property.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface SecretVaultReader extends SecretVaultProvider {

	/**
	 * Returns the secret identified by <code>identifier</code> for
	 * <code>companyId</code>.
	 *
	 * <p>
	 * Ownership of the returned {@link SecureSecret} transfers to the
	 * caller. The provider must not retain a reference, and the cleartext is
	 * zeroed when the caller closes the {@link SecureSecret}.
	 * </p>
	 */
	public SecureSecret getSecret(long companyId, String identifier)
		throws SecretManagerException;

	public List<String> getSecretIdentifiers(long companyId)
		throws SecretManagerException;

}