-- 1. 添加食物来源字段
ALTER TABLE hc_food
    ADD COLUMN data_source VARCHAR(20) NOT NULL DEFAULT 'BOOHEE';

-- 2. 添加是否锁定字段
ALTER TABLE hc_food
    ADD COLUMN is_locked BOOLEAN NOT NULL DEFAULT false;
-- 4.添加是否隐藏字段
ALTER TABLE hc_food
    ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false ;

-- 3. 给现有所有数据统一赋值（历史数据 = 薄荷来源 + 未锁定）
-- 保证历史数据正常运行，不会破坏业务
UPDATE hc_food
SET data_source = 'BOOHEE',
    is_locked  = false,
    hidden=false
WHERE data_source IS NULL;