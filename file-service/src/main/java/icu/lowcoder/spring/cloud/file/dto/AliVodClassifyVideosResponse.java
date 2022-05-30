package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AliVodClassifyVideosResponse {
    private List<String> forbiddenVideoIds;
    private List<String> nonExistVideoIds;
}
