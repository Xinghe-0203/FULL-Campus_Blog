package com.example.edu_project.service.social;

import com.example.edu_project.vo.social.MessageVO;
import com.example.edu_project.vo.social.PresenceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-process SSE and presence registry for the single-JAR deployment.
 * Presence is intentionally ephemeral and does not require a database migration.
 */
@Slf4j
@Service
public class MessageRealtimeService {

    private static final long NO_TIMEOUT = 0L;
    private static final int MAX_RECENT_USERS = 10_000;

    private final ConcurrentHashMap<Long, Set<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LocalDateTime> lastOnlineByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<Long>> presenceWatchersByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<Long>> watchedUsersByWatcher = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        AtomicBoolean becameOnline = new AtomicBoolean(false);
        emittersByUser.compute(userId, (id, existing) -> {
            Set<SseEmitter> emitters = existing == null ? new CopyOnWriteArraySet<>() : existing;
            becameOnline.set(emitters.isEmpty());
            emitters.add(emitter);
            return emitters;
        });

        emitter.onCompletion(() -> disconnect(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            disconnect(userId, emitter);
        });
        emitter.onError(error -> disconnect(userId, emitter));

        if (becameOnline.get()) {
            lastOnlineByUser.remove(userId);
            notifyPresenceWatchers(new PresenceVO(userId, true, null));
        }
        send(emitter, "connected", new PresenceVO(userId, true, null));
        return emitter;
    }

    public PresenceVO getPresence(Long watcherId, Long userId) {
        if (!userId.equals(watcherId)) {
            watchedUsersByWatcher.computeIfAbsent(watcherId, ignored -> ConcurrentHashMap.newKeySet()).add(userId);
            presenceWatchersByUser.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(watcherId);
        }
        boolean online = emittersByUser.containsKey(userId);
        return new PresenceVO(userId, online, online ? null : lastOnlineByUser.get(userId));
    }

    public void publishMessage(MessageVO message) {
        sendToUser(message.getReceiverId(), "message", message);
        if (!message.getReceiverId().equals(message.getSenderId())) {
            sendToUser(message.getSenderId(), "message", message);
        }
    }

    @Scheduled(fixedRate = 25_000)
    public void sendHeartbeats() {
        emittersByUser.values().forEach(emitters -> emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("keepalive"));
            } catch (IOException | IllegalStateException exception) {
                log.debug("Removing inactive message stream: {}", exception.getMessage());
                emitter.completeWithError(exception);
            }
        }));
    }

    private void disconnect(Long userId, SseEmitter emitter) {
        AtomicBoolean becameOffline = new AtomicBoolean(false);
        emittersByUser.computeIfPresent(userId, (id, emitters) -> {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                becameOffline.set(true);
                return null;
            }
            return emitters;
        });

        if (becameOffline.get()) {
            removePresenceSubscriptions(userId);
            LocalDateTime lastOnlineAt = LocalDateTime.now();
            lastOnlineByUser.put(userId, lastOnlineAt);
            pruneOldPresence(lastOnlineAt);
            notifyPresenceWatchers(new PresenceVO(userId, false, lastOnlineAt));
        }
    }

    private void pruneOldPresence(LocalDateTime now) {
        if (lastOnlineByUser.size() <= MAX_RECENT_USERS) {
            return;
        }
        LocalDateTime cutoff = now.minusDays(30);
        lastOnlineByUser.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        if (lastOnlineByUser.size() > MAX_RECENT_USERS) {
            lastOnlineByUser.entrySet().stream()
                    .sorted((left, right) -> left.getValue().compareTo(right.getValue()))
                    .limit(lastOnlineByUser.size() - MAX_RECENT_USERS)
                    .map(java.util.Map.Entry::getKey)
                    .forEach(lastOnlineByUser::remove);
        }
    }

    private void notifyPresenceWatchers(PresenceVO presence) {
        Set<Long> watcherIds = presenceWatchersByUser.get(presence.getUserId());
        if (watcherIds == null) {
            return;
        }
        watcherIds.forEach(watcherId -> {
            Set<SseEmitter> emitters = emittersByUser.get(watcherId);
            if (emitters != null) {
                sendToEmitters(emitters, "presence", presence);
            }
        });
    }

    private void removePresenceSubscriptions(Long watcherId) {
        Set<Long> watchedUsers = watchedUsersByWatcher.remove(watcherId);
        if (watchedUsers == null) {
            return;
        }
        watchedUsers.forEach(userId -> presenceWatchersByUser.computeIfPresent(userId, (id, watchers) -> {
            watchers.remove(watcherId);
            return watchers.isEmpty() ? null : watchers;
        }));
    }

    private void sendToUser(Long userId, String eventName, Object data) {
        if (userId == null) {
            return;
        }
        Set<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters != null) {
            sendToEmitters(emitters, eventName, data);
        }
    }

    private void sendToEmitters(Set<SseEmitter> emitters, String eventName, Object data) {
        emitters.forEach(emitter -> send(emitter, eventName, data));
    }

    private void send(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException | IllegalStateException exception) {
            emitter.completeWithError(exception);
        }
    }
}
