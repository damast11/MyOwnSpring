package com.dkulish;

import com.dkulish.service.CustomPostProcessor;
import com.dkulish.service.ProductService;
import com.dkulish.service.PromotionService;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args)
        throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.instantiate("com.dkulish.service");
        var productService = (ProductService) beanFactory.getBean("productService");
        var promotionService = (PromotionService) beanFactory.getBean("promotionService");
        System.out.println(productService);
        System.out.println(promotionService);

        beanFactory.populateProperties();
        System.out.println(productService.getPromotionService());

        beanFactory.injectBeanNames();
        System.out.println("Bean name = " + promotionService.getBeanName());

        beanFactory.initializeBeans();

        beanFactory.addPostProcessor(new CustomPostProcessor());
        beanFactory.initializeBeans();
    }
}
