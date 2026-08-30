package com.plantky.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.plantky.common.handler.GlobalExceptionHandler;
import com.plantky.domain.vo.PlantSearchItemVO;
import com.plantky.domain.vo.PlantSearchResponse;
import com.plantky.service.AssessmentOrchestrator;
import com.plantky.service.PlantSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * PlantController 的 Web Contract 单元测试。
 *
 * <p>这里使用 standalone MockMvc，不启动完整 Spring Boot、MySQL 或 Mapper，
 * 只测试 Controller 的 URL/JSON 契约是否符合接口文档。</p>
 */
class PlantControllerTest {

    /** 被 Mockito 模拟的 Search Service。 */
    private PlantSearchService plantSearchService;

    /** Spring MVC 测试工具，用来模拟 HTTP 请求。 */
    private MockMvc mockMvc;

    /**
     * 每个测试执行前创建新的 Controller 和 MockMvc。
     */
    @BeforeEach
    void setUp() {
        plantSearchService = mock(PlantSearchService.class);
        AssessmentOrchestrator assessmentOrchestrator = mock(AssessmentOrchestrator.class);

        // 直接使用构造器创建 Controller，证明 Controller 依赖清晰、无需字段注入。
        PlantController controller = new PlantController(
                plantSearchService,
                assessmentOrchestrator);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                // 同时挂载真实的 GlobalExceptionHandler，确保异常响应格式也可被测试。
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * 验证 Search API 成功响应严格遵守当前 Contract：
     * 顶层直接是 query + results，不额外包装 code/msg/data。
     */
    @Test
    void shouldReturnSearchContractWithoutEnvelope() throws Exception {
        // Arrange：规定 mock Service 在收到 wattle 时返回固定结果。
        when(plantSearchService.search("wattle")).thenReturn(
                PlantSearchResponse.builder()
                        .query("wattle")
                        .results(List.of(
                                PlantSearchItemVO.builder()
                                        .plantId(3L)
                                        .scientificName("Acacia baileyana")
                                        .commonName("Cootamundra Wattle")
                                        .build()))
                        .build());

        // Act + Assert：模拟真实 GET 请求并检查 HTTP/JSON。
        mockMvc.perform(get("/api/v1/plants/search").param("q", "wattle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("wattle"))
                .andExpect(jsonPath("$.results[0].plantId").value(3))
                // 防止开发人员以后错误地给成功响应加 code envelope。
                .andExpect(jsonPath("$.code").doesNotExist());
    }
}
