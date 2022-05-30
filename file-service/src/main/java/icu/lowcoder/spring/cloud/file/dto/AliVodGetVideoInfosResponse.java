package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AliVodGetVideoInfosResponse {
    private List<Video> videos = new ArrayList<>();

    @Getter
    @Setter
    public static class Video {
        private String videoId;
        private Boolean exist = true; // 视频存在（videoId 正确）
        private String coverUrl; // 视频封面URL
        private Long cateId; // 视频分类ID
        private String cateName; // 视频分类名称
        private Date creationTime; // 视频创建时间
        private String description; // 视频描述
        private Long duration; // 视频时长。单位：秒
        private Date modificationTime; // 更新时间
        private Long size; // 视频源文件大小。单位：字节
        /*
         * 视频状态。默认获取所有视频，多个使用英文逗号（,）分隔。取值包括：
         * Uploading：上传中。
         * UploadFail：上传失败。
         * UploadSucc：上传完成。
         * Transcoding：转码中。
         * TranscodeFail：转码失败。
         * Blocked：屏蔽。
         * Normal：正常。
         */
        private String status; // 视频源文件大小。单位：字节
        private String title; // 视频标题
    }
}
