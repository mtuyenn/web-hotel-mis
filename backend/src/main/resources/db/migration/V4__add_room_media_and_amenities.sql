CREATE TABLE amenities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_amenities PRIMARY KEY (id),
    CONSTRAINT uk_amenities_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room_type_amenities (
    room_type_id VARCHAR(10) NOT NULL,
    amenity_id BIGINT NOT NULL,
    CONSTRAINT pk_room_type_amenities PRIMARY KEY (room_type_id, amenity_id),
    CONSTRAINT fk_room_type_amenities_type FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    CONSTRAINT fk_room_type_amenities_amenity FOREIGN KEY (amenity_id) REFERENCES amenities (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id VARCHAR(10) NOT NULL,
    relative_path VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    cover BOOLEAN NOT NULL DEFAULT FALSE,
    content_type VARCHAR(40) NOT NULL,
    size_bytes BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_room_images PRIMARY KEY (id),
    CONSTRAINT uk_room_images_relative_path UNIQUE (relative_path),
    CONSTRAINT fk_room_images_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_room_images_order CHECK (display_order >= 0),
    CONSTRAINT chk_room_images_size CHECK (size_bytes > 0 AND size_bytes <= 5242880)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_room_images_room ON room_images (room_id, active, display_order);
CREATE INDEX idx_room_type_amenities_amenity ON room_type_amenities (amenity_id);
