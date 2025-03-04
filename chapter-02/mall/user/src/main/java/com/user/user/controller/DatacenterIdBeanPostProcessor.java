package com.user.user.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.stereotype.Component;

@Component
public class DatacenterIdBeanPostProcessor implements BeanPostProcessor {
    private static final String DATA_CENTER_ID = "2";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EurekaInstanceConfigBean) {
            EurekaInstanceConfigBean instanceConfig = (EurekaInstanceConfigBean) bean;
            instanceConfig.getMetadataMap().put("datacenter", DATA_CENTER_ID);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

