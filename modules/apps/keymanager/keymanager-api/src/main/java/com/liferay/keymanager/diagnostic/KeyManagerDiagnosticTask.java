/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.diagnostic;

/**
 * SPI for self-tests run by the key-manager diagnostic harness.
 *
 * <p>
 * Implementations register as OSGi services of type
 * {@link KeyManagerDiagnosticTask}. The keymanager Gogo command
 * <code>km:status</code> iterates over every registered task, invokes
 * {@link #execute()}, and prints the {@link KeyManagerDiagnosticResult} so
 * operators can sanity-check the subsystem.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface KeyManagerDiagnosticTask {

	/**
	 * Runs the self-test. Implementations must not throw — failures should
	 * be reported via {@link KeyManagerDiagnosticResult#fail(String)}.
	 */
	public KeyManagerDiagnosticResult execute();

	/**
	 * Returns a stable, human-readable name for the task. Used by the
	 * diagnostic harness to label the result.
	 */
	public String getName();

}
