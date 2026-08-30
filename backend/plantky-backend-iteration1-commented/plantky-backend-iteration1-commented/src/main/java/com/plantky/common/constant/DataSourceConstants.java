package com.plantky.common.constant;

/**
 * Iteration 1 数据来源相关常量。
 *
 * <p>把数据源名称和角色集中管理，避免在多个 Service 中重复写字符串。
 * 这样做有两个主要好处：</p>
 *
 * <ol>
 *     <li>避免拼写不一致，例如某处写 "VBA"，另一处写完整名称；</li>
 *     <li>未来如果产品文案调整，只需要修改一个类。</li>
 * </ol>
 *
 * <p>注意：这个类只负责“展示层面的固定元数据”，不负责数据库连接或数据读取。</p>
 */
public final class DataSourceConstants {

    /** VicFlora 数据源展示名称。 */
    public static final String VICFLORA = "VicFlora";

    /** VicFlora 在本项目中的业务角色。 */
    public static final String VICFLORA_ROLE = "Plant identity and establishment status";

    /** Victorian Biodiversity Atlas 数据源展示名称。 */
    public static final String VBA = "Victorian Biodiversity Atlas";

    /** VBA 在 Iteration 1 中只用于 City of Monash 的本地出现记录证据。 */
    public static final String VBA_ROLE = "City of Monash local occurrence evidence";

    /** 2022 Environmental Weeds Advisory List 的标准展示名称。 */
    public static final String ADVISORY_LIST = "2022 Advisory List of Environmental Weeds in Victoria";

    /** Advisory List 在 Iteration 1 中用于环境杂草风险。 */
    public static final String ADVISORY_LIST_ROLE = "Environmental weed risk";

    /**
     * 工具类不应该被实例化，因此声明私有构造器。
     *
     * <p>如果没有这个构造器，Java 会自动提供 public 无参构造器，
     * 那么开发人员就可能错误地执行 {@code new DataSourceConstants()}。</p>
     */
    private DataSourceConstants() {
    }
}
