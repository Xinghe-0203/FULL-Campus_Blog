package com.example.edu_project.service;

import com.example.edu_project.vo.StatisticsVO;

/**
 * 数据统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取平台完整统计数据
     * @return 平台统计数据
     */
    StatisticsVO getPlatformStatistics();

    /**
     * 获取社区统计数据（公开接口）
     * @return 社区统计数据
     */
    StatisticsVO getCommunityStats();
}
