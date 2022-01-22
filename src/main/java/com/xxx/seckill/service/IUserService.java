package com.xxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.vo.LoginVo;
import com.xxx.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Liang
 * @since 2022-01-10
 */
public interface IUserService extends IService<User> {

    /**
     * 登录功能的接口
     * @param loginVo 从前端传来的参数
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     *
     * 根据Ticket从redis中获取用户
     * @param userTicket cookie中的ticket值
     * @return 用户信息
     */
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}
