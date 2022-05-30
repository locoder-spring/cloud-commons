package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ClientsResponse {
    private UUID id;
    private String clientId;
    private String clientName;
    private String clientSecret;
    private Set<String> authorities;
    private Set<String> grantTypes;
    private String scopes;
}
