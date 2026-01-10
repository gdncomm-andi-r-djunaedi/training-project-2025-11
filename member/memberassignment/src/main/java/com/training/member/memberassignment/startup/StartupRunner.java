package com.training.member.memberassignment.startup;

import com.training.member.memberassignment.seeder.MemberSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final MemberSeeder seeder;

    public StartupRunner(MemberSeeder seeder) {
        this.seeder = seeder;
    }

    @Override
    public void run(String... args) {
        seeder.seed();
    }
}