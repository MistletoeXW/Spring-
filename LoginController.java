package com.xw.web;

import com.xw.domain.User;
import com.xw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller//将POJO类标注为SringMVC的控制器
public class LoginController {
    private UserService userService;
    //负责处理/index.html的请求
    @RequestMapping(value = {"/","/index.html"})
    public String loginPage()
    {
        return "login";
    }

    @RequestMapping(value = "/loginCheck.html")
    public ModelAndView loginCheck(HttpServletRequest request, LoginCommand loginCommand)
    {
        boolean isValidUser = userService.hasMatchUser(loginCommand.getUserName(),loginCommand.getPassword());

        if(!isValidUser)
        {
            return new ModelAndView("login","error","用户名或密码错误！");
        }
        else
        {
            User user = userService.findUserByUserName(loginCommand.getUserName());
            user.setLastIp(request.getLocalAddr());
            user.setLastVist(new Date());
            userService.loginSuccess(user);
            request.getSession().setAttribute("user",user);
            return new ModelAndView("main");
        }
    }

    @Autowired
    public void setUserService(UserService userService)
    {
        this.userService = userService;
    }

}