package com.hospitality.mis.service.room;

import com.hospitality.mis.common.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.Set;
import java.time.Instant;

/** Local filesystem implementation with fixed size/type/signature validation. */
@Service
public class LocalRoomImageStorage implements RoomImageStorage {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private final Path root;

    public LocalRoomImageStorage(@Value("${hotel.media.room-images-dir:./data/room-images}") String directory) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public StoredImage store(String roomId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new DomainException("IMAGE_REQUIRED", "Ảnh phòng là bắt buộc");
        if (file.getSize() > MAX_BYTES) throw new DomainException("IMAGE_TOO_LARGE", "Ảnh không được vượt quá 5 MB");
        String contentType = normalizedContentType(file.getContentType());
        if (!isSupported(contentType)) throw new DomainException("IMAGE_TYPE_NOT_SUPPORTED", "Chỉ hỗ trợ JPEG, PNG và WebP");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new DomainException("IMAGE_READ_FAILED", "Không thể đọc ảnh tải lên");
        }
        if (bytes.length > MAX_BYTES || !matchesSignature(contentType, bytes)) {
            throw new DomainException("IMAGE_CONTENT_INVALID", "Nội dung ảnh không khớp định dạng đã khai báo");
        }
        String safeRoomId = roomId == null ? "unknown" : roomId.replaceAll("[^A-Za-z0-9_-]", "_");
        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".webp";
        };
        String filename = UUID.randomUUID() + extension;
        String relative = safeRoomId + "/" + filename;
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) throw new DomainException("IMAGE_PATH_INVALID", "Đường dẫn ảnh không hợp lệ");
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DomainException("IMAGE_STORE_FAILED", "Không thể lưu ảnh phòng");
        }
        return new StoredImage(relative, contentType, bytes.length);
    }

    @Override
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) throw new DomainException("IMAGE_PATH_INVALID", "Đường dẫn ảnh không hợp lệ");
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new DomainException("IMAGE_DELETE_FAILED", "Không thể xóa file ảnh");
        }
    }

    @Override
    public int deleteOrphans(Set<String> referencedPaths, Instant olderThan) {
        if (!Files.exists(root)) return 0;
        Set<String> safeReferences = referencedPaths == null ? Set.of() : referencedPaths;
        int deleted = 0;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String relative = root.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
                if (!safeReferences.contains(relative)
                        && Files.getLastModifiedTime(path).toInstant().isBefore(olderThan)) {
                    Files.deleteIfExists(path);
                    deleted++;
                }
            }
        } catch (IOException exception) {
            throw new DomainException("IMAGE_CLEANUP_FAILED", "Không thể dọn file ảnh mồ côi");
        }
        return deleted;
    }

    private static String normalizedContentType(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean isSupported(String value) {
        return value.equals("image/jpeg") || value.equals("image/png") || value.equals("image/webp");
    }

    private static boolean matchesSignature(String contentType, byte[] bytes) {
        if (contentType.equals("image/jpeg")) return bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        if (contentType.equals("image/png")) return bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a;
        return bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }
}
