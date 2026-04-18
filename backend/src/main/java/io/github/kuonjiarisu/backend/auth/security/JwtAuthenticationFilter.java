package io.github.kuonjiarisu.backend.auth.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.kuonjiarisu.backend.auth.model.JwtUserClaims;
import io.github.kuonjiarisu.backend.auth.service.UserAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserAccountService userAccountService;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        UserAccountService userAccountService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userAccountService = userAccountService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        var authorization = request.getHeader("Authorization");

        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtUserClaims claims = jwtTokenService.parse(authorization.substring(7));
            var user = userAccountService.loadAuthenticatedUserById(claims.userId());

            var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            log.warn(
                "Ignored invalid bearer token: method={} path={} reasonType={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
            );
        }

        filterChain.doFilter(request, response);
    }
}
