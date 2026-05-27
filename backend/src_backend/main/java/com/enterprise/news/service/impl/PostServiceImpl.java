package com.enterprise.news.service.impl;

import com.enterprise.news.common.PageResponse;
import com.enterprise.news.dto.post.CommentRequest;
import com.enterprise.news.dto.post.CommentResponse;
import com.enterprise.news.dto.post.PostDetailResponse;
import com.enterprise.news.dto.post.PostRequest;
import com.enterprise.news.dto.post.PostSummaryResponse;
import com.enterprise.news.exception.BusinessException;
import com.enterprise.news.mapper.BBookMapper;
import com.enterprise.news.model.Post;
import com.enterprise.news.model.PostComment;
import com.enterprise.news.model.TagCategory;
import com.enterprise.news.model.UserProfile;
import com.enterprise.news.repository.PostRepository;
import com.enterprise.news.repository.TagCategoryRepository;
import com.enterprise.news.repository.UserProfileRepository;
import com.enterprise.news.security.AuthContext;
import com.enterprise.news.service.PostService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagCategoryRepository tagCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final MongoTemplate mongoTemplate;

    public PostServiceImpl(PostRepository postRepository,
                           TagCategoryRepository tagCategoryRepository,
                           UserProfileRepository userProfileRepository,
                           MongoTemplate mongoTemplate) {
        this.postRepository = postRepository;
        this.tagCategoryRepository = tagCategoryRepository;
        this.userProfileRepository = userProfileRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PageResponse<PostSummaryResponse> page(String keyword, String tagCode, String authorId, int page, int size) {
        PageResponse<Post> source = postRepository.search(keyword, tagCode, authorId, page, size);
        UserProfile viewer = viewer();
        Set<String> likedPostIds = viewer != null && viewer.getLikedPostIds() != null ? viewer.getLikedPostIds() : Set.of();
        List<PostSummaryResponse> records = source.records().stream()
                .map(post -> BBookMapper.toSummary(post, likedPostIds.contains(post.getId())))
                .toList();
        return new PageResponse<>(records, source.total(), source.page(), source.size(), source.totalPages());
    }

    @Override
    public PostDetailResponse getById(String id, boolean increaseView) {
        Post post;
        if (increaseView) {
            Query query = new Query(Criteria.where("id").is(id));
            Update update = new Update().inc("viewCount", 1L);
            post = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Post.class);
        } else {
            post = postRepository.findById(id).orElse(null);
        }
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "内容不存在");
        }
        UserProfile viewer = viewer();
        Set<String> likedPostIds = viewer != null && viewer.getLikedPostIds() != null ? viewer.getLikedPostIds() : Set.of();
        Set<String> likedCommentKeys = viewer != null && viewer.getLikedCommentKeys() != null ? viewer.getLikedCommentKeys() : Set.of();
        List<CommentResponse> comments = post.getComments() == null ? List.of() : post.getComments().stream()
                .map(comment -> BBookMapper.toComment(comment, likedCommentKeys.contains(buildCommentLikeKey(id, comment.getId()))))
                .toList();
        return BBookMapper.toDetail(post, likedPostIds.contains(post.getId()), comments);
    }

    @Override
    public PostDetailResponse create(PostRequest request) {
        UserProfile user = currentUser();
        TagCategory tag = tagCategoryRepository.findById(request.tagId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "内容标签不存在"));
        Post post = new Post();
        post.setTitle(request.title().trim());
        post.setContent(request.content().trim());
        post.setSummary(buildSummary(request.summary(), request.content()));
        List<String> mediaUrls = cleanList(request.mediaUrls());
        List<String> images = cleanList(request.images());
        if (images.isEmpty() && !mediaUrls.isEmpty()) {
            images = mediaUrls.stream().filter(this::isImageUrl).toList();
        }
        post.setImages(images);
        post.setMediaUrls(mediaUrls.isEmpty() ? images : mediaUrls);
        String cover = StringUtils.hasText(request.coverImage()) ? request.coverImage().trim() : null;
        if (!StringUtils.hasText(cover) && !images.isEmpty()) {
            cover = images.get(0);
        }
        post.setCoverImage(StringUtils.hasText(cover) ? cover : null);
        post.setTagId(tag.getId());
        post.setTagCode(tag.getCode());
        post.setTagName(tag.getName());
        post.setTopics(cleanList(request.topics()));
        post.setAuthorId(user.getId());
        post.setAuthorUsername(user.getUsername());
        post.setAuthorNickname(user.getNickname());
        post.setAuthorAvatar(user.getAvatar());
        post.setViewCount(0L);
        post.setLikeCount(0L);
        post.setCommentCount(0);
        post.setComments(new ArrayList<>());
        post = postRepository.save(post);
        return getById(post.getId(), false);
    }

    @Override
    public Map<String, Object> toggleLike(String id) {
        UserProfile user = currentUser();
        Post post = postRepository.findById(id).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "内容不存在"));
        Set<String> likedPostIds = user.getLikedPostIds();
        boolean liked;
        if (likedPostIds != null && likedPostIds.contains(id)) {
            likedPostIds.remove(id);
            post.setLikeCount(Math.max(0L, defaultLong(post.getLikeCount()) - 1L));
            liked = false;
        } else {
            likedPostIds.add(id);
            post.setLikeCount(defaultLong(post.getLikeCount()) + 1L);
            liked = true;
        }
        userProfileRepository.save(user);
        postRepository.save(post);
        return Map.of("likeCount", post.getLikeCount(), "liked", liked);
    }

    @Override
    public CommentResponse addComment(String id, CommentRequest request) {
        UserProfile user = currentUser();
        if (!postRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "内容不存在");
        }
        PostComment comment = new PostComment();
        comment.setId(UUID.randomUUID().toString());
        comment.setUserId(user.getId());
        comment.setNickname(user.getNickname());
        comment.setAvatar(user.getAvatar());
        comment.setContent(request.content().trim());
        comment.setLikeCount(0L);
        comment.setCreatedAt(LocalDateTime.now());

        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update()
                .push("comments", comment)
                .inc("commentCount", 1);
        mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Post.class);
        return BBookMapper.toComment(comment, false);
    }

    @Override
    public Map<String, Object> toggleCommentLike(String postId, String commentId) {
        UserProfile user = currentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "内容不存在"));
        PostComment comment = findComment(post, commentId);
        String likeKey = buildCommentLikeKey(postId, commentId);
        Set<String> likedCommentKeys = user.getLikedCommentKeys();
        boolean liked;
        if (likedCommentKeys != null && likedCommentKeys.contains(likeKey)) {
            likedCommentKeys.remove(likeKey);
            comment.setLikeCount(Math.max(0L, defaultLong(comment.getLikeCount()) - 1L));
            liked = false;
        } else {
            likedCommentKeys.add(likeKey);
            comment.setLikeCount(defaultLong(comment.getLikeCount()) + 1L);
            liked = true;
        }
        userProfileRepository.save(user);
        postRepository.save(post);
        return Map.of("likeCount", comment.getLikeCount(), "liked", liked);
    }

    @Override
    public void deletePost(String id) {
        UserProfile user = currentUser();
        Post post = postRepository.findById(id).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "内容不存在"));
        if (!user.getId().equals(post.getAuthorId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能删除自己发布的内容");
        }
        postRepository.deleteById(id);
    }

    @Override
    public void deleteComment(String postId, String commentId) {
        UserProfile user = currentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "内容不存在"));
        PostComment comment = findComment(post, commentId);
        boolean canDelete = user.getId().equals(comment.getUserId()) || user.getId().equals(post.getAuthorId());
        if (!canDelete) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能删除自己发布的评论");
        }
        List<PostComment> comments = post.getComments() == null ? new ArrayList<>() : new ArrayList<>(post.getComments());
        comments.removeIf(item -> commentId.equals(item.getId()));
        post.setComments(comments);
        post.setCommentCount(Math.max(0, defaultInt(post.getCommentCount()) - 1));
        postRepository.save(post);
    }

    private PostComment findComment(Post post, String commentId) {
        if (post.getComments() == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        return post.getComments().stream()
                .filter(item -> commentId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "评论不存在"));
    }

    private UserProfile currentUser() {
        UserProfile user = AuthContext.getUser();
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return userProfileRepository.findById(user.getId()).orElse(user);
    }

    private UserProfile viewer() {
        UserProfile user = AuthContext.getUser();
        if (user == null) {
            return null;
        }
        return userProfileRepository.findById(user.getId()).orElse(user);
    }

    private String buildSummary(String summary, String content) {
        if (StringUtils.hasText(summary)) {
            return summary.trim();
        }
        String plain = content.replaceAll("\\s+", " ").trim();
        return plain.length() > 90 ? plain.substring(0, 90) + "..." : plain;
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private boolean isImageUrl(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }

    private String buildCommentLikeKey(String postId, String commentId) {
        return postId + ":" + commentId;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}