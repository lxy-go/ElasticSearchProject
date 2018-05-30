package com.wdjr.repository;

import com.wdjr.entity.Subway;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubwayRepository extends CrudRepository<Subway,Long> {
    /**
     * 根据城市英文名称获取所有地铁
     * @param cityEnName
     * @return
     */
    List<Subway> findAllByCityEnName(String cityEnName);
}
