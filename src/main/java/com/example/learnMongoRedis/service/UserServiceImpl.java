package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.model.User;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.repository.UserRepository;
import com.example.learnMongoRedis.global.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getUserList() {
        return userRepository.findAll();
    }

    @Override
    public User getUserInfo(String id) {
        return userRepository.findById(id).orElseThrow(() -> new AppError.Expected.EntityNotFoundException("찾는 아이디가 존재하지 않습니다."));
    }

    @Override
    public User login(String id, String password) {
        User user = userRepository.findByLoginId(id);
        if(user == null) throw new AppError.Expected.EntityNotFoundException("찾는 아이디가 없습니다.");
        if(!user.getPassword().equals(password)) throw new AppError.Expected.ValidationFailedException("비밀번호가 다릅니다.");
        return user;
    }

    @Override
    public UserDetails getUserDetail(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppError.Unexpected.UnauthorizedException("토큰 정보와 일치하는 유저정보가 없습니다."));
        return new CustomUserDetails(user);
    }

}