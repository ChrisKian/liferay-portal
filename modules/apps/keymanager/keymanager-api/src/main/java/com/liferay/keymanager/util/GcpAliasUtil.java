/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.util;

/**
 * Normalizes Liferay-style identifiers into strings that satisfy GCP KMS
 * naming constraints.
 *
 * <p>
 * GCP KMS resource names accept letters, digits, underscore, and hyphen.
 * Liferay aliases routinely contain dots, spaces, and other characters.
 * {@link #normalize(String)} maps:
 * </p>
 *
 * <ul>
 * <li>letters and digits — preserved</li>
 * <li><code>.</code> and space — replaced with <code>-</code></li>
 * <li><code>-</code> — preserved</li>
 * <li>everything else — replaced with <code>_</code></li>
 * </ul>
 *
 * <p>
 * The mapping is a one-way convenience; it does not collapse runs and is not
 * guaranteed to be unique across distinct inputs that share characters
 * outside the preserved set.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public class GcpAliasUtil {

	public static String normalize(String alias) {
		if (alias == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(alias.length());

		for (int i = 0; i < alias.length(); i++) {
			char c = alias.charAt(i);

			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
			else if ((c == '.') || (c == ' ')) {
				sb.append('-');
			}
			else if (c == '-') {
				sb.append('-');
			}
			else {
				sb.append('_');
			}
		}

		return sb.toString();
	}

}