package com.enterprise.news.repository;

import com.enterprise.news.common.PageResponse;
import com.enterprise.news.model.Post;

public interface PostRepositoryCustom {

    PageResponse<Post> search(String keyword, String tagCode, String authorId, int page, int size);

    long sumLikeCountByAuthorId(String authorId);
}