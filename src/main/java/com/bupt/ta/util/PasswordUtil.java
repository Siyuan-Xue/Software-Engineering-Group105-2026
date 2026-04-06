package com.bupt.ta.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * 加密：将明文密码变为 BCrypt 哈希值（用于注册、重置密码等操作）
     */
    public static String hashPassword(String plainPassword) {
        // gensalt() 默认生成 cost=10 的盐
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 校验：验证输入的明文密码与数据库中的哈希值是否匹配
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // 如果 hashedPassword 的格式不是合法的 BCrypt，会抛出此异常
            return false;
        }
    }

    public static void main(String[] args) {
        // 生成 123456 的 BCrypt 哈希值
        System.out.println(hashPassword("123456"));
    }
}