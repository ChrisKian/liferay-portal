/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {instanceSettingsPagesTest} from "../../fixtures/instanceSettingsPagesTest";
import {waitForAlert} from "../../utils/waitForAlert";

export const test = mergeTests(
	loginTest(),
	instanceSettingsPagesTest
);

test('setting up LDAP server connection', async ({page, instanceSettingsPage}) => {

	// await instanceSettingsPage.goToInstanceSetting(
	// 	'LDAP',
	// 	'General',
	// 	false
	// );
	//
	// await instanceSettingsPage.page.getByText('Enabled', { exact: true }).setChecked(true);
	//
	// await instanceSettingsPage.page.getByRole('button', {name: 'Save'}).click();
	//
	// await waitForAlert(page, 'Success:Your request completed successfully.');
	//
	// await instanceSettingsPage.page.waitForTimeout(8000);

	await instanceSettingsPage.goToInstanceSetting(
		'LDAP',
		'Servers',
		false
	);

	await instanceSettingsPage.page.getByRole('button', {name: 'Add'}).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('Server Name Required').fill("test");

	//testing ApacheDS Server

	await instanceSettingsPage.page.getByText('Apache Directory Server').click();

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	await instanceSettingsPage.page.getByLabel('Base Provider URL The LDAP').fill("ldap://0.0.0.0:10389");

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	//test with different principal as well

	await instanceSettingsPage.page.getByText('Apache Directory Server').click();

	await instanceSettingsPage.page.getByLabel('Principal').fill('uid=admin,dc=example,dc=com');

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	await instanceSettingsPage.page.getByLabel('Base Provider URL The LDAP').fill("ldap://0.0.0.0:10389");

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('close').click();

	//testing OpenLDAP Server

	await instanceSettingsPage.page.getByLabel('OpenLDAP').click();

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('Base Provider URL The LDAP').fill("ldap://localhost:389");

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	// test with different principal as well

	await instanceSettingsPage.page.getByLabel('OpenLDAP').click();

	await instanceSettingsPage.page.getByLabel('Principal').fill('cn=admin,dc=example,dc=com');

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await instanceSettingsPage.page.getByLabel('Base Provider URL The LDAP').fill("ldap://localhost:389");

	await instanceSettingsPage.page.getByRole('button', { name: 'Test LDAP Connection' }).click();

	await instanceSettingsPage.page.waitForTimeout(2000);

	await expect(false);
});