package com.hospitality.mis.service.room;

import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.Set;

/** Abstraction lưu ảnh để sau này thay local storage bằng object storage không đổi nghiệp vụ. */
public interface RoomImageStorage {
    StoredImage store(String roomId, MultipartFile file);
    void delete(String relativePath);
    int deleteOrphans(Set<String> referencedPaths, Instant olderThan);

    record StoredImage(String relativePath, String contentType, long sizeBytes) {}
}
