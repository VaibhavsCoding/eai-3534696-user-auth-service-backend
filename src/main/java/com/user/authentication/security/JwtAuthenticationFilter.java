package com.user.authentication.security;

import com.user.authentication.model.User;
import com.user.authentication.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component // <--- Make it a Spring Bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // 1️⃣ Validate and parse token
                Claims claims = jwtUtil.validateToken(token).getBody();
                String email = claims.getSubject(); // JWT subject as email or userId

                // 2️⃣ Ensure user exists
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    throw new UsernameNotFoundException("User not found for token subject: " + email);
                }

                User user = userOpt.get();

                // 3️⃣ Optionally check if user is verified or active
                if (!user.isVerified()) {
                    throw new UsernameNotFoundException("User not verified or inactive: " + email);
                }

                // 4️⃣ Build Authentication object (roles can be added here if needed)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());

                // 5️⃣ Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (io.jsonwebtoken.JwtException | UsernameNotFoundException e) {
                logger.warn("Invalid JWT or user issue: {}", e.getMessage());
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
