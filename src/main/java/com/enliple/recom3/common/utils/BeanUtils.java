package com.enliple.recom3.common.utils;

import com.enliple.recom3.common.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

public class BeanUtils {
	public static <T> T getBean(Class<T> type) {
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		return applicationContext.getBean(type);
	}
}