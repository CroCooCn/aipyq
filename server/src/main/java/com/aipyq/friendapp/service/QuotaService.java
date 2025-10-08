package com.aipyq.friendapp.service;

import com.aipyq.friendapp.config.QuotaProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuotaService {
    public enum Action { GENERATE, RENDER }

    public static class State {
        public boolean subscribed = false;
        public long expiresAtMillis = 0L;
        public int remainingCredits = 0; // for subscribed plan
        public String dayKey = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        public int visitorRemainingToday;
    }

    private final Map<String, State> store = new ConcurrentHashMap<>();
    private final QuotaProperties props;

    public QuotaService(QuotaProperties props) {
        this.props = props;
    }

    public State getState(String clientKey) {
        return store.computeIfAbsent(clientKey, k -> {
            State s = new State();
            s.visitorRemainingToday = props.getVisitorDailyCredits();
            return s;
        });
    }

    public boolean consume(String clientKey, Action action) {
        State s = getState(clientKey);
        rotateIfNeeded(s);
        int cost = (action == Action.GENERATE) ? props.getCostGenerate() : props.getCostRender();
        long now = System.currentTimeMillis();
        if (s.subscribed && s.expiresAtMillis > now) {
            if (s.remainingCredits >= cost) {
                s.remainingCredits -= cost;
                return true;
            }
        }
        // visitor path
        if (s.visitorRemainingToday >= cost) {
            s.visitorRemainingToday -= cost;
            return true;
        }
        return false;
    }

    public void grantMonthly(String clientKey) {
        State s = getState(clientKey);
        s.subscribed = true;
        s.expiresAtMillis = System.currentTimeMillis() + 30L * 24 * 3600 * 1000;
        s.remainingCredits = props.getMonthlyCredits();
    }

    public Map<String, Object> snapshot(String clientKey) {
        State s = getState(clientKey);
        rotateIfNeeded(s);
        return Map.of(
                "subscribed", s.subscribed,
                "expiresAt", s.expiresAtMillis,
                "remainingCredits", s.remainingCredits,
                "visitorRemainingToday", s.visitorRemainingToday,
                "costGenerate", props.getCostGenerate(),
                "costRender", props.getCostRender()
        );
    }

    private void rotateIfNeeded(State s) {
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        if (!today.equals(s.dayKey)) {
            s.dayKey = today;
            s.visitorRemainingToday = props.getVisitorDailyCredits();
        }
        if (s.subscribed && s.expiresAtMillis < System.currentTimeMillis()) {
            // expired
            s.subscribed = false;
            s.remainingCredits = 0;
        }
    }
}

