package com.xxx.seckill.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.service.IUserService;
import com.xxx.seckill.utils.CookieUtil;
import com.xxx.seckill.vo.RespBean;
import com.xxx.seckill.vo.RespBeanEnum;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AccessLimitInterceptor implements HandlerInterceptor {

    @Resource
    private IUserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            User user = getUser(request, response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;

            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            if (accessLimit == null) {
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String uri = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                uri = uri + user.getId();
            }

            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(uri);

            if (count == null) {
                valueOperations.set(uri, 1, second, TimeUnit.SECONDS);
            }else if (count < maxCount){
                valueOperations.increment(uri);
            }else{
                render(response, RespBeanEnum.ACCESS_LIMIT_REAHCED);
                return false;
            }
        }

        return true;
    }

    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws JsonProcessingException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        RespBean respBean = RespBean.error(respBeanEnum);
        writer.write(new ObjectMapper().writeValueAsString(respBean));
        writer.flush();
        writer.close();
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }

        return userService.getUserByCookie(userTicket, request, response);
    }
}
