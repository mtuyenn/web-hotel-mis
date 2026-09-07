# Room module

Quản lý loại phòng, phòng, trạng thái vận hành và khả dụng theo khoảng thời gian.

`Room` và `RoomType` là hai concrete JPA owner duy nhất, lần lượt map tới
`rooms` và `room_types` theo schema V1.

`RoomService` chỉ phụ thuộc `RoomStore` và `ReservationOverlapPort`. `RoomRepository`
giữ `PESSIMISTIC_WRITE` cho cập nhật trạng thái và locking
reservation. Availability dùng khoảng nửa mở `existingIn < requestedOut &&
existingOut > requestedIn`, qua query overlap hiện hữu; các trạng thái `CLEANING`,
`MAINTENANCE` và `OUT_OF_SERVICE` luôn không khả dụng.

V1 tạo `room_types`/`rooms`/FK; Hibernate production dùng `ddl-auto=validate`.
