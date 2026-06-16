CREATE DATABASE IF NOT EXISTS emission_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE emission_platform;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plate_number VARCHAR(32) NOT NULL UNIQUE,
  vin VARCHAR(64) NOT NULL UNIQUE,
  vehicle_type VARCHAR(64) NOT NULL,
  fuel_type VARCHAR(32) NOT NULL,
  emission_standard VARCHAR(32) NOT NULL,
  owner_name VARCHAR(128) NOT NULL,
  register_date DATE NOT NULL,
  environmental_status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS inspection_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inspection_no VARCHAR(64) NOT NULL UNIQUE,
  plate_number VARCHAR(32) NOT NULL,
  station_name VARCHAR(128) NOT NULL,
  inspection_time DATETIME NOT NULL,
  co_value DECIMAL(8, 3) NOT NULL,
  hc_value DECIMAL(8, 3) NOT NULL,
  nox_value DECIMAL(8, 3) NOT NULL,
  opacity_value DECIMAL(8, 3) NOT NULL,
  result VARCHAR(32) NOT NULL,
  inspector VARCHAR(64) NOT NULL,
  report_status VARCHAR(32) NOT NULL,
  auditor VARCHAR(64),
  audit_time DATETIME,
  audit_opinion TEXT
);

CREATE TABLE IF NOT EXISTS audit_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inspection_no VARCHAR(64) NOT NULL,
  audit_action VARCHAR(32) NOT NULL,
  audit_opinion TEXT,
  auditor VARCHAR(64) NOT NULL,
  audit_time DATETIME NOT NULL,
  INDEX idx_inspection_no (inspection_no)
);

CREATE TABLE IF NOT EXISTS announcements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL COMMENT '公告标题',
  content TEXT COMMENT '公告内容',
  type VARCHAR(32) COMMENT '公告类型：政策公告、通知公告等',
  publish_status VARCHAR(32) NOT NULL DEFAULT '草稿' COMMENT '发布状态：草稿、已发布、已下线',
  publisher VARCHAR(64) COMMENT '发布人/发布单位',
  publish_time DATETIME COMMENT '发布时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_publish_status (publish_status),
  INDEX idx_type (type),
  INDEX idx_publish_time (publish_time)
);

CREATE TABLE IF NOT EXISTS pollutant_limit_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
  fuel_type VARCHAR(32) NOT NULL COMMENT '燃料类型：汽油、柴油、混合动力等',
  emission_standard VARCHAR(32) NOT NULL COMMENT '排放标准：国四、国五、国六等',
  co_limit DECIMAL(8,3) NOT NULL COMMENT 'CO限值 (%)',
  hc_limit DECIMAL(8,3) NOT NULL COMMENT 'HC限值 (ppm)',
  nox_limit DECIMAL(8,3) NOT NULL COMMENT 'NOx限值 (ppm)',
  opacity_limit DECIMAL(8,3) NOT NULL COMMENT '烟度限值 (m-1)',
  status VARCHAR(32) NOT NULL DEFAULT '启用' COMMENT '状态：启用、停用',
  remark VARCHAR(512) COMMENT '备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_fuel_standard (fuel_type, emission_standard),
  INDEX idx_fuel_type (fuel_type),
  INDEX idx_emission_standard (emission_standard),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='污染物限值规则表';

CREATE TABLE IF NOT EXISTS system_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  operator VARCHAR(64) NOT NULL COMMENT '操作人',
  role VARCHAR(64) NOT NULL COMMENT '角色',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  business_object VARCHAR(255) NOT NULL COMMENT '业务对象',
  detail TEXT COMMENT '操作详情',
  result VARCHAR(32) NOT NULL COMMENT '操作结果',
  operate_time DATETIME NOT NULL COMMENT '操作时间',
  ip VARCHAR(64) COMMENT 'IP地址',
  INDEX idx_operator (operator),
  INDEX idx_action (action),
  INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';
