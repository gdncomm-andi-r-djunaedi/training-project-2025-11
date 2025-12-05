package org.edmund.member.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edmund.member.entity.Member;
import org.edmund.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberDataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // Inject ini buat hash password default
    private final Random random = new Random();

    private final int TOTAL_MEMBERS = 5000;
    private final int BATCH_SIZE = 500;

    // --- DATABASE NAMA ---
    private final String[] BATAK_MARGA = {"Siregar", "Nasution", "Lubis", "Simanjuntak", "Panjaitan", "Samosir", "Hasibuan", "Gultom", "Sihombing", "Manullang"};
    private final String[] BATAK_FIRST = {"Poltak", "Butet", "Ucok", "Tigor", "Hotman", "Bona", "Goklas", "Dame", "Horas", "Lastri"};

    private final String[] JAWA_FIRST = {"Budi", "Slamet", "Sri", "Joko", "Agus", "Siti", "Bambang", "Eko", "Dwi", "Tri"};
    private final String[] JAWA_LAST = {"Santoso", "Widodo", "Lestari", "Susilo", "Hartono", "Mulyono", "Purnomo", "Setiawan", "Wibowo", "Saputra"};

    private final String[] SUNDA_FIRST = {"Asep", "Cecep", "Dede", "Euis", "Iis", "Jajang", "Ujang", "Neng", "Kokom", "Dadang"};
    private final String[] SUNDA_LAST = {"Sunarya", "Hidayat", "Somantri", "Sutisna", "Permana", "Kurnia", "Wirakusumah", "Subagja", "Gumelar", "Sastranegara"};

    private final String[] UNIQUE_FIRST = {"James", "Jessica", "Michael", "Sarah", "David", "Chika", "Kenzo", "Yuki", "Alex", "Bella"};
    private final String[] UNIQUE_LAST = {"Wijaya", "Tan", "Smith", "Kusuma", "Lim", "Wonderland", "Skywalker", "Maverick", "Potter", "Stark"};


    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 [MEMBER-SEED] Membersihkan data lama...");
        memberRepository.deleteAll();

        log.info("🚀 [MEMBER-SEED] Memulai seeding 5.000 member...");
        long startTime = System.currentTimeMillis();

        // OPTIMASI: Hash password sekali saja biar ngebut!
        // Semua user passwordnya: "password123"
        String defaultPasswordHash = passwordEncoder.encode("password123");

        List<Member> batch = new ArrayList<>();

        for (int i = 0; i < TOTAL_MEMBERS; i++) {
            batch.add(generateRandomMember(i, defaultPasswordHash));

            if (batch.size() >= BATCH_SIZE) {
                memberRepository.saveAll(batch);
                batch.clear();
                log.info("✅ [SEEDING] Progress: {}/{}", (i + 1), TOTAL_MEMBERS);
            }
        }

        if (!batch.isEmpty()) {
            memberRepository.saveAll(batch);
        }

        long endTime = System.currentTimeMillis();
        log.info("🎉 [MEMBER-SEED] SELESAI! 5.000 member siap dalam {} ms", (endTime - startTime));
    }

    @PreDestroy
    public void onExit() {
        log.info("🛑 [SHUTDOWN] Menghapus 5.000 data member...");
        memberRepository.deleteAll();
        log.info("🗑️ [SHUTDOWN] Member database bersih!");
    }

    private Member generateRandomMember(int index, String passwordHash) {
        double chance = random.nextDouble();
        String fullName;

        // --- LOGIKA DISTRIBUSI 25% ---
        if (chance < 0.25) {
            // 1. Batak (Nama Depan + Marga)
            String first = BATAK_FIRST[random.nextInt(BATAK_FIRST.length)];
            String marga = BATAK_MARGA[random.nextInt(BATAK_MARGA.length)];
            fullName = first + " " + marga;

        } else if (chance < 0.50) {
            // 2. Jawa (Nama Depan + Nama Belakang Jawa)
            String first = JAWA_FIRST[random.nextInt(JAWA_FIRST.length)];
            String last = JAWA_LAST[random.nextInt(JAWA_LAST.length)];
            fullName = first + " " + last;

        } else if (chance < 0.75) {
            // 3. Sunda (Nama Khas Sunda)
            String first = SUNDA_FIRST[random.nextInt(SUNDA_FIRST.length)];
            String last = SUNDA_LAST[random.nextInt(SUNDA_LAST.length)];
            fullName = first + " " + last;

        } else {
            // 4. Campuran/Unik
            String first = UNIQUE_FIRST[random.nextInt(UNIQUE_FIRST.length)];
            String last = UNIQUE_LAST[random.nextInt(UNIQUE_LAST.length)];
            fullName = first + " " + last;
        }

        // Generate Email Unik (pakai index biar ga duplicate error)
        String emailName = fullName.toLowerCase().replace(" ", "") + index;
        String email = emailName + "@example.com";

        return Member.builder()
                .email(email)
                .fullName(fullName)
                .passwordHashed(passwordHash) // Pakai hash yang sudah disiapkan
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}