package com.example.reggie_take_out.filter;

import com.alibaba.fastjson.JSON;
import com.example.reggie_take_out.common.BaseContext;
import com.example.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符(字符串不支持通配符比较，因此引入)
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 因为调用的方法getRequestURI是HttpServletRequest的，所以要向下转型
        // public interface HttpServletRequest extends ServletRequest
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1.获取本次请求的URI (/backend/index.html)
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //   定义好不需要处理的请求路径(登录、退出和静态资源)
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        // 2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        // 3.如果不需要处理，直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        // 4.判断登录状态，如果已登录，直接放行
        //   开发中最好校验session是否合法
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为{}",request.getSession().getAttribute("employee"));
            // 将当前用户id存入
            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        // 5.如果未登录，则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        //   因为方法没有返回值，要通过输出流给前端提供json数据
        //   不能直接传R是因为这里没有加@ResponseBody设定返回Json
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * 简单来说就是将urls里的字符串取出与URI进行ant比较是否match
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match==true){
                return true;
            }
        }
        return  false;
    }
}
