/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager;

/**
 * Marker service registered by bundles that want their bundle activator (or
 * an early-bound component) to participate in {@link KeyReference}
 * registration.
 *
 * <p>
 * Implementations declare themselves as OSGi services of this type. The
 * key-manager service module tracks them and triggers any registered
 * follow-up logic (for example, configuration interpolation hooks shipped
 * in a later ticket).
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface KeyReferenceRegistrar {
}