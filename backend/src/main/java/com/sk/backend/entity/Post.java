package com.sk.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;
    private String content;
    private String authorId;
    private int views;
    private boolean hasAttachment;
    private LocalDateTime regDate;
    private LocalDateTime updDate;

    @PrePersist
    public void onCreate() {
        this.regDate = LocalDateTime.now();
        this.updDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updDate = LocalDateTime.now();
    }
}
