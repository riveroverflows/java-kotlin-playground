package com.rofs.spring;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;

@SpringBootApplication
public class SpringSecurityJwtApplication {

    public static void main(String[] args) {
        generateSecretKey();
        SpringApplication.run(SpringSecurityJwtApplication.class, args);
    }

    private static void generateSecretKey() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        String secretKey = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Generated Secret Key: " + secretKey);
    }

}
