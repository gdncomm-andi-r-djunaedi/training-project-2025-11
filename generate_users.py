#!/usr/bin/env python3
"""
Script to generate 5000 users CSV file for registration API
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
    'Judith', 'Scott', 'Megan', 'Brandon', 'Cheryl', 'Benjamin', 'Andrea',
    'Samuel', 'Hannah', 'Gregory', 'Jacqueline', 'Frank', 'Martha', 'Raymond',
    'Gloria', 'Alexander', 'Teresa', 'Patrick', 'Sara', 'Jack', 'Janice',
    'Dennis', 'Marie', 'Jerry', 'Julia', 'Tyler', 'Grace', 'Aaron', 'Judy'
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

def generate_username(first_name, last_name, index):
    """Generate a unique username"""
    # Add index to ensure uniqueness
    variations = [
        f"{first_name.lower()}{last_name.lower()}",
        f"{first_name.lower()}.{last_name.lower()}",
        f"{first_name.lower()}_{last_name.lower()}",
        f"{first_name.lower()}{last_name.lower()}{index}",
        f"{first_name.lower()}.{last_name.lower()}{index}",
        f"{first_name[0].lower()}{last_name.lower()}{index}",
        f"{first_name.lower()}{last_name[0].lower()}{index}"
    ]
    # Select variation based on index to ensure variety
    return variations[index % len(variations)]

def generate_email(username, index):
    """Generate email with allowed domain"""
    domain = EMAIL_DOMAINS[index % len(EMAIL_DOMAINS)]
    # Ensure email is unique by adding index if needed
    if index < 1000:
        return f"{username}@{domain}"
    else:
        return f"{username}{index}@{domain}"

def generate_phone_number(index):
    """Generate unique 10-digit phone number"""
    # Start from 1000000000 and increment
    base_number = 1000000000 + index
    return str(base_number)[:10]  # Ensure exactly 10 digits

def generate_password(index):
    """Generate a secure password (at least 8 characters)"""
    # Create password with mix of characters
    password_length = random.randint(8, 12)
    password_chars = string.ascii_letters + string.digits + "!@#$%"
    password = ''.join(random.choice(password_chars) for _ in range(password_length))
    return password

def main():
    filename = 'users_5000.csv'
    
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Write header - matching RegisterRequest field order
        writer.writerow(['username', 'userMail', 'userPhoneNumber', 'password'])
        
        print("Generating 5000 users...")
        
        for i in range(1, 5001):
            first_name = random.choice(FIRST_NAMES)
            last_name = random.choice(LAST_NAMES)
            username = generate_username(first_name, last_name, i)
            email = generate_email(username, i)
            phone = generate_phone_number(i)
            password = generate_password(i)
            
            writer.writerow([username, email, phone, password])
            
            if i % 500 == 0:
                print(f"  Generated {i} users...")
    
    print(f"\n✅ Successfully generated {filename} with 5,000 users!")
    print(f"   File is ready for registration script.")

if __name__ == '__main__':
    main()



