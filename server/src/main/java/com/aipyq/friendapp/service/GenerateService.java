package com.aipyq.friendapp.service;

import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;
import com.aipyq.friendapp.api.dto.BatchCopyRequest;
import com.aipyq.friendapp.config.PromptProperties;
import com.aipyq.friendapp.persistence.entity.CopyHistoryEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class GenerateService {

    private static final String FALLBACK_MESSAGE = "\u6587\u6848\u751F\u6210\u5931\u8D25\uFF0C\u8BF7\u7A0D\u540E\u91CD\u8BD5\u3002";
    private static final String DEFAULT_INSTRUCTION = "\u6309\u7167\u7247\u6C1B\u56F4\u81EA\u7531\u53D1\u6325";
    private static final List<String> DEFAULT_REQUIREMENTS = List.of(
            "\u751F\u6210 3 \u6761 40-120 \u5B57\u7684\u4E2D\u6587\u6587\u6848\uFF0C\u6BCF\u6761\u72EC\u7ACB\u6210\u6BB5\u3002",
            "\u6587\u6848\u8BED\u6C14\u9700\u8D34\u5408\u6837\u5F0F\u6307\u4EE4\uFF0C\u9002\u5EA6\u4F7F\u7528 emoji\u3002",
            "\u4E0D\u8981\u63CF\u8FF0\u6A21\u578B\u6216\u751F\u6210\u8FC7\u7A0B\uFF0C\u4E5F\u4E0D\u8981\u63D0\u53CA AI\u3002"
    );
    private static final String HISTORY_EMPTY = "\u6682\u65E0\u5386\u53F2\u4E0A\u4E0B\u6587\uFF0C\u53EF\u7ED3\u5408\u56FE\u7247\u5185\u5BB9\u4E0E\u5F53\u524D\u6837\u5F0F\u6307\u4EE4\u76F4\u63A5\u521B\u4F5C\u3002";
    private static final String HISTORY_HEADER = "\u5386\u53F2\u4E0A\u4E0B\u6587\uFF08\u4F9B\u53C2\u8003\uFF09\uFF1A";
    private static final String HISTORY_ITEM_TEMPLATE = "- \u6837\u5F0F\u6307\u4EE4\uFF1A{{instruction}}\n  \u6587\u6848\uFF1A\n{{outputs}}";
    private static final String NO_INSTRUCTION = "\uFF08\u672A\u63D0\u4F9B\u6307\u4EE4\uFF09";
    private static final String DEFAULT_TEMPLATE =
            "\u4F60\u662F\u4E00\u540D\u8D44\u6DF1\u7684\u4E2D\u6587\u793E\u4EA4\u5A92\u4F53\u6587\u6848\u521B\u4F5C\u8005\uFF0C\u8BF7\u57FA\u4E8E\u4E0B\u8FF0\u4FE1\u606F\u8F93\u51FA\u65B0\u7684\u670B\u53CB\u5708\u6587\u6848\uFF1A\n"
                    + "\u56FE\u7247\u5730\u5740\uFF1A{{imageUrl}}\n"
                    + "\u6837\u5F0F\u6307\u4EE4\uFF1A{{styleInstruction}}\n"
                    + "\u82E5\u63D0\u4F9B\u591A\u5F20\u56FE\u7247\uFF0C\u8BF7\u7EDF\u4E00\u521B\u4F5C\u4E00\u5957\u9002\u7528\u7684\u6587\u6848\u3002\n"
                    + "{{historySection}}\n"
                    + "\u8F93\u51FA\u8981\u6C42\uFF1A\n"
                    + "{{outputRequirements}}";
    private static final String BATCH_HISTORY_SUFFIX_TEMPLATE = "\uFF08\u6279\u91CF\uFF1A\u5171123\u5F20\u56FE\u7247\uFF09";

    private final LLMProvider llmProvider;
    private final CopyHistoryService copyHistoryService;
    private final PromptProperties promptProperties;

    public GenerateService(LLMProvider llmProvider,
                           CopyHistoryService copyHistoryService,
                           PromptProperties promptProperties) {
        this.llmProvider = llmProvider;
        this.copyHistoryService = copyHistoryService;
        this.promptProperties = promptProperties;
    }

    public CopyCandidates generate(CopyHistoryService.HistoryIdentity identity, CopyRequest request) {
        List<String> imageUrls = sanitizeImageList(request.getImageUrl() == null ? null : List.of(request.getImageUrl()));
        String instruction = safeInstruction(request.getStyleInstruction());
        List<CopyHistoryEntity> previous = imageUrls.isEmpty()
                ? List.of()
                : copyHistoryService.listHistoryForImage(identity, imageUrls.get(0));
        String prompt = buildPrompt(imageUrls, instruction, previous);
        List<String> outputs = runModel(prompt, imageUrls, Boolean.TRUE.equals(request.getReasoningOn()));

        if (!imageUrls.isEmpty()) {
            copyHistoryService.appendHistory(
                    identity,
                    imageUrls.get(0),
                    instruction,
                    outputs,
                    CopyHistoryService.Source.GENERATE
            );
        } else {
            copyHistoryService.appendHistory(
                    identity,
                    "text-only-" + UUID.randomUUID(),
                    instruction,
                    outputs,
                    CopyHistoryService.Source.GENERATE
            );
        }

        CopyCandidates candidates = toCandidates(outputs);
        candidates.setRoundId("round-" + UUID.randomUUID());
        return candidates;
    }

    public CopyCandidates generateBatch(CopyHistoryService.HistoryIdentity identity, BatchCopyRequest request) {
        List<String> imageUrls = sanitizeImageList(request.getImageUrls());
        if (imageUrls.isEmpty()) {
            throw new IllegalArgumentException("batch request requires at least one imageUrl");
        }
        String instruction = safeInstruction(request.getStyleInstruction());
        List<CopyHistoryEntity> history = collectBatchHistory(identity, imageUrls);
        String prompt = buildPrompt(imageUrls, instruction, history);
        List<String> outputs = runModel(prompt, imageUrls, Boolean.TRUE.equals(request.getReasoningOn()));

        String historyInstruction = formatBatchInstruction(instruction, imageUrls.size());
        for (String imageUrl : imageUrls) {
            copyHistoryService.appendHistory(
                    identity,
                    imageUrl,
                    historyInstruction,
                    outputs,
                    CopyHistoryService.Source.BATCH
            );
        }

        CopyCandidates candidates = toCandidates(outputs);
        candidates.setRoundId("batch-" + UUID.randomUUID());
        return candidates;
    }

    private List<String> runModel(String prompt, List<String> imageUrls, boolean reasoningOn) {
        List<String> outputs = llmProvider.generateCopy(prompt, imageUrls, reasoningOn);
        if (CollectionUtils.isEmpty(outputs)) {
            outputs = List.of(FALLBACK_MESSAGE);
        }
        if (outputs.size() > 3) {
            outputs = outputs.subList(0, 3);
        }
        return outputs;
    }

    private CopyCandidates toCandidates(List<String> outputs) {
        CopyCandidates candidates = new CopyCandidates();
        List<CopyCandidates.Item> items = new ArrayList<>();
        for (String text : outputs) {
            CopyCandidates.Item item = new CopyCandidates.Item();
            item.setId(UUID.randomUUID().toString());
            item.setText(text.trim());
            items.add(item);
        }
        candidates.setItems(items);
        return candidates;
    }

    private String buildPrompt(List<String> imageUrls, String instruction, List<CopyHistoryEntity> history) {
        PromptProperties.Copy cfg = promptProperties.getCopy();
        String template = StringUtils.hasText(cfg.getTemplate())
                ? cfg.getTemplate()
                : DEFAULT_TEMPLATE;

        String historySection = formatHistory(history, cfg);
        String outputRequirements = formatRequirements(cfg.getOutputRequirements());

        return template
                .replace("{{imageUrl}}", formatImageSection(imageUrls))
                .replace("{{styleInstruction}}", instruction)
                .replace("{{historySection}}", historySection)
                .replace("{{outputRequirements}}", outputRequirements);
    }

    private String safeInstruction(String instruction) {
        return StringUtils.hasText(instruction) ? instruction.trim() : DEFAULT_INSTRUCTION;
    }

    private List<String> sanitizeImageList(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String url : imageUrls) {
            if (StringUtils.hasText(url)) {
                cleaned.add(url.trim());
            }
        }
        return cleaned;
    }

    private String formatImageSection(List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return "\u6682\u65E0\u56FE\u7247\u4FE1\u606F";
        }
        if (imageUrls.size() == 1) {
            return imageUrls.get(0);
        }
        return imageUrls.stream()
                .map(url -> "- " + url)
                .collect(Collectors.joining("\n"));
    }

    private String formatRequirements(List<String> requirements) {
        List<String> items = CollectionUtils.isEmpty(requirements) ? DEFAULT_REQUIREMENTS : requirements;
        return items.stream()
                .map(item -> item.startsWith("-") ? item : "- " + item)
                .collect(Collectors.joining("\n"));
    }

    private String formatHistory(List<CopyHistoryEntity> history, PromptProperties.Copy cfg) {
        if (history == null || history.isEmpty()) {
            String empty = cfg.getHistoryEmpty();
            return StringUtils.hasText(empty) ? empty : HISTORY_EMPTY;
        }
        String header = StringUtils.hasText(cfg.getHistoryHeader()) ? cfg.getHistoryHeader() : HISTORY_HEADER;
        String itemTemplate = StringUtils.hasText(cfg.getHistoryItem()) ? cfg.getHistoryItem() : HISTORY_ITEM_TEMPLATE;

        List<CopyHistoryEntity> ordered = history.stream()
                .sorted(Comparator.comparing(
                        CopyHistoryEntity::getSequenceNo,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .collect(Collectors.toList());
        List<String> formatted = new ArrayList<>();
        for (CopyHistoryEntity entity : ordered) {
            List<String> outputs = copyHistoryService.extractOutputs(entity);
            String outputText = outputs.stream()
                    .map(text -> "    \u2022 " + text)
                    .collect(Collectors.joining("\n"));
            String historyInstruction = StringUtils.hasText(entity.getInstruction())
                    ? entity.getInstruction()
                    : NO_INSTRUCTION;
            formatted.add(itemTemplate
                    .replace("{{instruction}}", historyInstruction)
                    .replace("{{outputs}}", outputText));
        }
        return header + "\n" + String.join("\n", formatted);
    }

    private List<CopyHistoryEntity> collectBatchHistory(CopyHistoryService.HistoryIdentity identity, List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return List.of();
        }
        List<CopyHistoryEntity> merged = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        for (String imageUrl : imageUrls) {
            List<CopyHistoryEntity> perImage = copyHistoryService.listHistoryForImage(identity, imageUrl);
            for (CopyHistoryEntity entity : perImage) {
                Long id = entity.getId();
                if (id == null || seenIds.add(id)) {
                    merged.add(entity);
                }
            }
        }
        return merged;
    }

    private String formatBatchInstruction(String instruction, int count) {
        String suffix = BATCH_HISTORY_SUFFIX_TEMPLATE.replace("123", String.valueOf(count));
        return instruction + " " + suffix;
    }
}
