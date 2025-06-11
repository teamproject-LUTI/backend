package com.luti.auth.dto;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

//JWT 발급과 검증을 담당할 클래스
@Component
public class JWTUtil {

    //JWT를 암호화하거나 검증할 때 사용하는 SecretKey 객체
    private SecretKey secretKey;

    //application.properties에서 지정한 값을 주입받음
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        //문자열로 받은 secret을 바이트 배열로 변환하여 HMAC SHA256 알고리즘용 SecretKey 생성
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //토큰에서 loginId 값 추출
    public String getLoginId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) //서명을 검증하기 위해 비밀 키를 설정
                .build()
                .parseSignedClaims(token) //서명된 토큰을 파싱하여 claims 객체 반환
                .getPayload()
                .get("loginId", String.class); //payload에서 loginId를 String 타입으로 추출
    }

    //토큰에서 userType값 추출
    public String getUserType(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userType", String.class);
    }

    //JWT의 만료 여부 확인
    public Boolean isExpired(String token) {
        //토큰의 만료 시간을 가져오고, 현재 시간(new Date())과 비교해 만료됐는지 boolean으로 반환
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    //JWT를 생성하는 메서드
    public String createJwt(String loginId, String userType, Long expiredMs) {
        return Jwts.builder()
                .claim("loginId", loginId) //JWT에 추가할 사용자 정의 클레임
                .claim("userType", userType) //JWT에 추가할 사용자 정의 클레임
                .issuedAt(new Date(System.currentTimeMillis())) //토큰 발급 시간 설정
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) //토큰 만료 시간 설정
                .signWith(secretKey) //지정한 비밀 키로 HMAC 서명
                .compact(); //최종적으로 JWT 문자열 생성
    }
}
