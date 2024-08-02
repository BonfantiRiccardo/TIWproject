CREATE DATABASE  IF NOT EXISTS `progetto` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `progetto`;
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: progetto
-- ------------------------------------------------------
-- Server version	8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cartelle`
--

DROP TABLE IF EXISTS `cartelle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cartelle` (
  `idcartella` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(45) NOT NULL,
  `dataCreazione` date NOT NULL,
  `creatore` varchar(45) NOT NULL,
  `cartellaPadre` int DEFAULT NULL,
  PRIMARY KEY (`idcartella`),
  KEY `username_idx` (`creatore`),
  CONSTRAINT `username` FOREIGN KEY (`creatore`) REFERENCES `user` (`username`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cartelle`
--

LOCK TABLES `cartelle` WRITE;
/*!40000 ALTER TABLE `cartelle` DISABLE KEYS */;
INSERT INTO `cartelle` VALUES (1,'desktop','2020-12-25','ricky',-1),(2,'downloads','2020-12-26','ricky',-1),(3,'documenti','2021-01-10','ricky',1),(4,'foto','2021-01-30','ricky',1),(5,'vacanze','2021-01-30','ricky',4),(6,'capodanno','2021-02-05','ricky',4),(7,'programmi','2024-06-18','ricky',-1),(8,'java','2024-06-18','ricky',7),(9,'java','2024-06-18','ricky',8),(16,'drive','2024-06-18','ricky',-1),(17,'GAMES','2024-06-18','ricky',-1),(18,'desktop','2024-06-19','luisa',-1),(19,'lavoro','2024-06-19','luisa',18),(20,'immagini','2024-06-19','luisa',-1),(21,'foto','2024-06-19','luisa',20),(22,'progetto TIW','2024-06-19','ricky',1),(23,'docker','2024-06-19','ricky',7),(25,'robotics','2024-06-19','ricky',23),(36,'Utenti','2024-06-27','ricky',-1),(37,'bonfa','2024-06-27','ricky',36),(38,'polimi','2024-06-29','ricky',-1),(39,'3 anno','2024-06-29','ricky',38),(41,'root','2024-07-05','riloda',-1),(42,'musica','2024-07-05','riloda',41),(43,'Codice','2024-07-05','riloda',41),(44,'Linguaggio C','2024-07-05','riloda',43),(45,'documenti','2024-07-06','riloda',-1),(46,'lavoro','2024-07-06','riloda',45),(47,'scuola','2024-07-06','riloda',45),(48,'Giugno 2024','2024-07-06','riloda',46);
/*!40000 ALTER TABLE `cartelle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documenti`
--

DROP TABLE IF EXISTS `documenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documenti` (
  `iddocumento` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(45) NOT NULL,
  `datacreazione` date NOT NULL,
  `sommario` varchar(100) NOT NULL,
  `tipo` varchar(45) NOT NULL,
  `creatore` varchar(45) NOT NULL,
  `cartellapadre` int NOT NULL,
  PRIMARY KEY (`iddocumento`),
  KEY `username_idx` (`creatore`),
  KEY `idcartella_idx` (`cartellapadre`),
  CONSTRAINT `doc_username` FOREIGN KEY (`creatore`) REFERENCES `user` (`username`) ON DELETE CASCADE,
  CONSTRAINT `idcartella` FOREIGN KEY (`cartellapadre`) REFERENCES `cartelle` (`idcartella`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documenti`
--

LOCK TABLES `documenti` WRITE;
/*!40000 ALTER TABLE `documenti` DISABLE KEYS */;
INSERT INTO `documenti` VALUES (1,'carta identità','2020-04-03','Foto della carta d\'identità','jpg','ricky',3),(2,'Ricetta per la pizza','2021-05-03','Video con la ricetta per fare una pizza','mp4','ricky',2),(3,'golden gate bridge','2021-10-13','Foto del golden gate bridge','jpg','ricky',5),(4,'modulo tasse polimi','2023-05-20','Modulo per il pagamento delle tasse universitarie','pdf','ricky',2),(5,'icona java','2024-06-18','Immagine dell\'icona java','jpg','ricky',9),(6,'star wars battlefront','2024-06-18','contiene una serie di informazioni riguardanti il gioco','docx','ricky',17),(9,'esami','2024-06-18','elenco di tutti gli esami che mi sono rimasti da sostenere','docx','ricky',1),(10,'orario','2024-06-19','orario lavorativo estate 2024','docx','luisa',18),(11,'turni','2024-06-19','turni settimanali oratorio','jpg','luisa',19),(12,'matrimonio','2024-06-19','foto del matrimonio','jpg','luisa',21),(13,'the last of us','2024-06-19','guida al gioco the last of us','mp4','ricky',17),(15,'home','2024-06-19','homepage del progetto TIW','html','ricky',22),(16,'icona java','2024-06-20','duplicato del doc di icona java per testare servlet','png','ricky',8),(18,'Star Wars Jedi Fallen Order','2024-06-24','Launcher del gioco','exe','ricky',17),(20,'f1 23','2024-06-26','impostazioni di gioco','txt','ricky',17),(22,'Star Wars The Clone Wars','2024-06-26','episodio 1-26','mp4','ricky',17),(28,'sfondo pc','2024-06-27','immagine di sfondo del computer','jpg','ricky',37),(29,'homeStyle.css','2024-06-29','foglio di stile css per la pagina home del progetto TIW','css','ricky',39),(31,'Imperial March','2024-07-05','Brano della colonna sonora de L\'impero Colpisce Ancora','mp3','riloda',42),(32,'Fibonacci','2024-07-05','Codice che stampa la sequenza dei primi 10 numeri di Fibonacci','c','riloda',44),(33,'Binary Sunset','2024-07-05','Brano della colonna sonora de Una Nuova Speranza','mp3','riloda',42),(34,'Rendiconto Giugno 2024','2024-07-06','Rendiconto delle ore di lavoro effettuate a giungo 2024','docx','riloda',48),(35,'Esame Meccanica','2024-07-06','Tema d\'esame di meccanica dell\'8 giugno 2024','pdf','riloda',47);
/*!40000 ALTER TABLE `documenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `username` varchar(45) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('dade','dade@outlook.it','aaa'),('dodo','dudu@gmail.com','bbb'),('luisa','luisaf@gmail.com','user2'),('matteo','matte@polimi.it','pwd'),('ricky','riloda897@gmail.com','admin'),('riloda','rld@polimi.it','ccc');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'progetto'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-07-06 17:23:02
