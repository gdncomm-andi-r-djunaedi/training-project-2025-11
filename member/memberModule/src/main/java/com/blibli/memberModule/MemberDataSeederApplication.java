package com.blibli.memberModule;

import com.blibli.memberModule.entity.Member;
import com.blibli.memberModule.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@SpringBootApplication
public class MemberDataSeederApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberDataSeederApplication.class, args);
    }
}

@Slf4j
//@Component
class MemberDataSeeder implements CommandLineRunner {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Mike", "Sarah", "David", "Emily", "Chris", "Lisa",
        "Tom", "Amy", "James", "Emma", "Robert", "Olivia", "William", "Sophia",
        "Richard", "Isabella", "Joseph", "Mia", "Thomas", "Charlotte", "Charles", "Amelia"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas", "Taylor"
    };
    
    @Override
    public void run(String... args) {
        log.info("Starting to create 5000 member records...");
        
        String passwordFile = "member_passwords.csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(passwordFile))) {
            writer.println("Email,Password,Full Name,Phone Number");
            
            int count = 0;
            for (int i = 1; i <= 5000; i++) {
                try {
                    MemberData memberData = createRandomMember(i);
                    
                    memberRepository.save(memberData.member);
                    
                    writer.println(String.format("%s,%s,%s,%s",
                        memberData.email,
                        memberData.plainPassword,
                        memberData.member.getFull_name(),
                        memberData.member.getPhoneNo()
                    ));
                    
                    count++;
                    
                    if (i % 500 == 0) {
                        log.info("Created {} members...", i);
                    }
                } catch (Exception e) {
                    log.error("Error creating member {}: {}", i, e.getMessage());
                }
            }
            
            log.info("Successfully created {} member records!", count);
            log.info("Passwords saved to: {}", passwordFile);
            
        } catch (IOException e) {
            log.error("Error writing password file: {}", e.getMessage());
        }
        
        System.exit(0);
    }
    
    private MemberData createRandomMember(int index) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String fullName = firstName + " " + lastName;
        
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + index + "@example.com";
        
        String phoneNo = generatePhoneNumber();
        
        String plainPassword = generateValidPassword();
        
        Member member = new Member();
        member.setEmail(email);
        member.setFull_name(fullName);
        member.setPhoneNo(phoneNo);
        member.setPassword(passwordEncoder.encode(plainPassword)); // Hash the password
        
        return new MemberData(member, email, plainPassword);
    }
    
    private String generatePhoneNumber() {
        StringBuilder phone = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            phone.append(random.nextInt(10));
        }
        return phone.toString();
    }
    
    private String generateValidPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "@#$%^&+=!";
        
        StringBuilder password = new StringBuilder();
        
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        int remainingLength = 4 + random.nextInt(13); // 4 to 16 more chars
        String allChars = uppercase + lowercase + numbers + special;
        for (int i = 0; i < remainingLength; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    private static class MemberData {
        Member member;
        String email;
        String plainPassword;
        
        MemberData(Member member, String email, String plainPassword) {
            this.member = member;
            this.email = email;
            this.plainPassword = plainPassword;
        }
    }
}

