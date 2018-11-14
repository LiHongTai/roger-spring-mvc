package com.roger.servlet;

import com.roger.adapter.HandlerAdapterService;
import com.roger.annotation.*;
import com.roger.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class RogerDispatcherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6 实际进行调用controller的方法
        if(urlMethodMap.isEmpty()){
            return ;
        }
        //6.1 获取实际请求的url
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String realUrl = url.replace(contextPath,"");
        if (!urlMethodMap.containsKey(realUrl)){
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }
        //6.2 获取url映射的方法
        Method method = urlMethodMap.get(realUrl);
        //6.3 获取方法的控制类,即方法是不能单独存在的,方法是属于某个类的
        String methodUrlPath = method.getAnnotation(RogerRequestMapping.class).value();
        String controllerUrlPath = realUrl.replace(methodUrlPath,"");
        Object clazzInstance = clazzInstanceMap.get(controllerUrlPath);
        //6.4 获取执行方法所需的参数
        //6.4.1获取参数列表类型
        HandlerAdapterService handlerAdapterService =(HandlerAdapterService) clazzInstanceMap.get("customArgumentHandlerAdapter");
        Object[] args = handlerAdapterService.handle(req,resp,method,clazzInstanceMap);
        //6.5 反射调用方法
        try {
            method.invoke(clazzInstance, args);
        }catch (InvocationTargetException | IllegalAccessException e){
            resp.getWriter().write("500 Server Internal!");
        }
    }

    //属性文件
    private Properties prop = new Properties();
    //项目中所有的class文件存储地址
    private List<String> clazzFilePathList = new ArrayList<>();
    //IOC 容器
    private Map<String, Object> clazzInstanceMap = new HashMap<>();
    //URL和Method映射关系的容器
    private Map<String, Method> urlMethodMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1 加载属性文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        String scanPackage = prop.getProperty("scan.package");
        //2 根据包名扫描项目中所有的class文件
        //  这里通过properties文件获取
        doScanPackage(scanPackage);
        //3 根据(1)获取到的class文件全路径,
        // 实例化具有注解配置的class,并放入IOC容器
        doInstance();
        //4 依赖注入
        // 把实例化的对象注入到相应的实例化对象中的成员变量
        doIOC();
        //5 初始化url和method映射关系
        doUrlMapping();
    }

    private void doLoadConfig(String location) {
        prop = PropertiesUtil.readPropertiesByClassLoader(location);
    }

    private void doScanPackage(String scanPackage) {
        // 2.1 获取项目编译后的统一资源定位符
        URL url = this.getClass().getClassLoader()
                .getResource(scanPackage.replaceAll("\\.", "/"));
        // 2.2 根据统一资源定位符,获取当前目录文件
        String absoluteFilePath = url.getFile();
        File currentFile = new File(absoluteFilePath);
        // 2.3 获取当前目录下的所有文件信息
        File[] tempFileArr = currentFile.listFiles();
        // 2.4 遍历当前目录文件下的文件信息
        //      如果文件类型是目录,则递归调用扫描包的方法
        //      如果文件类型是文件,则添加到文件存储容器中去
        for (File tempFile : tempFileArr) {
            String relativeFilePath = scanPackage + "." + tempFile.getName();
            if (tempFile.isDirectory()) {
                doScanPackage(relativeFilePath);
                continue;
            }
            clazzFilePathList.add(
                    relativeFilePath.replace(".class", ""));
        }
    }

    private void doInstance() {
        if (clazzFilePathList.size() == 0) {
            throw new RuntimeException("包扫描失败,未加载到任何class文件.");
        }
        try {
            for (String clazzFilePath : clazzFilePathList) {
                Class<?> clazz = Class.forName(clazzFilePath);
                //控制器类
                if (clazz.isAnnotationPresent(RogerController.class)) {
                    RogerRequestMapping rogerRequestMapping = clazz.getAnnotation(RogerRequestMapping.class);
                    String key = rogerRequestMapping.value();
                    clazzInstanceMap.put(key, clazz.newInstance());
                }//服务类
                else if (clazz.isAnnotationPresent(RogerService.class)) {
                    RogerService serviceAnno = clazz.getAnnotation(RogerService.class);
                    //默认值为接口类型的类名首字母小写
                    String key = StringUtils.uncapitalize(clazz.getInterfaces()[0].getSimpleName());
                    if (!serviceAnno.value().equals("")) {
                        key = serviceAnno.value();
                    }
                    clazzInstanceMap.put(key, clazz.newInstance());
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("扫描到不存在的class文件.");
        }
    }


    private void doIOC() {
        if (clazzInstanceMap.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<String, Object> clazzInstanceEntry : clazzInstanceMap.entrySet()) {
                Object clazzIntance = clazzInstanceEntry.getValue();
                //通过实例获取类的字节码
                Class<?> clazz = clazzIntance.getClass();
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(RogerResource.class)) {
                        RogerResource rogerResource = declaredField.getAnnotation(RogerResource.class);
                        String key = declaredField.getName();
                        if (!rogerResource.value().equals("")) {
                            key = rogerResource.value();
                        }
                        if (!clazzInstanceMap.containsKey(key)) {
                            throw new RuntimeException("没有" + key + "对应的实例.");
                        }
                        //访问私有属性时,需要设置访问权限
                        declaredField.setAccessible(true);
                        declaredField.set(clazzIntance, clazzInstanceMap.get(key));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("依赖注入失败");
        }
    }

    private void doUrlMapping() {
        if (clazzInstanceMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> clazzInstanceEntry : clazzInstanceMap.entrySet()) {
            Object clazzIntance = clazzInstanceEntry.getValue();
            //通过实例获取类的字节码
            Class<?> clazz = clazzIntance.getClass();
            //只有控制器的类才需要进行URL和Method建立映射关系
            if (clazz.isAnnotationPresent(RogerController.class)) {
                //URL路径的前半部分--控制器路径
                String controllerUrlPath = clazz.getAnnotation(RogerRequestMapping.class).value();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RogerRequestMapping.class)) {
                        //URL路径的后半部分--方法路径
                        String methodUrlPath = method.getAnnotation(RogerRequestMapping.class).value();
                        urlMethodMap.put(controllerUrlPath + methodUrlPath, method);
                    }
                }
            }
        }

    }

}
