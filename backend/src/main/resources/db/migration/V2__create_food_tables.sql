-- 食物主表（hc_food）
CREATE TABLE hc_food (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         code VARCHAR(255) NOT NULL UNIQUE,
                         name VARCHAR(255) NOT NULL,
                         image_url VARCHAR(500),
                         health_light INT,
                         calories_per_100g DOUBLE NOT NULL,
                         protein_per_100g DOUBLE,
                         fat_per_100g DOUBLE,
                         carbs_per_100g DOUBLE,
                         is_liquid BOOLEAN,
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 饮食记录表（hc_food_record）
CREATE TABLE hc_food_record (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id INT NOT NULL,
                                record_time DATETIME NOT NULL,
                                name VARCHAR(255) NOT NULL,
                                image_url VARCHAR(500),
                                food_code VARCHAR(255) NOT NULL,
                                meal_type VARCHAR(20) NOT NULL,
                                weight DOUBLE NOT NULL,
                                is_liquid BOOLEAN NOT NULL,
                                health_light INT,
                                calories DOUBLE NOT NULL,
                                protein DOUBLE,
                                fat DOUBLE,
                                carbs DOUBLE,
                                calories_per_100g DOUBLE,
                                protein_per_100g DOUBLE,
                                fat_per_100g DOUBLE,
                                carbs_per_100g DOUBLE,
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                INDEX idx_user_record_time (user_id, record_time)
);