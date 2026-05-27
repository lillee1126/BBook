package com.enterprise.news.service.impl;

import com.enterprise.news.dto.tag.TagResponse;
import com.enterprise.news.mapper.BBookMapper;
import com.enterprise.news.repository.TagCategoryRepository;
import com.enterprise.news.service.TagService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl implements TagService {

    private final TagCategoryRepository tagCategoryRepository;

    public TagServiceImpl(TagCategoryRepository tagCategoryRepository) {
        this.tagCategoryRepository = tagCategoryRepository;
    }

    @Override
    public List<TagResponse> listEnabled() {
        return tagCategoryRepository.findAllByEnabledTrueOrderBySortAsc().stream()
                .map(BBookMapper::toTag)
                .toList();
    }
}