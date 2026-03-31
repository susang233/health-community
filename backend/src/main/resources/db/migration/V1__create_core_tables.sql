CREATE TABLE hc_user (
                         user_id INT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(15) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         nick_name VARCHAR(20) NOT NULL,
                         role VARCHAR(20) NOT NULL DEFAULT 'USER',
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE hc_health_profile (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   user_id INT NOT NULL UNIQUE,
                                   gender VARCHAR(20) NOT NULL,
                                   height INT NOT NULL,
                                   birthday DATE NOT NULL,
                                   activity_level VARCHAR(20) NOT NULL,
                                   current_weight DOUBLE NOT NULL,
                                   target_weight DOUBLE NOT NULL,
                                   bmi DOUBLE NOT NULL,
                                   bmr DOUBLE NOT NULL,
                                   tdee INT NOT NULL,
                                   recommended_calories INT NOT NULL,
                                   create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);