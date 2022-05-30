package icu.lowcoder.spring.cloud.file.service;

import icu.lowcoder.spring.cloud.file.dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/files/ali-vod")
public interface AliVodFilesService {

    /**
     * 创建上传凭证
     *
     * @return
     */
    @PostMapping(value = "/upload-video-vouchers")
    AliVodUploadVideoVoucherResponse createUploadVideoVoucher(AliVodUploadVideoVoucherRequest request);

    /**
     * 创建上传凭证
     * @return
     */
    @PostMapping(value = "/upload-video-vouchers", params = "op=refresh")
    AliVodUploadVideoVoucherResponse refreshUploadVideoVoucher(String videoId);

    /**
     * 获取视频信息
     * @param request 请求体
     * @return 返回视频信息列表
     */
    @PostMapping(value = "/videos", params = "op=infos")
    AliVodGetVideoInfosResponse getVideoInfos(AliVodGetVideoInfosRequest request);

    /**
     * 视频分类
     * @param request 请求体
     * @return 返回失败的不存在的视频id
     */
    @PostMapping(value = "/videos", params = "op=classify")
    AliVodClassifyVideosResponse classify(AliVodClassifyVideosRequest request);

    /**
     * 创建视频播放凭证
     * @return AliVodVideoPlayAuth 播放凭证信息
     */
    @PostMapping(value = "/video-play-auth")
    AliVodVideoPlayAuth createVideoPlayAuth(AliVodCreateVideoPlayAuthRequest request);

    /**
     * 创建视频播放凭证
     * @return AliVodVideoPlayAuth 播放凭证信息
     */
    @PostMapping(value = "/videos", params = "op=playInfo")
    AliVodVideoPlayInfo getVideoPlayInfo(AliVodGetVideoPlayInfoRequest request);

}
