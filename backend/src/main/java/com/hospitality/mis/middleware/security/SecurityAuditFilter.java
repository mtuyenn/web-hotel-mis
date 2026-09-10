package com.hospitality.mis.middleware.security;

import com.hospitality.mis.service.governance.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Records outcomes only; credentials, query strings and request bodies are never logged. */
public class SecurityAuditFilter extends OncePerRequestFilter {
    private final SecurityAuditService audit;
    public SecurityAuditFilter(SecurityAuditService audit) { this.audit = audit; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request, response);
        if (!request.getRequestURI().startsWith("/api/") || request.getMethod().equals("OPTIONS")) return;
        int status = response.getStatus();
        String action = status == 401 ? "AUTHENTICATION_REJECTED" : status == 403 ? "ACCESS_DENIED" : null;
        if (action == null) return;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = "ANONYMOUS";
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            actor = authentication.getName();
            if (authentication instanceof JwtAuthenticationToken jwt
                    && "CUSTOMER".equals(jwt.getToken().getClaimAsString("principal_type"))) actor = "customer:" + actor;
        }
        audit.record(actor, action, request.getMethod(), request.getRequestURI(), status);
    }
}
