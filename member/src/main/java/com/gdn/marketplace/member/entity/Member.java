package com.gdn.marketplace.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String email;
}
