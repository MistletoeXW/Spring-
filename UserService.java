package com.xw.service;

import com.xw.dao.LoginLogDao;
import com.xw.dao.UserDao;
import com.xw.domain.LoginLog;
import com.xw.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service //将UserService标注为一个服务层的Bean
public class UserService {
    private UserDao userDao;
    private LoginLogDao loginLogDao;


    //hasMatchUser()业务方法简单调用Dao层的Bean
    public boolean hasMatchUser(String userName,String password)
    {
        int matchCount = userDao.getMatchCount(userName,password);
        return matchCount > 0;
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
        userDao.updateLoginInfo(user);
        loginLogDao.insertLoginLog(loginLog);
    }

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


}
