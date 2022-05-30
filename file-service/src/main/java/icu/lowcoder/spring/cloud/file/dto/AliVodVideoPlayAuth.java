package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AliVodVideoPlayAuth {
    private String playAuth;

    private VideoMeta videoMeta;

    @Getter
    @Setter
    public static class VideoMeta {
        private String coverURL;
        private Long duration; //视频时长。单位：秒。
        private String status; // 视频状态。具体取值范围及描述，请参见视频状态Status的取值列表。
        private String title; // 阿里云VOD视频标题
        private String videoId;    // 视频ID
    }
}
