/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.profile;

/**
 * Read-only handle on the active {@link KeyManagerProfile}.
 *
 * <p>
 * The implementation in <code>keymanager-service</code> tracks every
 * registered {@link KeyManagerProfile}, watches the active-profile
 * configuration, invokes {@link KeyManagerProfile#bootstrap()} on activation
 * and on configuration change, and falls back to the built-in
 * <code>custom</code> profile when the configured id is not registered.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface ProfileOrchestrator {

	/**
	 * Returns the profile currently active. Never returns <code>null</code>;
	 * the orchestrator falls back to the built-in
	 * <code>CustomKeyManagerProfile</code> when no other profile is
	 * registered or the configured id cannot be resolved.
	 */
	public KeyManagerProfile getActiveProfile();

}
