package com.enterprise.news.service;

import com.enterprise.news.common.PageResponse;
import com.enterprise.news.dto.post.CommentRequest;
import com.enterprise.news.dto.post.CommentResponse;
import com.enterprise.news.dto.post.PostDetailResponse;
import com.enterprise.news.dto.post.PostRequest;
import com.enterprise.news.dto.post.PostSummaryResponse;
import java.util.Map;

public interface PostService {

    PageResponse<PostSummaryResponse> page(String keyword, String tagCode, String authorId, int page, int size);

    PostDetailResponse getById(String id, boolean increaseView);

    PostDetailResponse create(PostRequest request);

    Map<String, Object> toggleLike(String id);

    CommentResponse addComment(String id, CommentRequest request);

    Map<String, Object> toggleCommentLike(String postId, String commentId);

    void deletePost(String id);

    void deleteComment(String postId, String commentId);
}