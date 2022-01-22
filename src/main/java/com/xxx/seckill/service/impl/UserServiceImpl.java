package com.xxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.seckill.exception.GlobalException;
import com.xxx.seckill.mapper.UserMapper;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IUserService;
import com.xxx.seckill.utils.CookieUtil;
import com.xxx.seckill.utils.MD5Util;
import com.xxx.seckill.utils.UUIDUtil;
import com.xxx.seckill.vo.LoginVo;
import com.xxx.seckill.vo.RespBean;
import com.xxx.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2022-01-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 登录功能的实现
     *
     * @param loginVo 从前端传来的参数
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
//        // 判断手机号码和密码是否为空
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//
//        //判断手机号码是否符合规则
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }

        //根据手机号获取用户信息
        User user = userMapper.selectById(mobile);
        //如果用户未注册
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //判断密码是否正确
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生成Ticket
        String ticket = UUIDUtil.uuid();

        //将用户信息存到redis中
        redisTemplate.opsForValue().set("user:"+ticket, user);


//        request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);
        return RespBean.success();
    }


    /**
     * 根据Ticket的值从redis中获取User对象
     * @param userTicket cookie中的ticket值
     * @return User对象
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)){
            return null;
        }

        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);

        if(user != null){
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }

        return user;
    }
}
