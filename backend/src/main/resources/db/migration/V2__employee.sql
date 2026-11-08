-- 员工基础信息表：兼容 H2（MODE=MySQL）与 MySQL 8.x
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    phone VARCHAR(11),
    hire_date DATE,
    status VARCHAR(8),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id),
    CONSTRAINT uk_employee_code UNIQUE (code)
);
