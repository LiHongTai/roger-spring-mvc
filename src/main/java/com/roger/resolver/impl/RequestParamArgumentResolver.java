package com.roger.resolver.impl;

import com.roger.annotation.RogerRequestParam;
import com.roger.annotation.RogerService;
import com.roger.resolver.ArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@RogerService("requestParamArgumentResolver")
public class RequestParamArgumentResolver implements ArgumentResolver {

    @Override
    public boolean support(Class<?> type, int paramIndex, Method method) {
        Annotation[] paramAnnotations = method.getParameterAnnotations()[paramIndex];
        if(paramAnnotations.length <= 0) {
            return false;
        }
        for(Annotation paramAnnotation : paramAnnotations){
            if(RogerRequestParam.class.isAssignableFrom(paramAnnotation.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object argumentResolver(HttpServletRequest request, HttpServletResponse response, Class<?> type, int paramIndex, Method method) {
        Annotation[] paramAnnotations = method.getParameterAnnotations()[paramIndex];
        if(paramAnnotations.length <= 0) {
            return null;
        }
        for(Annotation paramAnnotation : paramAnnotations){
            if(RogerRequestParam.class.isAssignableFrom(paramAnnotation.getClass())) {
                RogerRequestParam rogerRequestParam = (RogerRequestParam) paramAnnotation;
               return request.getParameter(rogerRequestParam.value());
            }
        }
        return null;
    }
}
