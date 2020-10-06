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

package com.liferay.document.library.internal.search.spi.model.index.contributor;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.GetDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.GetDocumentResponse;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.indexer.IndexerWriter;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.trash.TrashHelper;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFolder",
	service = ModelDocumentContributor.class
)
public class DLFolderModelDocumentContributor
	implements ModelDocumentContributor<DLFolder> {

	@Override
	public void contribute(Document document, DLFolder dlFolder) {
		if (_log.isDebugEnabled()) {
			_log.debug("Indexing folder " + dlFolder);
		}

		document.addText(Field.DESCRIPTION, dlFolder.getDescription());
		document.addKeyword(Field.FOLDER_ID, dlFolder.getParentFolderId());
		document.addKeyword(
			Field.HIDDEN, dlFolder.isHidden() || dlFolder.isInHiddenFolder());

		String title = dlFolder.getName();

		if (dlFolder.isInTrash()) {
			title = trashHelper.getOriginalTitle(title);
		}

		document.addText(Field.TITLE, title);

		document.addKeyword(Field.TREE_PATH, dlFolder.getTreePath());
		document.addKeyword(
			Field.TREE_PATH,
			StringUtil.split(dlFolder.getTreePath(), CharPool.SLASH));

		if (_log.isDebugEnabled()) {
			_log.debug("Document " + dlFolder + " indexed successfully");
		}

		if (_changingPermissions(document, dlFolder)) {
			_updateChildrenPermissions(document, dlFolder);
		}
	}

	@Reference(unbind = "-")
	protected void setIndexNameBuilder(IndexNameBuilder indexNameBuilder) {
		_indexNameBuilder = indexNameBuilder;
	}

	@Reference(
		target = "(indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry)"
	)
	protected IndexerWriter<DLFileEntry> dLFileEntryIndexerWriter;

	@Reference
	protected DLFileEntryLocalService dlFileEntryLocalService;

	@Reference(
		target = "(indexer.class.name=com.liferay.document.library.kernel.model.DLFolder)"
	)
	protected IndexerWriter<DLFolder> dLFolderIndexerWriter;

	@Reference
	protected DLFolderLocalService dlFolderLocalService;

	@Reference
	protected TrashHelper trashHelper;

	private boolean _changingPermissions(Document document, DLFolder dlFolder) {
		String indexName = _indexNameBuilder.getIndexName(
			dlFolder.getCompanyId());

		Field uidField = document.getField(Field.UID);

		GetDocumentRequest getDocumentRequest = new GetDocumentRequest(
			indexName, uidField.getValue());

		String permissionFieldNames =
			Field.ROLE_ID + StringPool.COMMA + Field.GROUP_ROLE_ID;

		getDocumentRequest.setFetchSource(true);
		getDocumentRequest.setFetchSourceInclude(permissionFieldNames);
		getDocumentRequest.setPreferLocalCluster(false);

		GetDocumentResponse getDocumentResponse = _searchEngineAdapter.execute(
			getDocumentRequest);

		if (!getDocumentResponse.isExists()) {
			return true;
		}

		com.liferay.portal.search.document.Document indexedDocument =
			getDocumentResponse.getDocument();

		List<String> indexedRoleIds = indexedDocument.getStrings(Field.ROLE_ID);
		List<String> indexedGroupRoleIds = indexedDocument.getStrings(
			Field.GROUP_ROLE_ID);

		List<Long> roleIds = new ArrayList<>();
		List<String> groupRoleIds = new ArrayList<>();

		try {
			List<Role> roles =
				_resourcePermissionLocalService.getDynamicInheritanceRoles(
					dlFolder.getCompanyId(),
					"com.liferay.document.library.kernel.model.DLFolder",
					ResourceConstants.SCOPE_INDIVIDUAL,
					String.valueOf(dlFolder.getFolderId()), "VIEW");

			for (Role role : roles) {
				if ((role.getType() == RoleConstants.TYPE_ORGANIZATION) ||
					(role.getType() == RoleConstants.TYPE_SITE)) {

					groupRoleIds.add(
						dlFolder.getGroupId() + StringPool.DASH +
							role.getRoleId());
				}
				else {
					roleIds.add(role.getRoleId());
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to retrieve roles for DLFolder " +
					dlFolder.getFolderId(),
				exception);
		}

		if ((roleIds.size() != indexedRoleIds.size()) ||
			(groupRoleIds.size() != indexedGroupRoleIds.size())) {

			return true;
		}

		for (String indexedGroupRole : indexedGroupRoleIds) {
			if (groupRoleIds.contains(indexedGroupRole)) {
				continue;
			}

			return true;
		}

		for (String indexedRoleId : indexedRoleIds) {
			if (roleIds.contains(GetterUtil.getLong(indexedRoleId))) {
				continue;
			}

			return true;
		}

		return false;
	}

	private void _updateChildrenPermissions(
		Document document, DLFolder parentFolder) {

		List<DLFileEntry> dlFileEntries =
			dlFileEntryLocalService.getFileEntries(
				parentFolder.getGroupId(), parentFolder.getFolderId());

		for (DLFileEntry dlFileEntry : dlFileEntries) {
			dLFileEntryIndexerWriter.updatePermissionFields(dlFileEntry);

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Document " + dlFileEntry +
						" permissions indexed successfully");
			}
		}

		List<DLFolder> dlFolders = dlFolderLocalService.getFolders(
			parentFolder.getGroupId(), parentFolder.getFolderId());

		for (DLFolder dlFolder : dlFolders) {
			dLFolderIndexerWriter.updatePermissionFields(dlFolder);

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Document " + dlFolder +
						" permissions indexed successfully");
			}

			_updateChildrenPermissions(document, dlFolder);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DLFolderModelDocumentContributor.class);

	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

}