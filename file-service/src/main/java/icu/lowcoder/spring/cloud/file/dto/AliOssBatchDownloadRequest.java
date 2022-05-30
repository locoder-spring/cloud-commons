package icu.lowcoder.spring.cloud.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AliOssBatchDownloadRequest {
    @NotEmpty
    @Valid
    private List<BatchOpFile> files = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOpFile {
        private String path;
        private String name;
    }
}
