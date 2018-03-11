package com.xw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //启动注释事务管理
public class Application extends SpringBootServletInitializer{ //继承了Spring Boot提供的Servlet初始化器SpringBootServletInitializer
    public static void main(String[] args){
        SpringApplication.run(Application.class,args);
    }

    //重写了SpringBootSerletInitializer的configure方法
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
