package com.marketplace.seeder.runner;

import com.marketplace.seeder.command.SeedMembersCommand;
import com.marketplace.seeder.command.SeedProductsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeederRunner implements CommandLineRunner {

    private final SeedMembersCommand seedMembersCommand;
    private final SeedProductsCommand seedProductsCommand;

    private static final int TARGET_MEMBERS = 5000;
    private static final int TARGET_PRODUCTS = 50000;

    @Override
    public void run(String... args) throws Exception {
        log.info("=".repeat(60));
        log.info("Starting Data Seeder");
        log.info("Target: {} members, {} products", TARGET_MEMBERS, TARGET_PRODUCTS);
        log.info("=".repeat(60));

        long startTime = System.currentTimeMillis();

        // Seed Members
        log.info("");
        log.info("--- Seeding Members ---");
        long memberStartTime = System.currentTimeMillis();
        int membersCreated = seedMembersCommand.execute(TARGET_MEMBERS);
        long memberDuration = System.currentTimeMillis() - memberStartTime;
        log.info("Members seeding completed in {} ms", memberDuration);

        // Seed Products
        log.info("");
        log.info("--- Seeding Products ---");
        long productStartTime = System.currentTimeMillis();
        int productsCreated = seedProductsCommand.execute(TARGET_PRODUCTS);
        long productDuration = System.currentTimeMillis() - productStartTime;
        log.info("Products seeding completed in {} ms", productDuration);

        long totalDuration = System.currentTimeMillis() - startTime;

        // Summary
        log.info("");
        log.info("=".repeat(60));
        log.info("Data Seeding Complete!");
        log.info("=".repeat(60));
        log.info("Members created: {}", membersCreated);
        log.info("Products created: {}", productsCreated);
        log.info("Total time: {} ms ({} seconds)", totalDuration, totalDuration / 1000);
        log.info("=".repeat(60));
    }
}

