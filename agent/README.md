# Hotel MIS Agent

Agent service boundary for chatbot orchestration and RAG.

- `app/`: HTTP/streaming entrypoint.
- `agent/`: state machine and conversation policy.
- `rag/`: document ingestion, retrieval and citations.
- `tools/`: typed calls to backend API; never direct SQL.
- `policies/`: confirmation, RBAC and sensitive-action rules.
- `tests/`: tool and safety tests.
