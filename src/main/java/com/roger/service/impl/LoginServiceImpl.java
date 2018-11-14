package com.roger.service.impl;

import com.roger.annotation.RogerService;
import com.roger.service.LoginService;

@RogerService
public class LoginServiceImpl implements LoginService {

    @Override
    public boolean isLoginUser(int id) {
        return false;
    }

    @Override
    public void add(String name, String age) {
        System.out.println("name :" + name + ",age = " +age );
    }
}
