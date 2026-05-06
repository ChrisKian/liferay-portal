# Key Manager API

The `keymanager-api` module exposes the public services Liferay code uses to encrypt data and manage secrets without binding to a specific backend (database, cloud KMS, KMIP, local keystore). Concrete providers ship in separate `keymanager-provider-*` modules.

## Core types

- **`KeyReference`** — Structured token addressing key or secret material. Syntax: `${keyRef:providerId:identifier}` (crypto) or `${secretRef:providerId:identifier}` (secret). The wildcard provider id `*` (`KeyReference.ANY_PROVIDER`) lets the active `KeyManagerProfile` route the call to its configured provider for the role.
- **`SecureSecret`** — `AutoCloseable` holder for sensitive byte material. Calling `close()` (or `destroy()`) zeros the underlying arrays via `Arrays.fill(_, (byte) 0)`. Use it inside try-with-resources.
- **`CryptoManager`** — Public facade for cryptographic operations (encrypt, decrypt, generate, import, wrap, unwrap, delete).
- **`SecretManager`** — Public facade for opaque secret storage (get, put, delete, list).

## Usage

```java
@Reference
private SecretManager _secretManager;

KeyReference jdbcPassword = KeyReference.fromString(
	"${secretRef:db:jdbc-password}");

try (SecureSecret secret = _secretManager.getSecret(companyId, jdbcPassword)) {
	String password = new String(secret.getChars());

	// use password — it is zeroed when the try block exits
}
```

```java
@Reference
private CryptoManager _cryptoManager;

KeyReference companyKek = KeyReference.fromString("${keyRef:*:company-kek}");

byte[] ciphertext = _cryptoManager.encrypt(companyId, companyKek, plaintext);
```

## Configuration & profiles

Provider routing is controlled by the active `KeyManagerProfile` (declared in `keymanager-spi`, orchestrated by `keymanager-service`). When a `KeyReference` uses the `*` wildcard, the manager resolves it to the profile's configured KEK / DEK / Secret provider id depending on the operation and `companyId` (system scope when `companyId == 0`).