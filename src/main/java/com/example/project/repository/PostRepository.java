package com.example.project.repository;

import com.example.project.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("Update Post p SET p.content = ?2 WHERE p.id = ?1")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    int setContentById(Long id, String content);

    @Query("SELECT p FROM Post p JOIN p.user u")
    Page<Post> findAllFetch(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.user u WHERE p.content LIKE CONCAT('%', ?1, '%')")
    Page<Post> findAllFetchBySentence(String sentence, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.user u WHERE u.email = ?1 AND p.content LIKE CONCAT('%', ?2, '%')")
    Page<Post> findUserPostBySentence(String username, String sentence, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.user u WHERE u.email = ?1")
    Page<Post> findUserPost(String username, Pageable pageable);
}
