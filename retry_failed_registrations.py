#!/usr/bin/env python3
"""
Script to retry failed registrations from the error JSON file
This will generate a new CSV with unique usernames for the failed users
"""

import csv
import json
import random
import string

EMAIL_DOMAINS = ['gmail.com', 'yahoo.com', 'hotmail.com']

def generate_unique_username_from_error(error_data, index, used_usernames):
    """Generate a unique username from the failed user data"""
    original_username = error_data['user']
    
    # Try variations of the original username
    variations = [
        f"{original_username}{index}",
        f"{original_username}_{index}",
        f"{original_username}.{index}",
        f"user{index}",
        f"user_{index}",
    ]
    
    for variation in variations:
        if variation not in used_usernames:
            used_usernames.add(variation)
            return variation
    
    # Fallback
    username = f"{original_username}_{index}_{random.randint(1000, 9999)}"
    used_usernames.add(username)
    return username

def generate_email(username, index):
    """Generate email with allowed domain"""
    domain = EMAIL_DOMAINS[index % len(EMAIL_DOMAINS)]
    return f"{username}@{domain}"

def generate_phone_number(index, base_index):
    """Generate unique 10-digit phone number"""
    # Use a different range to avoid conflicts
    base_number = 3000000000 + (base_index * 1000) + index
    return str(base_number)[:10]

def generate_password():
    """Generate a secure password (at least 8 characters)"""
    password_length = random.randint(8, 12)
    password_chars = string.ascii_letters + string.digits + "!@#$%"
    return ''.join(random.choice(password_chars) for _ in range(password_length))

def main():
    error_file = 'registration_errors.json'
    output_file = 'users_retry_unique.csv'
    
    try:
        with open(error_file, 'r', encoding='utf-8') as f:
            errors = json.load(f)
    except FileNotFoundError:
        print(f"❌ Error: File '{error_file}' not found!")
        return
    except Exception as e:
        print(f"❌ Error reading error file: {str(e)}")
        return
    
    if not errors:
        print("✅ No errors to retry!")
        return
    
    print(f"Found {len(errors)} failed registrations to retry")
    print("Generating new CSV with unique usernames...\n")
    
    used_usernames = set()
    
    with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['username', 'userMail', 'userPhoneNumber', 'password'])
        
        for index, error in enumerate(errors, 1):
            username = generate_unique_username_from_error(error, index, used_usernames)
            email = generate_email(username, index)
            phone = generate_phone_number(index, len(errors))
            password = generate_password()
            
            writer.writerow([username, email, phone, password])
            
            print(f"[{index}/{len(errors)}] Generated: {username} ({email})")
    
    print(f"\n✅ Successfully generated {output_file} with {len(errors)} unique users!")
    print(f"   You can now run: python3 register_users.py")
    print(f"   (Make sure to update the CSV filename in register_users.py to '{output_file}')")

if __name__ == '__main__':
    main()



