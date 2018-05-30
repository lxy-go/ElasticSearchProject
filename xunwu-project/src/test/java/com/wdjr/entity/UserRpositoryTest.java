package com.wdjr.entity;

import com.wdjr.ApplicationTests;
import com.wdjr.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRpositoryTest extends ApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindOne(){
        User user = userRepository.findOne(1L);
        System.out.println(user);
    }
}
