package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class AliVodClassifyVideosRequest {
    @NotEmpty
    private List<String> videoIds;

    @NotBlank
    private String categoryCode;
}
