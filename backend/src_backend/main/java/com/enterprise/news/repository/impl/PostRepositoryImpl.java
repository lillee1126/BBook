package com.enterprise.news.repository.impl;

import com.enterprise.news.common.PageResponse;
import com.enterprise.news.model.Post;
import com.enterprise.news.repository.PostRepositoryCustom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PostRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PageResponse<Post> search(String keyword, String tagCode, String authorId, int page, int size) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            String regex = ".*" + Pattern.quote(keyword.trim()) + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("summary").regex(regex, "i"),
                    Criteria.where("content").regex(regex, "i"),
                    Criteria.where("topics").regex(regex, "i"),
                    Criteria.where("tagName").regex(regex, "i")
            ));
        }
        if (StringUtils.hasText(tagCode)) {
            criteriaList.add(Criteria.where("tagCode").is(tagCode));
        }
        if (StringUtils.hasText(authorId)) {
            criteriaList.add(Criteria.where("authorId").is(authorId));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(Criteria[]::new)));
        }

        long total = mongoTemplate.count(query, Post.class);
        query.with(Sort.by(Sort.Order.desc("createdAt")));
        query.skip((long) page * size);
        query.limit(size);

        List<Post> records = mongoTemplate.find(query, Post.class);
        int totalPages = size <= 0 ? 0 : (int) Math.ceil(total * 1.0 / size);
        return new PageResponse<>(records, total, page, size, totalPages);
    }

    @Override
    public long sumLikeCountByAuthorId(String authorId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("authorId").is(authorId)),
                Aggregation.group().sum("likeCount").as("totalLikes")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "bb_posts", Document.class);
        Document unique = results.getUniqueMappedResult();
        if (unique == null || unique.get("totalLikes") == null) {
            return 0L;
        }
        Object value = unique.get("totalLikes");
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}