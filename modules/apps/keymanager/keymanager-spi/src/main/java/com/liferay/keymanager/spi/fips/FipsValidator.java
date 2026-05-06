/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.fips;

import java.util.Map;

/**
 * Per-PID validator that decides whether a candidate configuration honors
 * FIPS constraints (algorithm choices, key sizes, mode of operation, ...).
 *
 * <p>
 * Implementations register as OSGi services of type {@link FipsValidator}.
 * The <code>KeyManagerConfigurationModelListener</code> looks up the
 * validator registered for the saved configuration's PID via
 * {@link #getConfigurationPid()} and calls {@link #validate(Map)}; a
 * non-compliant {@link FipsReport} is rejected in Strict Mode and warn-logged
 * outside it.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface FipsValidator {

	/**
	 * Returns the OSGi configuration PID this validator vouches for. Acts as
	 * the join key for the configuration listener.
	 */
	public String getConfigurationPid();

	/**
	 * Inspects the proposed configuration <code>properties</code> and returns
	 * a compliant or non-compliant {@link FipsReport}.
	 */
	public FipsReport validate(Map<String, ?> properties);

}
