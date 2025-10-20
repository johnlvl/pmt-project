-- PMT database initialization script (MySQL)

CREATE DATABASE IF NOT EXISTS pmt_db;
USE pmt_db;

-- Table User
CREATE TABLE IF NOT EXISTS `User` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL
);

-- Table Role
CREATE TABLE IF NOT EXISTS `Role` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(30) NOT NULL
);

-- Table Project
CREATE TABLE IF NOT EXISTS `Project` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `start_date` DATE
);

-- Table ProjectMember
CREATE TABLE IF NOT EXISTS `ProjectMember` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `project_id` INT NOT NULL,
  `role_id` INT NOT NULL,
  CONSTRAINT fk_pm_user FOREIGN KEY (`user_id`) REFERENCES `User`(`id`),
  CONSTRAINT fk_pm_project FOREIGN KEY (`project_id`) REFERENCES `Project`(`id`),
  CONSTRAINT fk_pm_role FOREIGN KEY (`role_id`) REFERENCES `Role`(`id`)
);

-- Table Task
CREATE TABLE IF NOT EXISTS `Task` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `due_date` DATE,
  `end_date` DATE,
  `priority` VARCHAR(20),
  `status` VARCHAR(20),
  CONSTRAINT fk_task_project FOREIGN KEY (`project_id`) REFERENCES `Project`(`id`)
);

-- Table TaskAssignment
CREATE TABLE IF NOT EXISTS `TaskAssignment` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `task_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  CONSTRAINT fk_ta_task FOREIGN KEY (`task_id`) REFERENCES `Task`(`id`),
  CONSTRAINT fk_ta_user FOREIGN KEY (`user_id`) REFERENCES `User`(`id`)
);

-- Table TaskHistory
CREATE TABLE IF NOT EXISTS `TaskHistory` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `task_id` INT NOT NULL,
  `changed_by` INT NOT NULL,
  `change_date` DATETIME NOT NULL,
  `change_description` TEXT,
  CONSTRAINT fk_th_task FOREIGN KEY (`task_id`) REFERENCES `Task`(`id`),
  CONSTRAINT fk_th_user FOREIGN KEY (`changed_by`) REFERENCES `User`(`id`)
);

-- Table Notification
CREATE TABLE IF NOT EXISTS `Notification` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `task_id` INT NOT NULL,
  `message` TEXT,
  `is_read` BOOLEAN DEFAULT FALSE,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (`user_id`) REFERENCES `User`(`id`),
  CONSTRAINT fk_notif_task FOREIGN KEY (`task_id`) REFERENCES `Task`(`id`)
);

-- Seed data
INSERT INTO `Role` (`name`) VALUES ('Administrateur'), ('Membre'), ('Observateur');

INSERT INTO `User` (`username`, `email`, `password`) VALUES
  ('alice', 'alice@example.com', 'password1'),
  ('bob',   'bob@example.com',   'password2'),
  ('carol', 'carol@example.com', 'password3');

INSERT INTO `Project` (`name`, `description`, `start_date`) VALUES
  ('Projet Alpha', 'Premier projet de test', '2025-09-01'),
  ('Projet Beta',  'Deuxième projet de test', '2025-09-10');

INSERT INTO `ProjectMember` (`user_id`, `project_id`, `role_id`) VALUES
  (1, 1, 1), -- Alice admin Projet Alpha
  (2, 1, 2), -- Bob membre Projet Alpha
  (3, 1, 3), -- Carol observateur Projet Alpha
  (1, 2, 2), -- Alice membre Projet Beta
  (2, 2, 1); -- Bob admin Projet Beta

INSERT INTO `Task` (`project_id`, `name`, `description`, `due_date`, `priority`, `status`) VALUES
  (1, 'Tâche 1', 'Description tâche 1', '2025-09-20', 'Haute', 'A faire'),
  (1, 'Tâche 2', 'Description tâche 2', '2025-09-25', 'Moyenne', 'En cours'),
  (2, 'Tâche 3', 'Description tâche 3', '2025-09-30', 'Basse', 'Terminée');

INSERT INTO `TaskAssignment` (`task_id`, `user_id`) VALUES
  (1, 2), -- Bob sur Tâche 1
  (2, 1), -- Alice sur Tâche 2
  (3, 2); -- Bob sur Tâche 3

INSERT INTO `TaskHistory` (`task_id`, `changed_by`, `change_date`, `change_description`) VALUES
  (1, 1, '2025-09-15 10:00:00', 'Création de la tâche'),
  (1, 2, '2025-09-16 14:00:00', 'Changement de statut à "En cours"');

INSERT INTO `Notification` (`user_id`, `task_id`, `message`) VALUES
  (2, 1, 'Vous avez été assigné à la tâche 1'),
  (1, 2, 'Vous avez été assigné à la tâche 2');
