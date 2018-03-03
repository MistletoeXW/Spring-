# 建立领域对象

领域对象（Domain Object）也被称为实体类，它代表业务的状态，且贯穿展现层、业务层和持久层，并被持久化到数据库中。领域对象是数据库操作以面向对象的方式进行，领域对象不一定等同于数据库表，不过对于简单的应用来说，领域对象往往拥有对应的数据库表。

领域对象主要的工作是从数据库表中加载数据并实例化领域对象，或将领域对象持久化到数据库表中。

---

    package com.smart.domain;
    import java.io.Serializable;
    import java.util.Date;
    
    //用户领域对象，可以看成是用户数据表t_user表的对象映射，每个字段对应一个属性。
    //领域对象一般要实现Serializble接口，以便可以序列化
    public class User implements Serializable {
        private int userId;
        private String userName;
        private String password;
        private int credits;
        private String lastIp;
        private Date lastVist;
    
        public void setUserId(int id){ userId = id; }
        public void setUserName(String name){  userName = name; }
        public void setCredits(int credit){  credits = credit; }
        public void setPassword(String Password){password = Password;}
        public void setLastIp(String ip){lastIp = ip;}
        public void setLastVist(Date vist){lastVist = vist;}
    
        public Date getLastVisit(){ return lastVist; }
        public String getLastIp(){ return lastIp; }
        public int getCredits(){ return credits; }
        public int getUserId(){  return userId; }
        public String getUserName(){return userName;}
        public String getPassword(){return password;}
        public Date getLastVist(){return lastVist;}
    
    }

## implements:

implements 也是实现父类和子类之间继承关系的关键字，如类 A 继承 类 B 写成 class A implements B{}.

这是百度百科上的解释：

implements是一个类实现一个接口用的 关键字 ，他是用来实现接口中定义的抽象方法。比如：people是一个接口，他里面有say这个方法。public interface people(){ public say();}但是接口没有方法体。只能通过一个具体的类去实现其中的方法体。比如chinese这个类，就实现了people这个接口。 public class chinese implements people{ public say() {System.out.println("你好！");}}

JAVA中extends 与implements有啥区别？  
1. 在类的声明中，通过关键字extends来创建一个类的子类。一个类通过关键字implements声明自己使用一个或者多个接口。
   extends 是继承某个类, 继承之后可以使用父类的方法, 也可以重写父类的方法; implements 是实现多个接口, 接口的方法一般为空的, 必须重写才能使用  
2. extends是继承父类，只要那个类不是声明为final或者那个类定义为abstract的就能继承，JAVA中不支持多重继承，但是可以用接口来实现，这样就要用到implements，继承只能继承一个类，但implements可以实现多个接口，用逗号分开就行了
   比如 :class A extends B implements C,D,E  

## Serializable:

实现java.io.Serializable接口的类是可序列化的。没有实现此接口的类将不能使它们的任一状态被序列化或逆序列化。所谓的Serializable,就是java提供的通用数据保存和读取的接口。至于从什么地方读出来和保存到哪里去都被隐藏在函数参数的背后了。这样子，任何类型只要实现了Serializable接口，就可以被保存到文件中，或者作为数据流通过网络发送到别的地方。也可以用管道来传输到系统的其他程序中。这样子极大的简化了类的设计。只要设计一个保存一个读取功能就能解决上面说得所有问题。