# Cấu trúc project

```text
web-hotel-mis/
├── backend/
│   ├── src/main/java/com/hospitality/mis/
│   │   ├── auth/                   # token và authentication use cases
│   │   ├── common/                 # API contract, lỗi, cross-cutting concerns
│   │   ├── config/                 # Spring, security, database, observability
│   │   ├── identity/               # employee, role, permission
│   │   ├── guest/                  # hồ sơ khách, membership
│   │   ├── room/                   # phòng, loại phòng, availability
│   │   ├── reservation/            # booking, check-in/out, chuyển phòng
│   │   ├── billing/                # pricing, service, invoice, payment
│   │   ├── operations/             # housekeeping, inventory, equipment
│   │   ├── finance/                # cash handover, receipts, debt
│   │   ├── governance/             # audit, approval, reports
│   │   └── security/               # JWT, actor identity, authorization
│   ├── src/main/resources/db/migration/ # canonical Flyway schema line
│   └── src/test/                   # unit, contract, integration and DB tests
├── frontend/
│   └── src/
│       ├── app/                    # bootstrap, routing, providers
│       ├── features/               # UI theo capability nghiệp vụ
│       ├── shared/                 # API client, types, components
│       └── styles/
├── agent/
│   ├── app/                        # HTTP/chat entrypoint
│   ├── agent/                      # orchestration/state machine
│   ├── rag/                        # ingestion, retrieval, citations
│   ├── tools/                      # typed backend API tools
│   ├── policies/                   # safety and action policies
│   └── tests/
├── knowledge/sop/                  # tài liệu RAG, không chứa live data
├── infra/                          # Docker/local infrastructure
├── docs/                           # architecture, ADR, delivery plan
└── scripts/                        # developer/CI scripts
```

Mỗi module backend giữ một boundary nhất quán:

- `api`: REST controller và request/response DTO.
- `application`: use case, transaction boundary, ports.
- `domain`: aggregate, value object, policy, domain event.
- `adapter`: JPA adapter, external integration, persistence implementation.

`backend/src/main/resources/db/migration/` là nơi duy nhất định nghĩa thay
đổi schema. Backend là DB writer duy nhất; frontend và agent đi qua API.
