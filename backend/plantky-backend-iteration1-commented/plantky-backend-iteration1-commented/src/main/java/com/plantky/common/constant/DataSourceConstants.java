package com.plantky.common.constant;

/**
 * PlantAssure 数据来源名称与职责常量。
 *
 * <p>Iteration 2 继续强调“一个数据源负责一种语义”：VBA/ALA 只提供 occurrence evidence，
 * AusTraits 只提供 traits，GRIIS 只作为 supplementary evidence；这些来源都不能替代
 * 2022 Advisory List 去生成 environmental concern。</p>
 */
public final class DataSourceConstants {

    public static final String VICFLORA = "VicFlora";
    public static final String VICFLORA_ROLE = "Plant identity and Victorian establishment/origin";

    /*
     * Iteration 1 旧常量：
     * public static final String VBA = "Victorian Biodiversity Atlas";
     * public static final String VBA_ROLE = "City of Monash local occurrence evidence";
     *
     * Iteration 2 实际数据是 VBA_FLORA100，因此使用更精确名称，避免把新旧 extraction 混淆。
     */
    public static final String VBA = "Victorian Biodiversity Atlas (VBA_FLORA100)";
    public static final String VBA_ROLE = "City of Monash local occurrence evidence only";

    public static final String ALA = "Atlas of Living Australia";
    public static final String ALA_ROLE = "Supplementary City of Monash occurrence evidence";

    public static final String ADVISORY_LIST = "2022 Advisory List of Environmental Weeds in Victoria";
    public static final String ADVISORY_LIST_ROLE = "Environmental weed concern";

    public static final String AUSTRAITS = "AusTraits";
    public static final String AUSTRAITS_ROLE = "Growth form, life history, woodiness and height traits";

    public static final String GRIIS = "Global Register of Introduced and Invasive Species (GRIIS)";
    public static final String GRIIS_ROLE = "Supplementary introduced/invasive species evidence";

    private DataSourceConstants() {
    }
}
