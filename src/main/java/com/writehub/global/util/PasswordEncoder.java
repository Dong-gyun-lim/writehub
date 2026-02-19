package com.writehub.global.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    // 비밀번호 암호화
    public static String encode(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // 비밀번호 검증
    public static boolean matches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
