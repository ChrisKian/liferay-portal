/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from "../../../liferay.config";

export async function connectSpAndIdp(
    idpName: string,
    spName: string,
    idpDomain = idpName,
    idpEntityId = idpName,
    spDomain = spName,
    spEntityId = spName,
    {
        identityProviderConnectionsPage,
        serviceProviderConnectionsPage
    }
) {
    await addServiceProviderConnection(
        idpDomain, spName, spDomain, spEntityId,
        {serviceProviderConnectionsPage});

    await addIdentityProviderConnection(
        idpName, spDomain, idpDomain, idpEntityId,
        {identityProviderConnectionsPage});
}

async function addIdentityProviderConnection(
    idpName: string,
    spDomain: string,
    idpDomain = idpName,
    idpEntityId = idpName,
    {
        identityProviderConnectionsPage,
    }
) {
    const defaultBaseUrl = liferayConfig.environment.baseUrl;

    liferayConfig.environment.baseUrl = `http://${spDomain}:8080`;

    await identityProviderConnectionsPage.addIdentityProviderConnection(
        idpName,
        idpEntityId,
        undefined,
        undefined,
        undefined,
        undefined,
        `http://${idpDomain}:8080/c/portal/saml/metadata`
    );

    liferayConfig.environment.baseUrl = defaultBaseUrl;
}

async function addServiceProviderConnection(
    idpDomain: string,
    spName: string,
    spDomain = spName,
    spEntityId = spName,
    {
        serviceProviderConnectionsPage,
    }
) {
    const defaultBaseUrl = liferayConfig.environment.baseUrl;

    liferayConfig.environment.baseUrl = `http://${idpDomain}:8080`;

    await serviceProviderConnectionsPage.addServiceProviderConnection(
        spName,
        spEntityId,
        undefined,
        undefined,
        undefined,
        `http://${spDomain}:8080/c/portal/saml/metadata`
    );

    liferayConfig.environment.baseUrl = defaultBaseUrl;
}