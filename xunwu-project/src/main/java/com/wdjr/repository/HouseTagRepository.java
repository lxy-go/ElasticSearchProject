package com.wdjr.repository;

import com.wdjr.entity.HouseDetail;
import com.wdjr.entity.HousePicture;
import com.wdjr.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseTagRepository extends CrudRepository<HouseTag,Long> {

    List<HouseTag> findAllByHouseId(Long id);

    HouseTag findByNameAndHouseId(String tag, Long houseId);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
