package icu.lowcoder.spring.cloud.bridge.wechat.service;

import icu.lowcoder.spring.cloud.bridge.wechat.dto.AuthorizeParams;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/authorize")
public interface AuthorizeService {

    @GetMapping
    void authorize(AuthorizeParams params, HttpServletRequest request, HttpServletResponse response);

    @RequestMapping("/callback")
    void callback(HttpServletRequest request, HttpServletResponse response);
}
