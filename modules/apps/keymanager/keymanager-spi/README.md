# Key Manager SPI

The `keymanager-spi` module declares the extension points downstream provider modules implement to plug into the key-manager subsystem.

## Provider SPIs

### `CryptoVaultProvider`

Implementers serve cryptographic operations (encrypt, decrypt, generate, import, wrap, unwrap, delete) for a single backend (database, GCP KMS, AWS KMS, KMIP, local keystore, etc.). Register as an OSGi service of type `CryptoVaultProvider` with the `keymanager.provider.id` component property — the `CryptoManager` routes operations to the matching id.

### `SecretVaultProvider` / `SecretVaultReader` / `SecretVaultWriter`

`SecretVaultProvider` is the base contract (`isAllowedCompany`). Implementers extend `SecretVaultReader` for read paths (`getSecret`, `getSecretIdentifiers`) and/or `SecretVaultWriter` for write paths (`putSecret`, `deleteSecret`) so a backend can ship as read-only when appropriate. Register with the `keymanager.provider.id` component property.

## Profile mechanism

### `KeyManagerProfile`

A profile names six provider ids (system / company × KEK / DEK / Secret), declares whether Strict Mode is on, and whether FIPS is required. Implementers register as an OSGi service of type `KeyManagerProfile` with the `keymanager.profile.id` component property. The `keymanager-service` module ships `CustomKeyManagerProfile` (`keymanager.profile.id=custom`) for manual provider selection. Other profiles ship with their respective provider tickets.

### `ProfileOrchestrator`

Tracks registered profiles, watches the active-profile configuration, and exposes `getActiveProfile()` to managers. Bootstrap callbacks fire once on activation and on configuration change.

## FIPS validation framework

### `FipsComplianceChecker`

Boot-time gatekeeper. When `LIFERAY_KEYMANAGER_FIPS_ENFORCED=true`, `check()` verifies BCFIPS is the first JCE provider and throws otherwise. `keymanager-service` injects this into every crypto / secret operation as a fail-fast precondition.

### `FipsValidator`

Per-PID validator. Implementers return a `FipsReport` for a candidate configuration; the `KeyManagerConfigurationModelListener` rejects non-compliant configurations in Strict Mode and warn-logs them in Standard Mode.

### `BaseConfigurationModelListener`

Convenience base class for provider-config listeners that need to enforce uniqueness of `providerId` per scope.

## Registration

```java
@Component(
	configurationPid = "...",
	configurationPolicy = ConfigurationPolicy.REQUIRE,
	property = "keymanager.provider.id=my-backend",
	service = CryptoVaultProvider.class
)
public class MyCryptoVaultProvider implements CryptoVaultProvider {
	// ...
}
```