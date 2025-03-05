/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {instanceSettingsPagesTest} from '../../fixtures/instanceSettingsPagesTest';
import {ldapConfigurationPagesTest} from '../../fixtures/ldapConfigurationPagesTest';
import {loginTest} from '../../fixtures/loginTest';
import {systemSettingsPageTest} from '../../fixtures/systemSettingsPageTest';
import {usersAndOrganizationsPagesTest} from '../../fixtures/usersAndOrganizationsPagesTest';
import {
	TLdapConfiguration,
	TLdapServer
} from '../../helpers/LdapConfigurationHelper';
import getRandomString from '../../utils/getRandomString';
import {waitForAlert} from "../../utils/waitForAlert";

export const test = mergeTests(
	loginTest(),
	instanceSettingsPagesTest,
	ldapConfigurationPagesTest,
	systemSettingsPageTest,
	usersAndOrganizationsPagesTest
);

const LDAP_GROUP_1 = 'ldapgroup1';
const LDAP_GROUP_2 = 'ldapgroup2';
const LDAP_USER_1 = 'ldapuser1';
const LDAP_USER_2 = 'ldapuser2';

test.afterEach(async ({ldapConfigurationPage, ldapServerPage}) => {
	await ldapServerPage.deleteLdapServers();
	await ldapConfigurationPage.resetLdapConfiguration();
});

test('LPD-47428: Verify a single LDAP user can belong to multiple User Groups imported from LDAP', async ({
	browser,
	editUserPage,
	ldapConfigurationPage,
	ldapServerPage,
	systemSettingsPage,
	usersAndOrganizationsPage,
}) => {
	const ldapServer1: TLdapServer = {
		defaultValues: 'OpenLDAP',
		importSearchFilterGroup:
			`(&(objectClass=groupOfUniqueNames)(cn=${LDAP_GROUP_1}))`,
		principal: 'cn=admin,dc=example,dc=com',
		serverName: getRandomString(),
	};

	const ldapServer2: TLdapServer = {
		defaultValues: 'OpenLDAP',
		importSearchFilterGroup:
			`(&(objectClass=groupOfUniqueNames)(cn=${LDAP_GROUP_2}))`,
		principal: 'cn=admin,dc=example,dc=com',
		serverName: getRandomString(),
	};

	await test.step('Add the same LDAP server twice, but adjust the group import search filter so each entry adds a different group', async () => {
		await test.step('Add first LDAP server', async () => {
			await ldapServerPage.addLdapServer(ldapServer1);
		});

		await test.step('Verify first LDAP server it only imports the first group', async () => {
			await ldapServerPage.viewLdapServer(ldapServer1.serverName, false);

			await ldapServerPage.testLdapGroups.click();

			await expect(
				await ldapServerPage.page.getByRole('cell', {name: LDAP_GROUP_1})
			).toBeVisible();

			await expect(
				await ldapServerPage.page.getByRole('cell', {name: LDAP_GROUP_2})
			).not.toBeVisible();

			await ldapServerPage.closeButton.click();

			await ldapServerPage.cancelButton.click();
		});

		await test.step('Add second LDAP server', async () => {
			await ldapServerPage.addLdapServer(ldapServer2, false);
		});

		await test.step('Verify second LDAP server it only imports the second group', async () => {

			await ldapServerPage.viewLdapServer(ldapServer2.serverName, false);

			await ldapServerPage.testLdapGroups.click();

			await expect(
				await ldapServerPage.page.getByRole('cell', {name: LDAP_GROUP_2})
			).toBeVisible();

			await expect(
				await ldapServerPage.page.getByRole('cell', {name: LDAP_GROUP_1})
			).not.toBeVisible();

			await ldapServerPage.closeButton.click();

			await ldapServerPage.cancelButton.click();
		});
	});

	// We must also update the System LDAP configuration import interval

	await systemSettingsPage.goToSystemSetting(
		'LDAP',
		'Import'
	);

	await systemSettingsPage.page.getByLabel('Import Interval').fill('1');

	await systemSettingsPage.page.getByRole('button', {name: 'Save'}).click();

	await waitForAlert(
		systemSettingsPage.page,
		`Success:Your request completed successfully.`
	);

	const ldapConfiguration: TLdapConfiguration ={
		enableImport: true,
		enabled: true,
		importInterval: 1,
		importMethod: 'Group',
	}

	await ldapConfigurationPage.updateLDAPConfiguration(ldapConfiguration);

	// There is no way to trigger a bulk import via playwright, so we must wait
	// a full minute for the import interval to be reached

	await ldapConfigurationPage.page.waitForTimeout(60 * 1000);

	// Go to the imported LDAP user and verify the user groups are imported

	await usersAndOrganizationsPage.goToUsers(false);

	await(
		await usersAndOrganizationsPage.usersTableRowLink(LDAP_USER_1)
	).click();

	await editUserPage.membershipsLink.click();

	await expect(
		(
			await editUserPage.membershipsUserGroupsTableRow(
				0,
				LDAP_GROUP_1,
				true
			)
		).row
	).toBeVisible();

	await expect(
		(
			await editUserPage.membershipsUserGroupsTableRow(
				0,
				LDAP_GROUP_2,
				true
			)
		).row
	).toBeVisible();

	//login as ldapUser1 with bad credentials

	const page = await browser.newPage();

	await page.goto('/');

	await page.getByRole('button', {name: 'Sign In'}).last().click();

	await page.getByLabel('Email Address').fill(`${LDAP_USER_1}@liferay.com`);
	await page.getByLabel('Password').fill('badPassword');

	await page.getByRole('button', {name: 'Sign In'}).last().click();

	await editUserPage.page.waitForTimeout(5000);

	await await editUserPage.page.reload();

	await expect(
		(
			await editUserPage.membershipsUserGroupsTableRow(
				0,
				LDAP_GROUP_1,
				true
			)
		).row
	).toBeVisible();

	await expect(
		(
			await editUserPage.membershipsUserGroupsTableRow(
				0,
				LDAP_GROUP_2,
				true
			)
		).row
	).toBeVisible();

	//reindex users and user groups if needed
	//verify user groups are correct









});

test('smoke: Add LDAP server, verify connection, users, and groups are mapped properly, edit LDAP server, then delete LDAP server', async ({
	ldapConfigurationPage,
	ldapServerPage,
}) => {
	const serverName = getRandomString();

	const ldapServer: TLdapServer = {
		defaultValues: 'OpenLDAP',
		principal: 'cn=admin,dc=example,dc=com',
		serverName,
	};

	await test.step('Add LDAP Server', async () => {
		await ldapServerPage.addLdapServer(ldapServer);
	});

	await test.step('Test LDAP Server connections', async () => {
		await ldapServerPage.viewLdapServer(serverName, false);

		await ldapServerPage.testLdapConnection.click();

		await expect(
			await ldapServerPage.page.getByText(
				'Liferay has successfully connected to the LDAP server'
			)
		).toBeVisible();

		await ldapServerPage.closeButton.click();

		await ldapServerPage.testLdapUsers.click();

		await expect(
			await ldapServerPage.page.getByText(
				'A subset of users has been displayed for you to review'
			)
		).toBeVisible();

		await ldapServerPage.closeButton.click();

		await ldapServerPage.testLdapGroups.click();

		await expect(
			await ldapServerPage.page.getByText(
				'A subset of groups has been displayed for you to review'
			)
		).toBeVisible();

		await ldapServerPage.closeButton.click();

		await ldapServerPage.cancelButton.click();
	});

	await test.step('Edit LDAP Server by changing server name', async () => {
		ldapServer.serverName = 'newServerName';

		await ldapServerPage.editLdapServer(ldapServer, serverName, false);

		await expect(
			await ldapConfigurationPage.page.getByRole('row', {
				name: 'newServerName',
			})
		).toBeVisible();
	});

	await test.step('Delete LDAP server', async () => {
		await ldapServerPage.deleteLdapServer('newServerName', false);

		await expect(
			await ldapConfigurationPage.page.getByRole('row', {
				name: 'newServerName',
			})
		).toBeHidden();
	});
});
