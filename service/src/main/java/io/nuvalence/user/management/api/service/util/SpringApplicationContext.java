package io.nuvalence.user.management.api.service.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provides convenient static access to pull beans from the Spring application context.
 */
@Component
@SuppressFBWarnings
public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        SpringApplicationContext.context = context;
    }

    public static <T> T getBeanByClass(Class<T> clazz) throws BeansException {
        return context.getBean(clazz);
    }
}