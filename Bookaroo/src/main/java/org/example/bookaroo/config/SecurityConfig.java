package org.example.bookaroo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // thymeleaf
                        .requestMatchers("/", "/index").permitAll()
                        .requestMatchers("/register", "/login").permitAll()

                        // publiczne API
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()

                        // panel admina
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated() // reszta wymaga zalogowania
                )

                .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                // basic auth
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}