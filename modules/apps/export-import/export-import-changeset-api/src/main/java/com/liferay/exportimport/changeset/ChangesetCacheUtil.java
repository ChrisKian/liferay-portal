package com.liferay.exportimport.changeset;

import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.PortalCacheHelperUtil;
import com.liferay.portal.kernel.cache.PortalCacheManagerNames;
import com.liferay.portal.util.PropsValues;

import java.util.List;

/**
 * @author Christopher Kian
 */
public class ChangesetCacheUtil {

	public static final String CHANGESET_CACHE_NAME =
		ChangesetCacheUtil.class.getName() + "_CHANGESET";

	public static void clearCache() {
		if (ExportImportThreadLocal.isImportInProcess()) {
			return;
		}

		_changesetPortalCache.removeAll();
	}

	public static Changeset getChangeset(String changesetUuid) {
		return _changesetPortalCache.get(changesetUuid);
	}

	public static boolean hasChangeset(String changesetUuid) {
		List<String> keys = _changesetPortalCache.getKeys();

		if (keys.contains(changesetUuid)) {
			return true;
		}

		return false;
	}

	public static void putChangeset(Changeset changeset) {
		if (changeset == null) {
			return;
		}

		_changesetPortalCache.put(changeset.getUuid(), changeset);
	}

	public static Changeset removeChangeset(String changesetUuid) {
		Changeset changeset = getChangeset(changesetUuid);

		_changesetPortalCache.remove(changesetUuid);

		return changeset;
	}

	private static final PortalCache<String, Changeset> _changesetPortalCache =
		PortalCacheHelperUtil.getPortalCache(
			PortalCacheManagerNames.MULTI_VM, CHANGESET_CACHE_NAME,
			PropsValues.PERMISSIONS_OBJECT_BLOCKING_CACHE);

}