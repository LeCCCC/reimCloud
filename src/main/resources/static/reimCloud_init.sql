-- =============================================
-- reimCloud 数据库初始化脚本
-- 基于：表结构定义模板 + 差旅报销单概要设计 5.3 页面数据
-- 生成日期：2026-05-22
-- =============================================

-- ---------------------------------------------
-- 1. 创建数据库
-- ---------------------------------------------
CREATE DATABASE IF NOT EXISTS reimCloud
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE reimCloud;

-- ---------------------------------------------
-- 2. 基础数据表（来自概要设计 5.3 页面控件数据）
-- ---------------------------------------------

-- 2.1 费用归属公司
DROP TABLE IF EXISTS reim_company;
CREATE TABLE reim_company (
  id              VARCHAR(32)  NOT NULL COMMENT '主键ID',
  company_no      VARCHAR(20)  DEFAULT NULL COMMENT '公司编号',
  company_name    VARCHAR(50)  DEFAULT NULL COMMENT '公司名称',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用归属公司';

-- 2.2 报销部门
DROP TABLE IF EXISTS reim_department;
CREATE TABLE reim_department (
  id              VARCHAR(32)  NOT NULL COMMENT '主键ID',
  department_no   VARCHAR(20)  DEFAULT NULL COMMENT '部门编号',
  department_name VARCHAR(50)  DEFAULT NULL COMMENT '部门名称',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销部门';

-- 2.3 员工
DROP TABLE IF EXISTS employee;
CREATE TABLE employee (
  id              VARCHAR(32)  NOT NULL COMMENT '主键ID',
  employee_no     VARCHAR(20)  DEFAULT NULL COMMENT '员工工号',
  employee_name   VARCHAR(20)  DEFAULT NULL COMMENT '员工姓名',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

-- 2.4 业务类型
DROP TABLE IF EXISTS business_type;
CREATE TABLE business_type (
  id                  VARCHAR(32)  NOT NULL COMMENT '主键ID',
  business_type_no    VARCHAR(20)  DEFAULT NULL COMMENT '业务类型编号',
  business_type_name  VARCHAR(50)  DEFAULT NULL COMMENT '业务类型名称',
  there_subordinate_node VARCHAR(1) DEFAULT '0' COMMENT '是否有下级节点：0-无 1-有',
  superior_id         VARCHAR(32)  DEFAULT NULL COMMENT '上级业务类型ID，none表示最上级',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类型';

-- 2.5 城市
DROP TABLE IF EXISTS city;
CREATE TABLE city (
  id        VARCHAR(32)  NOT NULL COMMENT '主键ID',
  city_no   VARCHAR(20)  DEFAULT NULL COMMENT '城市编号',
  city_name VARCHAR(50)  DEFAULT NULL COMMENT '城市名称',
  city_type VARCHAR(1)   DEFAULT NULL COMMENT '城市类型：1-一线 2-二线 3-三线',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市';

-- 2.6 项目
DROP TABLE IF EXISTS project;
CREATE TABLE project (
  id           VARCHAR(32)  NOT NULL COMMENT '主键ID',
  project_no   VARCHAR(30)  DEFAULT NULL COMMENT '项目编号',
  project_name VARCHAR(50)  DEFAULT NULL COMMENT '项目名称',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

-- ---------------------------------------------
-- 3. 报销单主表（来自表结构定义模板）
-- ---------------------------------------------
DROP TABLE IF EXISTS fk_reim_main;
CREATE TABLE fk_reim_main (
  id                        VARCHAR(32)  NOT NULL COMMENT '主键ID',
  bill_no                   VARCHAR(32)  DEFAULT NULL COMMENT '报销单号',
  bill_status               VARCHAR(2)   DEFAULT '0' COMMENT '单据状态：0草稿 1已完成 2已作废',
  creation_time             VARCHAR(32)  DEFAULT NULL COMMENT '创建时间',
  update_time               VARCHAR(32)  DEFAULT NULL COMMENT '更新时间',
  reimbursement_title       VARCHAR(500) DEFAULT NULL COMMENT '报销标题',
  reimburser_id             VARCHAR(32)  DEFAULT NULL COMMENT '报销人ID',
  reimburser_no             VARCHAR(20)  DEFAULT NULL COMMENT '报销人工号',
  reimburser_name           VARCHAR(20)  DEFAULT NULL COMMENT '报销人姓名',
  reim_department_id        VARCHAR(20)  DEFAULT NULL COMMENT '报销部门ID',
  reim_department_no        VARCHAR(20)  DEFAULT NULL COMMENT '报销部门编号',
  reim_department_name      VARCHAR(20)  DEFAULT NULL COMMENT '报销部门名称',
  reim_company_id           VARCHAR(20)  DEFAULT NULL COMMENT '费用归属公司ID',
  reim_company_no           VARCHAR(20)  DEFAULT NULL COMMENT '费用归属公司编号',
  reim_company_name         VARCHAR(20)  DEFAULT NULL COMMENT '费用归属公司名称',
  business_type_id          VARCHAR(20)  DEFAULT NULL COMMENT '业务类型ID',
  business_type_no          VARCHAR(20)  DEFAULT NULL COMMENT '业务类型编号',
  business_type_name        VARCHAR(20)  DEFAULT NULL COMMENT '业务类型名称',
  business_trip_reason      VARCHAR(20)  DEFAULT NULL COMMENT '出差事由',
  subsidy_total             VARCHAR(20)  DEFAULT NULL COMMENT '补助总金额',
  meal_allowance            VARCHAR(20)  DEFAULT NULL COMMENT '餐费补助',
  transportation_allowance  VARCHAR(20)  DEFAULT NULL COMMENT '交通补助',
  phone_allowance           VARCHAR(20)  DEFAULT NULL COMMENT '通讯补助',
  remarks                   VARCHAR(20)  DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销单主表';

-- ---------------------------------------------
-- 4. 报销单明细表
-- ---------------------------------------------

-- 4.1 补录行程
DROP TABLE IF EXISTS fk_reim_itinerary;
CREATE TABLE fk_reim_itinerary (
  id                       VARCHAR(32)  NOT NULL COMMENT '主键ID',
  main_id                  VARCHAR(32)  NOT NULL COMMENT '报销单主表ID',
  subsidy_id               VARCHAR(32)  DEFAULT NULL COMMENT '关联补助信息ID',
  traveler_id              VARCHAR(32)  DEFAULT NULL COMMENT '出行人ID',
  traveler_no              VARCHAR(20)  DEFAULT NULL COMMENT '出行人工号',
  traveler_name            VARCHAR(20)  DEFAULT NULL COMMENT '出行人姓名',
  departure_date           DATE         DEFAULT NULL COMMENT '出发日期',
  arrival_date             DATE         DEFAULT NULL COMMENT '到达日期',
  departure_city           VARCHAR(50)  DEFAULT NULL COMMENT '出发城市',
  departure_city_no        VARCHAR(20)  DEFAULT NULL COMMENT '出发城市编号',
  arriving_city            VARCHAR(50)  DEFAULT NULL COMMENT '到达城市',
  arriving_city_no         VARCHAR(20)  DEFAULT NULL COMMENT '到达城市编号',
  itinerary_instructions   VARCHAR(500) DEFAULT NULL COMMENT '行程说明',
  PRIMARY KEY (id),
  KEY idx_itinerary_main (main_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销单补录行程';

-- 4.2 补助信息
DROP TABLE IF EXISTS fk_reim_subsidy;
CREATE TABLE fk_reim_subsidy (
  id                         VARCHAR(32)    NOT NULL COMMENT '主键ID',
  main_id                    VARCHAR(32)    NOT NULL COMMENT '报销单主表ID',
  traveler_id                VARCHAR(32)    DEFAULT NULL COMMENT '出行人ID',
  traveler_no                VARCHAR(20)    DEFAULT NULL COMMENT '出行人工号',
  traveler_name              VARCHAR(20)    DEFAULT NULL COMMENT '出行人姓名',
  departure_date             DATE           DEFAULT NULL COMMENT '出发日期',
  arrival_date               DATE           DEFAULT NULL COMMENT '到达日期',
  subsidy_days               INT            DEFAULT NULL COMMENT '补助天数',
  departure_city             VARCHAR(50)    DEFAULT NULL COMMENT '出发城市',
  departure_city_no          VARCHAR(20)    DEFAULT NULL COMMENT '出发城市编号',
  arriving_city              VARCHAR(50)    DEFAULT NULL COMMENT '到达城市',
  arriving_city_no           VARCHAR(20)    DEFAULT NULL COMMENT '到达城市编号',
  application_amount         DECIMAL(12,2)  DEFAULT 0.00 COMMENT '申请金额',
  subsidy_amount             DECIMAL(12,2)  DEFAULT 0.00 COMMENT '补助金额',
  meal_allowance             DECIMAL(12,2)  DEFAULT 0.00 COMMENT '餐费补助',
  transportation_allowance   DECIMAL(12,2)  DEFAULT 0.00 COMMENT '交通补助',
  phone_allowance            DECIMAL(12,2)  DEFAULT 0.00 COMMENT '通讯补助',
  business_type_id           VARCHAR(32)    DEFAULT NULL COMMENT '业务类型ID',
  business_type_no           VARCHAR(20)    DEFAULT NULL COMMENT '业务类型编号',
  business_type_name         VARCHAR(50)    DEFAULT NULL COMMENT '业务类型名称',
  PRIMARY KEY (id),
  KEY idx_subsidy_main (main_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销单补助信息';

-- 4.3 补助日历
DROP TABLE IF EXISTS fk_subsidy_calendar;
CREATE TABLE fk_subsidy_calendar (
  id                              VARCHAR(32)    NOT NULL COMMENT '主键ID',
  subsidy_id                      VARCHAR(32)    NOT NULL COMMENT '补助信息ID',
  travel_date                     DATE           DEFAULT NULL COMMENT '出差日期',
  travel_date_week                VARCHAR(10)    DEFAULT NULL COMMENT '星期',
  subsidized_cities               VARCHAR(50)    DEFAULT NULL COMMENT '补助城市',
  subsidized_city_number          VARCHAR(20)    DEFAULT NULL COMMENT '补助城市编号',
  city_type                       VARCHAR(1)     DEFAULT NULL COMMENT '城市类型',
  standard_meal_expenses_amount   DECIMAL(12,2)  DEFAULT 0.00 COMMENT '餐费标准',
  standard_traffic_amount         DECIMAL(12,2)  DEFAULT 0.00 COMMENT '交通标准',
  standard_communication_amount   DECIMAL(12,2)  DEFAULT 0.00 COMMENT '通讯标准',
  meal_expenses_amount            DECIMAL(12,2)  DEFAULT 0.00 COMMENT '餐费申请',
  traffic_amount                  DECIMAL(12,2)  DEFAULT 0.00 COMMENT '交通申请',
  communication_amount            DECIMAL(12,2)  DEFAULT 0.00 COMMENT '通讯申请',
  meal_selected                   VARCHAR(1)     DEFAULT '1' COMMENT '餐费选中',
  traffic_selected                VARCHAR(1)     DEFAULT '1' COMMENT '交通选中',
  communication_selected          VARCHAR(1)     DEFAULT '1' COMMENT '通讯选中',
  PRIMARY KEY (id),
  KEY idx_calendar_subsidy (subsidy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助日历';

-- 4.4 费用分摊
DROP TABLE IF EXISTS fk_reim_allocation;
CREATE TABLE fk_reim_allocation (
  id                VARCHAR(32)    NOT NULL COMMENT '主键ID',
  main_id           VARCHAR(32)    NOT NULL COMMENT '报销单主表ID',
  reim_company_id   VARCHAR(32)    DEFAULT NULL COMMENT '费用归属公司ID',
  reim_company_no   VARCHAR(20)    DEFAULT NULL COMMENT '公司编号',
  reim_company_name VARCHAR(50)    DEFAULT NULL COMMENT '公司名称',
  project_id        VARCHAR(32)    DEFAULT NULL COMMENT '项目ID',
  project_no        VARCHAR(30)    DEFAULT NULL COMMENT '项目编号',
  project_name      VARCHAR(50)    DEFAULT NULL COMMENT '项目名称',
  allocation_ratio  DECIMAL(5,4)   DEFAULT 0.00 COMMENT '分摊比例',
  allocation_amount DECIMAL(12,2)  DEFAULT 0.00 COMMENT '分摊金额',
  PRIMARY KEY (id),
  KEY idx_alloc_main (main_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用分摊';

-- ---------------------------------------------
-- 6. 插入数据（来自概要设计 5.3）
-- ---------------------------------------------

-- 4.1 费用归属公司（5.3.1）
INSERT INTO reim_company (id, company_no, company_name) VALUES
('1C54557F1782E000', '0407', '胜意科技北京分公司'),
('19218A262C976000', '0408', '胜意科技上海分公司'),
('1C61686865DA8000', '0409', '胜意科技武汉分公司'),
('1717271D1DA15000', '0410', '胜意科技杭州分公司'),
('16AE93CC7EF92002', '0411', '胜意科技荆州分公司');

-- 4.2 报销部门（5.3.2）
INSERT INTO reim_department (id, department_no, department_name) VALUES
('13AB8D7B52A9B002', '072001', '客户成功事业部'),
('13BFD31C6029A002', '072002', '企业消费事业部'),
('14515BB4BFB92003', '072003', '企业费控事业部'),
('19206611C47A6000', '072004', '集采事业部'),
('19D32F9FE9647000', '072005', '航旅事业部'),
('13C7E2BAE0393001', '072006', '运营事业部'),
('14055D22BB808001', '072007', '营销事业部');

-- 4.3 员工（5.3.3）
INSERT INTO employee (id, employee_no, employee_name) VALUES
('13AB3A3F72409002', '74541', '徐年年'),
('13AB498CC6409002', '74008', '郑雨雪'),
('13AB4A56BB009002', '21552', '邹薇'),
('13AB591FE8009002', '80681', '王成军'),
('13AB77281A408001', '89899', '潘展飞'),
('13AB7925EB808001', '10503', '姜林');

-- 4.4 业务类型（5.3.4）
INSERT INTO business_type (id, business_type_no, business_type_name, there_subordinate_node, superior_id) VALUES
('18F0916A8C2C4000', '1001001',     '员工差旅活动',   '1', 'none'),
('18F091913EEC4000', '100100101',   '境内出差',       '1', '18F0916A8C2C4000'),
('1B5FEB7DD4396000', '10010010101', '项目出差',       '0', '18F091913EEC4000'),
('1A92E43082EFC000', '10010010102', '市场拓展出差',   '0', '18F091913EEC4000'),
('13AB3A4138008001', '100100102',   '境外出差',       '1', '18F0916A8C2C4000'),
('13AB3A4248008002', '10010010201', '国外考察',       '0', '13AB3A4138008001'),
('13AB3A4154008001', '10010010202', '售后维护出差',   '0', '13AB3A4138008001'),
('13AB3A4172008001', '1001002',     '人力资源',       '1', 'none'),
('13AB3A418F808001', '100100201',   '个人团队培训',   '0', '13AB3A4172008001'),
('13AB3A41AC408001', '100100202',   '招聘会',         '0', '13AB3A4172008001'),
('13AB3A41CD808002', '1001003',     '员工福利',       '1', 'none'),
('13AB3A41ED408002', '100100301',   '员工旅游',       '0', '13AB3A41CD808002'),
('13AB3A420CC08002', '100100302',   '员工团建',       '0', '13AB3A41CD808002'),
('13AB3A422A808001', '100100303',   '员工体检',       '0', '13AB3A41CD808002');

-- 4.5 城市（5.3.5）
INSERT INTO city (id, city_no, city_name, city_type) VALUES
('1', '10119', '北京', '1'),
('2', '10621', '上海', '1'),
('3', '10458', '武汉', '2'),
('4', '10216', '杭州', '2'),
('5', '10455', '荆州', '3');

-- 4.6 项目（5.3.6）
INSERT INTO project (id, project_no, project_name) VALUES
('12BC248B25083001', 'nonProjectRelated', '非项目类费用归集'),
('1C811ABF96195000', 'centralChina',      '华中客户定制化项目'),
('1C5931735AC4A000', 'southChina',        '华南客户定制化项目'),
('1771EC45F2443000', 'northChina',        '华北客户定制化项目'),
('1762792DB4E9A002', 'eastChina',         '华东客户定制化项目'),
('17071065FC29A002', 'southWest',         '西南客户定制化项目'),
('162664EBE9ABE001', 'northWest',         '西北客户定制化项目'),
('162664B8526BE002', 'northEast',         '东北客户定制化项目');

-- 4.7 报销单主表 示例数据（可选）
INSERT INTO fk_reim_main (id, bill_no, bill_status, creation_time, reimbursement_title, reimburser_id, reimburser_no, reimburser_name,
  reim_department_id, reim_department_no, reim_department_name,
  reim_company_id, reim_company_no, reim_company_name,
  business_type_id, business_type_no, business_type_name,
  business_trip_reason, subsidy_total, meal_allowance, transportation_allowance, phone_allowance, remarks) VALUES
('REIM20250522001', 'REIM20250522001', '0', '2025-05-22 10:30:00', '武汉出差报销', '13AB3A3F72409002', '74541', '徐年年',
  '13AB8D7B52A9B002', '072001', '客户成功事业部',
  '1C61686865DA8000', '0409', '胜意科技武汉分公司',
  '1B5FEB7DD4396000', '10010010101', '项目出差',
  '华中客户定制化项目上线支持', '340.00', '200.00', '80.00', '60.00', '无');

-- 4.7.1 补录行程示例（关联 REIM20250522001）
INSERT INTO fk_reim_itinerary (id, main_id, subsidy_id, traveler_id, traveler_no, traveler_name,
  departure_date, arrival_date, departure_city, departure_city_no, arriving_city, arriving_city_no, itinerary_instructions) VALUES
('TRIP001', 'REIM20250522001', 'SUB001', '13AB3A3F72409002', '74541', '徐年年',
 '2025-05-20', '2025-05-22', '武汉', '10458', '北京', '10119', '项目上线支持出差');

-- 4.7.2 补助信息示例（关联 REIM20250522001）
INSERT INTO fk_reim_subsidy (id, main_id, traveler_id, traveler_no, traveler_name,
  departure_date, arrival_date, subsidy_days, departure_city, departure_city_no,
  arriving_city, arriving_city_no, application_amount, subsidy_amount,
  meal_allowance, transportation_allowance, phone_allowance,
  business_type_id, business_type_no, business_type_name) VALUES
('SUB001', 'REIM20250522001', '13AB3A3F72409002', '74541', '徐年年',
 '2025-05-20', '2025-05-22', 3, '武汉', '10458',
 '北京', '10119', 540.00, 540.00, 300.00, 120.00, 120.00,
 '1B5FEB7DD4396000', '10010010101', '项目出差');

-- 4.7.3 补助日历示例（关联 SUB001，3天）
INSERT INTO fk_subsidy_calendar (id, subsidy_id, travel_date, travel_date_week,
  subsidized_cities, subsidized_city_number, city_type,
  standard_meal_expenses_amount, standard_traffic_amount, standard_communication_amount,
  meal_expenses_amount, traffic_amount, communication_amount,
  meal_selected, traffic_selected, communication_selected) VALUES
('CAL001', 'SUB001', '2025-05-20', '星期二', '北京', '10119', '1', 100.00, 40.00, 40.00, 100.00, 40.00, 40.00, '1', '1', '1'),
('CAL002', 'SUB001', '2025-05-21', '星期三', '北京', '10119', '1', 100.00, 40.00, 40.00, 100.00, 40.00, 40.00, '1', '1', '1'),
('CAL003', 'SUB001', '2025-05-22', '星期四', '北京', '10119', '1', 100.00, 40.00, 40.00, 100.00, 40.00, 40.00, '1', '1', '1');

-- 4.7.4 费用分摊示例（关联 REIM20250522001）
INSERT INTO fk_reim_allocation (id, main_id, reim_company_id, reim_company_no, reim_company_name,
  project_id, project_no, project_name, allocation_ratio, allocation_amount) VALUES
('ALLOC001', 'REIM20250522001', '1C61686865DA8000', '0409', '胜意科技武汉分公司',
 '1C811ABF96195000', 'centralChina', '华中客户定制化项目', 1.0000, 540.00);
