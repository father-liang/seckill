package com.xxx.seckill.config;

import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IUserService;
import com.xxx.seckill.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义用户参数
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private IUserService userService;
    /**
     * 如果这个方法返回true，就会执行下面的resolveArgument方法，会对所有经过mvc的参数进行判断
     * @param parameter 进入的参数
     * @return boolean值
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;

    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if(StringUtils.isEmpty(ticket)){
            return null;
        }

        User user = userService.getUserByCookie(ticket, request, response);
        return user;
    }
}
