package icu.lowcoder.spring.cloud.file.controller;

import com.aliyuncs.vod.model.v20170321.*;
import icu.lowcoder.spring.commons.ali.vod.AliVodClient;
import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.file.dto.*;
import icu.lowcoder.spring.cloud.file.service.AliVodFilesService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AliVodFilesController implements AliVodFilesService {

    private final AliVodClient vodClient;

    public AliVodFilesController(AliVodClient vodClient) {
        this.vodClient = vodClient;
    }

    @Override
    public AliVodUploadVideoVoucherResponse createUploadVideoVoucher(@Valid @RequestBody AliVodUploadVideoVoucherRequest request) {
        return BeanUtils.instantiate(AliVodUploadVideoVoucherResponse.class, vodClient.uploadVideoVoucher(request.getTitle(), request.getFileName(), SecurityUtils.getPrincipalId()));
    }

    @Override
    public AliVodUploadVideoVoucherResponse refreshUploadVideoVoucher(@RequestParam(name = "video-id") String videoId) {
        return BeanUtils.instantiate(AliVodUploadVideoVoucherResponse.class, vodClient.refreshUploadVideo(videoId));
    }

    @Override
    public AliVodGetVideoInfosResponse getVideoInfos(@Valid @RequestBody AliVodGetVideoInfosRequest request) {
        GetVideoInfosResponse aliRes = vodClient.getVideoInfos(request.getVideoIds());
        AliVodGetVideoInfosResponse res = new AliVodGetVideoInfosResponse();

        List<AliVodGetVideoInfosResponse.Video> videos = new ArrayList<>();
        videos.addAll(aliRes.getNonExistVideoIds().stream().map(nev -> {
            AliVodGetVideoInfosResponse.Video v = new AliVodGetVideoInfosResponse.Video();
            v.setVideoId(nev);
            v.setExist(false);
            return v;
        }).collect(Collectors.toList()));

        videos.addAll(aliRes.getVideoList().stream().map(vi -> {
            AliVodGetVideoInfosResponse.Video v = BeanUtils.instantiate(AliVodGetVideoInfosResponse.Video.class, vi,
                "creationTime",
                "duration",
                "modificationTime"
            );
            try {
                v.setCreationTime(DateUtils.parseDate(vi.getCreationTime(), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
                v.setModificationTime(DateUtils.parseDate(vi.getModificationTime(), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            v.setCoverUrl(vi.getCoverURL());
            v.setDuration(vi.getDuration().longValue());
            return v;
        }).collect(Collectors.toList()));

        res.setVideos(videos);
        return res;
    }

    @Override
    public AliVodClassifyVideosResponse classify(@Valid @RequestBody AliVodClassifyVideosRequest request) {
        // 解析category， 按照 '/' 分割层级
        String[] categoryNames = StringUtils.split(request.getCategoryCode(), "/");
        // 维护阿里云vod分类(阿里云vod目前只支持3级分类，多余的丢弃)
        Long categoryId = -1L;
        int level = 1;
        for (String categoryName : categoryNames) {
            if (level > 3) {
                break;
            }
            if (StringUtils.isBlank(categoryName)) {
                continue;
            }

            categoryId = getCategoryId(categoryId, categoryName);
            level++;
        }

        // 设置视频分类
        UpdateVideoInfosResponse aliResult = vodClient.updateVideosCategory(request.getVideoIds(), categoryId);

        AliVodClassifyVideosResponse response = new AliVodClassifyVideosResponse();
        response.setNonExistVideoIds(aliResult.getNonExistVideoIds());
        response.setForbiddenVideoIds(aliResult.getForbiddenVideoIds());
        return response;
    }

    private Long getCategoryId(Long parentId, String categoryName) {
        GetCategoriesResponse aliResult = vodClient.getCategories(parentId);
        GetCategoriesResponse.Category category = aliResult.getSubCategories().stream()
                .filter(c -> c.getCateName().equals(categoryName))
                .findFirst()
                .orElse(null);

        if (category != null) {
            return category.getCateId();
        } else {
            // 创建分类
            AddCategoryResponse response = vodClient.addCategory(parentId, categoryName);
            return response.getCategory().getCateId();
        }
    }

    @Override
    public AliVodVideoPlayAuth createVideoPlayAuth(@Valid @RequestBody AliVodCreateVideoPlayAuthRequest request) {
        GetVideoPlayAuthResponse aliResult = vodClient.playAuth(request.getVideoId());

        AliVodVideoPlayAuth playAuth = new AliVodVideoPlayAuth();
        playAuth.setPlayAuth(aliResult.getPlayAuth());
        playAuth.setVideoMeta(BeanUtils.instantiate(AliVodVideoPlayAuth.VideoMeta.class, aliResult.getVideoMeta(), "duration"));
        playAuth.getVideoMeta().setDuration(aliResult.getVideoMeta().getDuration().longValue());

        return playAuth;
    }

    @Override
    public AliVodVideoPlayInfo getVideoPlayInfo(@Valid @RequestBody AliVodGetVideoPlayInfoRequest request) {
        GetPlayInfoResponse aliResult = vodClient.playInfo(request.getVideoId());
        AliVodVideoPlayInfo response = new AliVodVideoPlayInfo();

        AliVodVideoPlayInfo.VideoMeta videoMeta = new AliVodVideoPlayInfo.VideoMeta();
        BeanUtils.copyProperties(aliResult.getVideoBase(), videoMeta, "creationTime", "duration");
        try {
            videoMeta.setCreationTime(DateUtils.parseDate(aliResult.getVideoBase().getCreationTime(), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        videoMeta.setDuration(new Float(aliResult.getVideoBase().getDuration()).longValue());
        response.setVideoMeta(videoMeta);

        response.setPlayInfos(aliResult.getPlayInfoList().stream()
                .map(pi -> {
                    AliVodVideoPlayInfo.PlayInfo info = new AliVodVideoPlayInfo.PlayInfo();
                    BeanUtils.copyProperties(pi, info, "duration", "encrypt");
                    info.setDuration(new Float(pi.getDuration()).longValue());
                    info.setEncrypt(pi.getEncrypt() == 1);
                    return info;
                })
                .collect(Collectors.toList())
        );

        return response;
    }
}
