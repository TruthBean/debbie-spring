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

import com.truthbean.Logger;
import com.truthbean.logger.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author TruthBean/Rogar·Q
 * @since 0.0.2
 * Created on 2020-06-03 23:12
 */
public class SpringDebbieBeanFactory<Bean> implements BeanFactory<Bean>, FactoryBean<Bean> {

    private BeanFactoryHandler beanFactoryHandler;

    private final Class<Bean> beanClass;

    private boolean singleton = false;
    private org.springframework.beans.factory.BeanFactory springBeanFactory;

    private final Logger logger;

    public SpringDebbieBeanFactory(Class<Bean> beanClass) {
        this.beanClass = beanClass;
        this.logger = LoggerFactory.getLogger("com.truthbean.debbie.spring.SpringDebbieBeanFactory<" + beanClass.getName() + ">");
    }

    public SpringDebbieBeanFactory(DebbieBeanInfo<Bean> beanInfo) {
        this.beanClass = beanInfo.getBeanClass();
        this.logger = LoggerFactory.getLogger("com.truthbean.debbie.spring.SpringDebbieBeanFactory<" + beanClass.getName() + ">");
    }

    @Override
    public void setBeanFactoryHandler(BeanFactoryHandler beanFactoryHandler) {
        this.beanFactoryHandler = beanFactoryHandler;
    }

    @Override
    public Bean getBean() {
        if (springBeanFactory != null) {
            return springBeanFactory.getBean(beanClass);
        }
        try {
            return getObject();
        } catch (Exception e) {
            logger.error("", e);
            throw new BeanCreatedException(e);
        }
    }

    @Override
    public Class<Bean> getBeanType() {
        return beanClass;
    }

    public void setSpringBeanFactory(org.springframework.beans.factory.BeanFactory springBeanFactory) {
        this.springBeanFactory = springBeanFactory;
    }

    @Override
    public Bean getObject() throws Exception {
        Bean bean = beanFactoryHandler.factory(null, getBeanType(), false, false);
        if (bean != null) {
            return bean;
        } else {
            var beanBeanInvoker = beanFactoryHandler.factoryBeanInvoker(getBeanType());
            bean = beanBeanInvoker.getBean();
            var classInfo = beanBeanInvoker.getBeanInfo();
            beanFactoryHandler.resolveDependentBean(bean, classInfo);
            return bean;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return beanClass;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public void destroy() {
        // destory
    }
}
