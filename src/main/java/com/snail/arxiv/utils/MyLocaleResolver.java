package com.snail.arxiv.utils;

import org.springframework.web.servlet.LocaleResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class MyLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        // 从请求头中获取l参数的值
        String l = httpServletRequest.getParameter("l");
        // 得到操作系统默认的区域信息
        Locale locale = Locale.getDefault();
        if(!StringUtils.isEmpty(l)){ // l为空则使用操作系统默认的区域信息
            // l有两部分组成：语言_国家
            String[] split = l.split("_");
            locale = new Locale(split[0], split[1]);
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

    }
}
