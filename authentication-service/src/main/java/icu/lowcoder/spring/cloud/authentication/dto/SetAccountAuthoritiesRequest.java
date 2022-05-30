package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SetAccountAuthoritiesRequest {
    private Set<String> authorities = new HashSet<>();
}
