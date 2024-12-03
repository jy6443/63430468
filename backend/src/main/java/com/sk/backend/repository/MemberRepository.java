package com.sk.backend.repository;

import com.sk.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);

    @Query("SELECT COUNT(*) > 0 FROM Member WHERE nickname = ?1")
    boolean checkNickname(String nickname);

    Member save(Member member);
}
