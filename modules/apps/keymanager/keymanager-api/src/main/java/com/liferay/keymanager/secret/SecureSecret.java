/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.secret;

import com.liferay.keymanager.KeyReference;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;

import javax.security.auth.Destroyable;

/**
 * Holder for the cleartext bytes of a secret.
 *
 * <p>
 * Use inside a try-with-resources block. {@link #close} (and
 * {@link #destroy()}) zero the internal byte and character arrays via
 * <code>Arrays.fill(_, 0)</code> so the cleartext does not linger on the heap
 * after consumption.
 * </p>
 *
 * <p>
 * Constructors copy the input — the caller may zero its own buffer right
 * after construction. {@link #getBytes()} and {@link #getChars()} return the
 * live internal arrays; consumers must not retain references beyond the
 * try-with-resources scope.
 * </p>
 *
 * <p>
 * Thread safety: read and destroy methods are <code>synchronized</code>;
 * once {@link #destroy()} returns, every subsequent {@link #getBytes()} /
 * {@link #getChars()} call throws {@link IllegalArgumentException}. Multiple
 * destroys are idempotent.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public final class SecureSecret implements AutoCloseable, Destroyable {

	public SecureSecret(KeyReference keyReference, byte[] bytes) {
		_keyReference = keyReference;

		if (bytes == null) {
			_bytes = new byte[0];
		}
		else {
			_bytes = Arrays.copyOf(bytes, bytes.length);
		}
	}

	public SecureSecret(KeyReference keyReference, char[] chars) {
		_keyReference = keyReference;

		if (chars == null) {
			_bytes = new byte[0];
		}
		else {
			_chars = Arrays.copyOf(chars, chars.length);

			ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(
				CharBuffer.wrap(chars));

			_bytes = new byte[byteBuffer.remaining()];

			byteBuffer.get(_bytes);
		}
	}

	public SecureSecret(KeyReference keyReference, String value) {
		this(keyReference, (value != null) ? value.toCharArray() : null);
	}

	@Override
	public void close() {
		destroy();
	}

	/**
	 * Zeros the internal byte and character buffers. Idempotent: subsequent
	 * calls return immediately. After invocation, {@link #getBytes()} and
	 * {@link #getChars()} throw.
	 */
	@Override
	public synchronized void destroy() {
		_destroyed = true;

		if (_bytes != null) {
			Arrays.fill(_bytes, (byte)0);
		}

		if (_chars != null) {
			Arrays.fill(_chars, '\0');
		}
	}

	/**
	 * Returns the live internal byte array. The caller must not retain a
	 * reference beyond the lifetime of this {@link SecureSecret}.
	 *
	 * @throws IllegalArgumentException if the secret has already been
	 *         destroyed
	 */
	public synchronized byte[] getBytes() {
		if (_destroyed) {
			throw new IllegalArgumentException("Secret is destroyed");
		}

		return _bytes;
	}

	/**
	 * Returns the live internal character array (UTF-8 decoding of the
	 * stored bytes when the secret was constructed from raw bytes). The
	 * caller must not retain a reference beyond the lifetime of this
	 * {@link SecureSecret}.
	 */
	public char[] getChars() {
		return _getChars(StandardCharsets.UTF_8);
	}

	public KeyReference getKeyReference() {
		return _keyReference;
	}

	@Override
	public synchronized boolean isDestroyed() {
		return _destroyed;
	}

	private synchronized char[] _getChars(Charset charset) {
		if (_destroyed) {
			throw new IllegalArgumentException("Secret is destroyed");
		}

		if (_chars != null) {
			return _chars;
		}

		CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(_bytes));

		_chars = new char[charBuffer.remaining()];

		charBuffer.get(_chars);

		return _chars;
	}

	private volatile byte[] _bytes;
	private volatile char[] _chars;
	private volatile boolean _destroyed;
	private final KeyReference _keyReference;

}