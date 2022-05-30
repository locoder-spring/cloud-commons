package icu.lowcoder.spring.cloud.authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

public class EmptyPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence charSequence) {
        return "";
    }
    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return false;
    }
}
