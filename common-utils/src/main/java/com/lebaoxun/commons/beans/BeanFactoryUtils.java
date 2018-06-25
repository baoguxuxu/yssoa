package com.lebaoxun.commons.beans;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactoryUtils implements ApplicationContextAware {

	private static ApplicationContext context = null;
	private static BeanFactoryUtils stools = null;

	public synchronized static BeanFactoryUtils init() {
		if (stools == null) {
			stools = new BeanFactoryUtils();
		}
		return stools;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext(){
		return context;
	}

	public synchronized static Object getBean(String beanName) {
		return context.getBean(beanName);
	}
}