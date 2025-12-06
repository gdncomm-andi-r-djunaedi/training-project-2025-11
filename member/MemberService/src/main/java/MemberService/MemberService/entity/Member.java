package MemberService.MemberService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    private String id;

    @Column(unique = true,nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
