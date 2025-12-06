package MemberService.MemberService.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private final String secret = "thisois2aweeryestrongesecretekeyeforehmac256ealgorithm";

    public String generateToken(String userId){
      return Jwts.builder()
              .setSubject(userId)
              .setIssuedAt(new Date())
              .setExpiration(new Date(System.currentTimeMillis()+86400000))
              .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
              .compact();
    }

    public String extractUserId(String token){
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
