package com.plantky.service;

import com.plantky.domain.query.PlantCatalogQuery;
import com.plantky.domain.vo.catalog.PlantCatalogResponse;

/** Iteration 2 Epic 3 Catalog / Browse 业务接口。 */
public interface PlantCatalogService {
    PlantCatalogResponse browse(PlantCatalogQuery query);
}
