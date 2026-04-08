-- 用户体重记录表
CREATE TABLE hc_weight_record (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  user_id INT NOT NULL,
                                  record_date DATE NOT NULL,
                                  weight DOUBLE NOT NULL,
                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_weight_user_date ON hc_weight_record (user_id, record_date);