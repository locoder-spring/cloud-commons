package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AliOssListObjectPage {
    private String nextContinuationToken;
    private String continuationToken;
    private Integer maxKeys;
    private List<AliOssObject> objects = new ArrayList<>();
}
