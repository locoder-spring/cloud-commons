package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class AliVodGetVideoInfosRequest {
    @NotEmpty
    private List<String> videoIds;
}
