package com.plantky.service;

import com.plantky.domain.vo.compare.PlantComparisonResponse;

/** Iteration 2 Epic 2.2 Compare Plants 业务接口。 */
public interface PlantComparisonService {
    PlantComparisonResponse compare(String plantIds);
}
