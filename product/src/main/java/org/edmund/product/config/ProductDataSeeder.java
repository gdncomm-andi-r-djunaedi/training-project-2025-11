package org.edmund.product.config;

import jakarta.annotation.PreDestroy; // 1. Import Wajib untuk Cleanup
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edmund.product.entity.Product;
import org.edmund.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j // Biar gampang nge-log (opsional, bisa pakai System.out juga)
public class ProductDataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final Random random = new Random();

    private final int TOTAL_RECORDS = 50000;
    private final int BATCH_SIZE = 2000; // Batch dibesarin dikit biar ngebut

    // --- BAGIAN STARTUP (Isi Data) ---
    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 [STARTUP] Membersihkan data lama (jika ada)...");
        productRepository.deleteAll(); // Pastikan bersih dulu sebelum isi

        log.info("🚀 [STARTUP] Memulai proses seeding 50.000 data...");
        long startTime = System.currentTimeMillis();

        List<Product> batch = new ArrayList<>();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            batch.add(generateRandomProduct());

            // Simpan per Batch biar RAM gak meledak
            if (batch.size() >= BATCH_SIZE) {
                productRepository.saveAll(batch);
                batch.clear();
                log.info("✅ [SEEDING] Progress: {}/{}", (i + 1), TOTAL_RECORDS);
            }
        }

        // Simpan sisanya
        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
        }

        long endTime = System.currentTimeMillis();
        log.info("🎉 [STARTUP] SELESAI! 50.000 data masuk dalam {} ms", (endTime - startTime));
    }

    // --- BAGIAN SHUTDOWN (Hapus Data) ---
    @PreDestroy // <--- INI MAGIC-NYA! Jalan pas kamu Stop aplikasi
    public void onExit() {
        log.info("🛑 [SHUTDOWN] Aplikasi berhenti. Menghapus semua data produk...");
        productRepository.deleteAll();
        log.info("🗑️ [SHUTDOWN] Semua data produk berhasil dihapus. Database bersih!");
    }

    // --- LOGIKA RANDOM KAMU (Saya Copy Paste yang tadi) ---
    private Product generateRandomProduct() {
        double chance = random.nextDouble();
        String sku = UUID.randomUUID().toString();
        String name;
        String merchant;
        String description;
        long price;
        int stock;

        if (chance < 0.15) {
            int model = 10 + random.nextInt(6);
            String[] variants = {"Pro", "Pro Max", "Plus", "Mini"};
            String variant = variants[random.nextInt(variants.length)];
            name = "IPhone " + model + " " + variant;
            merchant = "Apple Authorized Reseller";
            description = "Experience the power of Apple. A" + (model + 4) + " Bionic chip.";
            price = 10_000_000L + (long)(random.nextDouble() * 15_000_000L);
            stock = 5 + random.nextInt(50);
        } else if (chance < 0.35) {
            int watts = 20 + random.nextInt(100);
            name = "Super Fast Charger " + watts + "W";
            String[] chargerMerchants = {"Anker", "Baseus", "Ugreen", "Samsung"};
            merchant = chargerMerchants[random.nextInt(chargerMerchants.length)];
            description = "Safe and fast charging with GaN technology.";
            price = 100_000L + (long)(random.nextDouble() * 500_000L);
            stock = 50 + random.nextInt(200);
        } else {
            String[] products = {"Gaming Mouse", "Mechanical Keyboard", "Monitor 24 inch", "Webcam 1080p", "Headset Bluetooth", "Smart Watch"};
            String[] adjectives = {"Pro", "Ultra", "Slim", "RGB", "Wireless"};
            String baseProduct = products[random.nextInt(products.length)];
            String adj = adjectives[random.nextInt(adjectives.length)];
            name = adj + " " + baseProduct;
            String[] randomMerchants = {"Logitech", "Samsung", "Blibli Official", "GamerZone"};
            merchant = randomMerchants[random.nextInt(randomMerchants.length)];
            description = "High quality " + baseProduct + " for your daily needs.";
            price = 50_000L + (long)(random.nextDouble() * 5_000_000L);
            stock = 1 + random.nextInt(100);
        }

        return Product.builder()
                .sku(sku)
                .name(name)
                .merchant(merchant)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
    }
}