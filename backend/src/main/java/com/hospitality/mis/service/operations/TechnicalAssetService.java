package com.hospitality.mis.service.operations;

import com.hospitality.mis.dto.operations.BackofficeDtos;
import org.springframework.stereotype.Service;

import java.util.List;

/** Technical asset use cases for the operations boundary. */
@Service
public class TechnicalAssetService {
    private final BackofficeService backoffice;

    public TechnicalAssetService(BackofficeService backoffice) { this.backoffice = backoffice; }
    public List<BackofficeDtos.TechnicalAssetResponse> assets() { return backoffice.assets(); }
    public BackofficeDtos.TechnicalAssetResponse createAsset(BackofficeDtos.TechnicalAssetRequest request) { return backoffice.createAsset(request); }
    public BackofficeDtos.TechnicalAssetResponse updateAssetStatus(String id, String status) { return backoffice.updateAssetStatus(id, status); }
}
