package APIGateway.APIGateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JWTUtil {
    private final String secret = "thisois2aweeryestrongesecretekeyeforehmac256ealgorithm";

    public String extractUserId(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody()
                .getSubject();
    }
}
