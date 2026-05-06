/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.io.Serializable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable, structured token that addresses a piece of key material managed
 * by the key manager subsystem.
 *
 * <p>
 * Wire format: <code>${keyRef:providerId:identifier}</code> for cryptographic
 * keys, <code>${secretRef:providerId:identifier}</code> for opaque secrets.
 * The provider id may be the wildcard {@link #ANY_PROVIDER} (<code>*</code>),
 * which the manager resolves through the active
 * <code>KeyManagerProfile</code>.
 * </p>
 *
 * <p>
 * The identifier may itself contain colons (used by
 * {@link ConfigurationKeyReference}); the parser is greedy on the third
 * capture group.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public class KeyReference implements Serializable {

	/**
	 * Provider id wildcard. When a {@link KeyReference} carries this value,
	 * the manager resolves the actual provider id from the active
	 * <code>KeyManagerProfile</code> based on the operation role (KEK / DEK
	 * / Secret) and on whether <code>companyId</code> is system or company
	 * scope.
	 */
	public static final String ANY_PROVIDER = StringPool.STAR;

	/**
	 * Parses the wire format into a {@link KeyReference}.
	 *
	 * @param  value the candidate wire-format string, or <code>null</code>
	 * @return a {@link KeyReference} on success, or <code>null</code> if the
	 *         input is <code>null</code> or does not match the
	 *         <code>${keyRef|secretRef:providerId:identifier}</code> grammar
	 */
	public static KeyReference fromString(String value) {
		if (value == null) {
			return null;
		}

		Matcher matcher = _pattern.matcher(value);

		if (!matcher.matches()) {
			return null;
		}

		String typeStr = matcher.group(1);

		Type type = null;

		if (Objects.equals(typeStr, "keyRef")) {
			type = Type.CRYPTO;
		}
		else if (Objects.equals(typeStr, "secretRef")) {
			type = Type.SECRET;
		}

		if (type == null) {
			return null;
		}

		String providerId = matcher.group(2);
		String identifier = matcher.group(3);

		return new KeyReference(type, providerId, identifier);
	}

	/**
	 * Returns <code>true</code> if the value parses as a {@link KeyReference}
	 * wire-format string. Equivalent to
	 * <code>{@link #fromString(String)} != null</code> without allocating the
	 * instance.
	 *
	 * @param  value the candidate string, or <code>null</code>
	 * @return whether the value matches the wire-format grammar
	 */
	public static boolean isKeyReference(String value) {
		if (value == null) {
			return false;
		}

		Matcher matcher = _pattern.matcher(value);

		return matcher.matches();
	}

	public KeyReference(Type type, String providerId, String identifier) {
		_type = Objects.requireNonNull(type, "Type must not be null");
		_providerId = Objects.requireNonNull(
			providerId, "Provider ID must not be null");
		_identifier = Objects.requireNonNull(
			identifier, "Identifier must not be null");
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof KeyReference)) {
			return false;
		}

		KeyReference keyReference = (KeyReference)object;

		if ((_type == keyReference._type) &&
			Objects.equals(_providerId, keyReference._providerId) &&
			Objects.equals(_identifier, keyReference._identifier)) {

			return true;
		}

		return false;
	}

	public String getIdentifier() {
		return _identifier;
	}

	public String getProviderId() {
		return _providerId;
	}

	public Type getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_identifier, _providerId, _type);
	}

	/**
	 * Returns the wire-format string for this reference. The result round-trips
	 * through {@link #fromString(String)}.
	 */
	@Override
	public String toString() {
		String typeStr = "keyRef";

		if (_type == Type.SECRET) {
			typeStr = "secretRef";
		}

		return StringBundler.concat(
			"${", typeStr, ":", _providerId, ":", _identifier, "}");
	}

	/**
	 * Discriminates between cryptographic keys (handled by
	 * <code>CryptoManager</code>) and opaque secrets (handled by
	 * <code>SecretManager</code>).
	 */
	public enum Type {

		CRYPTO, SECRET

	}

	private static final Pattern _pattern = Pattern.compile(
		"\\$\\{(keyRef|secretRef):([^:]+):(.+)\\}");
	private static final long serialVersionUID = 1L;

	private final String _identifier;
	private final String _providerId;
	private final Type _type;

}
