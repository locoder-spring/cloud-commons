package icu.lowcoder.spring.cloud.content.controller.management;

import icu.lowcoder.spring.cloud.content.config.ContentServiceProperties;
import icu.lowcoder.spring.cloud.content.dao.BannerRepository;
import icu.lowcoder.spring.cloud.content.dao.BannerTagRepository;
import icu.lowcoder.spring.cloud.content.dto.request.BannerQueryRequest;
import icu.lowcoder.spring.cloud.content.dto.request.BannerRequest;
import icu.lowcoder.spring.cloud.content.dto.response.BannerImageResponse;
import icu.lowcoder.spring.cloud.content.dto.response.BannerJumpResponse;
import icu.lowcoder.spring.cloud.content.dto.response.BannerResponse;
import icu.lowcoder.spring.cloud.content.entity.Banner;
import icu.lowcoder.spring.cloud.content.entity.BannerImage;
import icu.lowcoder.spring.cloud.content.entity.BannerJump;
import icu.lowcoder.spring.cloud.content.entity.BannerTag;
import icu.lowcoder.spring.cloud.content.feign.CommonsFilesClient;
import icu.lowcoder.spring.cloud.content.feign.model.FileAcl;
import icu.lowcoder.spring.cloud.content.feign.model.FileDescription;
import icu.lowcoder.spring.cloud.content.service.management.BannerManagerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: yanhan
 * @Description:
 * @Date: create in 2021/3/1 1:48 下午
 */
@RestController
public class BannerManagementController implements BannerManagerService {

    @Autowired
    private BannerTagRepository bannerTagRepository;
    @Autowired
    private BannerRepository bannerRepository;
    @Autowired
    private CommonsFilesClient commonsFilesClient;
    @Autowired
    private ContentServiceProperties contentServiceProperties;

    @Override
    @Transactional
    public UUID insertBanner(@Valid @RequestBody  BannerRequest request) {

        if (request.getPeriods()==null||request.getPeriods().length!=2){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        Banner banner = new Banner();
        BeanUtils.copyProperties(request, banner, "tags");
        banner.getTags().addAll(processTags(request.getTags()));

        BannerImage image = new BannerImage();
        BeanUtils.copyProperties(request.getImage(),image);

        // acl
        boolean isPublicRead = request.getTags().stream().anyMatch(tag -> contentServiceProperties.getPublicReadContentTags().contains(tag));
        List<FileDescription> fileDescriptions = commonsFilesClient.saveTmpFile(new UUID[]{image.getFileId()}, "content/banners", isPublicRead ? FileAcl.PUBLIC_READ : null);

        image.setPath(fileDescriptions.get(0).getPath());
        image.setName(fileDescriptions.get(0).getName());

        banner.setImage(image);
        image.setBanner(banner);



        if (request.getJump()!=null){
            BannerJump jump = new BannerJump();
            BeanUtils.copyProperties(request.getJump(),jump);
            banner.setJump(jump);
            jump.setBanner(banner);
        }

        try {
            banner.setBeginDate(new Date(request.getPeriods()[0]));
            banner.setEndDate(new Date(request.getPeriods()[1]));
        } catch (Exception e){
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"开始结束时间转换错误");
        }


        return bannerRepository.save(banner).getId();
    }

    @Override
    @Transactional
    public UUID updateBanner(@Valid @RequestBody BannerRequest request, UUID bannerId) {
        Optional<Banner> bannerOptional = bannerRepository.findById(bannerId);
        Banner banner = bannerOptional.get();
        BeanUtils.copyProperties(request, banner, "tags");


        BannerImage image = banner.getImage();
        //是否更改图片
        boolean isUp = false;
        if (image==null){
            image = new BannerImage();
            image.setBanner(banner);
            isUp = true;
        }else{
            if (!image.getFileId().equals(request.getImage().getFileId())){
                isUp = true;
            }
        }
        BeanUtils.copyProperties(request.getImage(),image);
        if (isUp){
            // acl
            boolean isPublicRead = request.getTags().stream().anyMatch(tag -> contentServiceProperties.getPublicReadContentTags().contains(tag));
            List<FileDescription> fileDescriptions = commonsFilesClient.saveTmpFile(new UUID[]{image.getFileId()}, "content/banners", isPublicRead ? FileAcl.PUBLIC_READ : null);

            image.setPath(fileDescriptions.get(0).getPath());
            image.setName(fileDescriptions.get(0).getName());
        }
        banner.setImage(image);

        if (request.getJump()!=null){
            BannerJump jump = banner.getJump();
            if (jump==null){
                jump = new BannerJump();
                jump.setBanner(banner);
            }
            BeanUtils.copyProperties(request.getJump(),jump);
            banner.setJump(jump);
        }else{
            banner.setJump(null);
        }

        try {
            banner.setBeginDate(new Date(request.getPeriods()[0]));
            banner.setEndDate(new Date(request.getPeriods()[1]));
        } catch (Exception e){
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"开始结束时间转换错误");
        }

        return bannerRepository.save(banner).getId();
    }

    @Override
    public Page<BannerResponse> list(BannerQueryRequest request,@PageableDefault Pageable pageable) {


        return bannerRepository.findAll((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if(!StringUtils.isEmpty(request.getKeyword())){
                predicateList.add(cb.like(root.get("name"), "%" + request.getKeyword() + "%"));
            }

            if (request.getTags()!=null&&request.getTags().size()>0){
                predicateList.add(root.join("tags").get("name").in(request.getTags()));
            }
            return  cb.and(predicateList.toArray(new Predicate[0]));
        },pageable).map(e->{
            BannerImageResponse image = null;
            if (e.getImage()!=null){
                image = BannerImageResponse
                        .builder()
                        .id(e.getImage().getId())
                        .path(e.getImage().getPath())
                        .name(e.getImage().getName())
                        .fileId(e.getImage().getFileId())
                        .build();
            }
            BannerJumpResponse jump = null;
            if (e.getJump()!=null){
                jump = BannerJumpResponse
                        .builder()
                        .type(e.getJump().getType())
                        .params(e.getJump().getParams())
                        .build();
            }

            return BannerResponse
                    .builder()
                    .id(e.getId())
                    .name(e.getName())
                    .enabled(e.isEnabled())
                    .periods(new Date[]{e.getBeginDate(),e.getEndDate()})
                    .sequence(e.getSequence())
                    .image(image)
                    .jump(jump)
                    .build();
        });
    }

    @Override
    @Transactional
    public UUID enabled(@RequestBody BannerRequest request, UUID bannerId) {

        if (request.getEnabled()==null){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"请填写启用参数");
        }
        Optional<Banner> bannerOptional = bannerRepository.findById(bannerId);
        Banner banner = bannerOptional.get();
        banner.setEnabled(request.getEnabled());
        return bannerRepository.save(banner).getId();
    }


    private List<BannerTag> processTags(Set<String> tagNames) {
        tagNames = tagNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<BannerTag> tags = bannerTagRepository.findAllByNameIn(tagNames);
        // new
        List<BannerTag> newTags = tagNames.stream()
                .filter(name -> tags.stream().noneMatch(t -> t.getName().equals(name)))
                .map(name -> {
                    BannerTag tag = new BannerTag();
                    tag.setName(name);
                    return tag;
                })
                .collect(Collectors.toList());
        if (!newTags.isEmpty()) {
            bannerTagRepository.saveAll(newTags);
            tags.addAll(newTags);
        }

        return tags;
    }
}
