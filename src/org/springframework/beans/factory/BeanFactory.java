package org.springframework.beans.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeanFactory {

    private final Map<String, Object> singletons = new HashMap<>();
    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public Object getBean(String beanName) {
        System.out.println("==getBean==");
        return singletons.get(beanName);
    }

    public void instantiate(String basePackage) throws IOException, URISyntaxException, ClassNotFoundException {
        System.out.println("==instantiate==");
        var classesInPackageWithAnnotation = findClassesInPackageWithAnnotation(basePackage, Component.class);
        var beansFromPackage = getBeansFromClasses(classesInPackageWithAnnotation);

        beansFromPackage.forEach(bean -> singletons.put(getBeanName(bean), bean));
    }

    public void populateProperties() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        System.out.println("==populateProperties==");

        for (Object object : singletons.values()) { // find beans with @Component
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) { // find all field with @Autowire

                    for (Object dependency : singletons.values()) {
                        if (dependency.getClass().equals(field.getType())) {
                            String setterName =
                                "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                            System.out.println("Setter name = " + setterName);
                            Method setter = object.getClass().getMethod(setterName, dependency.getClass());
                            setter.invoke(object, dependency);
                        }
                    }
                }
            }
        }
    }

    public void injectBeanNames() {
        System.out.println("==injectBeanNames==");

        singletons.forEach((k,v) -> {
            if (v instanceof BeanNameAware) {
                ((BeanNameAware) v).setBeanName(k);
            }
        });
    }

    public void initializeBeans(){
        System.out.println("==initializeBeans==");

        singletons.forEach((k,v) -> {
            postProcessors.forEach(postProcessor -> postProcessor.postProcessBeforeInitialization(v, k));
            if (v instanceof InitializingBean) {
                ((InitializingBean) v).afterPropertiesSet();
            }
            postProcessors.forEach(postProcessor -> postProcessor.postProcessAfterInitialization(v, k));
        });
    }

    public void addPostProcessor(BeanPostProcessor postProcessor){
        postProcessors.add(postProcessor);
    }

    //    public void populateProperties() {
    //        System.out.println("==populateProperties==");
    //        singletons.values()
    //            .forEach(bean -> Arrays.stream(bean.getClass().getDeclaredFields())
    //                .forEach(field -> {
    //                    if (field.isAnnotationPresent(Autowired.class) && field.getType().equals(bean.getClass())) {
    //                        invokeSetter(field, bean);
    //                    }
    //                }));
    //    }
    //
    //    private void invokeSetter(Field field, Object bean) {
    //        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);//setPromotionsService
    //        System.out.println("Setter name = " + setterName);
    //        Method setter;
    //        try {
    //            setter = field.getDeclaringClass().getMethod(setterName, field.getType());
    //            setter.invoke(bean, bean);
    //        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
    //            e.printStackTrace();
    //        }
    //    }

    private String getBeanName(Object bean) {
        var name = bean.getClass().getSimpleName();

        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private List<Object> getBeansFromClasses(List<Class> classesInPackageWithAnnotation) {
        return classesInPackageWithAnnotation.stream()
            .map(this::getNewInstance)
            .collect(Collectors.toList());
    }

    private Object getNewInstance(Class classInPackage) {
        try {
            return classInPackage.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Class> findClassesInPackageWithAnnotation(String basePackage, Class annotation)
        throws IOException, URISyntaxException, ClassNotFoundException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            File file = new File(resource.toURI());
            for (File classFile : file.listFiles()) {
                String fileName = classFile.getName();//ProductService.class
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(0, fileName.lastIndexOf("."));
                    Class classObject = Class.forName(basePackage + "." + className);
                    if (classObject.isAnnotationPresent(annotation)) {
                        System.out.println("Component: " + classObject);
                        classes.add(classObject);
                    }
                }
            }
        }
        return classes;
    }

//    public void close() {
//        for (Object bean : singletons.values()) {
//            for (Method method : bean.getClass().getMethods()) {
//                if (method.isAnnotationPresent(PreDestroy.class)) {
//                    try {
//                        method.invoke(bean);
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            if (bean instanceof DisposableBean) {
//                ((DisposableBean) bean).destroy();
//            }
//        }
//    }
}
