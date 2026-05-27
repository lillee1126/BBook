package com.enterprise.news.controller;

import com.enterprise.news.common.ApiResponse;
import com.enterprise.news.common.PageResponse;
import com.enterprise.news.dto.post.CommentRequest;
import com.enterprise.news.dto.post.CommentResponse;
import com.enterprise.news.dto.post.PostDetailResponse;
import com.enterprise.news.dto.post.PostRequest;
import com.enterprise.news.dto.post.PostSummaryResponse;
import com.enterprise.news.service.PostService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tagCode,
            @RequestParam(required = false) String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(postService.page(keyword, tagCode, authorId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable String id) {
        return ApiResponse.ok(postService.getById(id, true));
    }

    @PostMapping
    public ApiResponse<PostDetailResponse> create(@Valid @RequestBody PostRequest request) {
        return ApiResponse.ok("发布成功", postService.create(request));
    }

    @PostMapping("/{id}/likes")
    public ApiResponse<Map<String, Object>> like(@PathVariable String id) {
        Map<String, Object> data = postService.toggleLike(id);
        String message = Boolean.TRUE.equals(data.get("liked")) ? "点赞成功" : "已取消点赞";
        return ApiResponse.ok(message, data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        postService.deletePost(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<CommentResponse> comment(@PathVariable String id, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.ok("评论成功", postService.addComment(id, request));
    }

    @PostMapping("/{postId}/comments/{commentId}/likes")
    public ApiResponse<Map<String, Object>> likeComment(@PathVariable String postId, @PathVariable String commentId) {
        Map<String, Object> data = postService.toggleCommentLike(postId, commentId);
        String message = Boolean.TRUE.equals(data.get("liked")) ? "点赞成功" : "已取消点赞";
        return ApiResponse.ok(message, data);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String postId, @PathVariable String commentId) {
        postService.deleteComment(postId, commentId);
        return ApiResponse.ok("删除成功", null);
    }
}