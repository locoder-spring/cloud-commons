package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AliVodVideoPlayInfo {
    private List<PlayInfo> playInfos;
    private VideoMeta videoMeta;

    @Getter
    @Setter
    public static class PlayInfo {
        private String bitrate; // 视频流码率。单位：Kbps。
        private String definition; // 视频流清晰度定义
        private Long duration; // 视频流长度。单位：秒。
        private Boolean encrypt; // 视频流是否加密流
        private String encryptType; // 视频流加密类型。  AliyunVoDEncryption：阿里云视频加密。 HLSEncryption：HLS标准加密。
        private String format; // 视频流格式。 mp4 m3u8 mp3
        private String fps; // 视频流帧率。单位：帧/每秒。
        private Long height; // 视频流高度。单位：px。
        private Long width; // 视频流宽度。单位：px。
        private String playURL; // 视频流的播放地址。
        private Long size; // 视频流大小.单位：Byte。
        private String status; // 视频流状态: Normal：正常, Invisible：不可见
        private String streamType; // 视频流类型。若媒体流为视频则取值：video，若是纯音频则取值：audio。
        private String specification; // 音视频转码输出规格
        private String narrowBandType; // 窄带高清类型。取值：0 , 1.0, 2.0
    }

    @Getter
    @Setter
    public static class VideoMeta {
        private String coverURL;
        private Date creationTime;
        private Long duration; //视频时长。单位：秒。
        private String status; // 视频状态。具体取值范围及描述，请参见视频状态Status的取值列表。
        private String title; // 阿里云VOD视频标题
        private String videoId;    // 视频ID
        private String mediaType;    // 媒体文件类型，取值：video, audio
    }
}
