package com.sk.backend.repository;

import com.sk.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByTitleContainingOrAuthorIdContaining(String title, String authorId, Pageable pageable);
    Optional<Post> findByPostId(Long postId);
}
