#!/usr/bin/env python3
"""
Script to generate 50,000 products CSV file for MongoDB import
"""

import csv
import random
import uuid

# Product categories and their data
categories = {
    'phones': {
        'brands': ['iPhone', 'Samsung Galaxy', 'Google Pixel', 'OnePlus', 'Xiaomi', 'Huawei', 'Oppo', 'Vivo', 'Realme', 'Motorola'],
        'models': ['Pro', 'Max', 'Ultra', 'Plus', 'Mini', 'SE', 'Lite', 'Note', 'Edge', 'Fold'],
        'storage': ['64GB', '128GB', '256GB', '512GB', '1TB'],
        'colors': ['Black', 'White', 'Blue', 'Red', 'Green', 'Purple', 'Gold', 'Silver', 'Pink', 'Gray'],
        'price_range': (200, 1500)
    },
    'laptops': {
        'brands': ['MacBook', 'Dell XPS', 'HP Spectre', 'Lenovo ThinkPad', 'ASUS ROG', 'Acer Predator', 'MSI', 'Razer', 'Surface', 'LG Gram'],
        'models': ['Pro', 'Air', 'Gaming', 'Ultra', 'Elite', 'Premium', 'Standard', 'Workstation'],
        'specs': ['Intel i5', 'Intel i7', 'Intel i9', 'AMD Ryzen 5', 'AMD Ryzen 7', 'AMD Ryzen 9', 'M1', 'M2', 'M3'],
        'ram': ['8GB', '16GB', '32GB', '64GB'],
        'storage': ['256GB SSD', '512GB SSD', '1TB SSD', '2TB SSD'],
        'price_range': (400, 3500)
    },
    'shoes': {
        'brands': ['Nike', 'Adidas', 'Puma', 'Reebok', 'New Balance', 'Converse', 'Vans', 'Skechers', 'Under Armour', 'Jordan'],
        'types': ['Running', 'Basketball', 'Sneakers', 'Casual', 'Athletic', 'Training', 'Walking', 'Hiking', 'Football', 'Tennis'],
        'sizes': ['7', '8', '9', '10', '11', '12', '13'],
        'colors': ['Black', 'White', 'Red', 'Blue', 'Gray', 'Green', 'Orange', 'Pink', 'Yellow', 'Multi'],
        'price_range': (30, 250)
    },
    'clothes': {
        'categories': ['T-Shirt', 'Jeans', 'Jacket', 'Hoodie', 'Dress', 'Shirt', 'Shorts', 'Pants', 'Sweater', 'Blazer'],
        'brands': ['Nike', 'Adidas', 'Levi\'s', 'Zara', 'H&M', 'Gap', 'Uniqlo', 'Calvin Klein', 'Tommy Hilfiger', 'Ralph Lauren'],
        'sizes': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
        'colors': ['Black', 'White', 'Navy', 'Gray', 'Red', 'Blue', 'Green', 'Beige', 'Brown', 'Pink'],
        'materials': ['Cotton', 'Polyester', 'Denim', 'Wool', 'Linen', 'Silk', 'Leather'],
        'price_range': (15, 200)
    },
    'cosmetics': {
        'types': ['Foundation', 'Lipstick', 'Mascara', 'Eyeliner', 'Eyeshadow', 'Blush', 'Concealer', 'Powder', 'Primer', 'Highlighter'],
        'brands': ['Maybelline', 'L\'Oreal', 'MAC', 'Revlon', 'Covergirl', 'NYX', 'Elf', 'Milani', 'Wet n Wild', 'Rimmel'],
        'shades': ['Natural', 'Ivory', 'Beige', 'Tan', 'Deep', 'Rose', 'Coral', 'Nude', 'Pink', 'Red'],
        'price_range': (5, 80)
    },
    'electronics': {
        'types': ['Headphones', 'Speakers', 'Smartwatch', 'Tablet', 'Camera', 'TV', 'Monitor', 'Keyboard', 'Mouse', 'Webcam'],
        'brands': ['Sony', 'Bose', 'Apple', 'Samsung', 'LG', 'Canon', 'Nikon', 'Logitech', 'Razer', 'JBL'],
        'price_range': (20, 2000)
    },
    'home': {
        'types': ['Lamp', 'Pillow', 'Blanket', 'Curtains', 'Rug', 'Mirror', 'Frame', 'Vase', 'Candle', 'Clock'],
        'materials': ['Wood', 'Metal', 'Glass', 'Fabric', 'Ceramic', 'Plastic'],
        'price_range': (10, 500)
    },
    'sports': {
        'types': ['Basketball', 'Football', 'Tennis Racket', 'Yoga Mat', 'Dumbbells', 'Resistance Bands', 'Jump Rope', 'Water Bottle', 'Gym Bag', 'Fitness Tracker'],
        'brands': ['Nike', 'Adidas', 'Under Armour', 'Wilson', 'Spalding', 'Yonex', 'Head', 'Babolat'],
        'price_range': (15, 300)
    },
    'books': {
        'genres': ['Fiction', 'Non-Fiction', 'Mystery', 'Romance', 'Sci-Fi', 'Biography', 'History', 'Self-Help', 'Business', 'Cookbook'],
        'formats': ['Hardcover', 'Paperback', 'E-Book'],
        'price_range': (8, 50)
    },
    'toys': {
        'types': ['Action Figure', 'Board Game', 'Puzzle', 'LEGO Set', 'Doll', 'RC Car', 'Building Blocks', 'Art Supplies', 'Musical Toy', 'Educational Toy'],
        'age_groups': ['0-3', '3-6', '6-12', '12+'],
        'price_range': (10, 150)
    }
}

def generate_phone(index):
    cat = categories['phones']
    brand = random.choice(cat['brands'])
    model = random.choice(cat['models'])
    storage = random.choice(cat['storage'])
    color = random.choice(cat['colors'])
    
    product_name = f"{brand} {model} {storage} {color}"
    item_sku = f"PHN-{brand.upper().replace(' ', '')[:6]}-{model.upper()}-{storage}-{color.upper()[:3]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Premium {brand} {model} smartphone with {storage} storage in {color}. Features high-resolution display, advanced camera system, fast processor, and long-lasting battery. Perfect for professionals and tech enthusiasts."
    
    return item_sku, product_name, price, description

def generate_laptop(index):
    cat = categories['laptops']
    brand = random.choice(cat['brands'])
    model = random.choice(cat['models'])
    spec = random.choice(cat['specs'])
    ram = random.choice(cat['ram'])
    storage = random.choice(cat['storage'])
    
    product_name = f"{brand} {model} {spec} {ram} {storage}"
    item_sku = f"LAP-{brand.upper().replace(' ', '')[:6]}-{model.upper()}-{spec.replace(' ', '')[:4]}-{ram}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Powerful {brand} {model} laptop with {spec} processor, {ram} RAM, and {storage} storage. Ideal for work, gaming, and creative projects. Features premium build quality and excellent performance."
    
    return item_sku, product_name, price, description

def generate_shoe(index):
    cat = categories['shoes']
    brand = random.choice(cat['brands'])
    shoe_type = random.choice(cat['types'])
    size = random.choice(cat['sizes'])
    color = random.choice(cat['colors'])
    
    product_name = f"{brand} {shoe_type} Shoes Size {size} {color}"
    item_sku = f"SHO-{brand.upper()[:4]}-{shoe_type.upper()[:4]}-{size}-{color.upper()[:3]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Comfortable {brand} {shoe_type.lower()} shoes in {color.lower()} color, size {size}. Designed for {shoe_type.lower()} activities with superior cushioning and support. Durable construction for long-lasting wear."
    
    return item_sku, product_name, price, description

def generate_clothing(index):
    cat = categories['clothes']
    category = random.choice(cat['categories'])
    brand = random.choice(cat['brands'])
    size = random.choice(cat['sizes'])
    color = random.choice(cat['colors'])
    material = random.choice(cat['materials'])
    
    product_name = f"{brand} {category} {size} {color} {material}"
    item_sku = f"CLO-{brand.upper().replace(' ', '')[:4]}-{category.upper().replace(' ', '')[:4]}-{size}-{color.upper()[:3]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Stylish {brand} {category.lower()} in {color.lower()} color, size {size}. Made from premium {material.lower()} material. Comfortable fit and modern design perfect for everyday wear."
    
    return item_sku, product_name, price, description

def generate_cosmetic(index):
    cat = categories['cosmetics']
    cosmetic_type = random.choice(cat['types'])
    brand = random.choice(cat['brands'])
    shade = random.choice(cat['shades'])
    
    product_name = f"{brand} {cosmetic_type} {shade}"
    item_sku = f"COS-{brand.upper().replace(' ', '')[:4]}-{cosmetic_type.upper()[:4]}-{shade.upper()[:3]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"High-quality {brand} {cosmetic_type.lower()} in {shade.lower()} shade. Long-lasting formula with smooth application. Perfect for everyday makeup looks. Cruelty-free and dermatologically tested."
    
    return item_sku, product_name, price, description

def generate_electronics(index):
    cat = categories['electronics']
    product_type = random.choice(cat['types'])
    brand = random.choice(cat['brands'])
    
    product_name = f"{brand} {product_type}"
    item_sku = f"ELE-{brand.upper()[:4]}-{product_type.upper().replace(' ', '')[:6]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Premium {brand} {product_type.lower()} with advanced features and superior quality. Designed for performance and durability. Perfect addition to your tech collection."
    
    return item_sku, product_name, price, description

def generate_home(index):
    cat = categories['home']
    product_type = random.choice(cat['types'])
    material = random.choice(cat['materials'])
    
    product_name = f"{material} {product_type}"
    item_sku = f"HOM-{material.upper()[:3]}-{product_type.upper()[:4]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Beautiful {material.lower()} {product_type.lower()} for your home. Elegant design that complements any decor style. High-quality craftsmanship and materials."
    
    return item_sku, product_name, price, description

def generate_sports(index):
    cat = categories['sports']
    product_type = random.choice(cat['types'])
    brand = random.choice(cat['brands']) if random.random() > 0.3 else "Generic"
    
    product_name = f"{brand} {product_type}"
    item_sku = f"SPO-{brand.upper().replace(' ', '')[:4]}-{product_type.upper().replace(' ', '')[:6]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Professional-grade {brand} {product_type.lower()} for athletes and fitness enthusiasts. Durable construction designed for performance and longevity."
    
    return item_sku, product_name, price, description

def generate_book(index):
    cat = categories['books']
    genre = random.choice(cat['genres'])
    format_type = random.choice(cat['formats'])
    titles = ['The Journey', 'Mystery Solved', 'Love Story', 'Future World', 'Life Lessons', 'Success Guide', 'Adventure Tales', 'History Revealed', 'Cooking Mastery', 'Business Secrets']
    title = random.choice(titles)
    
    product_name = f"{title} - {genre} ({format_type})"
    item_sku = f"BOK-{genre.upper().replace('-', '')[:4]}-{format_type.upper()[:3]}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Engaging {genre.lower()} book '{title}' in {format_type.lower()} format. A must-read for fans of {genre.lower()} literature. Well-written and captivating from start to finish."
    
    return item_sku, product_name, price, description

def generate_toy(index):
    cat = categories['toys']
    toy_type = random.choice(cat['types'])
    age_group = random.choice(cat['age_groups'])
    
    product_name = f"{toy_type} for Ages {age_group}"
    item_sku = f"TOY-{toy_type.upper().replace(' ', '')[:6]}-{age_group.replace('-', '')}-{index:05d}"
    price = round(random.uniform(*cat['price_range']), 2)
    description = f"Fun and educational {toy_type.lower()} suitable for ages {age_group}. Safe, durable, and designed to encourage creativity and learning. Perfect gift for children."
    
    return item_sku, product_name, price, description

# Distribution: 10k phones, 8k laptops, 8k shoes, 8k clothes, 6k cosmetics, 3k electronics, 2k home, 2k sports, 2k books, 1k toys
distribution = [
    ('phones', 10000, generate_phone),
    ('laptops', 8000, generate_laptop),
    ('shoes', 8000, generate_shoe),
    ('clothes', 8000, generate_clothing),
    ('cosmetics', 6000, generate_cosmetic),
    ('electronics', 3000, generate_electronics),
    ('home', 2000, generate_home),
    ('sports', 2000, generate_sports),
    ('books', 2000, generate_book),
    ('toys', 1000, generate_toy)
]

def main():
    filename = 'products_50000.csv'
    
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Write header - matching ProductEntity field order: id, itemSku, productName, productPrice, productDescription
        writer.writerow(['id', 'itemSku', 'productName', 'productPrice', 'productDescription'])
        
        index = 1
        for category, count, generator_func in distribution:
            print(f"Generating {count} {category}...")
            for i in range(count):
                item_sku, product_name, price, description = generator_func(index)
                product_id = str(uuid.uuid4())
                
                writer.writerow([
                    product_id,
                    item_sku,
                    product_name,
                    price,
                    description
                ])
                index += 1
                
                if index % 5000 == 0:
                    print(f"  Generated {index} products so far...")
    
    print(f"\n✅ Successfully generated {filename} with 50,000 products!")
    print(f"   File is ready for MongoDB Compass import.")

if __name__ == '__main__':
    main()





