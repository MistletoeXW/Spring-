# Spring-
# 业务层

在论坛登录的实例中，业务层仅有一个业务类，即UserService.。UserService负责将 持久层的UserDao与LoginLogDao组织起来，完成用户/密码认证、登录日志记录等操作。

## UserService

    package com.smart.service;
    import com.smart.dao.LoginLogDao;
    import com.smart.dao.UserDao;
    import com.smart.domain.LoginLog;
    import com.smart.domain.User;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    
    @Service //将UserService标注为一个服务层的Bean
    public class UserService {
        private UserDao userDao;
        private LoginLogDao loginLogDao;
    
        @Autowired //注入userDao层的Bean
        public void setUserDao(UserDao userDao)
        {
            this.userDao = userDao;
        }
    
        @Autowired //注入loginLogDao层的Bean
        public void setLoginLogDao(LoginLogDao loginLogDao)
        {
            this.loginLogDao = loginLogDao;
        }
    
        //hasMatchUser()业务方法简单调用Dao层的Bean
        public boolean hasMatchUser(String userName,String password)
        {
            int matchCout = userDao.getMatchCount(userName,password);
            return matchCout > 0;
        }
        //findUserByUserName()业务方法简单调用Dao层的Bean
        public User findUserByUserName(String userName)
        {
            return userDao.findUserByUserName(userName);
        }
    
        /* loginSuccess（）方法注入@Transactional水注解，让该方法运行在事物环境中
        * 否则该方法将在无事务方法中运行*/
        @Transactional
        public void loginSuccess(User user)//将两个Dao组织起来共同完成一个事务性的数据操作
        {
            user.setCredits(5 + user.getCredits()); //根据参user对象构造出的LoginLog对象并将user.credits递增5
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(user.getUserId());
            loginLog.setIp(user.getLastIp());
            loginLog.setLoginDate(user.getLastVisit());
            userDao.updateLoginInfo((user));
            loginLogDao.insertLoginLog(loginLog);
        }
    
    }
    

- hasMatchUser()方法：用于检查用户名/密码的正确性
- findUserByUserName()方法：以用户名为条件加载User对象。
- loginSuccess()方法：在用户登录成功后调用，更新用户最后登录时间及ip信息，同时记录用户登录日志。

### @Service

@Service注解将UserService标注为一个服务层的Bean；然后利用@Autowired注入userDao和loginLogDao这两个Dao层的Bean；接着通过hasMatchUser()和findUserByUserName()简单地调用DAO完成对应的功能。

### @Transactional

将loginSuccess()方法标注@Transactional事务注解，让该方法运行在事务环境中（因为我们在Spring事务管理器拦截切入表达式上加入了@Transactional过滤），否则该方法将在无事务方法中运行。loginSuccess（) 根据入参的user对象构造出LoginLog对象并将User.credits递增5，然后调用UserDao更新到t_user中，再调用loginLogDao向t_login_log表中添加一条记录。

事务管理是应用系统开发中必不可少的一部分。Spring 为事务管理提供了丰富的功能支持。Spring 事务管理分为编码式和声明式的两种方式。编程式事务指的是通过编码方式实现事务；声明式事务基于 AOP,将具体业务逻辑与事务处理解耦。声明式事务管理使业务代码逻辑不受污染, 因此在实际使用中声明式事务用的比较多。声明式事务有两种方式，一种是在配置文件（xml）中做相关的事务规则声明，另一种是基于@Transactional 注解的方式，注释配置是目前流行的使用方式。

使用@Transactional 注解管理事务的实现步骤分为两步。第一步，在 xml 配置文件中添加如清单 1 的事务配置信息。除了用配置文件的方式，@EnableTransactionManagement 注解也可以启用事务管理功能。这里以简单的 DataSourceTransactionManager 为例。

    <tx:annotation-driven />
    <bean id="transactionManager"
    class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource" />
    </bean>

第二步，将@Transactional 注解添加到合适的方法上，并设置合适的属性信息。@Transactional 注解的属性信息:

@Transactional 注解的属性信息

  属性名             	说明                                      
  name            	当在配置文件中有多个 TransactionManager , 可以用该属性指定选择哪个事务管理器。
  propagation     	事务的传播行为，默认值为 REQUIRED。                  
  isolation       	事务的隔离度，默认值采用 DEFAULT。                   
  timeout         	事务的超时时间，默认值为-1。如果超过该时间限制但事务还没有完成，则自动回滚事务。
  read-only       	指定事务是否为只读事务，默认值为 false；为了忽略那些不需要事务的方法，比如读取数据，可以设置 read-only 为 true。
  rollback-for    	用于指定能够触发事务回滚的异常类型，如果有多个异常类型需要指定，各类型之间可以通过逗号分隔。
  no-rollback- for	抛出 no-rollback-for 指定的异常类型，不回滚事务。       

除此以外，@Transactional 注解也可以添加到类级别上。当把@Transactional 注解放在类级别时，表示所有该类的公共方法都配置相同的事务属性信息。见清单 2，EmployeeService 的所有方法都支持事务并且是只读。当类级别配置了@Transactional，方法级别也配置了@Transactional，应用程序会以方法级别的事务属性信息来管理事务，换言之，方法级别的事务属性信息会覆盖类级别的相关配置信息。

    @Transactional(propagation= Propagation.SUPPORTS,readOnly=true)
    @Service(value ="employeeService")
    public class EmployeeService

到此，您会发觉使用@Transactional 注解管理事务的实现步骤很简单。但是如果对 Spring 中的 @transaction 注解的事务管理理解的不够透彻，就很容易出现错误，比如事务应该回滚（rollback）而没有回滚事务的问题。接下来，将首先分析 Spring 的注解方式的事务实现机制，然后列出相关的注意事项，以最终达到帮助开发人员准确而熟练的使用 Spring 的事务的目的。

## 在Spring中装配Service

事务管理的代码虽然无须出现在程序代码中，但我们必须以某种方式告诉Spring哪些业务类需要工作在事务环境下及事务的规则等内容，以便Spring根据这些信息自动为目标业务类添加事务管理的功能。在.xml文件中进行配置。

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:p="http://www.springframework.org/schema/p"
           xmlns:context="http://www.springframework.org/schema/c"
           xmlns:aop="http://www.springframework.org/schema/aop"
           xmlns:tx="http://www.springframework.org/schema/tx"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/aop
           http://www.springframework.org/schema/aop/spring-aop.xsd
           http://www.springframework.org/schema/tx
           http://www.springframework.org/schema/tx/spring-tx.xsd">
    
        <!--扫描类包，将标注Spring注解的类自动转化为Bean，同时完成Bean的注入-->
        <!--使用Spring的<context:context-scan>扫描指定类包下的所有类-->
        <context:context-scan base-package="com.smart.dao"/>
        <!--扫描service类包，应用Spring的注解配置-->
        <context:component-scan base-package="com.smart.service"/>
    
        <!--定义一个使用 DBCP实现的数据源-->
        <!--使用Jakarta的DBCP开源数据源实现方案定义了一个数据源，数据驱动类为：com.mysql.jdbc.Driver
        数据-->
        <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
              p:driverClassName="com.mysql.jdbc.Driver"
              p:url="jdbc:mysql://localhost:3306/sampledb"
              p:username="root"
              p:password="root"/>
        <!--定义JDBC模板Bean-->
        <!--配置了JdbcTemplate Bean 将dataSource注入JdbcTemplate中，
        而这个JdbcTemplate Bean 将通过@Autowired自动注入LoginLog 和 UserDao 的Bean中-->
        <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate"
              p:dataSource-ref="dataSource"/>
    
        <!--配置事务管理器，定义了一个DataSourceTransactionManager事务管理器-->
        <bean id = "transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager"
              p:dataSource-ref = "dataSource"/>
        <!--通过AOP配置提供事务增强，让service包下Bean的所有方法拥有事务-->
        <aop:config proxy-target-class="true">
            <aop:pointcut id="serviceMethod"
                          expression="(execution(* com.smart.service..*(..))) and
                    (@annotation(org.springframework.transaction.annotation.Transactional))"/>
            <aop:advisor pointcut-ref="serviceMethod" advice-ref="txAdvice"/>
        </aop:config>
        <tx:advice id="txAdvice" transaction-manager="transactionManager">
            <tx:attributes>
                <tx:method name="*"/>
            </tx:attributes>
        </tx:advice>
    </beans>

在< beans >的声明中添加 aop 和tx命名空间的Schema定义文件的说明，这样，在配置文件中就可以使用这两个空间下的配置标签。

扫描service类包，将com.smart.service添加到上下文扫描路径中，以便使Service包中类的Spring注解生效。

 定义一个基于数据源的DataSourceTransationManager事务管理器，该事务管理器负责声明式事务的管理。该管理器需要引用dataSource Bean

通过aop及tx命名空间的语法，以AOP的方式为com.smart.service包下所有类的所有标注@Transactional注解的方法都添加了事务增强，即它们都将工作在事务环境中。




