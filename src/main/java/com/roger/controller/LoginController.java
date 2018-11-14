package com.roger.controller;

import com.roger.annotation.*;
import com.roger.service.LoginService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RogerController
@RogerRequestMapping("/system")
public class LoginController {

    @RogerResource
    private LoginService loginService;

    @RogerRequestMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response,
                      @RogerRequestParam("id") String id){
        try {
            PrintWriter out = response.getWriter();
            boolean result = loginService.isLoginUser(Integer.valueOf(id));
            out.write("login success.");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RogerRequestMapping("/add")
    public void add(HttpServletRequest request,HttpServletResponse response,
                    @RogerRequestParam("name") String name,
                    @RogerRequestParam("age") String age){
        try {
            PrintWriter out = response.getWriter();
            loginService.add(name,age);
            out.write(name + " add success.");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
