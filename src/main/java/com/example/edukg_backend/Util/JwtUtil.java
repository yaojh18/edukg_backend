package com.example.edukg_backend.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@PropertySource(value="classpath:config/JwtConfig.properties")
public class JwtUtil {
    @Value("${expire}")
    private Long expire;
    @Value("${secret}")
    private String secret;

    public String createToken(String id){
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(id)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims getTokenClaim(String token){
        try{
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        }catch(Exception e){
            return null;
        }
    }
}
