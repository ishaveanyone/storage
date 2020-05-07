/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2020-04-28 10:13
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：参数的过滤
 */

package com.xupp.storage.filter;

import com.google.common.base.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;

@Component
@Order(1)
@WebFilter(filterName = "testFilter1", urlPatterns = "/*")
public class ParamFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        //获取所有的参数
        Enumeration<String> enumerations=
                servletRequest.getParameterNames();
        while(enumerations.hasMoreElements()){
            if("space".equals(enumerations.nextElement())){
                //如果存在space 那么就一定 必填
                if(Strings.isNullOrEmpty((String)
                        servletRequest.getParameter("space"))){
                    throw  new RuntimeException("space 必填");
                }
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }

}
