package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ListAccountsParams {
    private Set<UUID> ids;
}
