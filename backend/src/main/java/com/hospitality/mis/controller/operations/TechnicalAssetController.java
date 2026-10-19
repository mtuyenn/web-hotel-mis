package com.hospitality.mis.controller.operations;

import com.hospitality.mis.dto.operations.TechnicalAssetDtos;
import com.hospitality.mis.service.operations.TechnicalAssetService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Technical asset endpoints kept with the operations module. */
@RestController
public class TechnicalAssetController {
    private final TechnicalAssetService service;
    public TechnicalAssetController(TechnicalAssetService service) { this.service = service; }

    @GetMapping("/api/operations/assets")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_READ')")
    public java.util.List<TechnicalAssetDtos.TechnicalAssetResponse> assets() { return service.assets(); }

    @PostMapping("/api/operations/assets")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_WRITE')")
    public TechnicalAssetDtos.TechnicalAssetResponse createAsset(@Valid @RequestBody TechnicalAssetDtos.TechnicalAssetRequest request) { return service.createAsset(request); }

    @PatchMapping("/api/operations/assets/{id}/status")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_WRITE')")
    public TechnicalAssetDtos.TechnicalAssetResponse updateAsset(@PathVariable String id, @Valid @RequestBody TechnicalAssetDtos.TechnicalAssetStatusRequest request) { return service.updateAssetStatus(id, request.status()); }
}
