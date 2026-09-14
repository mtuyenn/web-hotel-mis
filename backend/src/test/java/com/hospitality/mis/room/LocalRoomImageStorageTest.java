package com.hospitality.mis.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.service.room.LocalRoomImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalRoomImageStorageTest {
    @TempDir Path directory;

    @Test
    void storesSupportedImageWithServerGeneratedSafePath() throws Exception {
        var storage = new LocalRoomImageStorage(directory.toString());
        var result = storage.store("101", new MockMultipartFile("file", "anything.bin", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x01}));

        assertThat(result.relativePath()).startsWith("101/").endsWith(".jpg");
        assertThat(Files.exists(directory.resolve(result.relativePath()))).isTrue();
        assertThat(result.sizeBytes()).isEqualTo(4);
    }

    @Test
    void rejectsUnsupportedTypeAndMismatchedSignature() {
        var storage = new LocalRoomImageStorage(directory.toString());
        assertThatThrownBy(() -> storage.store("101", new MockMultipartFile("file", "x.gif", "image/gif", new byte[] {1})))
                .isInstanceOf(DomainException.class).extracting("code").isEqualTo("IMAGE_TYPE_NOT_SUPPORTED");
        assertThatThrownBy(() -> storage.store("101", new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[] {1})))
                .isInstanceOf(DomainException.class).extracting("code").isEqualTo("IMAGE_CONTENT_INVALID");
    }

    @Test
    void rejectsFilesOverFiveMegabytes() {
        var storage = new LocalRoomImageStorage(directory.toString());
        assertThatThrownBy(() -> storage.store("101", new MockMultipartFile("file", "x.jpg", "image/jpeg",
                new byte[(int) LocalRoomImageStorage.MAX_BYTES + 1])))
                .isInstanceOf(DomainException.class).extracting("code").isEqualTo("IMAGE_TOO_LARGE");
    }

    @Test
    void cleanupDeletesOnlyOldUnreferencedFiles() throws Exception {
        var storage = new LocalRoomImageStorage(directory.toString());
        Path referenced = directory.resolve("101/referenced.jpg");
        Path orphan = directory.resolve("101/orphan.jpg");
        Files.createDirectories(referenced.getParent());
        Files.write(referenced, new byte[] {1});
        Files.write(orphan, new byte[] {1});
        Files.setLastModifiedTime(referenced, FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));
        Files.setLastModifiedTime(orphan, FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));

        assertThat(storage.deleteOrphans(Set.of("101/referenced.jpg"), Instant.parse("2021-01-01T00:00:00Z")))
                .isEqualTo(1);
        assertThat(referenced).exists();
        assertThat(orphan).doesNotExist();
    }
}
