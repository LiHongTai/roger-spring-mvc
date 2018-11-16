package com.roger.annotation;

import java.lang.annotation.*;
import java.util.concurrent.ThreadPoolExecutor;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RogerRequestMapping {
    String value();
}
