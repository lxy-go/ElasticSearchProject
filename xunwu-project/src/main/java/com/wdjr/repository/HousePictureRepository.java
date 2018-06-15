package com.wdjr.repository;

import com.wdjr.entity.HouseDetail;
import com.wdjr.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HousePictureRepository extends CrudRepository<HousePicture,Long> {

    List<HousePicture> findAllByHouseId(Long id);
}
