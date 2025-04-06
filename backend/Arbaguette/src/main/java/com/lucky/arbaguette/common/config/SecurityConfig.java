package com.lucky.arbaguette.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucky.arbaguette.common.ApiResponse;
import com.lucky.arbaguette.common.jwt.JWTFilter;
import com.lucky.arbaguette.common.jwt.JWTUtil;
import com.lucky.arbaguette.common.jwt.LoginFilter;
import com.lucky.arbaguette.common.jwt.LogoutFilter;
import com.lucky.arbaguette.common.repository.TokenRedisRepository;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CrewRepository crewRepository;
    private final TokenRedisRepository tokenRedisRepository;

    @Value("${symmetric.key}")
    private String symmetrickey;

    @Value("${symmetric.salt}")
    private String salt;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AesBytesEncryptor aesBytesEncryptor() {
        return new AesBytesEncryptor(symmetrickey, salt);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        return http
                .cors(cors -> cors.configurationSource(apiConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //누구나 접근 가능
                        .requestMatchers("/", "/api/login", "/api/logout", "/join", "/api/user/**").permitAll()
                        //dispatcher 를 통해 넘어오는 에러는 모두 접근 가능
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        //BOSS 만 접근 가능
                        .requestMatchers("/api/boss/**", "/api/contract/boss", "/api/substitute/agree")
                        .hasAuthority("BOSS")
                        //CREW 만 접근 가능
                        .requestMatchers("/api/crew/**", "/api/schedule/crew/**", "/api/contract/crew")
                        .hasAuthority("CREW")
                        .anyRequest().authenticated())
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterBefore(new LogoutFilter(jwtUtil, tokenRedisRepository),
                        org.springframework.security.web.authentication.logout.LogoutFilter.class)
                .addFilterAt(
                        new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, crewRepository,
                                tokenRedisRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(unauthorizedEntryPoint()) // 인증 예외 처리 (401)
                        .accessDeniedHandler(accessDeniedHandler()))        // 인가 예외 처리 (403)
                .build();
    }

    // AuthenticationEntryPoint 를 사용하여 인증되지 않은 사용자가 접근할 때 에러 처리
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    ApiResponse.error(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.")
            ));
        };
    }

    // 권한이 없는 사용자가 접근할 때 처리
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    ApiResponse.error(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.")
            ));
        };
    }

    private CorsConfigurationSource apiConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://j11c101.p.ssafy.io:8080", "http://localhost:8081"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
