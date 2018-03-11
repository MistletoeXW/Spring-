package com.xw.domain;

import java.io.Serializable;
import java.util.Date;

//登录日志领域对象
public class LoginLog implements Serializable {
    private int loginLogId;
    private int userId;
    private String ip;
    private Date loginDate;

    public void setLoginLogId(int i){loginLogId=i;}
    public void setUserId(int id)
    {
        userId = id;
    }
    public void setIp(String ip1)
    {
        ip = ip1;
    }
    public void setLoginDate(Date Date){loginDate = Date;}

    public int getLoginLogId(){return loginLogId;}
    public int getUserId()
    {
        return userId;
    }
    public String getIp()
    {
        return ip;
    }
    public Date getLoginDate()
    {
        return loginDate;
    }

}
