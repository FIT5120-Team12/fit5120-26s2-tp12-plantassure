package com.plantky.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * species_data 数据库表对应的 MyBatis-Plus Entity。
 *
 * <p>Entity 的职责是“映射数据库数据”，不是直接作为 API Response 返回给前端。
 * 这样数据库字段发生调整时，不会直接破坏前端接口契约。</p>
 *
 * <p>{@code @TableName("species_data")} 明确指定实体对应的数据库表。</p>
 * <p>{@code @Data} 是 Lombok 组合注解，会生成 getter、setter、toString、equals 和 hashCode。</p>
 *
 * <p>项目已在 application.yml 中开启 {@code map-underscore-to-camel-case: true}，因此：</p>
 * <pre>
 * scientific_name        -> scientificName
 * vba_record_count       -> vbaRecordCount
 * inat_most_recent_date  -> inatMostRecentDate
 * </pre>
 * 无需为每个标准字段重复写 {@code @TableField}。</p>
 */
@Data
@TableName("species_data")
public class SpeciesDataEntity {

    /**
     * 数据库主键，同时作为 API 中的 plantId。
     * AUTO 表示使用 MySQL AUTO_INCREMENT 生成主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 植物 scientific name，例如 Acacia baileyana。 */
    private String scientificName;

    /** 植物 common/vernacular name；允许为 null。 */
    private String vernacularName;

    /** 植物科名，例如 Fabaceae。 */
    private String family;

    /** VicFlora establishment means，例如 native / introduced / uncertain。 */
    private String establishmentMeans;

    /** VicFlora degree of establishment，例如 naturalised / native。 */
    private String degreeOfEstablishment;

    /**
     * 数据清洗阶段生成的标准化匹配键。
     * 当前 Iteration 1 Search API 不直接暴露该字段给前端。
     */
    private String matchKey;

    /**
     * 2022 Advisory List 风险值，例如 "High Risk"；
     * null 表示没有 exact matching assessment，即业务上的 NOT_ASSESSED。
     */
    private String riskRating;

    /** City of Monash 范围内匹配到的 VBA 记录数量。 */
    private Integer vbaRecordCount;

    /** City of Monash VBA 匹配记录中最新记录年份。 */
    private Integer vbaMostRecentYear;

    /** iNaturalist 记录数量；Iteration 1 当前 API 暂不使用。 */
    private Integer inatRecordCount;

    /** iNaturalist 最近记录时间；Iteration 1 当前 API 暂不使用。 */
    private LocalDateTime inatMostRecentDate;
}
