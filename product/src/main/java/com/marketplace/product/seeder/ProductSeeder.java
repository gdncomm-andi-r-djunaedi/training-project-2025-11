package com.marketplace.product.seeder;

import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile("seeder")
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {

    private static final int TOTAL_PRODUCTS = 50000;
    private static final int BATCH_SIZE = 1000;

    private final ProductRepository productRepository;
    private final Random random = new Random();

    private final Map<String, String[]> categoryProducts = new LinkedHashMap<>() {{
        put("Electronics", new String[]{
                "Smartphone", "Laptop", "Tablet", "Smartwatch", "Headphones", "Earbuds",
                "Monitor", "Keyboard", "Mouse", "Webcam", "Speaker", "Power Bank",
                "USB Hub", "External SSD", "Graphics Card", "RAM Module", "CPU Cooler",
                "Gaming Console", "VR Headset", "Drone"
        });
        put("Fashion", new String[]{
                "T-Shirt", "Jeans", "Dress", "Jacket", "Sneakers", "Boots", "Sandals",
                "Backpack", "Handbag", "Wallet", "Watch", "Sunglasses", "Belt", "Hat",
                "Scarf", "Hoodie", "Blazer", "Shorts", "Skirt", "Cardigan"
        });
        put("Home & Living", new String[]{
                "Sofa", "Bed Frame", "Mattress", "Dining Table", "Chair", "Lamp",
                "Curtains", "Carpet", "Mirror", "Shelf", "Drawer", "Wardrobe",
                "Coffee Table", "TV Stand", "Bookcase", "Bean Bag", "Plant Pot",
                "Wall Art", "Cushion", "Blanket"
        });
        put("Sports & Outdoors", new String[]{
                "Running Shoes", "Yoga Mat", "Dumbbell Set", "Exercise Bike", "Treadmill",
                "Basketball", "Football", "Tennis Racket", "Golf Club", "Bicycle",
                "Camping Tent", "Sleeping Bag", "Hiking Backpack", "Fishing Rod",
                "Skateboard", "Surfboard", "Boxing Gloves", "Jump Rope", "Resistance Band",
                "Foam Roller"
        });
        put("Beauty & Health", new String[]{
                "Facial Cleanser", "Moisturizer", "Sunscreen", "Serum", "Face Mask",
                "Shampoo", "Conditioner", "Hair Dryer", "Straightener", "Perfume",
                "Lipstick", "Foundation", "Mascara", "Eye Shadow", "Nail Polish",
                "Electric Toothbrush", "Vitamins", "Essential Oil", "Massage Gun",
                "Blood Pressure Monitor"
        });
        put("Books & Stationery", new String[]{
                "Novel", "Textbook", "Notebook", "Planner", "Pen Set", "Pencil Case",
                "Marker Set", "Highlighter", "Stapler", "Paper Clips", "Binder",
                "Calculator", "Globe", "Dictionary", "Comic Book", "Art Book",
                "Cookbook", "Self-Help Book", "Journal", "Sketch Pad"
        });
        put("Toys & Games", new String[]{
                "Action Figure", "Board Game", "Puzzle", "LEGO Set", "Doll",
                "Remote Control Car", "Building Blocks", "Card Game", "Video Game",
                "Stuffed Animal", "Educational Toy", "Art Set", "Science Kit",
                "Musical Toy", "Outdoor Play Set", "Water Gun", "Nerf Gun",
                "Play-Doh Set", "Magic Set", "Chess Set"
        });
        put("Food & Beverages", new String[]{
                "Coffee Beans", "Tea Set", "Chocolate Box", "Snack Pack", "Protein Bar",
                "Energy Drink", "Fruit Juice", "Instant Noodles", "Rice", "Pasta",
                "Olive Oil", "Honey", "Jam", "Cereal", "Cookies", "Chips",
                "Dried Fruits", "Nuts Mix", "Spice Set", "Sauce Collection"
        });
    }};

    private final String[] brands = {
            "TechPro", "StyleMax", "HomePlus", "SportFit", "BeautyGlow", "BookWorm",
            "ToyLand", "FoodieChoice", "EliteTech", "FashionHub", "ComfortHome",
            "ActiveLife", "GlowUp", "LearnMore", "PlayTime", "TasteGood",
            "ProGear", "TrendyWear", "CozyNest", "PowerMove", "RadiantSkin",
            "SmartRead", "FunZone", "FlavorBurst", "InnovateTech", "ChicStyle"
    };

    private final String[] colors = {
            "Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Purple",
            "Pink", "Gray", "Brown", "Navy", "Silver", "Gold", "Rose Gold"
    };

    private final String[] sizes = {"XS", "S", "M", "L", "XL", "XXL", "One Size"};

    @Override
    public void run(String... args) {
        long existingCount = productRepository.count();
        if (existingCount >= TOTAL_PRODUCTS) {
            log.info("Database already has {} products. Skipping seeding.", existingCount);
            return;
        }

        log.info("Starting product seeding. Target: {} products", TOTAL_PRODUCTS);

        int productsToCreate = TOTAL_PRODUCTS - (int) existingCount;
        int created = 0;

        List<Product> batch = new ArrayList<>();
        List<String> categories = new ArrayList<>(categoryProducts.keySet());

        for (int i = 0; i < productsToCreate; i++) {
            Product product = createRandomProduct(i + (int) existingCount, categories);
            batch.add(product);

            if (batch.size() >= BATCH_SIZE) {
                productRepository.saveAll(batch);
                created += batch.size();
                log.info("Created {} / {} products", created, productsToCreate);
                batch.clear();
            }
        }

        // Save remaining
        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
            created += batch.size();
        }

        log.info("Product seeding completed. Total created: {}", created);
    }

    private Product createRandomProduct(int index, List<String> categories) {
        String category = categories.get(random.nextInt(categories.size()));
        String[] products = categoryProducts.get(category);
        String productType = products[random.nextInt(products.length)];
        String brand = brands[random.nextInt(brands.length)];
        String color = colors[random.nextInt(colors.length)];

        String name = String.format("%s %s %s - %s Edition", brand, productType, color, generateVariant());
        
        BigDecimal price = generatePrice(category);
        BigDecimal originalPrice = price;
        Integer discount = 0;
        
        // 30% chance of discount
        if (random.nextDouble() < 0.3) {
            discount = (random.nextInt(5) + 1) * 5; // 5%, 10%, 15%, 20%, 25%
            originalPrice = price.multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(100 - discount), 2, RoundingMode.HALF_UP);
        }

        return Product.builder()
                .name(name)
                .description(generateDescription(productType, brand, category))
                .category(category)
                .brand(brand)
                .price(price)
                .originalPrice(originalPrice)
                .discountPercentage(discount)
                .images(generateImages(index))
                .tags(generateTags(category, productType, brand))
                .specs(generateSpecs(color, category))
                .rating(3.0 + random.nextDouble() * 2) // 3.0 - 5.0
                .reviewCount(random.nextInt(1000))
                .active(true)
                .createdAt(LocalDateTime.now().minusDays(random.nextInt(365)))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private String generateVariant() {
        String[] variants = {"Pro", "Plus", "Lite", "Max", "Ultra", "Basic", "Premium", "Elite", "Classic", "Modern"};
        return variants[random.nextInt(variants.length)];
    }

    private BigDecimal generatePrice(String category) {
        double basePrice;
        switch (category) {
            case "Electronics":
                basePrice = 50 + random.nextDouble() * 2000;
                break;
            case "Fashion":
                basePrice = 10 + random.nextDouble() * 500;
                break;
            case "Home & Living":
                basePrice = 20 + random.nextDouble() * 1500;
                break;
            case "Sports & Outdoors":
                basePrice = 15 + random.nextDouble() * 800;
                break;
            case "Beauty & Health":
                basePrice = 5 + random.nextDouble() * 200;
                break;
            case "Books & Stationery":
                basePrice = 2 + random.nextDouble() * 100;
                break;
            case "Toys & Games":
                basePrice = 5 + random.nextDouble() * 300;
                break;
            case "Food & Beverages":
                basePrice = 2 + random.nextDouble() * 50;
                break;
            default:
                basePrice = 10 + random.nextDouble() * 100;
        }
        return BigDecimal.valueOf(basePrice).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateDescription(String productType, String brand, String category) {
        String[] adjectives = {"Premium", "High-quality", "Durable", "Stylish", "Innovative", "Comfortable", "Reliable", "Versatile"};
        String[] features = {"featuring advanced technology", "with ergonomic design", "made with premium materials", 
                "designed for everyday use", "perfect for all occasions", "built to last"};
        
        return String.format("%s %s from %s. This %s product is %s and offers excellent value. " +
                        "Ideal for %s enthusiasts looking for quality and performance.",
                adjectives[random.nextInt(adjectives.length)],
                productType,
                brand,
                category.toLowerCase(),
                features[random.nextInt(features.length)],
                category.toLowerCase());
    }

    private List<String> generateImages(int index) {
        List<String> images = new ArrayList<>();
        int imageCount = random.nextInt(4) + 1;
        for (int i = 0; i < imageCount; i++) {
            images.add(String.format("https://picsum.photos/seed/%d%d/400/400", index, i));
        }
        return images;
    }

    private List<String> generateTags(String category, String productType, String brand) {
        List<String> tags = new ArrayList<>();
        tags.add(category.toLowerCase().replace(" & ", "-").replace(" ", "-"));
        tags.add(productType.toLowerCase().replace(" ", "-"));
        tags.add(brand.toLowerCase());
        tags.add("new-arrival");
        if (random.nextBoolean()) tags.add("bestseller");
        if (random.nextBoolean()) tags.add("trending");
        if (random.nextBoolean()) tags.add("featured");
        return tags;
    }

    private Product.ProductSpecs generateSpecs(String color, String category) {
        Map<String, String> additionalSpecs = new HashMap<>();
        additionalSpecs.put("Material", generateMaterial(category));
        additionalSpecs.put("Origin", generateOrigin());
        additionalSpecs.put("Warranty", random.nextInt(24) + 1 + " months");

        return Product.ProductSpecs.builder()
                .color(color)
                .size(sizes[random.nextInt(sizes.length)])
                .weight(String.format("%.2f kg", 0.1 + random.nextDouble() * 5))
                .material(generateMaterial(category))
                .additionalSpecs(additionalSpecs)
                .build();
    }

    private String generateMaterial(String category) {
        String[] materials;
        switch (category) {
            case "Electronics":
                materials = new String[]{"Aluminum", "Plastic", "Glass", "Carbon Fiber", "Metal"};
                break;
            case "Fashion":
                materials = new String[]{"Cotton", "Polyester", "Leather", "Denim", "Silk", "Wool"};
                break;
            case "Home & Living":
                materials = new String[]{"Wood", "Metal", "Fabric", "Glass", "Bamboo", "Rattan"};
                break;
            default:
                materials = new String[]{"Mixed Materials", "Composite", "Natural", "Synthetic"};
        }
        return materials[random.nextInt(materials.length)];
    }

    private String generateOrigin() {
        String[] origins = {"Indonesia", "China", "Japan", "USA", "Germany", "South Korea", "Taiwan", "Vietnam"};
        return origins[random.nextInt(origins.length)];
    }
}

