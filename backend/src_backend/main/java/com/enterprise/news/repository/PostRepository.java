package com.enterprise.news.repository;

import com.enterprise.news.model.Post;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {

    long countByAuthorId(String authorId);

    List<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId);
}