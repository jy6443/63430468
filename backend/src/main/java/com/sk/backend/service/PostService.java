package com.sk.backend.service;

import com.sk.backend.config.JwtTokenProvider;
import com.sk.backend.entity.Post;
import com.sk.backend.repository.PostRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    // post 생성하는 메서드
    @Transactional
    public Post createPost(Post post, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        String nickname = memberService.getNicknameByEmail(email);
        post.setAuthorId(nickname);
        post.setViews(0);
        return postRepository.save(post);
    }

    // post 목록 페이지네이션으로 불러오는 메서드
    public Page<Post> getAllPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "regDate"));
        if (keyword == null || keyword.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }
        return postRepository.findByTitleContainingOrAuthorIdContaining(keyword, keyword, pageable);
    }

    // post Id 로 게시글 조회하는 메서드 -> 조회수 증가
    @Transactional
    public Map<String, Object> getPostByPostId(Long postId, String token) {
        Map<String, Object> result = new HashMap<>();
        Optional<Post> post = postRepository.findByPostId(postId);
        post.ifPresent(p -> {
            p.setViews(p.getViews() + 1);
            postRepository.save(p);
        });
        result.put("post", post);
        if (post.isPresent()) {
            Post postTemp = post.get();
            String email = jwtTokenProvider.getEmailFromToken(token);
            String nicknamea = memberService.getNicknameByEmail(email);
            String nicknameb = postTemp.getAuthorId();
            if (nicknamea.equals(nicknameb)) {
                result.put("isMine", true);
            } else {
                result.put("isMine", false);
            }
        }
        return result;
    }

    // post 수정하는 메서드
    @Transactional
    public Optional<Post> updatePost(Long postId, Post updatedPost, String token) {
        Optional<Post> post = postRepository.findById(postId);
        String email = jwtTokenProvider.getEmailFromToken(token);
        String authorId = memberService.getNicknameByEmail(email);
        if (post.isPresent() && post.get().getAuthorId().equals(authorId)) {
            Post existingPost = post.get();
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setContent(updatedPost.getContent());
            existingPost.setHasAttachment(updatedPost.isHasAttachment());
            return Optional.of(postRepository.save(existingPost));
        }
        return Optional.empty();
    }

    // post 삭제하는 메서드
    @Transactional
    public boolean deletePost(Long postId, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        String authorId = memberService.getNicknameByEmail(email);
        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent() && post.get().getAuthorId().equals(authorId)) {
            postRepository.delete(post.get());
            return true;
        }
        return false;
    }


}
