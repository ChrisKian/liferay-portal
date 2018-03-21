package com.liferay.wysiwyg.web.internal.exportimport.data.handler;

import com.liferay.exportimport.content.processor.ExportImportContentProcessor;
import com.liferay.exportimport.kernel.lar.BasePortletDataHandler;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataException;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.wysiwyg.web.internal.constants.WYSIWYGPortletKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletPreferences;

@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + WYSIWYGPortletKeys.WYSIWYG
	},
	service = {
		PortletDataHandler.class
	}
)
public class WYSIWYGPortletDataHandler extends BasePortletDataHandler {

	@Override
	public PortletPreferences processExportPortletPreferences(
		PortletDataContext portletDataContext, String portletId,
		PortletPreferences portletPreferences)
		throws PortletDataException {

		try {
			return doProcessExportPortletPreferences(
				portletDataContext, portletId, portletPreferences);
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	@Override
	public PortletPreferences processImportPortletPreferences(
		PortletDataContext portletDataContext, String portletId,
		PortletPreferences portletPreferences)
		throws PortletDataException {

		try {
			return doProcessImportPortletPreferences(
				portletDataContext, portletId, portletPreferences);
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	protected PortletPreferences doProcessExportPortletPreferences(
		PortletDataContext portletDataContext, String portletId,
		PortletPreferences portletPreferences)
		throws Exception {

		String message = portletPreferences.getValue(
			"message", StringPool.BLANK);

		if (message.matches("((?s).*)(\"\\/documents\\/(?s).*\")((?s).*)")) {
			long groupId = portletDataContext.getGroupId();

			StringBundler sb = new StringBundler();
			sb.append("/documents/");
			sb.append(groupId);
			String newMessage = message.replace(
				sb.toString(), "/documents/[$groupId$]");

			portletPreferences.setValue("message", newMessage);
		}

		return portletPreferences;
	}

	protected PortletPreferences doProcessImportPortletPreferences(
		PortletDataContext portletDataContext, String portletId,
		PortletPreferences portletPreferences)
		throws Exception {

		String message = portletPreferences.getValue(
			"message", StringPool.BLANK);

		if (message.matches("((?s).*)(\"\\/documents\\/(?s).*\")((?s).*)")) {
			long groupId = portletDataContext.getGroupId();
			StringBundler sb = new StringBundler();
			sb.append("/documents/");
			sb.append(groupId);
			String newMessage = message.replace(
				"/documents/[$groupId$]", sb.toString());

			portletPreferences.setValue("message", newMessage);
		}

		return portletPreferences;
	}

	@Reference(target = "(content.processor.type=DLReferences)")
	private ExportImportContentProcessor<String>
		_dlReferencesExportImportContentProcessor;
}
