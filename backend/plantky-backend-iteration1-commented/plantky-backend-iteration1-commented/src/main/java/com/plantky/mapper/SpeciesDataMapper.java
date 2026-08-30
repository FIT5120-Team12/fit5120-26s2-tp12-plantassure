package com.plantky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantky.domain.entity.SpeciesDataEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * species_data 表的数据访问层 Mapper。
 *
 * <p>{@code @Mapper} 让 MyBatis 将该接口注册成 Mapper Bean，Service 可以通过构造器直接注入。</p>
 *
 * <p>继承 {@link BaseMapper} 后，MyBatis-Plus 自动提供常用 CRUD 方法，例如：</p>
 * <ul>
 *     <li>{@code selectById(id)}</li>
 *     <li>{@code selectList(wrapper)}</li>
 *     <li>{@code selectOne(wrapper)}</li>
 *     <li>{@code insert(entity)}</li>
 *     <li>{@code updateById(entity)}</li>
 *     <li>{@code deleteById(id)}</li>
 * </ul>
 *
 * <p>Iteration 1 当前只有查询需求，所以暂时不需要自定义 XML SQL。
 * 以后如果出现复杂 join/统计查询，可以在这里新增方法并使用 XML 或注解 SQL。</p>
 */
@Mapper
public interface SpeciesDataMapper extends BaseMapper<SpeciesDataEntity> {
}
