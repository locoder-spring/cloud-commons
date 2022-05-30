package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class AliOssCopyObjectsRequest {
    @NotEmpty
    private List<Copy> objects;

    @Getter
    @Setter
    public static class Copy {
        private String sourceKey;
        private String targetKey;
    }
}
