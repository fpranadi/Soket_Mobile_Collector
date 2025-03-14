package com.soket.soketmobilecollector;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JWTUtils {
    private static final String SECRET_KEY = "db35Isj+BXE9NZDy0t8W3TcNekrF+2d/S0k3tM0b1l3T3ll3r280220258abWvB1GlOgJuQZdcF2Luqm/hccMw--"; // Ganti dengan secret key yang aman
    //private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 jam
    private static final long EXPIRATION_TIME = 1000 * 15 ; // 15 detik

    public static String generateToken(String institutionCode, String identity, String hashKey, String AndroidId) {
        return JWT.create()
                .withSubject(institutionCode)
                .withClaim("identity", identity)  // Claim untuk email
                .withClaim("hashkey", hashKey)    // Claim untuk role user
                .withClaim("androidid", AndroidId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token); // Mengembalikan objek DecodedJWT jika valid
        } catch (JWTVerificationException e) {
            return null; // Token tidak valid
        }
    }
}


//cara pakai
/*

String token = JwtUtils.generateToken("user123", "user@example.com", "admin");
Log.d("JWT", "Token: " + token);
 */

//ambil jwt
/*

DecodedJWT decodedJWT = JwtUtils.verifyToken(token);
if (decodedJWT != null) {
    String username = decodedJWT.getSubject();  // Ambil username
    String email = decodedJWT.getClaim("email").asString(); // Ambil email
    String role = decodedJWT.getClaim("role").asString();  // Ambil role
    Log.d("JWT", "Username: " + username + ", Email: " + email + ", Role: " + role);
} else {
    Log.d("JWT", "Token tidak valid!");
}

 */
