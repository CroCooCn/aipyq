package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.Persona;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private Persona inMemory = new Persona();

    @GetMapping
    public ResponseEntity<Persona> get() {
        return ResponseEntity.ok(inMemory);
    }

    @PutMapping
    public ResponseEntity<Void> put(@RequestBody Persona persona) {
        this.inMemory = persona;
        return ResponseEntity.ok().build();
    }
}

