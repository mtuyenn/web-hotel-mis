package com.hospitality.mis.entity.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One of a fixed set of database rows used to serialize creation of new command keys. */
@Entity
@Table(name = "idempotency_lock_buckets")
public class IdempotencyLockBucket {
    @Id
    @Column(name = "bucket_id")
    private short id;

    protected IdempotencyLockBucket() {}
    public IdempotencyLockBucket(short id) { this.id = id; }
    public short getId() { return id; }
}
