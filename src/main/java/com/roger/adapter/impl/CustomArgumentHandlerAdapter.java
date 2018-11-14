package com.roger.adapter.impl;

import com.roger.adapter.HandlerAdapterService;
import com.roger.annotation.RogerService;
import com.roger.resolver.ArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@RogerService("customArgumentHandlerAdapter")
public class CustomArgumentHandlerAdapter implements HandlerAdapterService {

    @Override
    public Object[] handle(HttpServletRequest req, HttpServletResponse resp, Method method, Map<String, Object> iocContainer) {
        //获取方法中含有的参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        //定义一个返回参数结果的结果集
        Object[] args = new Object[parameterTypes.length];
        //定义一个ArgumentResolver实例的Map
        Map<String, Object> argumentResolverMap = getBeansOfType(iocContainer, ArgumentResolver.class);

        //定义参数索引
        int paramIndex = 0;
        //定义数据下标
        int i = 0;
        for (Class<?> paramClazz : parameterTypes) {
            //哪个参数使用哪个类来解析，使用策略模式
            for (Map.Entry<String, Object> argumentResolverEntry : argumentResolverMap.entrySet()) {
                ArgumentResolver argumentResolver = (ArgumentResolver) argumentResolverEntry.getValue();
                if (argumentResolver.support(paramClazz, paramIndex, method)) {
                    args[i++] = argumentResolver.argumentResolver(req, resp, paramClazz, paramIndex, method);
                }
            }
            paramIndex++;
        }

        return args;
    }

    /**
     * @param iocContainer IOC容器中全部的bean
     * @param intfType     定义的ArgumentResolver类
     * @return
     */
    private Map<String, Object> getBeansOfType(Map<String, Object> iocContainer,
                                               Class<ArgumentResolver> intfType) {
        Map<String, Object> argumentResolverMap = new HashMap<>();

        for (Map.Entry<String, Object> instanceBeanEntry : iocContainer.entrySet()) {
            Class clazz = instanceBeanEntry.getValue().getClass();
            Class[] clazzInterfaces = clazz.getInterfaces();
            if (clazzInterfaces == null || clazzInterfaces.length <= 0) {
                continue;
            }
            for (Class clazzInterface : clazzInterfaces) {
                if (intfType.isAssignableFrom(clazzInterface)) {
                    argumentResolverMap.put(instanceBeanEntry.getKey(), instanceBeanEntry.getValue());
                }
            }
        }

        return argumentResolverMap;
    }

}
