/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.diagnostic;

/**
 * Outcome of a {@link KeyManagerDiagnosticTask#execute()}.
 *
 * <p>
 * Status semantics:
 * </p>
 *
 * <ul>
 * <li><b>OK</b> — diagnostic passed; no operator action required.</li>
 * <li><b>WARN</b> — non-fatal issue detected (degraded but operational).
 * </li>
 * <li><b>FAIL</b> — diagnostic failed; the subsystem is unhealthy and
 * requires operator action.</li>
 * </ul>
 *
 * @author Tomas Polesovsky
 */
public class KeyManagerDiagnosticResult {

	public static KeyManagerDiagnosticResult fail(String message) {
		return new KeyManagerDiagnosticResult(Status.FAIL, message);
	}

	public static KeyManagerDiagnosticResult ok(String message) {
		return new KeyManagerDiagnosticResult(Status.OK, message);
	}

	public static KeyManagerDiagnosticResult warn(String message) {
		return new KeyManagerDiagnosticResult(Status.WARN, message);
	}

	public String getMessage() {
		return _message;
	}

	public Status getStatus() {
		return _status;
	}

	public enum Status {

		FAIL, OK, WARN

	}

	private KeyManagerDiagnosticResult(Status status, String message) {
		_status = status;
		_message = message;
	}

	private final String _message;
	private final Status _status;

}