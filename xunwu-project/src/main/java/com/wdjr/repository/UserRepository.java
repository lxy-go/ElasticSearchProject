package com.wdjr.repository;

import com.wdjr.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {

    User findByName(String userName);
}
