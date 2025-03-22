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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String requestPath = httpReq.getRequestURI();

        if (requestPath.equals("/scores/ping")) {
            chain.doFilter(request, response);
            return;
        }

        String receivedKey = httpReq.getHeader("X-API-KEY");
        if (receivedKey == null || !receivedKey.equals(apiKey)) {
            HttpServletResponse httpRes = (HttpServletResponse) response;
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpRes.getWriter().write("Invalid or missing API key");
            return;
        }

        chain.doFilter(request, response);
    }
}
