package com.sk.backend.controller;

import com.sk.backend.config.JwtTokenProvider;
import com.sk.backend.entity.Post;
import com.sk.backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(@RequestParam(name = "keyword", defaultValue = "") String keyword,
                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<Post> posts = postService.getAllPosts(keyword, page, size);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPostByPostId(@PathVariable("postId") Long postId, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Map<String, Object> response = postService.getPostByPostId(postId, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Post> createPost(@RequestBody Post post, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        Post newPost = postService.createPost(post, token);
        return ResponseEntity.status(201).body(newPost);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable("postId") Long postId,
                                           @RequestBody Post post, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        Optional<Post> newPost = postService.updatePost(postId, post, token);
        log.info("new post: {}", newPost);
        return newPost.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId,
                                           HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        boolean isDeleted = postService.deletePost(postId, token);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
