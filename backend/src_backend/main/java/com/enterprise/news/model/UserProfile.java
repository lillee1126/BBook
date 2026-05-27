package com.enterprise.news.model;

import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bb_users")
public class UserProfile extends BaseDocument {

    @Id
    private String id;

    @Indexed(name = "idx_user_username", unique = true)
    private String username;

    private String passwordHash;

    private String nickname;

    private String avatar;

    private String bio;

    private String location;

    private Set<String> likedPostIds = new HashSet<>();

    private Set<String> likedCommentKeys = new HashSet<>();

    @Indexed(name = "idx_user_token", unique = true)
    private String token;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Set<String> getLikedPostIds() { return likedPostIds; }
    public void setLikedPostIds(Set<String> likedPostIds) { this.likedPostIds = likedPostIds; }
    public Set<String> getLikedCommentKeys() { return likedCommentKeys; }
    public void setLikedCommentKeys(Set<String> likedCommentKeys) { this.likedCommentKeys = likedCommentKeys; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}