package com.plantky.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.plantky.common.exception.InvalidSearchQueryException;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantSearchResponse;
import com.plantky.mapper.SpeciesDataMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PlantSearchServiceImpl 单元测试。
 *
 * <p>Mapper 使用 Mockito 模拟，因此测试不依赖真实 MySQL，重点只验证 Search Service 自己的业务行为。</p>
 */
@ExtendWith(MockitoExtension.class)
class PlantSearchServiceImplTest {

    /**
     * 初始化 MyBatis-Plus 的实体字段元数据。
     *
     * <p>
     * 当前测试使用 Mockito，不会启动完整的 Spring Boot / MyBatis-Plus
     * ApplicationContext，因此 SpeciesDataEntity 的字段映射缓存不会像真实运行环境
     * 那样被自动创建。
     * </p>
     *
     * <p>
     * PlantSearchServiceImpl 使用 LambdaQueryWrapper，例如：
     * SpeciesDataEntity::getScientificName。
     * MyBatis-Plus 需要提前知道 scientificName 对应 scientific_name，
     * 因此在测试开始前手动初始化 TableInfo。
     * </p>
     */
    @BeforeAll
    static void initMyBatisPlusTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();

        MapperBuilderAssistant builderAssistant =
                new MapperBuilderAssistant(configuration, "test");

        TableInfoHelper.initTableInfo(
                builderAssistant,
                SpeciesDataEntity.class
        );
    }

    @Mock
    private SpeciesDataMapper speciesDataMapper;

    private PlantSearchServiceImpl plantSearchService;

    @BeforeEach
    void setUp() {
        plantSearchService = new PlantSearchServiceImpl(speciesDataMapper);
    }

    /**
     * 多匹配场景必须返回全部结果，后端不能自动选择第一株植物。
     */
    @Test
    void shouldReturnAllMapperMatchesWithoutAutoSelectingOne() {
        SpeciesDataEntity first = plant(1L, "Acacia acinacea", "Gold-dust Wattle");
        SpeciesDataEntity second = plant(3L, "Acacia baileyana", "Cootamundra Wattle");

        when(speciesDataMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(first, second));

        PlantSearchResponse response = plantSearchService.search("wattle");

        assertThat(response.getQuery()).isEqualTo("wattle");
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults())
                .extracting("plantId")
                .containsExactly(1L, 3L);
    }

    /** 没有匹配数据是正常业务结果：results=[]，不是异常。 */
    @Test
    void shouldReturnEmptyArrayWhenNoPlantMatches() {
        when(speciesDataMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PlantSearchResponse response = plantSearchService.search("zzzzz");

        assertThat(response.getResults()).isEmpty();
    }

    /** 纯空格搜索词必须在 Service 层被拒绝。 */
    @Test
    void shouldRejectBlankSearchQuery() {
        assertThatThrownBy(() -> plantSearchService.search("   "))
                .isInstanceOf(InvalidSearchQueryException.class);
    }

    /** 创建只包含 Search API 所需字段的 Entity 测试对象。 */
    private SpeciesDataEntity plant(Long id, String scientificName, String commonName) {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setId(id);
        entity.setScientificName(scientificName);
        entity.setVernacularName(commonName);
        return entity;
    }
}
