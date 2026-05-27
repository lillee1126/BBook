package com.enterprise.news.service;

import com.enterprise.news.dto.tag.TagResponse;
import java.util.List;

public interface TagService {

    List<TagResponse> listEnabled();
}