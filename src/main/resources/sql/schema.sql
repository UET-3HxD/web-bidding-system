-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: auction_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--
USE auction_db;
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `username` varchar(100) NOT NULL UNIQUE,
                         `password` varchar(255) NOT NULL,
                         `email` varchar(100) DEFAULT NULL,
                         `role` varchar(50) NOT NULL,
    -- Cột mới đây Captain: Tự động lấy thời gian hiện tại khi thêm user
                         `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         seller_id INT NOT NULL,
                         product_name VARCHAR(255) NOT NULL,
                         description TEXT,
                         starting_price DECIMAL(15, 2),
                         image_path TEXT,
    -- Cột phân loại quan trọng
                         item_type ENUM('ELECTRONIC', 'ART', 'VEHICLE') NOT NULL,
                         status ENUM('WAITING', 'LIVE', 'SOLD', 'APPROVED', 'REJECTED') DEFAULT 'WAITING',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);
USE auction_db;
DROP TABLE IF EXISTS `auction_sessions`;
CREATE TABLE auction_sessions (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  item_id INT NOT NULL,
                                  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  end_time TIMESTAMP NOT NULL,
                                  status ENUM('ACTIVE', 'ENDED', 'CANCELLED') DEFAULT 'ACTIVE',
                                  FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);
DROP TABLE IF EXISTS `bids`;
CREATE TABLE bids (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      auction_id INT NOT NULL,
                      user_id INT NOT NULL,
                      bid_amount DECIMAL(15, 2) NOT NULL,
                      bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (auction_id) REFERENCES auction_sessions(id) ON DELETE CASCADE,
                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-17 21:05:43
