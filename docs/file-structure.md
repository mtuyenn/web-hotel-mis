# Cấu trúc project

```text
web-hotel-mis/
├── backend/
│   ├── src/main/java/com/hotelmanagement/web/
│   │   ├── common/                  # API contract, lỗi, cross-cutting concerns
│   │   ├── config/                  # Spring, security, database, observability
│   │   ├── identity/                # auth, account, role, permission
│   │   ├── guest/                   # hồ sơ khách, membership
│   │   ├── room/                    # phòng, loại phòng, availability
│   │   ├── reservation/             # booking, check-in/out, chuyển phòng
│   │   ├── billing/                 # pricing, service, invoice, payment
│   │   ├── operations/              # housekeeping, inventory, equipment
│   │   ├── finance/                 # cash handover, receipts, debt
│   │   ├── governance/              # audit, approval, reports
│   │   └── legacy/                  # code đã tái sử dụng, sẽ refactor dần
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── frontend/
│   └── src/
│       ├── app/                     # bootstrap, routing, providers
│       ├── features/                # UI theo capability nghiệp vụ
│       ├── shared/                  # API client, types, components
│       └── styles/
├── agent/
│   ├── app/                         # HTTP/chat entrypoint
│   ├── agent/                       # orchestration/state machine
│   ├── rag/                         # ingestion, retrieval, citations
│   ├── tools/                       # typed backend API tools
│   ├── policies/                    # safety and action policies
│   └── tests/
├── knowledge/sop/                   # tài liệu RAG, không chứa live data
├── database/legacy/                 # baseline schema cũ
├── legacy-desktop/                  # snapshot desktop chỉ để tham chiếu
├── infra/                           # Docker/local infrastructure
├── docs/                            # architecture, ADR, migration plan
└── scripts/                         # developer/CI scripts
```

Mỗi module backend giữ một boundary nhất quán:

- `api`: REST controller và request/response DTO.
- `application`: use case, transaction boundary, ports.
- `domain`: aggregate, value object, policy, domain event.
- `infrastructure`: JPA adapter, external integration, persistence implementation.
