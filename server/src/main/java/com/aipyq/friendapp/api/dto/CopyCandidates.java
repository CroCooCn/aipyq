package com.aipyq.friendapp.api.dto;

import java.util.List;

public class CopyCandidates {
    public static class Item {
        private String id;
        private String text;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    private List<Item> items;
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}

