package com.wdjr.repository;

import com.wdjr.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SupportAddressRepository extends CrudRepository<SupportAddress,Long> {
    /**
     * 获取所有的对应的行政级别的信息
     */

    List<SupportAddress> findAllByLevel(String level);

    SupportAddress findByEnNameAndLevel(String cityName,String level);

    SupportAddress findByEnNameAndBelongTo(String cityEnName,String belongTo);

    List<SupportAddress> findAllByLevelAndBelongTo(String enName,String belongTo);
}
