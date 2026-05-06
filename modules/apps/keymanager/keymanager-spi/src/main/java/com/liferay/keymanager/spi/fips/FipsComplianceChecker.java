/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.fips;

/**
 * Boot-time and per-call gatekeeper for FIPS-mode crypto.
 *
 * <p>
 * Enforcement is gated by the environment variable
 * <code>LIFERAY_KEYMANAGER_FIPS_ENFORCED</code>: when truthy, the manager
 * insists that BCFIPS is the first registered JCE provider, and the
 * <code>CryptoManager</code> / <code>SecretManager</code> implementations
 * call {@link #check()} before delegating each cryptographic operation. When
 * not enforced, {@link #check()} returns silently.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface FipsComplianceChecker {

	/**
	 * Verifies the JCE provider stack is FIPS-compliant. Throws a runtime
	 * exception (typically <code>RuntimeException</code>) if enforcement is
	 * on and BCFIPS is not the first provider. Returns silently when
	 * enforcement is off, or when enforcement is on and the stack is
	 * compliant.
	 */
	public void check();

	/**
	 * Returns whether FIPS enforcement is on. Equivalent to evaluating the
	 * <code>LIFERAY_KEYMANAGER_FIPS_ENFORCED</code> environment variable.
	 */
	public boolean isFipsEnforced();

}