package com.gdn.faurihakim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Member.TABLE_NAME)
public class Member {

  public static final String TABLE_NAME = "members";

  @Id
  private String id;

  private String memberId;

  private String fullName;

  private String phoneNumber;

  private String email;

  private String password;

  @Version
  private Long version;

  @CreatedDate
  private Long createdDate;

  @CreatedBy
  private String createdBy;

  @LastModifiedDate
  private Long lastModifiedDate;

  @LastModifiedBy
  private String lastModifiedBy;
}
