/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.platform.runtime.config;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.more.core.global.Global;
import org.more.core.global.assembler.XmlGlobalFactory;
import org.more.core.map.DecSequenceMap;
import org.more.core.map.Properties;
import org.more.util.ResourceWatch;
import org.more.util.ResourcesUtil;
import org.platform.Assert;
import org.platform.api.context.Config;
import org.platform.runtime.Platform;
/**
 * ServletContext��ContextConfig����
 * @version : 2013-4-2
 * @author ������ (zyc@byshell.org)
 */
public class PlatformConfig implements Config {
    final String           appSettingsName1 = "config.xml";
    final String           appSettingsName2 = "static-config.xml";
    final String           appSettingsName3 = "config-mapping.properties";
    private ServletContext servletContext   = null;
    //
    //
    public PlatformConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    @Override
    public String getInitParameter(String name) {
        return this.servletContext.getInitParameter(name);
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }
    /***/
    public String getSettingsEncoding() {
        return "utf-8";
    }
    //
    //
    //
    private Global              globalConfig      = null;
    private Map<String, Object> allStaticSettings = null; /*���о�̬����*/
    private ResourceWatch       resourceWatch     = null; /*��س���*/
    @Override
    public Global getSettings() {
        if (this.globalConfig != null)
            return this.globalConfig;
        //1.finalSettings
        DecSequenceMap<String, Object> finalSettings = new DecSequenceMap<String, Object>();
        Map<String, Object> mainConfig = this.loadMainConfig();
        finalSettings.addMap(mainConfig);
        if (this.allStaticSettings == null)
            this.allStaticSettings = this.loadStaticConfig();
        finalSettings.addMap(this.allStaticSettings);
        Map<String, Object> mappingConfig = this.loadMappingConfig(finalSettings);
        finalSettings.addMap(mappingConfig);
        //2.resourceWatch
        if (this.resourceWatch == null) {
            try {
                URL configURL = ResourcesUtil.getResource(appSettingsName1);
                Assert.isNotNull(configURL, "Can't get to " + configURL);
                this.resourceWatch = new SettingsResourceWatch(configURL.toURI(), 15 * 1000/*15����һ��*/, this);
                this.resourceWatch.setDaemon(true);
                this.resourceWatch.start();
            } catch (Exception e) {
                Platform.error("resourceWatch start error, on : " + appSettingsName1 + " Settings file !", e);
            }
        }
        //3.globalConfig
        this.globalConfig = Global.newInterInstance(finalSettings);
        this.globalConfig.disableCaseSensitive();
        return globalConfig;
    }
    //
    //
    //
    /**װ���������ļ���̬���á�*/
    protected Map<String, Object> loadMainConfig() {
        String encoding = this.getSettingsEncoding();
        Map<String, Object> mainConfig = new HashMap<String, Object>();
        try {
            URL configURL = ResourcesUtil.getResource(appSettingsName1);
            if (configURL != null) {
                Platform.info("load ��" + configURL + "��");
                loadConfig(configURL.toURI(), encoding, mainConfig);
            }
        } catch (Exception e) {
            Platform.error("load ��" + appSettingsName1 + "�� error. ", e);
        }
        return mainConfig;
    }
    /**װ�ؾ�̬���á�*/
    protected Map<String, Object> loadStaticConfig() {
        String encoding = this.getSettingsEncoding();
        DecSequenceMap<String, Object> allStaticSettings = new DecSequenceMap<String, Object>();
        //1.װ������static-config.xml
        try {
            Map<String, Object> staticConfig = new HashMap<String, Object>();
            List<URL> streamList = ResourcesUtil.getResources(appSettingsName2);
            if (streamList != null) {
                for (URL resURL : streamList) {
                    Platform.info("load ��" + resURL + "��");
                    loadConfig(resURL.toURI(), encoding, staticConfig);
                }
            }
            allStaticSettings.addMap(staticConfig);
        } catch (Exception e) {
            Platform.error("load ��" + appSettingsName2 + "�� error. ", e);
        }
        return allStaticSettings;
    }
    /**װ������ӳ�䣬�����ǲ��յ�ӳ�����á�*/
    protected Map<String, Object> loadMappingConfig(Map<String, Object> referConfig) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        try {
            List<URL> mappingList = ResourcesUtil.getResources(appSettingsName3);
            if (mappingList != null)
                for (URL url : mappingList) {
                    InputStream inputStream = ResourcesUtil.getResourceAsStream(url);
                    Properties prop = new Properties();
                    prop.load(inputStream);
                    for (String key : prop.keySet()) {
                        String $propxyKey = key.toLowerCase();
                        String $key = prop.get(key).toLowerCase();
                        String value = (String) referConfig.get($key);
                        if (value == null)
                            Platform.warning("mapping.. " + $propxyKey + "=" + $key + " ���棬ֵΪ��");
                        else
                            dataMap.put($propxyKey, value.trim());
                    }
                }
        } catch (Exception e) {
            Platform.error("load ��" + appSettingsName3 + "�� error. ", e);
        }
        return dataMap;
    }
    //
    //
    /**loadConfigװ������*/
    private static Map<String, Object> loadConfig(URI configURI, String encoding, Map<String, Object> loadTo) throws IOException {
        Map<String, Object> configData = (loadTo == null) ? new HashMap<String, Object>() : loadTo;
        Platform.info("PlatformSettings loadConfig Xml namespace : " + Platform.logString(configURI));
        XmlGlobalFactory xmlg = null;
        //1.<������Ч�������ռ�>
        try {
            xmlg = new XmlGlobalFactory();
            xmlg.setIgnoreRootElement(true);
            //xmlg.getLoadNameSpace().add("http://project.byshell.org/framework/schema/global");
            //xmlg.getLoadNameSpace().add("http://project.byshell.org/framework/schema/global");
            //xmlg.getLoadNameSpace().add("http://noe.xdf.cn/schema/product/global-config");
            Map<String, Object> version_1_DataMap = xmlg.createMap(encoding, new Object[] { ResourcesUtil.getResourceAsStream(configURI) });
            for (String key : version_1_DataMap.keySet())
                configData.put(key.toLowerCase(), version_1_DataMap.get(key));
        } catch (Exception e) {
            Platform.warning("namespcae [" + configURI + "] no support!");
        }
        return configData;
    }
    /** */
    private static class SettingsResourceWatch extends ResourceWatch {
        private PlatformConfig platformConfig = null;
        public SettingsResourceWatch(URI uri, int watchStepTime, PlatformConfig platformConfig) {
            super(uri, watchStepTime);
            this.platformConfig = platformConfig;
        }
        @Override
        public void reload(URI resourceURI) throws IOException {
            this.platformConfig.globalConfig = null;//������globalConfigȻ������װ������
            this.platformConfig.getSettings();
        }
        @Override
        public long lastModify(URI resourceURI) throws IOException {
            if ("file".equals(resourceURI.getScheme()) == true)
                return new File(resourceURI).lastModified();
            return 0;
        }
    }
}