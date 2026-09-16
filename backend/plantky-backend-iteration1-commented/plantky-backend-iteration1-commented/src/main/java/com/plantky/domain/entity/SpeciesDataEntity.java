package com.plantky.domain.entity;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * species_data 数据库表对应的 MyBatis-Plus Entity。
 *
 * <p>Iteration 2 继续沿用 Iteration 1 的原则：Entity 只负责数据库映射，
 * Controller 不直接向前端暴露 Entity。这样数据字段继续变化时，REST API 可以通过 VO 保持稳定。</p>
 *
 * <p>Iteration 2 数据来自新的 species_data_i2 数据集，但后端初始化脚本仍使用
 * {@code species_data} 作为实际表名，以避免破坏 Iteration 1 的 Mapper 与既有接口。</p>
 *
 * <p>项目开启 {@code map-underscore-to-camel-case: true}，例如：</p>
 * <pre>
 * scientific_name          -> scientificName
 * vba100_record_count      -> vba100RecordCount
 * ala_most_recent_date     -> alaMostRecentDate
 * griis_is_invasive        -> griisIsInvasive
 * </pre>
 */
@Data
@TableName("species_data")
public class SpeciesDataEntity {

    /**
     * 数据库稳定主键，同时作为所有 REST API 中统一的 plantId。
     *
     * <p>Iteration 2 的 backend seed SQL 会保留 Iteration 1 已存在植物的 ID，
     * 新增植物从 778 开始继续分配，避免同一个 plantId 在不同迭代代表不同植物。</p>
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 植物 scientific name，例如 Acacia baileyana。 */
    private String scientificName;

    /** 植物 common/vernacular name；允许为 null。 */
    private String vernacularName;

    /** 植物科名；数据缺失时允许为 null。 */
    private String family;

    /** VicFlora establishment means，例如 native / introduced / uncertain。 */
    private String establishmentMeans;

    /** VicFlora degree of establishment，例如 naturalised / adventive / native。 */
    private String degreeOfEstablishment;

    /**
     * 数据清洗阶段生成的标准化匹配键。
     *
     * <p>Iteration 2 中 subspecies/variety 可能共享同一个 match_key，
     * 因此该字段不能作为主键或唯一键。</p>
     */
    private String matchKey;

    /**
     * 2022 Advisory List 风险原始值。
     *
     * <p>Iteration 2 数据可能显式保存 {@code Not Assessed / No exact match}，
     * 后端会在 RiskAssessmentService 中把它安全地映射为 NOT_ASSESSED。</p>
     */
    private String riskRating;

    // ---------------------------------------------------------------------
    // Iteration 2 — AusTraits supported traits
    // ---------------------------------------------------------------------

    /** AusTraits growth form；缺失时保持 null，后端不得推断。 */
    private String growthForm;

    /** AusTraits woodiness；缺失时保持 null，后端不得推断。 */
    private String woodiness;

    /** AusTraits life history；缺失时保持 null，后端不得推断。 */
    private String lifeHistory;

    /** AusTraits 最小成熟高度（米）；缺失时保持 null。 */
    private Double heightMin;

    /** AusTraits 最大成熟高度（米）；缺失时保持 null。 */
    private Double heightMax;

    // ---------------------------------------------------------------------
    // Iteration 2 — GRIIS supplementary evidence
    // ---------------------------------------------------------------------

    /** GRIIS 是否收录该物种。 */
    private Boolean griisListed;

    /** GRIIS 是否把该物种标记为 invasive。 */
    private Boolean griisIsInvasive;

    // ---------------------------------------------------------------------
    // Iteration 2 — Local occurrence evidence
    // ---------------------------------------------------------------------

    /*
     * Iteration 1 旧字段保留在注释中，说明为什么不再使用：
     *
     * private Integer vbaRecordCount;
     * private Integer vbaMostRecentYear;
     * private Integer inatRecordCount;
     * private LocalDateTime inatMostRecentDate;
     *
     * Iteration 2 实际数据源发生变化：
     * VBA25 -> VBA_FLORA100；iNaturalist(GBIF) -> ALA Monash download。
     * 因此不能继续把新数据映射到旧字段名，否则会造成来源语义错误。
     */

    /** VBA_FLORA100 匹配记录数量，仅作为 local occurrence supporting evidence。 */
    private Integer vba100RecordCount;

    /** VBA_FLORA100 匹配记录中最新记录年份。 */
    private Integer vba100MostRecentYear;

    /** ALA Monash 直接下载数据中的匹配记录数量。 */
    private Integer alaRecordCount;

    /** ALA Monash 直接下载数据中的最近记录日期。 */
    private LocalDate alaMostRecentDate;

    // ---------------------------------------------------------------------
    // Pipeline traceability fields
    // ---------------------------------------------------------------------

    /**
     * 数据 pipeline 预计算的 recommendation。
     *
     * <p>后端保留该字段用于数据追踪，但 REST API 的 recommendation 仍由
     * RecommendationService 根据受控 environmental concern 重新计算，
     * 不能直接信任该字符串。</p>
     */
    private String recommendation;

    /**
     * 数据 pipeline 预生成的 alternatives JSON。
     *
     * <p>该字段仅保留用于 traceability。Iteration 2 Backend 不直接把它作为
     * Find a Better Plant 的最终答案，因为当前数据中可能包含 NOT_ASSESSED 候选，
     * 与最新 Epic 的 lower documented concern 规则不一致。</p>
     */
    private String alternativesJson;
}
