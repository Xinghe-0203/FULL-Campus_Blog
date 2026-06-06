package com.example.edu_project.controller;

import com.example.edu_project.controller.content.StatisticsController;
import com.example.edu_project.service.content.StatisticsService;
import com.example.edu_project.utils.JwtUtils;
import com.example.edu_project.vo.content.StatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StatisticsController 单元测试
 */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取社区统计数据")
    void getCommunityStats_Success() throws Exception {
        StatisticsVO stats = new StatisticsVO();
        stats.setUserCount(100L);
        stats.setPostCount(500L);
        when(statisticsService.getCommunityStats()).thenReturn(stats);

        mockMvc.perform(get("/statistics/community"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userCount").value(100));
    }
}
