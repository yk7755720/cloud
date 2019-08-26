package com.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @ Purpose:
 * @Package Name: com.dpm.his.common
 * @Author: liuxiaoxin
 * @Date: 2018/5/17
 */
@Component
public class ContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;
// 非@import显式注入，@Component是必须的，且该类必须与main同包或子包  
    // 若非同包或子包，则需手动import 注入，有没有@Component都一样  
    // 可复制到Test同包测试  

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ContextUtils.applicationContext == null) {
            ContextUtils.applicationContext = applicationContext;
        }
    }

    //获取applicationContext  
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.  
    public Object getBean(String name) {
        return getApplicationContext().getBean(name);

    }

    //通过class获取Bean.  
    public <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean  
    public <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
