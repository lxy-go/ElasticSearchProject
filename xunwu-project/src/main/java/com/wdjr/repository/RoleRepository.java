package com.wdjr.repository;

import com.wdjr.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<Role,Long> {
    List<Role> findRoleByUserId(Long userId);
}
