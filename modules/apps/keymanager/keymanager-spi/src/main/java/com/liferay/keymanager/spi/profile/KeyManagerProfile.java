/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.spi.profile;

/**
 * Wiring policy for the key-manager subsystem.
 *
 * <p>
 * A profile names six provider ids (<em>system</em> and <em>company</em>
 * scopes × <em>KEK</em> / <em>DEK</em> / <em>Secret</em> roles), declares
 * whether Strict Mode is on, and whether FIPS is required. The
 * <code>ProfileOrchestrator</code> picks one active profile at boot (and
 * watches the active-profile configuration for changes) so the manager can
 * resolve the {@link com.liferay.keymanager.KeyReference#ANY_PROVIDER}
 * wildcard to a concrete provider id.
 * </p>
 *
 * <p>
 * Register as an OSGi service of this type with the
 * <code>keymanager.profile.id</code> component property — for example
 * <code>property = "keymanager.profile.id=local-dev"</code>.
 * </p>
 *
 * @author Tomas Polesovsky
 */
public interface KeyManagerProfile {

	/**
	 * Performs any one-time initialization the profile needs (vault
	 * handshake, credential fetch, ...).
	 *
	 * <p>
	 * Invoked once on activation and again whenever the active-profile
	 * configuration changes to this profile's id. Implementations should be
	 * idempotent — the orchestrator does not guarantee strict
	 * exactly-once semantics across restarts.
	 * </p>
	 */
	public void bootstrap() throws Exception;

	public String getCompanyDekProviderId();

	public String getCompanyKekProviderId();

	public String getCompanySecretProviderId();

	/**
	 * Returns the stable id under which this profile is registered. Must
	 * match the value of the <code>keymanager.profile.id</code> component
	 * property.
	 */
	public String getProfileId();

	public String getSystemDekProviderId();

	public String getSystemKekProviderId();

	public String getSystemSecretProviderId();

	/**
	 * In Strict Mode the FIPS validator chain rejects non-compliant
	 * configurations with a {@link
	 * com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException};
	 * outside Strict Mode the same configurations log a warn-level diagnostic
	 * but are accepted.
	 */
	public boolean isStrictMode();

	/**
	 * Returns whether this profile insists on FIPS-validated cryptographic
	 * primitives. When <code>true</code>, the boot-time
	 * <code>FipsComplianceChecker</code> aborts startup unless BCFIPS is the
	 * first registered JCE provider.
	 */
	public boolean requireFips();

}
