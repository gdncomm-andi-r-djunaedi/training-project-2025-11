package com.marketplace.member.seeder;

import com.marketplace.member.entity.SystemConfig;
import com.marketplace.member.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder for system configuration including RSA keys.
 * Runs before MemberSeeder to ensure keys are available.
 */
@Slf4j
@Component
@Order(1) // Run before MemberSeeder
@RequiredArgsConstructor
public class SystemConfigSeeder implements CommandLineRunner {

    private final SystemConfigService systemConfigService;

    // Default RSA keys - in production, generate new keys and inject via environment
    private static final String DEFAULT_PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnUcdaQBpzumEFFSyPuhZ
            bmo4AwnRsi+1RwbzlSmkPa3FXRCuHduYXtp1wQcIk+ICzC02sengnSFP3lLrPz3s
            co3ch2f/9q6ulkiF3VU8wQmSIXd04PqY7E2nBjF4gYCteWrtQ1qP5VVPBwMVQRsZ
            E3uMQZuIOUp/9ppfVtgMydGGJHSqSdE/dq/3ZWhkBGL/ipEYkuoyKbF/hYetfkVK
            5t+fJsHwrATAzFo8FCe/OyLUOF8ZPrEE53efUiwAgN54/76FaewmpWgJg9MRsfGA
            TuwVxR5EOlJ+MTNPUljr7SvntQ68LijYjpE5FzsFUXm6HZjdKxwLDEE/vjMNz4NR
            rwIDAQAB
            -----END PUBLIC KEY-----""";

    private static final String DEFAULT_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCdRx1pAGnO6YQU
            VLI+6FluajgDCdGyL7VHBvOVKaQ9rcVdEK4d25he2nXBBwiT4gLMLTax6eCdIU/e
            Uus/PexyjdyHZ//2rq6WSIXdVTzBCZIhd3Tg+pjsTacGMXiBgK15au1DWo/lVU8H
            AxVBGxkTe4xBm4g5Sn/2ml9W2AzJ0YYkdKpJ0T92r/dlaGQEYv+KkRiS6jIpsX+F
            h61+RUrm358mwfCsBMDMWjwUJ787ItQ4Xxk+sQTnd59SLACA3nj/voVp7CalaAmD
            0xGx8YBO7BXFHkQ6Un4xM09SWOvtK+e1DrwuKNiOkTkXOwVRebodmN0rHAsMQT++
            Mw3Pg1GvAgMBAAECggEASHFJpCQ7GEGqAx1R7rTiAIAvZYLf0iW0Z09V4fZIvtos
            BqZYtMsBf2s7gtoFAuy+4iHPSBqUSXvfCGYEEmCo6Mjb84idNxgVtYYZFq4cqkOI
            yh52OOxkhK9tx+YAfhHxlXQBbh0oG3d0S6QJZbAgGD+3hj6n+8+LamCHmn4wQcKl
            aI1O5CxkCKR6ixAO6Ceev6ILBL5jIH25AO2qG+AblP0u+WFETceS5BR0inac0ESl
            jOjMH7K4N5yTmMLoNLiR2ZFRZ44+42/iTg+0CHU9wOtef6PJCOtc//xPVZCQfJMf
            g+OWCwiRNEuvd4QmCAV9JJc4zZZ2SgDHt6d8JyjNSQKBgQDbIEGU2i5qZmRqDl/z
            Ed/Xr0Q8m+4boiyb7j+zXi4kNWLPCK0KsrbADv/DPNcg2Vo/HL1rnpu3VVMVon7u
            feWqux83K47AloHRHjxzfA8FCU0weB266HKlFK40KU4quuHTHVJqgJZnAFbrepiY
            7DQwLRLYElyL6dAj7854QRkNJwKBgQC3vn7c25rGIQb7BPYDWb4KqPTzwTSpy9YA
            e6zjNdzRT0ehoFISWSdTW0Jsmq+LR8hFLGc72ozFJiB9pj3IpznulzsCictb130p
            EY7iBSTwJFQ5gB7t77H6Jh8h66FpjWiL2NPkONO2bG3gM6boxVO+EbaqZNaMR5/e
            bJJSYDv8OQKBgGOKbKA82Lb22oifDREzPncRJDNQNkMRUahn+0H094r1QXSBIXJp
            qsDmT2MWYTeH5QokeRvJSj1nIj4CuhyGzmzXYh/Cxq9P75raXGWtpnkIN6tb9U1x
            yJqbIWOKm/qnOGyZtlWxIiGaMuH3qBhXvIFiCFxlQutABJA+oJFaKRIjAoGAQkMO
            WPNRA3ZRoCCP33FzmUCI5YadSUZa7F2tYVrBQTJH8L9yGW/RQLw+XhACkkXWsSts
            JyePcGFpgiZ7TWsQ9zvOagHrNjmlxzOvxU55nahcP0g23zN7iWxJ+d+RnBEEzLnq
            3/imULkVfq7h2DuhEYAt0ZD/1iLWQk4BDnfp6fECgYBTm7cAXoF2Go3qRRBx61EY
            03g7leEMUC1y6gq7gIfTDIN1T5aWyyM/XtXXe8Y0TcjtIuxIxfhluyMc1K1CMqZT
            UFQo6aFGN7EknCC61c16xamijpCc7vxuscznBCwRxLZFnbGHYHOss1JDHVW9HF2w
            Po4brvYVBi82f1SgzV1sYA==
            -----END PRIVATE KEY-----""";

    @Override
    public void run(String... args) {
        seedJwtKeys();
    }

    private void seedJwtKeys() {
        // Seed public key if not exists
        if (!systemConfigService.configExists(SystemConfig.JWT_PUBLIC_KEY)) {
            systemConfigService.saveConfig(
                    SystemConfig.JWT_PUBLIC_KEY,
                    DEFAULT_PUBLIC_KEY,
                    "RSA Public Key for JWT token verification. Can be shared with other services."
            );
            log.info("JWT Public Key seeded to database");
        } else {
            log.info("JWT Public Key already exists in database");
        }

        // Seed private key if not exists
        if (!systemConfigService.configExists(SystemConfig.JWT_PRIVATE_KEY)) {
            systemConfigService.saveConfig(
                    SystemConfig.JWT_PRIVATE_KEY,
                    DEFAULT_PRIVATE_KEY,
                    "RSA Private Key for JWT token signing. KEEP SECRET - only member-service should access this."
            );
            log.info("JWT Private Key seeded to database");
        } else {
            log.info("JWT Private Key already exists in database");
        }
    }
}

