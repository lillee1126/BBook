package com.enterprise.news.repository;

import com.enterprise.news.model.UserProfile;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    boolean existsByUsername(String username);

    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findByToken(String token);
}