package com.ratnakar.security.config;

import com.ratnakar.security.config.DataSourceConfig.DataSourceContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class DataSourceContextCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal after each request
            // Prevents context leaking into the next request on the same thread
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}