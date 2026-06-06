package com.example.edu_project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edu_project.controller.content.TrendingController;
import com.example.edu_project.service.content.TrendingService;
import com.example.edu_project.utils.JwtUtils;
import com.example.edu_project.vo.content.HotContentVO;
import com.example.edu_project.vo.content.HotPostVO;
import com.example.edu_project.vo.content.HotTagVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TrendingController 单元测试
 */
@WebMvcTest(TrendingController.class)
class TrendingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrendingService trendingService;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取热门文章列表")
    void getHotPosts_Success() throws Exception {
        IPage<HotPostVO> page = new Page<>(1, 10, 0);
        when(trendingService.getHotPosts(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/trending/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取热门内容列表")
    void getHotContent_Success() throws Exception {
        IPage<HotContentVO> page = new Page<>(1, 20, 0);
        when(trendingService.getHotContent(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/trending/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取热门标签")
    void getHotTags_Success() throws Exception {
        IPage<HotTagVO> page = new Page<>(1, 10, 0);
        when(trendingService.getHotTags()).thenReturn(page);

        mockMvc.perform(get("/trending/hot-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

}
