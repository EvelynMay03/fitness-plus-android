-- FITNESS+ database schema for XAMPP
-- Database name must match config.php (Fitness)

USE Fitness;

CREATE TABLE IF NOT EXISTS user (
  userid INT(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  PRIMARY KEY (userid),
  UNIQUE KEY unique_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activities (
  id BIGINT NOT NULL AUTO_INCREMENT,
  activity_key VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  emoji VARCHAR(10) DEFAULT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_activity_key (activity_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS goals (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  goals INT NOT NULL,
  goal_date DATE NOT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_goal_date (user_id, goal_date),
  CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES user (userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workouts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  activity_type VARCHAR(50) NOT NULL,
  duration_minutes INT NOT NULL,
  weight_kg DECIMAL(6,2) NOT NULL,
  calories INT DEFAULT NULL,
  logged_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) REFERENCES user (userid),
  CONSTRAINT fk_workouts_activity FOREIGN KEY (activity_type) REFERENCES activities (activity_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO activities (activity_key, name, description, emoji) VALUES
('running', 'Running', 'Duration and weight', '🏃'),
('cycling', 'Cycling', 'Duration and weight', '🚴'),
('weightlifting', 'Weightlifting', 'Duration and weight lifted', '🏋️'),
('yoga', 'Yoga', 'Duration and weight', '🧘'),
('cardio', 'Cardio', 'Duration and weight', '🤸'),
('swimming', 'Swimming', 'Duration and weight', '🏊'),
('hiking', 'Hiking', 'Duration and weight', '🥾'),
('walking', 'Walking', 'Duration and weight', '🚶')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  emoji = VALUES(emoji);
