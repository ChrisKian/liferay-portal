/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from "../../../liferay.config";
import performLogin from "../../../utils/performLogin";

export async function createServiceProviderVirtualInstance(
    name: string,
    entityId: string,
    {
        page,
        samlAdminPage,
        virtualInstancesPage
    }
){
    await _createSamlVirtualInstance(
        name, entityId, "Service Provider",
        {page, samlAdminPage, virtualInstancesPage});
}

export async function createIdentityProviderVirtualInstance(
    name: string,
    entityId: string,
    {
        page,
        samlAdminPage,
        virtualInstancesPage
    }
){
    await _createSamlVirtualInstance(
        name, entityId, "Identity Provider",
        {page, samlAdminPage, virtualInstancesPage});
}

async function _createSamlVirtualInstance(
    name: string,
    entityId: string,
    samlRole: string,
    {
        page,
        samlAdminPage,
        virtualInstancesPage
    }
){
    await virtualInstancesPage.addNewVirtualInstance(
        undefined,
        undefined,
        name,
        undefined
    );

    const defaultBaseUrl = liferayConfig.environment.baseUrl;

    liferayConfig.environment.baseUrl = `http://${name}:8080`;

    await performLogin(page, 'test', liferayConfig.environment.baseUrl, `@${name}.com`);

    await samlAdminPage.configureSAML(
        true,
        entityId,
        samlRole
    );

    liferayConfig.environment.baseUrl = defaultBaseUrl;
}