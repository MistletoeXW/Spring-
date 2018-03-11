package com.xw.domain;

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

    public void setUserId(int id)
    {
        userId = id;
    }
    public void setUserName(String name)
    {
        userName = name;
    }
    public void setCredits(int credit)
    {
        credits = credit;
    }
    public void setPassword(String Password){password = Password;}
    public void setLastIp(String ip){lastIp = ip;}
    public void setLastVist(Date vist){lastVist = vist;}

    public Date getLastVisit()
    {
        return lastVist;
    }
    public String getLastIp()
    {
        return lastIp;
    }
    public int getCredits()
    {
        return credits;
    }
    public int getUserId()
    {
        return userId;
    }
    public String getUserName(){return userName;}
    public String getPassword(){return password;}
    public Date getLastVist(){return lastVist;}

}