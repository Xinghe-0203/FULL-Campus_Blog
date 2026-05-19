package com.example.edu_project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.security.UserContext;
import com.example.edu_project.service.BlogLikeService;
import com.example.edu_project.utils.JwtUtils;
import com.example.edu_project.vo.LikeItemVO;
import com.example.edu_project.vo.LikeResultVO;
import com.example.edu_project.vo.LikeStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BlogLikeController 单元测试
 */
@WebMvcTest(BlogLikeController.class)
class BlogLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogLikeService blogLikeService;

    @MockBean
    private JwtUtils jwtUtils;

    private LikeResultVO likeResult;
    private LikeStatusVO likeStatus;

    @BeforeEach
    void setUp() {
        likeResult = new LikeResultVO();
        likeResult.setAction("like");
        likeResult.setLikeCount(10);

        likeStatus = new LikeStatusVO();
        likeStatus.setLiked(true);
        likeStatus.setLikeCount(10);

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext(Long userId, boolean isAdmin) {
        UserContext userContext = new UserContext(userId, isAdmin);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userContext, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("toggleLike_Success_ReturnsLikeResult")
    void toggleLike_Success_ReturnsLikeResult() throws Exception {
        // Given
        setUpSecurityContext(1L, false);
        when(blogLikeService.toggleLike(100L, 1L)).thenReturn(likeResult);

        // When & Then
        mockMvc.perform(post("/like/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.action").value("like"))
                .andExpect(jsonPath("$.data.likeCount").value(10));
    }

    @Test
    @DisplayName("toggleLike_NotLoggedIn_Returns401")
    void toggleLike_NotLoggedIn_Returns401() throws Exception {
        // Given - SecurityContext is empty

        // When & Then
        mockMvc.perform(post("/like/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("toggleLike_PostNotFound_Returns404")
    void toggleLike_PostNotFound_Returns404() throws Exception {
        // Given
        setUpSecurityContext(1L, false);
        when(blogLikeService.toggleLike(999L, 1L))
                .thenThrow(new BusinessException(404, "文章不存在"));

        // When & Then
        mockMvc.perform(post("/like/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("checkLikeStatus_LoggedIn_ReturnsStatus")
    void checkLikeStatus_LoggedIn_ReturnsStatus() throws Exception {
        // Given
        setUpSecurityContext(1L, false);
        when(blogLikeService.checkLikeStatus(100L, 1L)).thenReturn(likeStatus);

        // When & Then
        mockMvc.perform(get("/like/check/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    @DisplayName("checkLikeStatus_NotLoggedIn_ReturnsNotLiked")
    void checkLikeStatus_NotLoggedIn_ReturnsNotLiked() throws Exception {
        // Given - SecurityContext is empty
        when(blogLikeService.checkLikeStatus(100L, null))
                .thenReturn(new LikeStatusVO());

        // When & Then
        mockMvc.perform(get("/like/check/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getMyLikes_Success_ReturnsPage")
    void getMyLikes_Success_ReturnsPage() throws Exception {
        // Given
        setUpSecurityContext(1L, false);
        IPage<LikeItemVO> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(blogLikeService.getMyLikes(eq(1L), anyInt(), anyInt())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/like/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getMyLikes_NotLoggedIn_Returns401")
    void getMyLikes_NotLoggedIn_Returns401() throws Exception {
        // Given - SecurityContext is empty

        // When & Then
        mockMvc.perform(get("/like/my")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("checkLikeStatusBatch_Success_ReturnsBooleanList")
    void checkLikeStatusBatch_Success_ReturnsBooleanList() throws Exception {
        // Given
        setUpSecurityContext(1L, false);
        when(blogLikeService.checkLikeStatusBatch(anyList(), eq(1L)))
                .thenReturn(List.of(true, false, true));

        // When & Then
        mockMvc.perform(post("/like/check/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value(true));
    }

    @Test
    @DisplayName("checkLikeStatusBatch_EmptyList_Returns400")
    void checkLikeStatusBatch_EmptyList_Returns400() throws Exception {
        // Given
        setUpSecurityContext(1L, false);

        // When & Then
        mockMvc.perform(post("/like/check/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("checkLikeStatusBatch_NotLoggedIn_Returns401")
    void checkLikeStatusBatch_NotLoggedIn_Returns401() throws Exception {
        // Given - SecurityContext is empty

        // When & Then
        mockMvc.perform(post("/like/check/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("checkLikeStatusBatch_ExceedsMaxSize_Returns400")
    void checkLikeStatusBatch_ExceedsMaxSize_Returns400() throws Exception {
        // Given
        setUpSecurityContext(1L, false);

        // Create a list with 51 items (max is 50)
        StringBuilder largeList = new StringBuilder("[");
        for (int i = 0; i < 51; i++) {
            largeList.append(i).append(",");
        }
        largeList.append("1]");

        // When & Then
        mockMvc.perform(post("/like/check/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(largeList.toString()))
                .andExpect(status().isBadRequest());
    }
}