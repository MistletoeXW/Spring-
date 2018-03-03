# 持久层

持久层负责数据的访问与操作，DAO类被上层的业务调用。Spring本身支持多种流行的ORM框架，这里使用Spring JDBC作为持久层的实现技术。

## UserDao

    package com.smart.dao;
    import com.smart.domain.User;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.jdbc.core.RowCallbackHandler;
    import org.springframework.stereotype.Repository;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    //使用Spring JDBC 方式实现DAO类
    @Repository  //通过Spring注解定义一个DAO ,需要注入依赖包spring-context
    public class UserDao
    {
        private JdbcTemplate jdbcTemplate;//需要注入依赖包spring-jdbc
    
        //根据用户名查询用户的SQL语句
        private final static String UPDATE_LOGIN_INFO_SQL = "UPDATE t_user SET"+
                "last_visit=?,last_ip=?,credits=? WHERE user_id=?";
    
        private final static  String QUERY_BY_USERNAME = "SELECT user_id,user_name,credits"
                + "FROM t_user WHERE user_name =?";
    
        private final static String MATCH_COUNT_SQL = "SELECT count(*) FROM t_user" +
                "WHERE user_name =? and password=? ";
    
        @Autowired  //自动注入JdbcTemplate的Bean
        public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
        {
            this.jdbcTemplate = jdbcTemplate;
        }
        //getMatchCount根据用户名和密码获取匹配的用户数，等于1则代表用户名/ 密码正确，等于0表示错误
        //这是最简单的用户身份认证方式
        public int getMatchCount(String userName,String password)
        {
            return jdbcTemplate.queryForObject(MATCH_COUNT_SQL, new Object[]{userName,password},Integer.class);
        }
        //通过JdbcTemplate 的支持，我们可以轻松实现UserDao的另外两个接口
        //使用findUserByUserName()方法来根据用户名获取User对象
        public User findUserByUserName(final String userName)
        {
            final User user = new User();
            jdbcTemplate.query(QUERY_BY_USERNAME, new Object[]{userName},
                    //匿名类方式实现的回调函数
                    new RowCallbackHandler()
                    {
                        public void processRow(ResultSet rs) throws SQLException
                        {
                            user.setUserId(rs.getInt("user_id"));
                            user.setUserName(userName);
                            user.setCredits(rs.getInt("credits"));
                        }
                    });
            return user;
        }
        public void updateLoginInfo(User user){
            jdbcTemplate.update(UPDATE_LOGIN_INFO_SQL,new Object[] {user.getLastVisit(),
            user.getLastIp(),user.getCredits(),user.getUserId()});
        }
    
    }

## LoginLogDao

    package com.smart.dao;

    import com.smart.domain.LoginLog;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.stereotype.Repository;
    
    @Repository
    public class LoginLogDao
    {
        private JdbcTemplate jdbcTemplate;
    
        //保存日志的sql
        private final static String INSERT_LOGIN_LOG_SQL = "INSERT INTO " +
                "t_login_log(user_id,ip,login_datetime) VALUES(?,?,?)";
    
        public void insertLoginLog(LoginLog loginLog){
            Object[] args = { loginLog.getUserId(),loginLog.getIp(),loginLog.getLoginDate() };
            jdbcTemplate.update(INSERT_LOGIN_LOG_SQL,args);
        }
    
        @Autowired
        public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
        {
            this.jdbcTemplate = jdbcTemplate;
        }
    }

传统的	JDBC API 大底层，即使用户执行一条简单的数据查询操作，都必须执行如下过程：获取连接、创建Statement、执行数据操作、获取结果、关闭Statement、关闭结果集、关闭连接。除此之外，还需要进行异常处理工作，如果使用传统的JDBC API进行数据访问，则可能会产生单调乏味的重复性代码。

Spring JDBC 对传统的 Spring JDBC API 进行了薄层封装，将样式的代码和那些必不可少的的代码进行分离，用户只需要编写哪些必不可少的代码，剩余哪些单调乏味的代码直接交给Spring JDBC 框架处理。简单来说，Spring JDBC通过一个，模板类org.spring.framwork.jdbc.core.JdbcTemplate封装了样板的代码，用户通过，模板类就可以轻松完成大部分数据大部分数据访问工作。

- getMatchCount(): 根据用户名和密码获取匹配的用户数。等于1表示用户名/密码正确；等于0表示用户名或密码错误（这是最简单的用户身份认证方法，在实际应用中需要采用诸多密码加密等安全策略）。  
  我们仅提供了一个查询SQL语句MATCH_COUNT_SQL，直接调用模板的queryForInt()方法就可以获取查询，用户不用担心获取连接，关闭连接，异常处理等繁琐的事务。
- findUserByUserName(): 根据用户名获取User对象。、
  我使用了JdbcTemplate#query()方法，该方法签名为query(String sql,Object[] args,RowCallbackHandler rch),它有3个入参。  
  sqlStr：查询的SQL语句，允许使用带“？”的参数占位符。  
  args：SQL语句中占位符对应的参数数组。  
  RowCallbackHandler：查询结果的处理回调接口，该回调接口有一个方法processRow(ResultSet rs),负责将ResultSet装载到类似于领域对象的对象实例中。在此代码中，我通过匿名内部类的方式定义了一个RowCallbackHandler回调接口实例，将ResultSet()转换为User对象。
- updateLoginInfo(): 更新用户积分、最后登录及IP  。  
  通过JdbcTemplate#update(String sql,Object[])进行数据的更新操作。
- insertLoginLog() 接口的方法与UserDao相似，其实现类也通过Jdbc Template#update（String sql,Object[] args)方法完成登录日志的插入操作。


### @Repository

在spring 2.5后，可以使用注解的方式定义Bean。较XML配置方式，注解的方式简单性非常明显。

这里使用了一个@Repository定义了一个Dao Bean。

它用于将数据访问层 (DAO 层 ) 的类标识为 Spring Bean。具体只需将该注解标注在 DAO类上即可。同时，为了让 Spring 能够扫描类路径中的类并识别出 @Repository 注解，需要在 XML 配置文件中启用Bean 的自动扫描功能，这可以通过context:component-scan/实现。

    // 首先使用 @Repository 将 DAO 类声明为 Bean 
     package bookstore.dao; 
     @Repository 
     public class UserDaoImpl implements UserDao{ …… } 
    
     // 其次，在 XML 配置文件中启动 Spring 的自动扫描功能
     <beans … > 
        ……
     <context:component-scan base-package=”bookstore.dao” /> 
    ……
     </beans> 

如此，我们就不再需要在 XML 中显式使用 <bean/> 进行Bean 的配置。Spring 在容器初始化时将自动扫描 base-package 指定的包及其子包下的所有 class文件，所有标注了 @Repository 的类都将被注册为 Spring Bean。

为什么 @Repository 只能标注在 DAO 类上呢？这是因为该注解的作用不只是将类识别为Bean，同时它还能将所标注的类中抛出的数据访问异常封装为 Spring 的数据访问异常类型。 Spring本身提供了一个丰富的并且是与具体的数据访问技术无关的数据访问异常结构，用于封装不同的持久层框架抛出的异常，使得异常独立于底层的框架。

Spring 2.5 在 @Repository的基础上增加了功能类似的额外三个注解：@Component、@Service、@Constroller，它们分别用于软件系统的不同层次：

- @Component 是一个泛化的概念，仅仅表示一个组件 (Bean) ，可以作用在任何层次。
- @Service 通常作用在业务层，但是目前该功能与 @Component 相同。
- @Constroller 通常作用在控制层，但是目前该功能与 @Component 相同。

通过在类上使用 @Repository、@Component、@Service 和 @Constroller 注解，Spring会自动创建相应的 BeanDefinition 对象，并注册到 ApplicationContext 中。这些类就成了 Spring受管组件。这三个注解除了作用于不同软件层次的类，其使用方式与 @Repository 是完全相同的。

### @Autowired

@Autowired是一种函数，可以对成员变量、方法和构造函数进行标注，来完成自动装配的工作，@Autowired标注可以放在成员变量上，也可以放在成员变量的set方法上。

这里必须明确：@Autowired是根据类型进行自动装配的，如果需要按名称进行装配，则需要配合@Qualifier1() 使用；

UserRepository.java

    1 package com.proc.bean.repository;
    2 
    3 public interface UserRepository {
    4     
    5     void save();
    6 }

这里定义了一个UserRepository接口，其中定义了一个save方法

UserRepositoryImps.java

     1 package com.proc.bean.repository;
     2 
     3 import org.springframework.stereotype.Repository;
     4 
     5 @Repository("userRepository")
     6 public class UserRepositoryImps implements UserRepository{
     7 
     8     @Override
     9     public void save() {
    10         System.out.println("UserRepositoryImps save");
    11     }
    12 }

定义一个UserRepository接口的实现类，并实现save方法，在这里指定了该bean在IoC中标识符名称为userRepository

UserService.java

     1 package com.proc.bean.service;
     2 
     3 import org.springframework.beans.factory.annotation.Autowired;
     4 import org.springframework.stereotype.Service;
     5 
     6 import com.proc.bean.repository.UserRepository;
     7 
     8 @Service
     9 public class UserService {
    10 
    11     @Autowired
    12     private UserRepository userRepository;
    13     
    14     public void save(){
    15         userRepository.save();
    16     }
    17 }

这里需要一个UserRepository类型的属性，通过@Autowired自动装配方式，从IoC容器中去查找到，并返回给该属性

那么使用@Autowired的原理是什么？

　　其实在启动spring IoC时，容器自动装载了一个AutowiredAnnotationBeanPostProcessor后置处理器，当容器扫描到@Autowied、@Resource或@Inject时，就会在IoC容器自动查找需要的bean，并装配给该对象的属性

注意事项：

在使用@Autowired时，首先在容器中查询对应类型的bean

　　　　如果查询结果刚好为一个，就将该bean装配给@Autowired指定的数据

　　　　如果查询的结果不止一个，那么@Autowired会根据名称来查找。

　　　　如果查询的结果为空，那么会抛出异常。解决方法时，使用required=false
### 在Spring 中装配DAO

在编写DAO接口的实现类时，大家也许会有疑问：在以上两个DAO实现类中没有打开/释放Connection的代码，Dao类究竟如何实现访问数据库的能？

前面说过，样板式的操作都被JdbcTemplate 封装起来了，JdbcTemplate本身需要一个DataSource，这样它就可以根据需要从DataSource中获取或者返回连接。UserDao与LoginLog都提供了一个带@Autowried注解的Jdbc Template 变量，所以我们必须先声明一个数据源，然后定义一个JdbcTemplate Bean ，通过Spring 的容器的上下文自动绑定机制进行Bean的注入。  

这里就需要在src/resources（在Maven工程中，资源文件统一都放在resources文件夹中）目录下创建一个名为smart-context.xml的Spring配置文件：

smart-context.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:p="http://www.springframework.org/schema/p"
           xmlns:context="http://www.springframework.org/schema/c"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd">
    
        <!--扫描类包，将标注Spring注解的类自动转化为Bean，同时完成Bean的注入-->
        <!--使用Spring的<context:context-scan>扫描指定类包下的所有类-->
        <context:context-scan base-package="com.smart.dao"/>
    
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


    </beans>

- 使用Spring 的< context:component-scan  >扫描指定类包下的所有类，这样在类中定义的Spring注解（如@Repository、@Autowired等）才能使用。
- 使用Jakarta 的 DBCP开源数据实现方案定义了一个数据源，数据库驱动类为com.mysql.jdbc.Driver。
- 配置了JdbcTemplate Bean ，将其声明的dataSource注入JdbcTemplate中，而这个JdbcTemplate Bean 将通过@Autowired自动注入UserDao与LoginLog的Bean中。  

