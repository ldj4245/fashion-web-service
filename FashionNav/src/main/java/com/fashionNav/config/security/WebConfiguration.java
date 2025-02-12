package com.fashionNav.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fashionNav.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;


/**
 * WebConfiguration
 *
 * 이 클래스는 Spring Security 설정을 정의합니다. JWT 인증 및 OAuth2 로그인 기능을 설정하고,
 * 특정 엔드포인트에 대한 접근 권한을 설정하며, CORS 설정을 정의합니다.
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@ComponentScan(basePackages = {"com.fashionNav.controller", "com.fashionNav.service"})
public class WebConfiguration {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;

    private static final List<String> SWAGGER = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    //프론트 엔드와 통신할때의 CORS 설정
//    ,"http://192.168.0.124:3000"
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000","http://192.168.0.124:3000","https://web-fashion-web-lydr4cy5f698c981.sel5.cloudtype.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "PUT"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/users/*", "/api/users/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/refresh").permitAll()
                        .requestMatchers(SWAGGER.toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.PUT,"/api/users/").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/news-comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/raw-news/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/page/archive/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/news/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/surveys/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/surveys/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/top3/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/brands/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/styles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/oauth2/google").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/processed-news/**").permitAll()
//                        .requestMatchers(HttpMethod.POST,"/api/users/oauth2/naver").permitAll()
//                        .requestMatchers(HttpMethod.POST,"/api/users/oauth2/kakao").permitAll()


                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, jwtAuthenticationFilter.getClass())
                .httpBasic(HttpBasicConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error=true")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                );

        return http.build();
    }
}
