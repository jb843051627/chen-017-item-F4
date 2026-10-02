-- 事项卡「一项事项一张卡·一张卡只挂一本册·一册只归一家部门」升级迁移
-- 适用于已按旧版 item.sql 建过 t_item_implement_card 的库（库：chen_017）。
-- 全新库直接执行 item.sql 即可，不必跑本文件。
-- MySQL 8.0 的 ADD COLUMN 不支持 IF NOT EXISTS，重复执行会报列已存在，属正常。

ALTER TABLE t_item_implement_card
  CHANGE COLUMN site_id site_id bigint DEFAULT NULL COMMENT '所属承办部门底册主键',
  ADD COLUMN impl_code varchar(64) DEFAULT NULL COMMENT '实施编码(系统发)' AFTER bill_no,
  ADD COLUMN item_code varchar(64) DEFAULT NULL COMMENT '事项码(与底册代号配平认卡)' AFTER site_no,
  ADD COLUMN handle_level int DEFAULT NULL COMMENT '承办层级 1省定标准 2市实施 3县延伸' AFTER item_code,
  ADD COLUMN upper_item_no varchar(64) DEFAULT NULL COMMENT '省定:上位名录那一条的代号' AFTER handle_level,
  ADD COLUMN upper_basis_clause varchar(128) DEFAULT NULL COMMENT '省定:设定依据的条款号' AFTER upper_item_no,
  ADD COLUMN city_refine_text varchar(500) DEFAULT NULL COMMENT '市实施:本市细化那几条' AFTER upper_basis_clause,
  ADD COLUMN city_eff_start datetime DEFAULT NULL COMMENT '市实施:起头那一日' AFTER city_refine_text,
  ADD COLUMN city_eff_end datetime DEFAULT NULL COMMENT '市实施:结尾那一日' AFTER city_eff_start,
  ADD COLUMN extend_window_count int DEFAULT NULL COMMENT '县延伸:延伸到的窗口数' AFTER city_eff_end,
  ADD COLUMN extend_point_count int DEFAULT NULL COMMENT '县延伸:收件点数目' AFTER extend_window_count,
  ADD COLUMN region_path varchar(128) DEFAULT NULL COMMENT '立卡时底册区划快照,改隶不搬旧卡' AFTER extend_point_count,
  ADD COLUMN entrust_site_no varchar(64) DEFAULT NULL COMMENT '跨市协办受托底册代号' AFTER region_path,
  ADD COLUMN year_no varchar(8) DEFAULT NULL COMMENT '立卡年度(跨年同屏不重复计)' AFTER entrust_site_no,
  ADD COLUMN steps_done int DEFAULT NULL COMMENT '已走完步数(状态照它自数)' AFTER grade_level,
  ADD COLUMN current_flag int DEFAULT 1 COMMENT '现行标记 0历史旧版 1现行' AFTER status,
  ADD COLUMN stop_flag int DEFAULT 0 COMMENT '就地叫停 0否 1是' AFTER current_flag,
  ADD COLUMN legacy_flag int DEFAULT 0 COMMENT '纸质簿迁入旧卡 0否 1是' AFTER stop_flag,
  ADD COLUMN opener_name varchar(64) DEFAULT NULL COMMENT '开卡人(随卡一生不换)' AFTER legacy_flag,
  ADD COLUMN handler_name varchar(64) DEFAULT NULL COMMENT '本趟经办人(办一次续一名)' AFTER opener_name,
  ADD KEY idx_card_pair (site_no, item_code, current_flag),
  ADD KEY idx_card_bill (bill_no, node_no),
  ADD KEY idx_card_region (region_path);

-- 旧卡迁入不抛错：历史数据版次序/步数/校验串留空即视为旧纸卡，
-- 服务层 admitLegacy 会把它们按现行待核对接住。

CREATE TABLE IF NOT EXISTS t_item_card_trail (
  id bigint NOT NULL COMMENT '主键',
  card_id bigint DEFAULT NULL COMMENT '卡主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '卡号',
  node_no int DEFAULT NULL COMMENT '当时版次序',
  round_no int DEFAULT 1 COMMENT '第几趟(每回退一次,后补记在下一趟)',
  action varchar(32) DEFAULT NULL COMMENT '动作 open/advance/back/qty/rehang/revise/stop',
  from_step int DEFAULT NULL COMMENT '动前步数',
  to_step int DEFAULT NULL COMMENT '动后步数',
  from_grade int DEFAULT NULL COMMENT '动前层级',
  to_grade int DEFAULT NULL COMMENT '动后层级',
  operator_name varchar(64) DEFAULT NULL COMMENT '本趟经办人',
  trail_time datetime DEFAULT NULL COMMENT '落记时刻',
  note varchar(500) DEFAULT NULL COMMENT '记事',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (id),
  KEY idx_trail_card (card_id, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事项卡过步留痕';
