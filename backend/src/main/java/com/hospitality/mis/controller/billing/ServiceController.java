package com.hospitality.mis.controller.billing;
import com.hospitality.mis.dto.billing.ServiceDtos;




import com.hospitality.mis.service.billing.ServiceCatalogService;

import com.hospitality.mis.middleware.security.SecurityActor;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**
 * Quản lý danh mục dịch vụ khách sạn và lượng tồn kho của từng dịch vụ.
 */
@RestController

@RequestMapping("/api/services")

public class ServiceController {

    /** Dịch vụ điều phối danh mục, tạo dịch vụ và cập nhật tồn kho. */
    private final ServiceCatalogService service;



    public ServiceController(ServiceCatalogService service) {

        this.service = service;

    }



    /**
     * Liệt kê danh mục qua GET /api/services; không nhận tham số và trả danh sách dịch vụ hiện có.
     * Chỉ phạm vi SERVICE_READ được phép; lỗi truy vấn do dịch vụ xử lý. Đây là thao tác đọc, không có idempotency concern.
     */
    @GetMapping


    @PreAuthorize("@departmentAccess.allows(authentication, 'SERVICE_READ')")
    public List<ServiceDtos.Response> findAll() {

        return service.findAll();

    }





    /**
     * Tạo dịch vụ qua POST /api/services; body tạo dịch vụ được {@code @Valid} kiểm tra và trả 201 cùng dịch vụ mới.
     * Chỉ SERVICE_WRITE được phép, actor hiện tại được truyền để ghi nhận trách nhiệm; không có khóa idempotency,
     * vì vậy dữ liệu trùng hoặc lỗi nghiệp vụ do dịch vụ báo lỗi.
     */
    @PostMapping

    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("@departmentAccess.allows(authentication, 'SERVICE_WRITE')")
    public ServiceDtos.Response create(@Valid @RequestBody ServiceDtos.CreateRequest request) {

        return service.create(request, SecurityActor.currentActor());

    }





    /**
     * Bổ sung tồn kho qua POST /api/services/{id}/stock; id là path parameter, body nhập kho được {@code @Valid} kiểm tra.
     * Trả dịch vụ sau cập nhật; chỉ INVENTORY_WRITE được phép và actor hiện tại được ghi nhận. Không có idempotency key,
     * nên lỗi số lượng/trạng thái hoặc yêu cầu lặp do dịch vụ xử lý.
     */
    @PostMapping("/{id}/stock")


    @PreAuthorize("@departmentAccess.allows(authentication, 'INVENTORY_WRITE')")
    public ServiceDtos.Response restock(@PathVariable String id,

                                        @Valid @RequestBody ServiceDtos.StockRequest request) {

        return service.restock(id, request, SecurityActor.currentActor());

    }

}
