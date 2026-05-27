package com.enterprise.news.repository;

import com.enterprise.news.model.TagCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TagCategoryRepository extends MongoRepository<TagCategory, String> {

    List<TagCategory> findAllByEnabledTrueOrderBySortAsc();

    Optional<TagCategory> findByCode(String code);
}