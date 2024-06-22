package com.fashionNav.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {
    private int userId;
    private String password;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String role; // 역할을 문자열로 저장

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if(this.role.equals("ROLE_ADMIN")){
            return List.of(
                    new SimpleGrantedAuthority("ROLE_" + "ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );

        }else{
            return List.of(
                    new SimpleGrantedAuthority("ROLE_" + "ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // of 메서드 추가
    public static User of(String password, String name, String email, String role) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .password(password)
                .name(name)
                .email(email)
                .createdAt(now)
                .updatedAt(now)
                .role(role)
                .build();
    }
}