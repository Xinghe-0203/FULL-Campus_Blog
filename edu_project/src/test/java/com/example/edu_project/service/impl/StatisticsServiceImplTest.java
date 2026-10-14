package com.example.edu_project.service.impl;

import com.example.edu_project.service.content.StatisticsService;
import com.example.edu_project.vo.content.StatisticsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StatisticsServiceImpl 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "api.root-prefix.enabled=true")
@Transactional
class StatisticsServiceImplTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("获取社区统计数据")
    void getCommunityStats_Success() {
        StatisticsVO stats = statisticsService.getCommunityStats();
        assertNotNull(stats);
    }

    @Test
    @DisplayName("平台统计按选定时间范围返回完整增长趋势")
    void getPlatformStatistics_ReturnsRequestedTrendDays() {
        StatisticsVO stats = statisticsService.getPlatformStatistics(7);

        assertNotNull(stats);
        assertEquals(7, stats.getUserGrowthTrend().size());
        assertEquals(7, stats.getPostGrowthTrend().size());
    }

    @Test
    @DisplayName("保留 /api 前缀并转发到现有接口")
    void apiPrefixRoutesToExistingController() throws Exception {
        mockMvc.perform(get("/api/statistics/community"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("清空统计缓存不抛异常")
    void evictStatsCache_NoException() {
        assertDoesNotThrow(() -> statisticsService.evictStatsCache());
    }
}
