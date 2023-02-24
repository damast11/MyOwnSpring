package com.dkulish.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.stereotype.Component;

@Component
public class PromotionService implements BeanNameAware, InitializingBean {
    private String beanName;

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("PromotionService : ----afterPropertiesSet----");
    }
}
