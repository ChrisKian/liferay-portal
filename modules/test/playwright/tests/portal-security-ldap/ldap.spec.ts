/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {instanceSettingsPagesTest} from "../../fixtures/instanceSettingsPagesTest";

export const test = mergeTests(
	loginTest(),
	instanceSettingsPagesTest
);

test('setting up LDAP server connection', async ({page, instanceSettingsPage}) => {

	await instanceSettingsPage.goToInstanceSetting(
		'LDAP',
		'Servers',
		false
	);

	await instanceSettingsPage.page.getByRole('button', {name: 'Add'}).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('Server Name Required').fill("test");

	//testing OpenLDAP Server default with correct principal

	await instanceSettingsPage.page.getByLabel('OpenLDAP').click();

	await instanceSettingsPage.page.getByLabel('Principal').fill('cn=admin,dc=example,dc=com');

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	await instanceSettingsPage.page.waitForTimeout(500);

	// Testing users

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Users' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	await instanceSettingsPage.page.waitForTimeout(500);

	// Testing Groups

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Groups' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await expect(false);
});