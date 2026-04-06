package com.bupt.ta.service;

import com.bupt.ta.model.User;
import com.bupt.ta.repository.UserRepository;
import com.bupt.ta.util.PasswordUtil;

import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean setActive(UUID userId, boolean active) {
        return userRepository.setActive(userId, active);
    }

    /**
     * 新增：核心登录验证逻辑
     * @param email 用户邮箱
     * @param plainPassword 用户输入的明文密码
     * @return 如果验证成功，返回包含 User 的 Optional；如果失败（邮箱不存在或密码错误），返回空的 Optional
     */
    public Optional<User> authenticate(String email, String plainPassword) {
        // 1. 先通过邮箱去数据库找人
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // 2. 如果人存在
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 3. 检查账号是否被禁用 (对应 User 类里的 active 字段)
            if (!user.isActive()) {
                return Optional.empty(); // 账号被停用，不让登录
            }

            // 4. 验证密码！将用户输入的明文密码加密，与数据库里的 hash 值比对
            //String hashedInputPassword = PasswordUtil.hashPassword(plainPassword);
            if (PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            //if (hashedInputPassword.equals(user.getPasswordHash())) {
                return Optional.of(user); // 密码正确，返回用户对象
            }
        }
        
        // 邮箱不存在，或者密码错误，都统一返回空（为了安全，不告诉用户具体是哪一个错了）
        return Optional.empty();
    }
}