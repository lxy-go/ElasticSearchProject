package com.wdjr.service.user;

import com.wdjr.entity.Role;
import com.wdjr.entity.User;
import com.wdjr.repository.RoleRepository;
import com.wdjr.repository.UserRepository;
import com.wdjr.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User findUserName(String userName) {

        User user = userRepository.findByName(userName);
        if(user==null){
            return null;
        }
        List<Role> roles = roleRepository.findRoleByUserId(user.getId());

        if (roles == null || roles.isEmpty()){
            throw  new DisabledException("非法权限");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_"+ role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }
}
