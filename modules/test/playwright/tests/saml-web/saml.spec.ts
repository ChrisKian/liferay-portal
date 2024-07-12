/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {loginTest} from '../../fixtures/loginTest';
import {samlSsoTest} from '../../fixtures/samlSsoTest';
import {samlAdminPagesTest} from '../../fixtures/samlAdminPagesTest';
import {virtualInstancesPagesTest} from '../../fixtures/virtualInstancesPagesTest';
import {systemSettingsPageTest} from '../../fixtures/systemSettingsPageTest';
import {ApiHelpers} from '../../helpers/ApiHelpers';
import {liferayConfig} from '../../liferay.config';
import {getRandomInt} from '../../utils/getRandomInt';
import getRandomString from '../../utils/getRandomString';
import performLogin, {performLogout} from '../../utils/performLogin';
import {connectSpAndIdp} from './utils/samlProviderConnectionUtil';
import {
	createIdentityProviderVirtualInstance,
	createServiceProviderVirtualInstance,
} from './utils/samlVirtualInstanceUtil';

export const fixtureTest = mergeTests(
	samlSsoTest(),
);

export const test = mergeTests(
	apiHelpersTest,
	applicationsMenuPageTest,
	loginTest(),
	samlAdminPagesTest,
	systemSettingsPageTest,
	virtualInstancesPagesTest
);

test('Create, edit, and delete a new virtual instance', async ({
	editVirtualInstancePage,
	virtualInstancesPage,
}) => {
	const name = getRandomString();

	await virtualInstancesPage.addNewVirtualInstance(
		undefined,
		undefined,
		name,
		undefined
	);

	const newName = getRandomString();

	await editVirtualInstancePage.editVirtualInstance(
		false,
		name,
		newName + '.com',
		'100',
		newName
	);

	await expect(
		await virtualInstancesPage.page
			.getByRole('row')
			.getByText(name + ' ' + newName + ' ' + newName + '.com 0 100 No')
	).toBeVisible();

	await virtualInstancesPage.deleteVirtualInstance(name);
});

test('Create two virtual instances, one IdP and one SP, connect them, perform SP initialted SSO, perform SP initiated SLO', async ({
	browser,
	page,
	systemSettingsPage,
	virtualInstancesPage,
}) => {

	// Set the Keystore Manager Target to Doc Lib, so we can store multiple certificates in one instance

	await systemSettingsPage.goToSystemSetting(
		'SSO', 'SAML KeyStoreManager Implementation Configuration');

	await systemSettingsPage.page.getByLabel('Keystore Manager Target').click();

	await systemSettingsPage.page.getByRole('option', {name: 'Document Library Keystore Manager'}).click();

	await systemSettingsPage.page.getByRole('button', { name: 'Update' }).click();

	// Create new idp virtual instance

	const idpVirtualInstanceName = 'idp';

	await createIdentityProviderVirtualInstance(
		idpVirtualInstanceName,
		idpVirtualInstanceName,
		page
	);

	// Create new sp virtual instance

	await page.goto('/');

	const spVirtualInstanceName = 'sp';

	await createServiceProviderVirtualInstance(
		spVirtualInstanceName,
		spVirtualInstanceName,
		page
	);

	// Add a new connection for each provider, of the opposite provider

	await connectSpAndIdp(
		idpVirtualInstanceName,
		spVirtualInstanceName,
		undefined,
		undefined,
		undefined,
		undefined,
		page
	);

	// Next, attempt auth from SP, to IdP, then redirected back to SP

	const defaultBaseUrl = liferayConfig.environment.baseUrl;

	liferayConfig.environment.baseUrl = `http://${idpVirtualInstanceName}:8080`;

	// Create new page and apiHelper implementation based off IdP virtual instance

	const idpVirtualInstancePage = await browser.newPage({
		baseURL: liferayConfig.environment.baseUrl,
	});

	await performLogin(
		idpVirtualInstancePage,
		'test',
		undefined,
		`@${idpVirtualInstanceName}.com`
	);

	const idpApiHelpers = new ApiHelpers(idpVirtualInstancePage);

	liferayConfig.environment.baseUrl = defaultBaseUrl;

	// Create user in IdP instance

	const userId = getRandomInt();

	const user = await idpApiHelpers.headlessAdminUser.postUserAccount(
		undefined,
		userId
	);

	await performLogout(idpVirtualInstancePage);

	liferayConfig.environment.baseUrl = `http://${spVirtualInstanceName}:8080`;

	// Create new page and apiHelper implementation based off IdP virtual instance

	const spVirtualInstancePage = await browser.newPage({
		baseURL: `http://${spVirtualInstanceName}:8080`,
	});

	await performLogin(
		spVirtualInstancePage,
		'test',
		'?p_p_id=com_liferay_login_web_portlet_LoginPortlet&p_p_state=maximized',
		`@${spVirtualInstanceName}.com`
	);

	const spApiHelpers = new ApiHelpers(spVirtualInstancePage);

	liferayConfig.environment.baseUrl = defaultBaseUrl;

	// Create user in SP instance, using the same information as IdP user

	await spApiHelpers.headlessAdminUser.postUserAccount(undefined, userId);

	await performLogout(spVirtualInstancePage);

	// Try to login as the new user from SP

	await spVirtualInstancePage.goto('/');

	const signInButton = await spVirtualInstancePage.getByRole('button', {
		name: 'Sign In',
	});

	await signInButton.click();

	// Verify we are being redirected to the IdP instance

	await expect(
		await spVirtualInstancePage.getByText(
			'Redirecting to your identity provider...'
		)
	).toBeVisible();

	await spVirtualInstancePage.waitForTimeout(5000);

	// Verify we have been successfully redirected

	await expect(await spVirtualInstancePage.url()).toContain(
		idpVirtualInstancePage.url()
	);

	// Sign in

	await spVirtualInstancePage
		.getByLabel('Email Address')
		.fill(user.emailAddress);
	await spVirtualInstancePage.getByLabel('Password').fill('test');
	await spVirtualInstancePage.getByLabel('Remember Me').check();
	await spVirtualInstancePage.getByRole('button', {name: 'Sign In'}).click();

	// Wait for authentication to complete, and verify we've been redirected back to SP

	await spVirtualInstancePage.waitForTimeout(5000);

	await expect(await spVirtualInstancePage.url()).toContain(
		`http://${spVirtualInstanceName}:8080`
	);

	// Verify we are logged in

	await expect(await page.getByTitle('User Profile Menu')).toBeVisible();

	// Logout, verify we are also logged out of IdP

	await performLogout(spVirtualInstancePage);

	await idpVirtualInstancePage.goto('/');

	await expect(
		await idpVirtualInstancePage.getByRole('button', {name: 'Sign In'})
	).toBeVisible();

	// Lastly, delete both virtual instances

	await virtualInstancesPage.deleteVirtualInstance(idpVirtualInstanceName);

	await virtualInstancesPage.deleteVirtualInstance(spVirtualInstanceName);
});

fixtureTest('testing samlSsoTest fixture, please do not run', async ({
	idpVirtualInstance,
	spVirtualInstance,
}) => {

	// just showing we can get it

	await idpVirtualInstance.adminUser.emailAddress;
	await spVirtualInstance.adminUser.emailAddress;
});
