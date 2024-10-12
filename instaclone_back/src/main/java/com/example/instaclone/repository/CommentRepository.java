package com.example.instaclone.repository;

import com.example.instaclone.entity.Comment;
import com.example.instaclone.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPost(Post post);

    List<Comment> findAllByPostOrderByCreatedAtDesc(Post post);

    Optional<Comment> findByIdAndUserId(Long commentId, Long userId);
}
