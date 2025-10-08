package com.aipyq.friendapp.store;

import com.aipyq.friendapp.api.dto.Generation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryStore {
    private static final InMemoryStore INSTANCE = new InMemoryStore();
    public static InMemoryStore get() { return INSTANCE; }

    private final List<Generation> generations = new CopyOnWriteArrayList<>();
    private final Set<String> favorites = Collections.synchronizedSet(new HashSet<>());

    public void addGeneration(Generation g) {
        generations.add(g);
    }

    public Map<String, Object> pageGenerations(int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(generations.size(), from + size);
        List<Generation> items = from >= to ? List.of() : generations.subList(from, to);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items.stream().map(g -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", g.getId());
            m.put("imageId", g.getImageId());
            m.put("selectedCopy", g.getSelectedCopy());
            m.put("createdAt", g.getCreatedAt());
            m.put("favorite", favorites.contains(g.getId()));
            return m;
        }).collect(Collectors.toList()));
        resp.put("total", generations.size());
        return resp;
    }

    public void setFavorite(String id, boolean fav) {
        if (fav) favorites.add(id); else favorites.remove(id);
    }
}

