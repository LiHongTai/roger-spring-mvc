package com.roger.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesUtil {

    public static Properties readPropertiesByClassPath(String filePath4ClassPath) {
        InputStream in = PropertiesUtil.class
                .getResourceAsStream(filePath4ClassPath);
        return load(in);
    }

    public static Properties readPropertiesByClassLoader(String filePath4ClassLoader) {
        InputStream in = PropertiesUtil.class.getClassLoader()
                .getResourceAsStream(filePath4ClassLoader);
        return load(in);
    }

    public static Properties readPropertiesByClassLoader2(String filePath4ClassLoader) {
        InputStream in = ClassLoader
                .getSystemResourceAsStream(filePath4ClassLoader);
        return load(in);
    }


    public static ResourceBundle readPropertiesByResourceBundle(String filePath4ResourceBundle) {
        ResourceBundle rb = ResourceBundle.getBundle(filePath4ResourceBundle);
        return rb;
    }

    private static Properties load(InputStream in) {
        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException e) {
            //TODO
        }
        return prop;
    }
}
