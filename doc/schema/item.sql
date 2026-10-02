-- item 政务服务事项标准化管理与实施清单同源发布管理 -- schema (chen-017)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_017

CREATE TABLE IF NOT EXISTS t_item_alert_rule (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '办件逾期风险判定线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '低界逾期率(百分比)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '中界逾期率(百分比)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '高界逾期率(百分比)',
  eff_start datetime DEFAULT NULL COMMENT '启用之日',
  eff_end datetime DEFAULT NULL COMMENT '交棒之日(不含)',
  priority int DEFAULT NULL COMMENT '让线顺位(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0现行 1已让位',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='办件逾期风险判定线';

CREATE TABLE IF NOT EXISTS t_item_catalog_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '上级目录下发册次号',
  row_no int DEFAULT NULL COMMENT '原册行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被挑出的前置项代号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行待核要素项数',
  status int DEFAULT NULL COMMENT '行落地情形 0待核 1已挂接 2挂起',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上级目录下发册核对行';

CREATE TABLE IF NOT EXISTS t_item_catalog_ver (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '事项目录版次单号',
  stage int DEFAULT NULL COMMENT '当前档次 0..3（草拟/复核/付发布/已上柜）',
  status int DEFAULT NULL COMMENT '版次落点 0未起 1在办 2已作废',
  content varchar(255) DEFAULT NULL COMMENT '版次记事',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次过口动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事项目录条目版次单';

CREATE TABLE IF NOT EXISTS t_item_change_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '要素调整核签单号',
  node_no int DEFAULT NULL COMMENT '当前所在道次 0..2（县核/市核/省定）',
  sign_mode int DEFAULT NULL COMMENT '同口并签办法 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本口应落名数',
  sign_count int DEFAULT NULL COMMENT '本口已落名数',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事项要素调整逐级核签单';

CREATE TABLE IF NOT EXISTS t_item_check_task (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '核查派单号',
  due_at datetime DEFAULT NULL COMMENT '该动手那一日的止点时刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '提前几日开口派单',
  content varchar(255) DEFAULT NULL COMMENT '事由与被派部门记要',
  status int DEFAULT NULL COMMENT '条目情形 0未开口 1已派出 2办不成',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='要素定期核查派单';

CREATE TABLE IF NOT EXISTS t_item_dept_arch (
  id bigint NOT NULL COMMENT '主键',
  site_no varchar(64) DEFAULT NULL COMMENT '承办部门底册代号',
  site_name varchar(128) DEFAULT NULL COMMENT '部门全称',
  site_type varchar(32) DEFAULT NULL COMMENT '机构类别',
  road_name varchar(128) DEFAULT NULL COMMENT '落在哪一级政务区划(省—市—县)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '单卡年办件量低界(件/年)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '中界(件/年)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '高界(件/年)',
  status int DEFAULT NULL COMMENT '底册情形 0在编 1已撤并',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='承办部门底册';

CREATE TABLE IF NOT EXISTS t_item_implement_card (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '实施清单配置卡号(系统发,同一卡跨版次共用)',
  impl_code varchar(64) DEFAULT NULL COMMENT '实施编码(系统发)',
  node_no int DEFAULT NULL COMMENT '本卡版次序(旧纸卡迁入可空,首立按1起)',
  site_id bigint DEFAULT NULL COMMENT '所属承办部门底册主键',
  site_no varchar(64) DEFAULT NULL COMMENT '所属部门底册代号(只凭代号对册)',
  item_code varchar(64) DEFAULT NULL COMMENT '事项码(与底册代号配平认卡)',
  handle_level int DEFAULT NULL COMMENT '承办层级 1省定标准 2市实施 3县延伸',
  upper_item_no varchar(64) DEFAULT NULL COMMENT '省定:上位名录那一条的代号',
  upper_basis_clause varchar(128) DEFAULT NULL COMMENT '省定:设定依据的条款号',
  city_refine_text varchar(500) DEFAULT NULL COMMENT '市实施:本市细化那几条',
  city_eff_start datetime DEFAULT NULL COMMENT '市实施:起头那一日',
  city_eff_end datetime DEFAULT NULL COMMENT '市实施:结尾那一日',
  extend_window_count int DEFAULT NULL COMMENT '县延伸:延伸到的窗口数',
  extend_point_count int DEFAULT NULL COMMENT '县延伸:收件点数目',
  region_path varchar(128) DEFAULT NULL COMMENT '立卡时底册区划快照(省—市—县),改隶不搬旧卡',
  entrust_site_no varchar(64) DEFAULT NULL COMMENT '跨市协办受托底册代号(只摆给受托那一家)',
  year_no varchar(8) DEFAULT NULL COMMENT '立卡年度(跨年同屏不重复计)',
  qty decimal(12,2) DEFAULT NULL COMMENT '本卡申报年办件量(件/年)',
  fine_amt decimal(12,2) DEFAULT NULL COMMENT '本卡承诺时限(工作日)',
  content varchar(255) DEFAULT NULL COMMENT '校验串(走到已核对才钉,只由系统写)',
  grade_level int DEFAULT NULL COMMENT '年办件量折到的服务监督层级(只由系统折)',
  steps_done int DEFAULT NULL COMMENT '已走完步数 0待核对 1已核对 2已冻住(状态照它自数)',
  status int DEFAULT NULL COMMENT '进展镜像 0待核对 1已核对 2已冻住(旧纸卡可空)',
  current_flag int DEFAULT '1' COMMENT '现行标记 0历史旧版 1现行(日常只摆现行)',
  stop_flag int DEFAULT '0' COMMENT '就地叫停 0否 1是(叫停即腾出同事项名额)',
  legacy_flag int DEFAULT '0' COMMENT '纸质簿迁入旧卡 0否 1是(缺步数/版次/串也接得住)',
  opener_name varchar(64) DEFAULT NULL COMMENT '开卡人(一钉下随卡一生,不换)',
  handler_name varchar(64) DEFAULT NULL COMMENT '本趟经办人(办一次续一名,不覆盖开卡人)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_card_pair (site_no, item_code, current_flag),
  KEY idx_card_bill (bill_no, node_no),
  KEY idx_card_region (region_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实施清单配置卡';

-- 卡的每一趟过步留痕：回退重走另起一趟，旧记录不与后补并作一处。
CREATE TABLE IF NOT EXISTS t_item_card_trail (
  id bigint NOT NULL COMMENT '主键',
  card_id bigint DEFAULT NULL COMMENT '卡主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '卡号',
  node_no int DEFAULT NULL COMMENT '当时版次序',
  round_no int DEFAULT '1' COMMENT '第几趟(每回退一次,后补记在下一趟)',
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

-- 初始档案数据（两条种子底册：一条在编、一条已撤并）
-- t_item_dept_arch 两条种子底册：id=0 在编、id=1 已撤并。
-- 单卡年办件量三线上限取 300/1500/6000 件。
INSERT IGNORE INTO t_item_dept_arch (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'ZW00', '青屏市承办部门底册（市政务服务和数据管理局，在编）', '行政许可', '青屏省—青屏市—青屏县', 300.00, 1500.00, 6000.00, 0, 0, 'seed', NOW()),
       (1, 'ZW01', '青屏市承办部门底册（市住房和城乡建设管理局，已撤并）', '行政确认', '青屏省—青屏市—青屏县', 300.00, 1500.00, 6000.00, 1, 0, 'seed', NOW());

