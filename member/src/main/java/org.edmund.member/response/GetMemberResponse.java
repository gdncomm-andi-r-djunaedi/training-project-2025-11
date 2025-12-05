package org.edmund.member.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberResponse {

    private Long id;
    private String email;
    private String fullName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}