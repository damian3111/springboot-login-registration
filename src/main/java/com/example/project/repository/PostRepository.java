package com.example.project.repository;

import com.example.project.entity.Post;
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

}
