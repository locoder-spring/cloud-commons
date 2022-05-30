package icu.lowcoder.spring.cloud.authentication.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public interface UserDetailsByIdService extends UserDetailsService {
    UserDetails loadUserById(UUID id) throws UsernameNotFoundException;
}
