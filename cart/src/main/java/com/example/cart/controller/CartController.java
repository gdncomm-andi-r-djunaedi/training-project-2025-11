package com.example.cart.controller;

import com.example.cart.command.*;
import com.example.cart.entity.CartItem;
import com.example.cart.repo.CartItemRepository;
import com.example.cart.service.CartCacheService;
import com.example.commandlib.CommandExecutor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/cart")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    
    private final CartItemRepository repo;
    private final CartCacheService cacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final byte[] secret;
    private final CommandExecutor commandExecutor = new CommandExecutor();

    public CartController(CartItemRepository repo, CartCacheService cacheService, 
                         RedisTemplate<String, Object> redisTemplate,
                         @Value("${jwt.secret}") String secret) {
        this.repo = repo;
        this.cacheService = cacheService;
        this.redisTemplate = redisTemplate;
        this.secret = secret.getBytes();
    }

    @PreDestroy
    public void shutdown() {
        commandExecutor.shutdown();
    }

    // Debug endpoint to test Redis connection
    @GetMapping("/debug/redis")
    public ResponseEntity<?> debugRedis() {
        try {
            // Test Redis connection
            redisTemplate.opsForValue().set("test:ping", "pong");
            Object value = redisTemplate.opsForValue().get("test:ping");
            Set<String> keys = redisTemplate.keys("*");
            redisTemplate.delete("test:ping");
            
            return ResponseEntity.ok(Map.of(
                "status", "connected",
                "testValue", value != null ? value : "null",
                "allKeys", keys != null ? keys : Set.of()
            ));
        } catch (Exception e) {
            log.error("Redis connection error: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    private String usernameFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret)).build().parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    private String extractToken(String authHeader, String cookie) {
        log.debug("Extracting token - authHeader: {}, cookie: {}", authHeader, cookie);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Token extracted from Authorization header");
            return token;
        }
        
        if (cookie != null) {
            for (String c : cookie.split(";")) {
                String t = c.trim();
                if (t.startsWith("JWT-TOKEN=")) {
                    String token = t.substring("JWT-TOKEN=".length());
                    log.debug("Token extracted from cookie");
                    return token;
                }
            }
        }
        
        log.warn("No token found in request");
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "Authorization", required = false) String auth, 
                                  @CookieValue(value = "JWT-TOKEN", required = false) String cookie, 
                                  @RequestBody CartItem req) {
        String token = extractToken(auth, cookie);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "auth required"));
        }
        
        try {
            String username = usernameFromToken(token);
            return commandExecutor.execute(new AddToCartCommand(repo, cacheService, req, username));
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    @GetMapping
    public ResponseEntity<?> view(@RequestHeader(value = "Authorization", required = false) String auth, 
                                   @CookieValue(value = "JWT-TOKEN", required = false) String cookie) {
        String token = extractToken(auth, cookie);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "auth required"));
        }
        
        try {
            String username = usernameFromToken(token);
            return commandExecutor.execute(new ViewCartCommand(repo, cacheService, username));
        } catch (Exception e) {
            log.error("Error viewing cart: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    @DeleteMapping("/remove/{sku}")
    public ResponseEntity<?> remove(@RequestHeader(value = "Authorization", required = false) String auth, 
                                     @CookieValue(value = "JWT-TOKEN", required = false) String cookie, 
                                     @PathVariable String sku) {
        String token = extractToken(auth, cookie);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "auth required"));
        }
        
        try {
            String username = usernameFromToken(token);
            return commandExecutor.execute(new RemoveFromCartCommand(repo, cacheService, username, sku));
        } catch (Exception e) {
            log.error("Error removing from cart: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
