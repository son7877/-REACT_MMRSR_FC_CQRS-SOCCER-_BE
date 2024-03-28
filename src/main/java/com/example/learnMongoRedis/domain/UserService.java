package com.example.learnMongoRedis.domain;

import com.example.learnMongoRedis.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getUserList();
    User getUserInfo(String uid);
    User login(String id, String password);
    UserDetails getUserDetail(String id);
}