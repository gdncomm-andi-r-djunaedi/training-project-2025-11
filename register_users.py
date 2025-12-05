#!/usr/bin/env python3
"""
Script to register users from CSV file by hitting the register API
"""

import csv
import requests
import json
import time
from typing import Dict, List

# API Configuration
API_BASE_URL = "http://localhost:8092"  # API Gateway port
REGISTER_ENDPOINT = f"{API_BASE_URL}/apiGateway/member/register"

# Request configuration
HEADERS = {
    "Content-Type": "application/json",
    "Accept": "application/json"
}

# Rate limiting - delay between requests (in seconds)
REQUEST_DELAY = 0.1  # 100ms delay between requests to avoid overwhelming the server

# Retry configuration
MAX_RETRIES = 3
RETRY_DELAY = 1  # seconds

def register_user(user_data: Dict[str, str]) -> tuple[bool, str, int]:
    """
    Register a single user via API
    
    Returns:
        (success: bool, message: str, status_code: int)
    """
    payload = {
        "username": user_data["username"],
        "userMail": user_data["userMail"],
        "userPhoneNumber": user_data["userPhoneNumber"],
        "password": user_data["password"]
    }
    
    for attempt in range(MAX_RETRIES):
        try:
            response = requests.post(
                REGISTER_ENDPOINT,
                headers=HEADERS,
                json=payload,
                timeout=10  # 10 second timeout
            )
            
            if response.status_code == 201:
                return True, "User registered successfully", response.status_code
            else:
                error_msg = response.text
                # If it's a duplicate, that's expected for retries, so don't retry
                if response.status_code == 409:  # Conflict (duplicate)
                    return False, f"User already exists: {error_msg}", response.status_code
                # For other errors, retry
                if attempt < MAX_RETRIES - 1:
                    time.sleep(RETRY_DELAY)
                    continue
                return False, f"Error: {error_msg}", response.status_code
                
        except requests.exceptions.Timeout:
            if attempt < MAX_RETRIES - 1:
                time.sleep(RETRY_DELAY)
                continue
            return False, "Request timeout", 0
        except requests.exceptions.ConnectionError:
            if attempt < MAX_RETRIES - 1:
                time.sleep(RETRY_DELAY)
                continue
            return False, "Connection error - is the server running?", 0
        except Exception as e:
            if attempt < MAX_RETRIES - 1:
                time.sleep(RETRY_DELAY)
                continue
            return False, f"Unexpected error: {str(e)}", 0
    
    return False, "Max retries exceeded", 0

def read_users_from_csv(filename: str) -> List[Dict[str, str]]:
    """Read users from CSV file"""
    users = []
    try:
        with open(filename, 'r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                users.append({
                    "username": row["username"].strip(),
                    "userMail": row["userMail"].strip(),
                    "userPhoneNumber": row["userPhoneNumber"].strip(),
                    "password": row["password"].strip()
                })
        return users
    except FileNotFoundError:
        print(f"❌ Error: File '{filename}' not found!")
        print("   Please run 'generate_users.py' first to create the CSV file.")
        return []
    except Exception as e:
        print(f"❌ Error reading CSV file: {str(e)}")
        return []

def main():
    csv_filename = "users_5000.csv"
    
    print("=" * 60)
    print("User Registration Script")
    print("=" * 60)
    print(f"Reading users from: {csv_filename}")
    print(f"API Endpoint: {REGISTER_ENDPOINT}")
    print(f"Request delay: {REQUEST_DELAY}s between requests")
    print("=" * 60)
    print()
    
    # Read users from CSV
    users = read_users_from_csv(csv_filename)
    
    if not users:
        return
    
    total_users = len(users)
    print(f"Found {total_users} users to register\n")
    
    # Statistics
    successful = 0
    failed = 0
    duplicates = 0
    errors = []
    
    # Process each user
    start_time = time.time()
    
    for index, user in enumerate(users, 1):
        print(f"[{index}/{total_users}] Registering: {user['username']} ({user['userMail']})", end=" ... ")
        
        success, message, status_code = register_user(user)
        
        if success:
            print("✅ SUCCESS")
            successful += 1
        elif status_code == 409:  # Conflict - duplicate
            print(f"⚠️  DUPLICATE (already exists)")
            duplicates += 1
        else:
            print(f"❌ FAILED: {message}")
            failed += 1
            errors.append({
                "user": user['username'],
                "email": user['userMail'],
                "error": message,
                "status": status_code
            })
        
        # Add delay between requests (except for the last one)
        if index < total_users:
            time.sleep(REQUEST_DELAY)
    
    elapsed_time = time.time() - start_time
    
    # Print summary
    print("\n" + "=" * 60)
    print("REGISTRATION SUMMARY")
    print("=" * 60)
    print(f"Total users processed: {total_users}")
    print(f"✅ Successful: {successful}")
    print(f"⚠️  Duplicates: {duplicates}")
    print(f"❌ Failed: {failed}")
    print(f"⏱️  Total time: {elapsed_time:.2f} seconds")
    print(f"📊 Average time per request: {elapsed_time/total_users:.2f} seconds")
    print("=" * 60)
    
    # Save errors to file if any
    if errors:
        error_filename = "registration_errors.json"
        with open(error_filename, 'w', encoding='utf-8') as f:
            json.dump(errors, f, indent=2)
        print(f"\n⚠️  {len(errors)} errors occurred. Details saved to: {error_filename}")
    
    if successful > 0:
        print(f"\n✅ Successfully registered {successful} users!")

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  Registration interrupted by user.")
        print("   Partial registrations may have occurred.")



