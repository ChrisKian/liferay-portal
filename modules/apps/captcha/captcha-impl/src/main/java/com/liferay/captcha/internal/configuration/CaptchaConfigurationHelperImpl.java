///**
// * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
// * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
// */
//
//package com.liferay.captcha.internal.configuration;
//
//import com.liferay.captcha.configuration.CaptchaConfiguration;
//import com.liferay.captcha.configuration.CaptchaConfigurationHelper;
//import com.liferay.petra.string.StringPool;
//import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
//import com.liferay.portal.kernel.log.Log;
//import com.liferay.portal.kernel.log.LogFactoryUtil;
//import com.liferay.portal.kernel.model.Company;
//import com.liferay.portal.kernel.model.CompanyConstants;
//import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
//import com.liferay.portal.kernel.util.GetterUtil;
//import com.liferay.portal.kernel.util.HashMapDictionary;
//import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
//import com.liferay.portal.kernel.util.UnicodeProperties;
//import com.liferay.portal.kernel.util.Validator;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.Constants;
//import org.osgi.framework.ServiceRegistration;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.service.cm.ConfigurationException;
//import org.osgi.service.cm.ManagedServiceFactory;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Deactivate;
//import org.osgi.service.component.annotations.Reference;
//
//import java.util.Collections;
//import java.util.Dictionary;
//import java.util.Enumeration;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author Christopher Kian
// */
//@Component(service = CaptchaConfigurationHelper.class)
//public class CaptchaConfigurationHelperImpl
//	implements CaptchaConfigurationHelper {
//
//	@Override
//	public CaptchaConfiguration getCaptchaConfiguration() {
//		ConfigurationHolder configurationHolder =
//			_configurationHolderByCompanyId.get(
//				CompanyThreadLocal.getCompanyId());
//
//		if (configurationHolder == null) {
//			configurationHolder = _configurationHolderByCompanyId.get(
//				CompanyConstants.SYSTEM);
//		}
//
//		if (configurationHolder != null) {
//			return configurationHolder.getCaptchaConfiguration();
//		}
//
//		return _defaultCaptchaConfiguration;
//	}
//
//	@Activate
//	protected void activate(BundleContext bundleContext) {
//		_serviceRegistration = bundleContext.registerService(
//			ManagedServiceFactory.class,
//			new CaptchaConfigurationServiceFactory(),
//			HashMapDictionaryBuilder.put(
//				Constants.SERVICE_PID,
//				CaptchaConfigurationHelperImpl.FACTORY_PID
//			).build());
//	}
//
//	private void _updateConfiguration(
//		String pid, Dictionary<String, ?> properties)
//		throws ConfigurationException {
//
////		Long companyId = GetterUtil.getLong(properties.get("companyId"));
//
//		Long companyId = CompanyThreadLocal.getCompanyId();
//
//		CaptchaConfiguration samlProviderConfiguration =
//			ConfigurableUtil.createConfigurable(
//				CaptchaConfiguration.class, properties);
//
//		ConfigurationHolder configurationHolder = new ConfigurationHolder(
//			samlProviderConfiguration, pid);
//
//		_configurationHolderByCompanyId.put(companyId, configurationHolder);
//
//		ConfigurationHolder oldConfigurationHolder =
//			_configurationHolderByPid.put(pid, configurationHolder);
//
//		if (oldConfigurationHolder != null) {
//			CaptchaConfiguration oldCaptchaConfiguration =
//				oldConfigurationHolder.getCaptchaConfiguration();
//
//			if (oldCaptchaConfiguration.companyId() != companyId) {
//				_configurationHolderByCompanyId.remove(
//					oldCaptchaConfiguration.companyId());
//			}
//		}
//	}
//
//	@Deactivate
//	protected void deactivate() {
//		_serviceRegistration.unregister();
//	}
//
//	protected static final String FACTORY_PID =
//		"com.liferay.captcha.configuration.CaptchaConfiguration";
//
//	private Dictionary<String, ?> _getSystemProperties() throws Exception {
//		ConfigurationHolder configurationHolder =
//			_configurationHolderByCompanyId.get(CompanyConstants.SYSTEM);
//
//		if (configurationHolder == null) {
//			return null;
//		}
//
//		Configuration configuration = _configurationAdmin.getConfiguration(
//			configurationHolder.getPid(), StringPool.QUESTION);
//
//		return configuration.getProperties();
//	}
//
//
//	private static final Log _log = LogFactoryUtil.getLog(
//		CaptchaConfigurationHelperImpl.class);
//
//	@Reference
//	private ConfigurationAdmin _configurationAdmin;
//
//	private final Map<Long, ConfigurationHolder>
//		_configurationHolderByCompanyId = new ConcurrentHashMap<>();
//	private final Map<String, ConfigurationHolder> _configurationHolderByPid =
//		new ConcurrentHashMap<>();
//	private final CaptchaConfiguration _defaultCaptchaConfiguration =
//		ConfigurableUtil.createConfigurable(
//			CaptchaConfiguration.class, Collections.emptyMap());
//	private ServiceRegistration<ManagedServiceFactory> _serviceRegistration;
//
//	private class ConfigurationHolder {
//
//		public ConfigurationHolder(
//			CaptchaConfiguration captchaConfiguration, String pid) {
//
//			_captchaConfiguration = captchaConfiguration;
//			_pid = pid;
//		}
//
//		public String getPid() {
//			return _pid;
//		}
//
//		public CaptchaConfiguration getCaptchaConfiguration() {
//			return _captchaConfiguration;
//		}
//
//		private final String _pid;
//		private final CaptchaConfiguration _captchaConfiguration;
//
//	}
//
//	private class CaptchaConfigurationServiceFactory
//		implements ManagedServiceFactory {
//
//		@Override
//		public void deleted(String pid) {
//			ConfigurationHolder configurationHolder =
//				_configurationHolderByPid.remove(pid);
//
//			if (configurationHolder == null) {
//				if (_log.isWarnEnabled()) {
//					_log.warn("Unable to delete missing configuration " + pid);
//				}
//
//				return;
//			}
//
//			CaptchaConfiguration captchaConfiguration =
//				configurationHolder.getCaptchaConfiguration();
//
//			_configurationHolderByCompanyId.remove(
//				captchaConfiguration);
//		}
//
//		@Override
//		public String getName() {
//			return "Captcha Configuration Factory";
//		}
//
//		@Override
//		public void updated(String pid, Dictionary<String, ?> properties)
//			throws ConfigurationException {
//
//			_updateConfiguration(pid, properties);
//		}
//
//	}
//
//}