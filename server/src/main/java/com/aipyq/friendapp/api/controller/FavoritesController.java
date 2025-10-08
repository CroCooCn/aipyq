package com.aipyq.friendapp.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.aipyq.friendapp.store.InMemoryStore;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    public static class FavReq { public String generationId; public Boolean favorite; }

    @PostMapping
    public ResponseEntity<Map<String, Object>> toggle(@RequestBody FavReq req) {
        if (req.generationId != null && req.favorite != null) {
            InMemoryStore.get().setFavorite(req.generationId, req.favorite);
        }
        Map<String,Object> toReturn=new HashMap<>();
        toReturn.put("ok", true);
        return ResponseEntity.ok(toReturn);
    }
}
