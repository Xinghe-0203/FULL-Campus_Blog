package com.example.edu_project.vo.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Online state for a messaging partner. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceVO {

    private Long userId;
    private boolean online;
    private LocalDateTime lastOnlineAt;
}
