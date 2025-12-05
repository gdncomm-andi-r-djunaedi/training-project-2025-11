package com.zasura.member.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class V1__Insert_Hashed_Users extends BaseJavaMigration {

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void migrate(Context context) throws Exception {
    // Use the JDBC connection provided by Flyway
    JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getConfiguration().getDataSource());

    // 1. Hash the password using your application logic
    String hashedPassword = passwordEncoder.encode("password");

    // 2. Execute the INSERT statement using the hashed value
    String sql =
        "INSERT INTO members (id,name, email, password,phone_number) VALUES (?,?, ?, ?, ?)";
    for (int i = 0; i < 5000; i++) {
      jdbcTemplate.update(sql,
          UUID.randomUUID(),
          "admin" + i,
          i + "admin@example.com",
          hashedPassword,
          "08136267" + (10000 - i + 1));
    }
  }
}
