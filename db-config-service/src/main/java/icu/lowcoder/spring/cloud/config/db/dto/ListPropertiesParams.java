package icu.lowcoder.spring.cloud.config.db.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListPropertiesParams {
    private String applicationKeyword;
    private String profile;
    private String label;
    private String keyKeyword;
    private String valueKeyword;

}
