# 展现层

## 配置Spring MVC框架

首先需要对web.xml文件进行配置，以便Web容器启动时能够自动启动Spring容器。

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5"
         xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

    <!--1、从类路径下加载Spring配置文件，classpath关键字特指类路径下加载-->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>
            classpath:smart-context.xml
        </param-value>
    </context-param>
    <!--2、负责启动Spring容器的监听器，它将引用前面的获得是Spring配置文件的地址-->
    <listener>
    <listener-class>
        org.springframework.web.context.ContextLoaderListener
    </listener-class>
    </listener>
    <!--3、Spring MVC 的主控Servlet-->
    <servlet>
        <servlet-name>smart</servlet-name>
            <servlet-class>
                org.springframework.web.servlet.DispatcherServlet
            </servlet-class>
            <load-on-startup>2</load-on-startup>
    </servlet>
    <!--Spring MVC 处理的url-->
    <servlet-mapping>  <!--4-->
        <servlet-name>smart</servlet-name>
        <url-pattern>*.html</url-pattern>
    </servlet-mapping>
</web-app>
```

- 在1处通过Web上下文参数指定Spring配置文件的地址，classpath关键字特指类路径下加载。

  多个配置文件可用逗号或者空格分隔。

- 在2处指定Spring所提供的ComtextLoaderListener的Web容器监听器，该监听器在Web容器启动时自动运行，它会根据contextCoinfigLocation Web容器参数获取Spring配置文件，并启动Spring容器。

- 最后，需要配置Spring MVC相关的信息。Spring MVC像Struts一样，也通过一个Servlet来截取URL请求，然后再进行相关的处理。


**配置Servlet部分**

+ 在3处声明了一个Servlet,Spring MVC也拥有一个Spring配置文件，该配置文件的文件名与此处定义的Servlet名有一个契约，即采用< Servlet 名 >-servlet.xml的形式。在此处Servlet名为smart，则在/WEB-INF目录下必须提供一个名为smart-servlet.xml的Spring MVC配置文件，但这个配置文件无须通过web.xml和contextConfigLocation上下文参数进行声明，因为Spring MVC 的Servlet会自动将smart-servlet.xml文件和Spring的其他配置文件（smart-dao.xml、smart-service.xml）进行拼装。
+ 在4 处，对这个Servlet的URL路径映射进行定义，，这里让所有以.html为后缀的URL都能被smart Servlet截获，进而转由Spring MVC框架处理。
+ 请求被Spring MVC截获后，首先根据请求的URL查找到目标的处理控制器，并将请求参数封装“命令”对象一起传给控制器处理；然后，控制器调用Spring容器中的业务Bean完成处理工作并返回结果视图。  

## 处理登录请求

### 控制器类

#### LoginController

首先要编写一个LoginController,它负责处理登录请求，完成登录业务，并根据登录成功与否转向欢迎页面或失败页面。

```
package com.smart.web;

import com.smart.domain.User;
import com.smart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

1 @Controller//将POJO类标注为SringMVC的控制器
public class LoginController {
    private UserService userService;

   2  //负责处理/index.html的请求
    @RequestMapping(value = "/index.html")
    public String loginPage()
    {
        return "login";
    }

    3 @RequestMapping(value = "/loginCheck.html")
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

```

+ 在1处通过Spring MVC的@Controller注解可以将任何一个POJO的类标注为Spring MVC的控制器，处理HTTP的请求。

+ 在2 和  3处，一个控制器可以拥有多个处理映射不同HTTP请求路径的方法，通过@RequestMapping指定方法如何映射请求路径。请求参数会根据参数名称默认契约自动绑定到相应方法的入参中。例如：在3中的loginCheck(HttpServletRequest request, LoginCommand loginCommand)方法中，请求参数会按名称匹配绑定到loginCommand的入参中。

+ 请求方法可以返回一个ModelAndView，或者直接返回一个字符串，Spring MVC会解析之并转向目标响应页面。在2 和 3 处控制器根据登录处理结果分别返回ModelAndView("login","error","用户名或密码错误！")和

  ModelAndView("main")。ModelAndView的第一个参数代表视图的逻辑名，第二、第三个参数分别为数据模型名称和数据模型对象，数据模型对象将以数据模型名称为参数名放置到request的属性中。

+ ModelAndView对象及包括视图信息，又包括视图渲染所需的模型数据信息。在这里用户仅需要了解它一张视图即可，在后面的内容中，将了解到SpringMVC如何根据这个对象转向真正的页面。

  #### LoginCommand

  前面用到的LoginCommand对象是一个POJO,没有继承特定的父类或者实现特定的接口。LoginCommand类仅包括用户/密码这两个属性(和)

```
package com.smart.web;

public class LoginCommand {
    private String userName;
    private String password;

    public void setUserName(String name){userName = name;}
    public void setPassword(String Password){password = Password;}

    public String getUserName(){return userName;}
    public String getPassword(){return password;}
}

```

## Spring MVC配置文件

编写好LoginCommand后，需要在smart-servlet.xml中声明该控制器，扫描Web路径，指定Spring MVC的视图解析器。

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <!--1 扫描web包，应用spring的注解-->
    <context:component-scan base-package="com.smart.web"/>

    <!--2 配置视图解析器，将ModelAndView及字符解析为具体页面-->
    <bean
           class="org.springframework.web.servlet.view.InternalResourceViewResolver"
           p:viewClass="org.springframework.web.servlet.view.JstlView"
           p:prefix="/WEB-INF/jsp/"
           p:suffix=".jsp"/>

</beans>
```

Spring MVC如何将视图逻辑名解析为具体的视图页面呢?解决思路类似，需要在smart-servlet.xml中提供一个定义解析规则的Bean。

SpringMVC 为视图名到具体视图映射提供了许多可供选者的方法。在这里，我们使用InternalResourceViewRsolver,它通过为视图逻辑名添加前后缀的方式进行解析。通过prefix指定在视图名前所添加的前缀，通过suffix指定在视图名后所添加的后缀。

如视图名为“login”，将解析为/WEB-INF/jsp/login.jsp；视图逻辑名为"mian"，将其解析为/WEB-INF/jsp/main.jsp。

## JSP视图页面

登录模块共包括两个JSP页面，分别是登录页面login.jsp和欢迎页面main