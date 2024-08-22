/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from '../../../liferay.config';
import {ServiceProviderPage} from '../../../pages/saml-web/ServiceProviderPage';
import {performLogout} from '../../../utils/performLogin';
import {
	DEFAULT_SP_NAME,
	DEFAULT_SP_URL,
	performSamlSafeLogin,
} from './samlVirtualInstanceUtil';

export type TServiceProvider = {
	allowShowingTheLoginPortlet?: boolean;
	clockSkew?: string;
	ldapImportEnabled?: boolean;
	requireAssertionSignature?: boolean;
	signAuthnRequests?: boolean;
	signMetadata?: boolean;
	sslRequired?: boolean;
};

export async function configureServiceProvider(
	browser,
	serviceProvider: TServiceProvider,
	spName = DEFAULT_SP_NAME,
	spUrl = DEFAULT_SP_URL
) {
	const defaultBaseUrl = liferayConfig.environment.baseUrl;

	liferayConfig.environment.baseUrl = spUrl;

	const spInstancePage = await performSamlSafeLogin(browser, spName);

	const serviceProviderPage = await new ServiceProviderPage(spInstancePage);

	await serviceProviderPage.goTo();

	if (serviceProvider.allowShowingTheLoginPortlet === false) {
		await serviceProviderPage.allowShowingTheLoginPortlet.setChecked(false);
	}
	else {
		await serviceProviderPage.allowShowingTheLoginPortlet.setChecked(true);
	}

	if (serviceProvider.clockSkew) {
		await serviceProviderPage.clockSkew.fill(serviceProvider.clockSkew);
	}
	else {
		await serviceProviderPage.clockSkew.fill('3000');
	}

	if (serviceProvider.ldapImportEnabled === true) {
		await serviceProviderPage.ldapImportEnabled.setChecked(true);
	}
	else {
		await serviceProviderPage.ldapImportEnabled.setChecked(false);
	}

	if (serviceProvider.requireAssertionSignature === true) {
		await serviceProviderPage.requireAssertionSignature.setChecked(true);
	}
	else {
		await serviceProviderPage.requireAssertionSignature.setChecked(false);
	}

	if (serviceProvider.signAuthnRequests === false) {
		await serviceProviderPage.signAuthnRequests.setChecked(false);
	}
	else {
		await serviceProviderPage.signAuthnRequests.setChecked(true);
	}

	if (serviceProvider.signMetadata === false) {
		await serviceProviderPage.signMetadata.setChecked(false);
	}
	else {
		await serviceProviderPage.signMetadata.setChecked(true);
	}

	if (serviceProvider.sslRequired === true) {
		await serviceProviderPage.sslRequired.setChecked(true);
	}
	else {
		await serviceProviderPage.sslRequired.setChecked(false);
	}

	await serviceProviderPage.saveButton.click();

	await serviceProviderPage.successMessage.waitFor();

	await performLogout(serviceProviderPage.page);

	liferayConfig.environment.baseUrl = defaultBaseUrl;
}
