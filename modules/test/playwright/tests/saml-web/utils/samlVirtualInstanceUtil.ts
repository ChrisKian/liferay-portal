/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from '../../../liferay.config';
import {VirtualInstancesPage} from '../../../pages/portal-instances-web/VirtualInstancesPage';
import {SamlAdminPage} from '../../../pages/saml-web/SamlAdminPage';
import performLogin from '../../../utils/performLogin';

export type SamlInstanceNames = 'idp' | 'sp';

export async function createServiceProviderVirtualInstance(
	name: string,
	entityId: string,
	page
) {
	await _createSamlVirtualInstance(name, entityId, 'Service Provider', page);
}

export async function createIdentityProviderVirtualInstance(
	name = 'idp',
	entityId = name,
	page
) {
	await _createSamlVirtualInstance(name, entityId, 'Identity Provider', page);
}

export async function deleteVirtualInstance(name: string, page) {
	const virtualInstancesPage = new VirtualInstancesPage(page);

	await virtualInstancesPage.deleteVirtualInstance(name);
}

async function _createSamlVirtualInstance(
	name: string,
	entityId: string,
	samlRole: string,
	page
) {
	const virtualInstancesPage = new VirtualInstancesPage(page);

	await virtualInstancesPage.addNewVirtualInstance(
		undefined,
		undefined,
		name,
		undefined
	);

	const defaultBaseUrl = liferayConfig.environment.baseUrl;

	liferayConfig.environment.baseUrl = `http://${name}:8080`;

	await performLogin(
		page,
		'test',
		liferayConfig.environment.baseUrl,
		`@${name}.com`
	);

	const samlAdminPage = new SamlAdminPage(page);

	await samlAdminPage.configureSAML(true, entityId, samlRole);

	liferayConfig.environment.baseUrl = defaultBaseUrl;
}
