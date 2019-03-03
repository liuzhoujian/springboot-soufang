package com.lzj.soufang.service.house;

import com.google.common.collect.Maps;
import com.lzj.soufang.base.ApiResponse;
import com.lzj.soufang.base.HouseSort;
import com.lzj.soufang.base.HouseStatus;
import com.lzj.soufang.base.LoginUserUtil;
import com.lzj.soufang.entity.*;
import com.lzj.soufang.repository.*;
import com.lzj.soufang.service.ServiceMultiResult;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.web.dto.HouseDTO;
import com.lzj.soufang.web.dto.HouseDetailDTO;
import com.lzj.soufang.web.dto.HousePictureDTO;
import com.lzj.soufang.web.form.DataTableSearch;
import com.lzj.soufang.web.form.HouseForm;
import com.lzj.soufang.web.form.PhotoForm;
import com.lzj.soufang.web.form.RentSearch;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HousePictureRepository housePictureRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private IQiNiuService qiNiuService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;


    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        //Sort sort = new Sort(Sort.Direction.DESC, "lastUpdateTime");//默认按照更新时间降序排列

        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());

        int page = rentSearch.getStart() / rentSearch.getSize();//分页
        Pageable pageable = PageRequest.of(page, rentSearch.getSize(), sort);

        Specification<House> specification = ((root, criteriaQuery, criteriaBuilder) -> {
            //只有状态为PASSED的房源才能被用户查询到
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSED.getCode());
            predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            //有的房源没有距离地铁信息的条件，要排序的话，距离地铁的距离大于-1
            if(HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY),
                        -1));
            }

            return predicate;
        });

        Page<House> houses = houseRepository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();

        //查询房屋详细信息和标签
        List<Integer> idList = new ArrayList<>();
        Map<Integer, HouseDTO> idToHouseDTOMap = Maps.newHashMap();

        houses.forEach(house -> {
            HouseDTO houseDTO = this.modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
            idList.add(houseDTO.getId()); //存储房屋id信息
            idToHouseDTOMap.put(houseDTO.getId(), houseDTO); //将id和houseDTO进行映射存储
        });

        //渲染房源详细信息和标签
        wrapperHouseDTO(idList, idToHouseDTOMap);

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染房源详细信息和标签
     * @param ids
     * @param idToHouseDTOMap
     */
    private void wrapperHouseDTO(List<Integer> ids, Map<Integer, HouseDTO> idToHouseDTOMap) {
        List<HouseDetail> houseDetailList = houseDetailRepository.findAllByHouseIdIn(ids);
        houseDetailList.forEach(houseDetail -> {
            HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            HouseDTO houseDTO = idToHouseDTOMap.get(houseDetail.getHouseId());
            houseDTO.setHouseDetail(houseDetailDTO);
        });

        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(ids);
        houseTags.forEach(houseTag -> {
            HouseDTO houseDTO = idToHouseDTOMap.get(houseTag.getId());
            if(houseDTO != null) {
                houseDTO.getTags().add(houseTag.getName());
            }
        });


    }

    @Override
    @Transactional
    public ServiceResult updateHouseStatus(int houseId, int status) {
       //一系列校验
        House house = houseRepository.findById(houseId).get();
        if(house == null) {
            return ServiceResult.notFound();
        }

        if(house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }

        if(house.getStatus() == HouseStatus.RETENDED.getCode()) {
            return new ServiceResult(false, "已出租的房屋不能更改状态");
        }

        if(house.getStatus() == HouseStatus.DELETED.getCode()) {
            return new ServiceResult(false, "已删除的资源不允许操作");
        }

        //执行更新操作
        houseRepository.updateStatus(houseId, status);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Integer id) {
        //校验photoId
        HousePicture picture = housePictureRepository.findById(id).get();
        if(picture == null) {
            return ServiceResult.notFound();
        }

        //在七牛云上面删除图片信息
        try {
            Response response = qiNiuService.delete(picture.getPath());
            if(response.isOK()) {
                //在数据库删除图片信息
                housePictureRepository.deleteById(id);
                return ServiceResult.success();
            } else {
                return new ServiceResult(false, response.error);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult updateCover(Integer coverId, Integer targetId) {
        HousePicture housePicture = housePictureRepository.findById(coverId).get();
        if(housePicture == null) {
            return ServiceResult.notFound();
        }

        houseRepository.updateCover(targetId, housePicture.getPath());
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Integer houseId, String tag) {
        //验证参数
        House house = houseRepository.findById(houseId).get();
        if(house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagRepository.findByHouseIdAndAndName(houseId, tag);
        if(houseTag != null) {
            return new ServiceResult(false, "该标签已存在！");
        }

        houseTagRepository.save(new HouseTag(houseId, tag));

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removeTag(Integer houseId, String tag) {
        House house = houseRepository.findById(houseId).get();
        if(house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagRepository.findByHouseIdAndAndName(houseId, tag);
        if(houseTag == null) {
            return new ServiceResult(false, "该标签不存在！");
        }

        houseTagRepository.deleteById(houseTag.getId());
        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult<HouseDTO> update(HouseForm houseForm) {
        House house = houseRepository.findById(houseForm.getId()).get();
        if(house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId());
        if(houseDetail == null) {
            return ServiceResult.notFound();
        }

        //封装房源详细信息并校验
        ServiceResult<HouseDTO> result = wrapperDetailInfo(houseForm, houseDetail);
        //说明校验houseDetail有错误
        if(result != null) {
            return result;
        }

        //保存房源详细信息
        houseDetailRepository.save(houseDetail);

        //保存照片信息
        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        housePictureRepository.saveAll(pictures);

        if(houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }

        modelMapper.map(houseForm, house);
        //设置最后更新时间
        house.setLastUpdateTime(new Date());

        //保存房源信息
        houseRepository.save(house);

        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult<HouseDTO> findCompleteOne(Integer id) {
        //查找house表
        House house = houseRepository.findById(id).get();
        if(house == null) {
            return ServiceResult.notFound();
        }

        //查找houseDetail表
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId());

        //查找housePicture表
        List<HousePicture> pictures = housePictureRepository.findByHouseId(house.getId());

        //查找houseTag表
        List<HouseTag> houseTags = houseTagRepository.findByHouseId(house.getId());
        List<String> tags = new ArrayList<>();
        houseTags.forEach(houseTag -> {
            tags.add(houseTag.getName());
        });

        //组装HouseDTO返回前端
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);

        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);

        List<HousePictureDTO> housePictureDTOS = new ArrayList<>();
        pictures.forEach(housePicture -> {
            housePictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class));
        });

        houseDTO.setHouseDetail(houseDetailDTO);
        houseDTO.setPictures(housePictureDTOS);
        houseDTO.setTags(tags);

        return ServiceResult.of(houseDTO);
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody) {
        //定义排序规则
        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()),
                searchBody.getOrderBy());

        //定义分页规则
        int page = searchBody.getStart() / searchBody.getLength(); //第几页
        Pageable pageable = PageRequest.of(page, searchBody.getLength(), sort);

        //定义多条件查询规则
        Specification<House> specification = new Specification<House>() {
            @Override
            public Predicate toPredicate(Root<House> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                //管理员只能浏览自己创建的房源信息
                Predicate predicate = criteriaBuilder.equal(root.get("adminId"), LoginUserUtil.getUserId());
                //过滤掉已经删除的房源信息
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.notEqual(root.get("status"), HouseStatus.DELETED.getCode()));

                //多条件查询拼装
                if(searchBody.getCity() != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), searchBody.getCity()));
                }

                if(searchBody.getStatus() != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), searchBody.getStatus()));
                }

                if(searchBody.getCreateTimeMin() != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
                }

                if(searchBody.getGetCreateTimeMax() != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), searchBody.getGetCreateTimeMax()));
                }

                if (searchBody.getTitle() != null) {
                    //模糊查询
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("title"), "%" + searchBody.getTitle() + "%"));
                }

                return predicate;
            }
        };

        //分页查询
        Page<House> houses = houseRepository.findAll(specification, pageable);

        List<HouseDTO> houseDTOS = new ArrayList<>();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + houseDTO.getCover()); //设置照片回显路径
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    @Override
    @Transactional //开启事务 通过houseFrom向四张表中存储数据
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        //1、验证表单中地铁线路和地铁站并填充房源详细信息
        HouseDetail houseDetail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidtionResult = wrapperDetailInfo(houseForm, houseDetail);
        if(subwayValidtionResult != null) {
            //验证结果不为空，说明验证出错，直接返回错误信息
            return subwayValidtionResult;
        }

        //2、保存房源主体信息
        House house = new House();
        modelMapper.map(houseForm, house);//将表单数据映射到javaBean

        Date date = new Date();
        house.setLastUpdateTime(date);
        house.setCreateTime(date);
        house.setAdminId(LoginUserUtil.getUserId()); //从security中获取登录的用户信息
        house = houseRepository.save(house);


        //3、房源详细信息houseId填充,并保存
        houseDetail.setHouseId(house.getId());
        houseDetailRepository.save(houseDetail);

        //4、房源图片信息保存
        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        List<HousePicture> housePictures = housePictureRepository.saveAll(pictures);

        //5、组装向前端返回的HouseDTO
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        //6、保存标签
        List<String> tags = houseForm.getTags();
        if(tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }

            houseTagRepository.saveAll(houseTags);
            //返给前端
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }

    /**
     * 填充房源详细信息，并对地铁站、地铁线路进行验证
     * @param houseForm
     * @param houseDetail
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseForm houseForm, HouseDetail houseDetail) {
        //校验地铁站、地铁线路
        Subway subway = subwayRepository.findById(houseForm.getSubwayLineId()).get();
        if(subway == null) {
            return new ServiceResult<>(false, "Not Valid Subway Line!");
        }

        SubwayStation subwayStation = subwayStationRepository.findById(houseForm.getSubwayStationId()).get();
        if(subwayStation == null && subwayStation.getSubwayId() != subway.getId()) {
            return new ServiceResult<>(false, "Not Valid Subway Station!");
        }

        //填充房源详细信息
        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setTraffic(houseForm.getTraffic());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());
        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        //校验没错返回null
        return null;
    }


    /**
     * 房源信息列表对象填充
     * @param houseForm
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm houseForm, Integer houseId) {
        List<HousePicture> pictures = new ArrayList<>();

        List<PhotoForm> houseFormPhotos = houseForm.getPhotos();
        if(houseFormPhotos == null || houseFormPhotos.isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : houseFormPhotos) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setPath(photoForm.getPath());
            picture.setHeight(photoForm.getHeight());
            picture.setWidth(photoForm.getWidth());
            picture.setCdnPrefix(cdnPrefix);
            pictures.add(picture);
        }

        return pictures;
    }

}
