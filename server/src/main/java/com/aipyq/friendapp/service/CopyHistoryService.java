package com.aipyq.friendapp.service;

import com.aipyq.friendapp.persistence.entity.CopyHistoryEntity;
import com.aipyq.friendapp.persistence.mapper.CopyHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CopyHistoryService {
    public enum Source {
        MANUAL,
        GENERATE,
        BATCH,
        RENDER
    }

    public record HistoryIdentity(String userId, String clientKey) {
        public HistoryIdentity {
            userId = normalize(userId);
            clientKey = normalize(clientKey);
        }

        private static String normalize(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return value;
        }

        public boolean hasUser() {
            return userId != null;
        }

        public boolean hasClientKey() {
            return clientKey != null;
        }

        public static HistoryIdentity forUser(String userId) {
            return new HistoryIdentity(userId, null);
        }

        public static HistoryIdentity forClient(String clientKey) {
            return new HistoryIdentity(null, clientKey);
        }

        public HistoryIdentity withClientKey(String newClientKey) {
            return new HistoryIdentity(userId, newClientKey);
        }
    }

    private final CopyHistoryMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<String>> listType = new TypeReference<>() {};

    public CopyHistoryService(CopyHistoryMapper mapper) {
        this.mapper = mapper;
    }

    public CopyHistoryEntity appendHistory(HistoryIdentity identity,
                                           String imageId,
                                           String instruction,
                                           List<String> copyTexts,
                                           Source source) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(imageId, "imageId must not be null");
        Objects.requireNonNull(copyTexts, "copyTexts must not be null");
        CopyHistoryEntity entity = new CopyHistoryEntity();
        entity.setUserId(identity.userId());
        entity.setClientKey(identity.clientKey());
        entity.setImageId(imageId);
        entity.setInstruction(instruction);
        entity.setCopyText(serialize(copyTexts));
        entity.setSource(source.name().toLowerCase());
        entity.setSequenceNo(nextSequence(identity, imageId));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        mapper.insert(entity);
        return entity;
    }

    public CopyHistoryEntity appendHistory(HistoryIdentity identity,
                                           String imageId,
                                           String instruction,
                                           String copyText,
                                           Source source) {
        return appendHistory(identity, imageId, instruction, List.of(copyText), source);
    }

    public List<CopyHistoryEntity> listHistoryForImage(HistoryIdentity identity, String imageId) {
        if (identity == null || imageId == null || imageId.isBlank()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<CopyHistoryEntity> wrapper = baseWrapper(identity)
                .eq(CopyHistoryEntity::getImageId, imageId)
                .orderByAsc(CopyHistoryEntity::getSequenceNo);
        return mapper.selectList(wrapper);
    }

    public IPage<CopyHistoryEntity> pageHistory(HistoryIdentity identity, int page, int size) {
        if (identity == null) {
            return Page.of(page, size);
        }
        LambdaQueryWrapper<CopyHistoryEntity> wrapper = baseWrapper(identity)
                .orderByDesc(CopyHistoryEntity::getCreatedAt);
        return mapper.selectPage(Page.of(page, size), wrapper);
    }

    public boolean delete(HistoryIdentity identity, Long id) {
        if (identity == null || id == null) {
            return false;
        }
        LambdaQueryWrapper<CopyHistoryEntity> wrapper = baseWrapper(identity)
                .eq(CopyHistoryEntity::getId, id);
        return mapper.delete(wrapper) > 0;
    }

    public List<String> extractOutputs(CopyHistoryEntity entity) {
        if (entity == null || entity.getCopyText() == null || entity.getCopyText().isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> parsed = objectMapper.readValue(entity.getCopyText(), listType);
            return parsed.stream()
                    .map(text -> text == null ? "" : text.trim())
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return List.of(entity.getCopyText());
        }
    }

    private int nextSequence(HistoryIdentity identity, String imageId) {
        LambdaQueryWrapper<CopyHistoryEntity> wrapper = baseWrapper(identity)
                .eq(CopyHistoryEntity::getImageId, imageId)
                .orderByDesc(CopyHistoryEntity::getSequenceNo)
                .last("LIMIT 1");
        CopyHistoryEntity last = mapper.selectOne(wrapper);
        int previous = last != null && last.getSequenceNo() != null ? last.getSequenceNo() : 0;
        return previous + 1;
    }

    private LambdaQueryWrapper<CopyHistoryEntity> baseWrapper(HistoryIdentity identity) {
        LambdaQueryWrapper<CopyHistoryEntity> wrapper = Wrappers.lambdaQuery(CopyHistoryEntity.class);
        if (identity.hasUser()) {
            wrapper.eq(CopyHistoryEntity::getUserId, identity.userId());
        } else {
            wrapper.isNull(CopyHistoryEntity::getUserId);
        }
        if (identity.hasClientKey()) {
            wrapper.eq(CopyHistoryEntity::getClientKey, identity.clientKey());
        } else {
            wrapper.isNull(CopyHistoryEntity::getClientKey);
        }
        return wrapper;
    }

    private String serialize(List<String> copyTexts) {
        try {
            return objectMapper.writeValueAsString(copyTexts);
        } catch (Exception ex) {
            return String.join("\n", copyTexts);
        }
    }
}
