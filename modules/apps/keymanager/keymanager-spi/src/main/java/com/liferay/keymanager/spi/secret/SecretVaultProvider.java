/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.secret;

/**
 * Base contract for opaque-secret backends.
 *
 * <p>
 * A provider exposes read or write capability (or both) by also implementing
 * {@link SecretVaultReader} and/or {@link SecretVaultWriter}. Direct
 * implementers of {@link SecretVaultProvider} alone are unusual and serve
 * only as a marker (for example, a provider that registers but is not yet
 * ready for traffic).
 * </p>
 *
 * <p>
 * Register as an OSGi service with the <code>keymanager.provider.id</code>
 * component property.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface SecretVaultProvider {

	/**
	 * Returns whether this provider serves the given company. The manager
	 * treats a <code>false</code> answer as "no provider registered for this
	 * scope".
	 */
	public boolean isAllowedCompany(long companyId);

}