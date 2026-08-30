package com.plantky.controller;

import com.plantky.domain.vo.PlantAssessmentResponse;
import com.plantky.domain.vo.PlantSearchResponse;
import com.plantky.service.AssessmentOrchestrator;
import com.plantky.service.PlantSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Iteration 1 植物相关 REST Controller。
 *
 * <p>Controller 是 HTTP 接入层，只负责：</p>
 * <ol>
 *     <li>声明 URL、HTTP Method 和参数绑定；</li>
 *     <li>进行非常基础的请求参数校验；</li>
 *     <li>调用 Service/Orchestrator；</li>
 *     <li>把业务层返回对象交给 Spring/Jackson 序列化成 JSON。</li>
 * </ol>
 *
 * <p><strong>Controller 不负责：</strong>SQL、风险计算、Recommendation 规则、数据拼装。</p>
 *
 * <p>类级别 {@code @RequestMapping("/api/v1/plants")} 定义统一 URL 前缀。</p>
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant", description = "Iteration 1 plant search and assessment APIs")
public class PlantController {

    /** Search API 业务服务。 */
    private final PlantSearchService plantSearchService;

    /** Assessment 完整业务流程编排服务。 */
    private final AssessmentOrchestrator assessmentOrchestrator;

    /**
     * 搜索植物。
     *
     * <p>完整 URL：</p>
     * <pre>GET /api/v1/plants/search?q=wattle</pre>
     *
     * <p>同一个接口同时供“主动 Search”与“Autocomplete”使用。
     * 前端负责 3-character trigger/debounce；后端只负责根据传入 q 返回匹配结果。</p>
     *
     * @param query common name 或 scientific name 搜索关键词
     * @return PlantSearchResponse
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search plants",
            description = "Search by common name or scientific name. Also used by autocomplete.")
    public PlantSearchResponse searchPlants(
            @Parameter(description = "Common or scientific name keyword", example = "wattle")
            @RequestParam("q") String query) {

        // Controller 不自己处理 LIKE/数据库查询，全部交给业务层。
        return plantSearchService.search(query);
    }

    /**
     * 获取完整植物 Assessment。
     *
     * <p>完整 URL：</p>
     * <pre>GET /api/v1/plants/3/assessment</pre>
     *
     * @param plantId species_data.id；必须为正数
     * @return 包含 identity、occurrence、risk、recommendation、sources、warnings 的完整响应
     */
    @GetMapping("/{plantId}/assessment")
    @Operation(
            summary = "Get plant assessment",
            description = "Returns the complete Iteration 1 assessment and recommendation.")
    public PlantAssessmentResponse getAssessment(
            @Parameter(description = "species_data.id", example = "3")
            @Positive @PathVariable Long plantId) {

        // @Positive 由 Jakarta Validation 校验；0/负数会被全局异常处理器转换为 400。
        return assessmentOrchestrator.assess(plantId);
    }
}
