package com.example.prenotazioni_aule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMINISTRATORS")
                        .requestMatchers("/user/**").hasAnyRole("USERS", "ADMINISTRATORS")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        // Logica intelligente: Reindirizzamento in base al ruolo
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMINISTRATORS"));

                            if (isAdmin) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/user");
                            }
                        })
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        // 1. URL: Legge la variabile d'ambiente LDAP_URL (da Docker)
        // Se non esiste (es. sei in IntelliJ), usa localhost come fallback
        String ldapUrl = System.getenv("LDAP_URL");
        if (ldapUrl == null || ldapUrl.isEmpty()) {
            ldapUrl = "ldap://localhost:389/dc=example,dc=com";
        }

        // 2. Password: Legge la variabile LDAP_ADMIN_PASSWORD (da .env)
        // SICUREZZA: Questo evita di caricare la password vera su GitHub
        String ldapPassword = System.getenv("LDAP_ADMIN_PASSWORD");
        if (ldapPassword == null || ldapPassword.isEmpty()) {
            // Fallback per sviluppo locale rapido (opzionale)
            ldapPassword = "admin";
        }

        DefaultSpringSecurityContextSource contextSource =
                new DefaultSpringSecurityContextSource(ldapUrl);

        // Utente Manager (questo non è segreto, può stare nel codice)
        contextSource.setUserDn("cn=admin,dc=example,dc=com");

        // Imposta la password recuperata in modo sicuro
        contextSource.setPassword(ldapPassword);

        return contextSource;
    }

    @Bean
    public BindAuthenticator bindAuthenticator(BaseLdapPathContextSource contextSource) {
        FilterBasedLdapUserSearch userSearch =
                new FilterBasedLdapUserSearch("ou=people", "(uid={0})", contextSource);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);
        return authenticator;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(
            BindAuthenticator bindAuthenticator,
            BaseLdapPathContextSource contextSource) {

        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(contextSource, "ou=groups");

        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        authoritiesPopulator.setGroupRoleAttribute("cn");
        authoritiesPopulator.setRolePrefix("ROLE_");
        authoritiesPopulator.setSearchSubtree(true);

        LdapAuthenticationProvider provider =
                new LdapAuthenticationProvider(bindAuthenticator, authoritiesPopulator);
        return provider;
    }
}