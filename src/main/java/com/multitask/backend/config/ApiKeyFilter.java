package com.multitask.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Configuration
@Order(1)
public class ApiKeyFilter implements Filter {

    @Value("${app.api-key}")
    private String apiKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        httpRes.setHeader("Access-Control-Allow-Origin", frontendUrl);
        httpRes.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, X-API-KEY");
        httpRes.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
            httpRes.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = httpReq.getRequestURI();
        if ("/scores/ping".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String receivedKey = httpReq.getHeader("X-API-KEY");
        if (receivedKey == null || !receivedKey.equals(apiKey)) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpRes.getWriter().write("Invalid or missing API key");
            return;
        }

        chain.doFilter(request, response);
    }
}
