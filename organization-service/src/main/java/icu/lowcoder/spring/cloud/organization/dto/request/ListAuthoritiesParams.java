package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ListAuthoritiesParams {
    private List<UUID> roleIds;
}
