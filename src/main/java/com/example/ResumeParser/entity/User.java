package com.example.ResumeParser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data // includes @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
}
