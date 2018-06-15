package com.wdjr.service.house;

import com.google.common.collect.Maps;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.wdjr.base.HouseSort;
import com.wdjr.base.HouseStatus;
import com.wdjr.base.LoginUserUtil;
import com.wdjr.dto.HouseDTO;
import com.wdjr.dto.HouseDetailDTO;
import com.wdjr.dto.HousePictureDTO;
import com.wdjr.entity.*;
import com.wdjr.form.DataTableSearch;
import com.wdjr.form.HouseForm;
import com.wdjr.form.PhotoForm;
import com.wdjr.form.RentSearch;
import com.wdjr.repository.*;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.ServiceResult;
import com.wdjr.service.search.ISearchService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private IQiNiuService qiNiuService;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private HousePictureRepository housePictureRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    HouseRepository houseRepository;

    @Autowired
    ISearchService searchService;

    @Value("${qiniu.cdn.prefix}")
    private String cdnPreifx;

    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail detail = new HouseDetail();

        ServiceResult<HouseDTO> subwayValidtorResult = wrapperDetailInfo(detail, houseForm);
        //因为最终结果是返回null,所以如果报错，返回对应的result
        if (subwayValidtorResult != null){
            return subwayValidtorResult;
        }
        //房屋信息
        House house = new House();
        modelMapper.map(houseForm, house);
        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        house = houseRepository.save(house);
        //房屋描述信息
        detail.setHouseId(house.getId());
        HouseDetail houseDetail = houseDetailRepository.save(detail);
        //房屋图片信息
        List<HousePicture> pictures = generatorPictures(houseForm, house.getId());
        Iterable<HousePicture> housePictures = housePictureRepository.save(pictures);
        //house保存信息到HouseDTO
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        //detail保存信息到HouseDetailDTO
        HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
        //将detail注入到house
        houseDTO.setHouseDetail(detailDTO);
        //picture注入到PictureDTO
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        pictureDTOS.forEach(housePicture->pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class) ));
        //保存图片+封面信息到houseDTO
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPreifx+houseDTO.getCover());
        List<String> tags = houseForm.getTags();

        if(tags!=null || !tags.isEmpty()){
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(),tag) );
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null,houseDTO);
    }

    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        House house = houseRepository.findOne(houseForm.getId());
        if (house == null){
            return ServiceResult.notFound();
        }
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId());
        if (houseDetail == null){
            return ServiceResult.notFound();
        }
        ServiceResult<HouseDTO> wrapperResult = wrapperDetailInfo(houseDetail, houseForm);
        if (wrapperResult!=null){
            return wrapperResult;
        }
        houseDetailRepository.save(houseDetail);
        List<HousePicture> pictures = generatorPictures(houseForm, houseForm.getId());
        housePictureRepository.save(pictures);

        if (houseForm.getCover() == null){
            houseForm.setCover(house.getCover());
        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);
        //如果状态通过 构建es的索引
        if (house.getStatus() == HouseStatus.PASSES.getValue()){
            searchService.index(house.getId());
        }
        return ServiceResult.success();
    }


    /**
     * 图片保存
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatorPictures(HouseForm form,Long houseId){
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos()==null || form.getPhotos().isEmpty()){
            return pictures;
        }
        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPreifx);
            picture.setPath(photoForm.getPath());
            picture.setHeight(photoForm.getHeight());
            picture.setWidth(photoForm.getWidth());
            pictures.add(picture);
        }
        return pictures;
    }

    /**
     * 房屋描述信息的对象填充
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail,HouseForm houseForm){
        //地铁线数据库有没有
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());

        if (subway == null){
            return new ServiceResult<>(false,"Not Valid subway line");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null){
            return new ServiceResult<>(false, "Not Valid SubwayStation");
        }
        //地铁线id+name
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());
        //地铁站id+name
        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());
        //其他属性信息，直接在HouseForm的表单中获取
        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;

    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();
        //排序规则
        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()),searchBody.getOrderBy());
        //有几页
        int page = searchBody.getStart() / searchBody.getLength();

        Pageable pageable = new PageRequest(page,searchBody.getLength(),sort);
        //java8的lambda表达式
        Specification<House> specification = (root,query,cb)->{
            //条件：查询自己的房源，不是所有用户的
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());
            predicate = cb.and(predicate,cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));
            if(searchBody.getCity() != null){
                predicate = cb.and(predicate,cb.equal(root.get("cityEnName"), searchBody.getCity()));
            }
            if (searchBody.getStatus() != null){
                predicate = cb.and(predicate,cb.equal(root.get("status"), searchBody.getStatus()));
            }

            if (searchBody.getCreateTimeMin() != null){
                predicate = cb.and(predicate,cb.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
            }
            if (searchBody.getCreateTimeMax() != null){
                predicate = cb.and(predicate,cb.lessThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMax()));
            }
            if (searchBody.getTitle() != null){
                predicate = cb.and(predicate,cb.like(root.get("title"), "%"+searchBody.getTitle()+"%"));
            }
            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification,pageable);

        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPreifx+house.getCover());
            houseDTOS.add(houseDTO );
        });
        return new ServiceMultiResult<>(houses.getTotalElements(),houseDTOS);
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRepository.findOne(id);
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId());
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);

        List<HousePicture> pictures = housePictureRepository.findAllByHouseId(house.getId());
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO );
        }

        List<HouseTag> tags = houseTagRepository.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();

        for (HouseTag tag : tags) {
            tagList.add(tag.getName() );
        }

        houseDTO.setHouseDetail(houseDetailDTO);
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setTags(tagList);

        return ServiceResult.of(houseDTO);
    }

    /**
     * 删除图片接口实现
     * @param id
     * @return
     */
    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture picture = this.housePictureRepository.findOne(id);
        if (picture==null){
            return ServiceResult.notFound();
        }
        try {
            Response response = this.qiNiuService.delete(picture.getPath());
            if (response.isOK()){
                housePictureRepository.delete(id);
                return ServiceResult.success();
            }else{
                return new ServiceResult(false, response.error);
            }

        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    /**
     * 更新封面
     * @param coverId
     * @param targetId
     * @return
     */
    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture cover = housePictureRepository.findOne(coverId);
        if (cover == null){
            return ServiceResult.notFound();
        }
        houseRepository.updateCover(targetId,cover.getPath());
        return ServiceResult.success();
    }

    /**
     * 添加标签
     * @param houseId
     * @param tag
     * @return
     */
    @Override
    @Transactional
    public ServiceResult addTag(Long houseId, String tag) {
        House house = houseRepository.findOne(houseId);
        if (house == null){
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag,houseId);
        if (houseTag != null){
            return new ServiceResult(false, "标签已存在");
        }
        houseTagRepository.save(new HouseTag(houseId,tag));
        return ServiceResult.success();
    }

    /**
     * 删除标签
     * @param houseId
     * @param tag
     * @return
     */
    @Override
    @Transactional
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseRepository.findOne(houseId);
        if (house == null){
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag == null){
            return new ServiceResult(false, "标签不存在");
        }
        houseTagRepository.delete(houseTag.getId());
        return ServiceResult.success();

    }

    @Override
    @Transactional
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseRepository.findOne(id);
        if (house == null){
            return ServiceResult.notFound();
        }
        if (house.getStatus() == status){
            return new ServiceResult(false, "状态没有发生变化");
        }
        if (house.getStatus()== HouseStatus.RENTED.getValue()){
            return new ServiceResult(false, "以出租房屋不允许修改状态");
        }
        if (house.getStatus() == HouseStatus.DELETED.getValue()){
            return new ServiceResult(false, "以删除的资源不允许操作");
        }
        houseRepository.updateStatus(id,status);
        //上架更新索引，其他都要删除索引
        if (status == HouseStatus.PASSES.getValue()){
            searchService.index(id);
        }else{
            searchService.remove(id);
        }

        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        //默认排序
        //Sort sort = new Sort(Sort.Direction.DESC,"lastTime");
        //多维度排序
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();

        Pageable pageable = new PageRequest(page, rentSearch.getSize(), sort);

        Specification<House> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES.getValue());

            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();


        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPreifx + house.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });


        wrapperHouseList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染详细信息 及 标签
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }

}
