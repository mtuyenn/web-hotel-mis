package com.hospitality.mis.service.operations;

import com.hospitality.mis.dao.operations.TechnicalAssetDatabase;
import com.hospitality.mis.dto.operations.TechnicalAssetDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechnicalAssetService {
    private static final VietnameseCodeConverters.TechnicalAssetStatusConverter ASSET_STATUS = new VietnameseCodeConverters.TechnicalAssetStatusConverter();
    private static final VietnameseCodeConverters.TechnicalAssetLocationTypeConverter ASSET_LOCATION_TYPE = new VietnameseCodeConverters.TechnicalAssetLocationTypeConverter();
    private final TechnicalAssetDatabase database;

    public TechnicalAssetService(TechnicalAssetDatabase database) {
        this.database = database;
    }

    @Transactional(readOnly = true)
    public List<TechnicalAssetDtos.TechnicalAssetResponse> assets() {
        return database.assets();
    }

    public TechnicalAssetDtos.TechnicalAssetResponse createAsset(TechnicalAssetDtos.TechnicalAssetRequest request) {
        database.createAsset(request,ASSET_LOCATION_TYPE.convertToDatabaseColumn(code(request.locationType())),ASSET_STATUS.convertToDatabaseColumn(request.status()==null?"GOOD":code(request.status())));
        return assets().stream().filter(row -> row.id().equals(request.id())).findFirst().orElseThrow();
    }

    public TechnicalAssetDtos.TechnicalAssetResponse updateAssetStatus(String id, String status) {
        database.updateAsset(id,ASSET_STATUS.convertToDatabaseColumn(code(status)));
        return assets().stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow();
    }

    private static String code(String value) { return value == null ? null : value.trim().toUpperCase(java.util.Locale.ROOT); }
}
