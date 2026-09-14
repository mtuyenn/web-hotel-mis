CREATE TABLE employee_shifts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id VARCHAR(10) NOT NULL,
    shift_date DATE NOT NULL,
    shift_code VARCHAR(30) NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_employee_shift_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT chk_employee_shift_status CHECK (status IN ('ASSIGNED','STARTED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_employee_shift_date ON employee_shifts (shift_date, employee_id);

CREATE TABLE housekeeping_checklist_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_housekeeping_checklist_template_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE housekeeping_checklist_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    item VARCHAR(200) NOT NULL,
    passed BOOLEAN NOT NULL,
    note VARCHAR(500),
    completed_by VARCHAR(10) NOT NULL,
    completed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_housekeeping_checklist_result_task FOREIGN KEY (task_id) REFERENCES housekeeping_tasks (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_housekeeping_checklist_result_task ON housekeeping_checklist_results (task_id, id);
