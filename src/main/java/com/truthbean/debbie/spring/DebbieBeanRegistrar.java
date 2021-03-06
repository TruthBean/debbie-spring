/**
 * Copyright (c) 2020 TruthBean(Rogar·Q)
 * Debbie is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.truthbean.debbie.spring;

import com.truthbean.Logger;
import com.truthbean.debbie.bean.BeanInfoFactory;
import com.truthbean.debbie.bean.DebbieBeanInfo;
import com.truthbean.debbie.core.ApplicationContext;
import com.truthbean.debbie.core.ApplicationFactory;
import com.truthbean.logger.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

/**
 * @author TruthBean/Rogar·Q
 * @since 0.0.2
 * Created on 2020-06-04 23:43
 */
public class DebbieBeanRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    /**
     * The bean name of the internally managed Autowired annotation processor.
     */
    public static final String BEAN_INJECT_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.context.annotation.internalBeanInjectAnnotationProcessor";

    private ClassLoader classLoader;

    public DebbieBeanRegistrar() {
        LOGGER.debug(() -> "enable debbie bean by spring");
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_INJECT_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition definition = new RootBeanDefinition(DebbieBeanInjectAnnotationBeanPostProcessor.class);
            definition.setSource(null);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(BEAN_INJECT_ANNOTATION_PROCESSOR_BEAN_NAME, definition);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(definition, BEAN_INJECT_ANNOTATION_PROCESSOR_BEAN_NAME);
        }

        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableDebbieApplication.class.getName());
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(annotationAttributes);
        if (mapperScanAttrs != null) {
            String[] basePackages = (String[]) mapperScanAttrs.get("basePackages");
            Class<?>[] classes = (Class<?>[]) mapperScanAttrs.get("classes");
            String[] excludePackages = (String[]) mapperScanAttrs.get("excludePackages");
            Class<?>[] excludeClasses = (Class<?>[]) mapperScanAttrs.get("excludeClasses");

            ApplicationFactory applicationFactory = ApplicationFactory.configure(classLoader, configuration -> {
                configuration.addScanBasePackages(basePackages);
                configuration.addScanClasses(classes);
                configuration.addScanExcludeClasses(excludeClasses);
                configuration.addScanExcludePackages(excludePackages);
            });
            ApplicationContext applicationContext = applicationFactory.getApplicationContext();
            BeanInfoFactory debbieBeanInfoFactory = applicationContext.getBeanInfoFactory();
            Set<DebbieBeanInfo<?>> allDebbieBeanInfo = debbieBeanInfoFactory.getAllDebbieBeanInfo();
            for (DebbieBeanInfo<?> beanInfo : allDebbieBeanInfo) {
                Class<Object> beanClass = beanInfo.getBeanClass();
                SpringDebbieBeanFactory<?> factory = new SpringDebbieBeanFactory<>(debbieBeanInfoFactory, beanInfo);
                factory.setGlobalBeanFactory(applicationContext.getGlobalBeanFactory());
                registry.registerBeanDefinition(beanInfo.getServiceName(), new RootBeanDefinition(beanClass, () -> factory));
            }
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        LOGGER.debug(() -> "set debbie application classLoader by spring");
        this.classLoader = classLoader;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DebbieBeanRegistrar.class);
}
