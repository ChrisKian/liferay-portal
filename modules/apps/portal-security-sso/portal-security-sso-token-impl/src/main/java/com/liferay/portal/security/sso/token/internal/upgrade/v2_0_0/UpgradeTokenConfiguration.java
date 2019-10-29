/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.sso.token.internal.upgrade.v2_0_0;

import com.liferay.portal.configuration.upgrade.PrefsPropsToConfigurationUpgradeHelper;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.security.sso.token.configuration.TokenConfiguration;
import com.liferay.portal.security.sso.token.constants.LegacyTokenPropsKeys;
import com.liferay.portal.security.sso.token.constants.TokenConfigurationKeys;

/**
 * @author Christopher Kian
 */
public class UpgradeTokenConfiguration extends UpgradeProcess {

	public UpgradeTokenConfiguration(
		PrefsPropsToConfigurationUpgradeHelper
			prefsPropsToConfigurationUpgradeHelper) {

		_prefsPropsToConfigurationUpgradeHelper =
			prefsPropsToConfigurationUpgradeHelper;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeConfiguration();
	}

	private void _upgradeConfiguration() throws Exception {
		_prefsPropsToConfigurationUpgradeHelper.mapConfigurations(
			TokenConfiguration.class,
			new KeyValuePair(
				LegacyTokenPropsKeys.SHIBBOLETH_AUTH_ENABLED,
				TokenConfigurationKeys.AUTH_ENABLED),
			new KeyValuePair(
				LegacyTokenPropsKeys.SHIBBOLETH_IMPORT_FROM_LDAP,
				TokenConfigurationKeys.IMPORT_FROM_LDAP),
			new KeyValuePair(
				LegacyTokenPropsKeys.SHIBBOLETH_LOGOUT_URL,
				TokenConfigurationKeys.LOGOUT_REDIRECT_URL),
			new KeyValuePair(
				LegacyTokenPropsKeys.SHIBBOLETH_USER_HEADER,
				TokenConfigurationKeys.USER_TOKEN_NAME),
			new KeyValuePair(
				LegacyTokenPropsKeys.SITEMINDER_AUTH_ENABLED,
				TokenConfigurationKeys.AUTH_ENABLED),
			new KeyValuePair(
				LegacyTokenPropsKeys.SITEMINDER_IMPORT_FROM_LDAP,
				TokenConfigurationKeys.IMPORT_FROM_LDAP),
			new KeyValuePair(
				LegacyTokenPropsKeys.SITEMINDER_USER_HEADER,
				TokenConfigurationKeys.USER_TOKEN_NAME));
	}

	private final PrefsPropsToConfigurationUpgradeHelper
		_prefsPropsToConfigurationUpgradeHelper;

}