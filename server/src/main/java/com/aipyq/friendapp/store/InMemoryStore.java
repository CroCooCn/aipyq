package com.aipyq.friendapp.store;

import com.aipyq.friendapp.api.dto.Generation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryStore {
    private static final InMemoryStore INSTANCE = new InMemoryStore();
    public static InMemoryStore get() { return INSTANCE; }

    private final List<Generation> generations = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFile;

    private InMemoryStore() {
        Path dir = Paths.get("server", "data");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        dataFile = dir.resolve("store.json");
        load();
    }

    public synchronized void addGeneration(Generation g) {
        generations.add(0, g);
        save();
    }

    public synchronized Generation addManualGeneration(String copyText, String imageId) {
        Generation g = new Generation();
        g.setId(UUID.randomUUID().toString());
        g.setImageId(imageId == null ? "" : imageId);
        g.setSelectedCopy(copyText);
        g.setCreatedAt(OffsetDateTime.now());
        generations.add(0, g);
        save();
        return g;
    }

    public synchronized Map<String, Object> pageGenerations(int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(generations.size(), from + size);
        List<Generation> items = from >= to ? List.of() : generations.subList(from, to);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items.stream().map(this::toMap).collect(Collectors.toList()));
        resp.put("total", generations.size());
        return resp;
    }


    public synchronized boolean deleteGeneration(String id) {
        boolean removed = generations.removeIf(g -> Objects.equals(g.getId(), id));
        // favorites removed
        if (removed) save();
        return removed;
    }

    private Map<String, Object> toMap(Generation g) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", g.getId());
        m.put("imageId", g.getImageId());
        m.put("selectedCopy", g.getSelectedCopy());
        m.put("createdAt", g.getCreatedAt());
        // favorites removed
        return m;
    }

    private void load() {
        if (!Files.exists(dataFile)) return;
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(Files.readAllBytes(dataFile));
            generations.clear();
            if (root.has("generations") && root.get("generations").isArray()) {
                for (var it : root.get("generations")) {
                    Generation g = new Generation();
                    g.setId(it.path("id").asText());
                    g.setImageId(it.path("imageId").asText());
                    g.setSelectedCopy(it.path("selectedCopy").asText());
                    String ts = it.path("createdAt").asText();
                    if (ts != null && !ts.isBlank()) g.setCreatedAt(OffsetDateTime.parse(ts));
                    generations.add(g);
                }
            }
            // favorites removed
        } catch (Exception ignored) {}
    }

    private void save() {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.putArray("generations").addAll(
                    generations.stream().map(g -> {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("id", g.getId());
                        n.put("imageId", g.getImageId());
                        n.put("selectedCopy", g.getSelectedCopy());
                        n.put("createdAt", g.getCreatedAt() != null ? g.getCreatedAt().toString() : "");
                        return n;
                    }).collect(Collectors.toList())
            );
            // favorites removed
            Files.writeString(dataFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (IOException ignored) {}
    }
}
