package com.plantky.service;

import com.plantky.domain.vo.alternatives.PlantAlternativesResponse;

/** Iteration 2 Epic 2.1 Find a Better Plant 业务接口。 */
public interface PlantAlternativeService {
    PlantAlternativesResponse findAlternatives(Long plantId, Integer limit);
}
