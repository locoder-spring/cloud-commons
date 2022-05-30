package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.cloud.authentication.EmptyPasswordEncoder;
import icu.lowcoder.spring.cloud.authentication.service.JpaUserDetailsByIdService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSecurityConfiguration {

    @Configuration
    public static class Oauth2WebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        private final JpaUserDetailsByIdService userDetailsService;

        public Oauth2WebSecurityConfigurationAdapter(JpaUserDetailsByIdService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @Primary
        @Bean("oauth2AuthenticationManager")
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Bean("oauth2UserDetailsService")
        @Override
        public UserDetailsService userDetailsServiceBean() throws Exception {
            return super.userDetailsServiceBean();
        }

        @Bean("oauth2PasswordEncoder")
        public PasswordEncoder passwordEncoder() {
            String encodingId = "bcrypt";
            String defaultForMatches = "noop";

            Map<String, PasswordEncoder> encoders = new HashMap<>();
            encoders.put(encodingId, new BCryptPasswordEncoder());
            encoders.put("ldap", new LdapShaPasswordEncoder());
            encoders.put("MD4", new Md4PasswordEncoder());
            encoders.put("MD5", new MessageDigestPasswordEncoder("MD5"));
            encoders.put("noop", NoOpPasswordEncoder.getInstance());
            encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
            encoders.put("scrypt", new SCryptPasswordEncoder());
            encoders.put("SHA-1", new MessageDigestPasswordEncoder("SHA-1"));
            encoders.put("SHA-256", new MessageDigestPasswordEncoder("SHA-256"));
            encoders.put("sha256", new StandardPasswordEncoder());
            encoders.put("empty", new EmptyPasswordEncoder());

            DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(encodingId, encoders);
            passwordEncoder.setDefaultPasswordEncoderForMatches(encoders.get(defaultForMatches));

            return passwordEncoder;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasenames("classpath:org/springframework/security/messages");

            auth
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(passwordEncoder())
                    .withObjectPostProcessor(new ObjectPostProcessor<AbstractUserDetailsAuthenticationProvider>() {
                        @Override
                        public <O extends AbstractUserDetailsAuthenticationProvider> O postProcess(O object) {
                            object.setMessageSource(messageSource);
                            return object;
                        }
                    });
        }

    }

}
