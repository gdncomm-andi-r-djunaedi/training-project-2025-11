#!/usr/bin/env python3
"""
Script to generate 5000 users CSV file with guaranteed unique usernames
"""

import csv
import random
import string

# Email domains allowed
EMAIL_DOMAINS = ['gmail.com', 'yahoo.com', 'hotmail.com']

# Common first names and last names for realistic usernames
FIRST_NAMES = [
    'John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'James', 'Jessica',
    'Robert', 'Ashley', 'William', 'Amanda', 'Richard', 'Melissa', 'Joseph',
    'Nicole', 'Thomas', 'Michelle', 'Charles', 'Kimberly', 'Christopher',
    'Amy', 'Daniel', 'Angela', 'Matthew', 'Brenda', 'Anthony', 'Emma',
    'Mark', 'Olivia', 'Donald', 'Cynthia', 'Steven', 'Marie', 'Paul',
    'Janet', 'Andrew', 'Catherine', 'Joshua', 'Frances', 'Kenneth',
    'Christine', 'Kevin', 'Samantha', 'Brian', 'Deborah', 'George',
    'Rachel', 'Timothy', 'Carolyn', 'Ronald', 'Janet', 'Jason', 'Virginia',
    'Edward', 'Maria', 'Jeffrey', 'Heather', 'Ryan', 'Diane', 'Jacob',
    'Julie', 'Gary', 'Joyce', 'Nicholas', 'Victoria', 'Eric', 'Kelly',
    'Jonathan', 'Christina', 'Stephen', 'Joan', 'Larry', 'Evelyn', 'Justin',
    'Judith', 'Scott', 'Megan', 'Brandon', 'Andrea', 'Samuel', 'Hannah',
    'Gregory', 'Jacqueline', 'Frank', 'Martha', 'Raymond', 'Gloria',
    'Alexander', 'Teresa', 'Patrick', 'Sara', 'Jack', 'Janice', 'Dennis',
    'Marie', 'Jerry', 'Julia', 'Tyler', 'Grace', 'Aaron', 'Judy'
]

LAST_NAMES = [
    'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller',
    'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson', 'Anderson',
    'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee', 'Thompson',
    'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson',
    'Walker', 'Young', 'Allen', 'King', 'Wright', 'Scott', 'Torres',
    'Nguyen', 'Hill', 'Flores', 'Green', 'Adams', 'Nelson', 'Baker',
    'Hall', 'Rivera', 'Campbell', 'Mitchell', 'Carter', 'Roberts', 'Gomez',
    'Phillips', 'Evans', 'Turner', 'Diaz', 'Parker', 'Cruz', 'Edwards',
    'Collins', 'Reyes', 'Stewart', 'Morris', 'Morales', 'Murphy', 'Cook',
    'Rogers', 'Gutierrez', 'Ortiz', 'Morgan', 'Cooper', 'Peterson', 'Bailey',
    'Reed', 'Kelly', 'Howard', 'Ramos', 'Kim', 'Cox', 'Ward', 'Richardson',
    'Watson', 'Brooks', 'Chavez', 'Wood', 'James', 'Bennett', 'Gray',
    'Mendoza', 'Ruiz', 'Hughes', 'Price', 'Alvarez', 'Castillo', 'Sanders'
]

def generate_unique_username(first_name, last_name, index, used_usernames):
    """Generate a unique username ensuring no duplicates"""
    base_username = f"{first_name.lower()}{last_name.lower()}"
    
    # Try variations until we find a unique one
    variations = [
        base_username,
        f"{first_name.lower()}.{last_name.lower()}",
        f"{first_name.lower()}_{last_name.lower()}",
        f"{first_name[0].lower()}{last_name.lower()}",
        f"{first_name.lower()}{last_name[0].lower()}",
    ]
    
    # Try each variation with index suffix if needed
    for variation in variations:
        username = variation
        if username not in used_usernames:
            used_usernames.add(username)
            return username
        
        # Try with index suffix
        for suffix_len in range(1, 6):  # Try 1-5 digit suffixes
            username = f"{variation}{index % (10 ** suffix_len)}"
            if username not in used_usernames:
                used_usernames.add(username)
                return username
    
    # Fallback: use index to ensure uniqueness
    username = f"{base_username}{index}"
    used_usernames.add(username)
    return username

def generate_email(username, index):
    """Generate email with allowed domain"""
    domain = EMAIL_DOMAINS[index % len(EMAIL_DOMAINS)]
    return f"{username}@{domain}"

def generate_phone_number(index):
    """Generate unique 10-digit phone number"""
    # Start from 2000000000 to avoid conflicts with previous batch
    base_number = 2000000000 + index
    return str(base_number)[:10]  # Ensure exactly 10 digits

def generate_password(index):
    """Generate a secure password (at least 8 characters)"""
    password_length = random.randint(8, 12)
    password_chars = string.ascii_letters + string.digits + "!@#$%"
    password = ''.join(random.choice(password_chars) for _ in range(password_length))
    return password

def main():
    filename = 'users_5000_unique.csv'
    used_usernames = set()
    
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Write header - matching RegisterRequest field order
        writer.writerow(['username', 'userMail', 'userPhoneNumber', 'password'])
        
        print("Generating 5000 users with unique usernames...")
        
        for i in range(1, 5001):
            first_name = random.choice(FIRST_NAMES)
            last_name = random.choice(LAST_NAMES)
            username = generate_unique_username(first_name, last_name, i, used_usernames)
            email = generate_email(username, i)
            phone = generate_phone_number(i)
            password = generate_password(i)
            
            writer.writerow([username, email, phone, password])
            
            if i % 500 == 0:
                print(f"  Generated {i} users...")
    
    print(f"\n✅ Successfully generated {filename} with 5,000 unique users!")
    print(f"   All usernames are guaranteed to be unique.")
    print(f"   File is ready for registration script.")

if __name__ == '__main__':
    main()



