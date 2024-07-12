/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {connectSpAndIdp} from '../tests/saml-web/utils/samlProviderConnectionUtil';
import {
	SamlInstanceNames,
	deleteVirtualInstance,
} from '../tests/saml-web/utils/samlVirtualInstanceUtil';

export interface SamlSsoOptions {
	password?: string;
	screenName?: string;
}

export interface SamlSso {
	idpVirtualInstance: {
		name: SamlInstanceNames;
		adminUser: {
			emailAddress: string;
			password: string;
			screenName: string;
		};
		baseUrl: string;
	};
	spVirtualInstance: {
		name: SamlInstanceNames;
		adminUser: {
			emailAddress: string;
			password: string;
			screenName: string;
		};
		baseUrl: string;
	};
}

import {
	createIdentityProviderVirtualInstance,
	createServiceProviderVirtualInstance,
} from '../tests/saml-web/utils/samlVirtualInstanceUtil';

function samlSsoTest(options: SamlSsoOptions = {}) {
	const fixtureImpl = test.extend<SamlSso>({
		idpVirtualInstance: [
			async ({page}, use) => {
				const password = options.password || 'test';
				const screenName = options.screenName || 'test';

				try {

					// create virtual instance

					await createIdentityProviderVirtualInstance(
						'idp',
						'idp',
						page
					);
				}
				catch (error) {

					// if they already exist, skip

				}

				try {
					await use({
						name: 'idp',
						adminUser: {
							emailAddress: `${screenName}@idp.com`,
							password,
							screenName,
						},
						baseUrl: 'http://idp:8080/',
					});
				}
				finally {
					await page.goto('/');
					await deleteVirtualInstance('idp', page);
				}
			},
			{auto: true},
		],
		spVirtualInstance: [
			async ({page}, use) => {
				const password = options.password || 'test';
				const screenName = options.screenName || 'test';

				try {

					// create virtual instance

					await createServiceProviderVirtualInstance(
						'sp',
						'sp',
						page
					);
				}
				catch (error) {

					// if they already exist, skip

				}

				try {

					// connect idp and sp

					await connectSpAndIdp(
						'idp',
						'sp',
						undefined,
						undefined,
						undefined,
						undefined,
						page
					);
				}
				catch (error) {

					// if connections already exist, skip

				}

				try {
					await use({
						name: 'sp',
						adminUser: {
							emailAddress: `${screenName}@sp.com`,
							password,
							screenName,
						},
						baseUrl: 'http://sp:8080/',
					});
				}
				finally {
					await page.goto('/');
					await deleteVirtualInstance('sp', page);
				}
			},
			{auto: true},
		],
	});

	return fixtureImpl;
}

export {samlSsoTest};
