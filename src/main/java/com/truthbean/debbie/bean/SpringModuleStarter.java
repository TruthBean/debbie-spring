/**
 * Copyright (c) 2020 TruthBean(Rogar·Q)
 * Debbie is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.truthbean.debbie.bean;

import com.truthbean.debbie.boot.DebbieModuleStarter;
import com.truthbean.debbie.properties.DebbieConfigurationFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author TruthBean/Rogar·Q
 * @since 0.0.2
 * Created on 2020-06-07 13:53
 */
public class SpringModuleStarter implements DebbieModuleStarter {

    private volatile ConfigurableApplicationContext applicationContext;

    @SuppressWarnings("Unchecked")
    @Override
    public void starter(DebbieConfigurationFactory configurationFactory, BeanFactoryHandler beanFactoryHandler) {
        BeanInitialization beanInitialization = beanFactoryHandler.getBeanInitialization();

        BeanScanConfiguration configuration = configurationFactory.factory(BeanScanConfiguration.class, beanFactoryHandler);
        String scanBasePackage = configuration.getScanBasePackage();

        if (scanBasePackage != null) {
            applicationContext = new AnnotationConfigApplicationContext(scanBasePackage);
        } else {
            applicationContext = new AnnotationConfigApplicationContext();
        }
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            Class<?> beanClass = bean.getClass();
            DebbieBeanInfo beanInfo = new DebbieBeanInfo<>(beanClass);
            beanInfo.setBean(bean);
            beanInfo.setBeanName(name);
            if (applicationContext.isSingleton(name)) {
                beanInfo.setBeanType(BeanType.SINGLETON);
            } else {
                beanInfo.setBeanType(BeanType.NO_LIMIT);
                AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
                SpringDebbieBeanFactory beanFactory = new SpringDebbieBeanFactory<>(beanClass);
                beanFactory.setSpringBeanFactory(autowireCapableBeanFactory);
                beanFactory.setSingleton(false);
                beanInfo.setBeanFactory(beanFactory);
            }

            if (!beanFactoryHandler.containsBean(name)) {
                beanInitialization.initBean(beanInfo);
            }
        }
        beanFactoryHandler.refreshBeans();
    }

    @Override
    public void release(DebbieConfigurationFactory configurationFactory, BeanFactoryHandler beanFactoryHandler) {
        if (applicationContext != null)
            applicationContext.close();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
