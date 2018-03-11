package com.xw.web;

public class LoginCommand {
    private String userName;
    private String password;

    public void setUserName(String name){userName = name;}
    public void setPassword(String Password){password = Password;}

    public String getUserName(){return userName;}
    public String getPassword(){return password;}
}
