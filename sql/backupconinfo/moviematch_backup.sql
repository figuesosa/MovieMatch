-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: moviematch
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `genres`
--

DROP TABLE IF EXISTS `genres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `genres` (
  `genre_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`genre_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `genres`
--

LOCK TABLES `genres` WRITE;
/*!40000 ALTER TABLE `genres` DISABLE KEYS */;
INSERT INTO `genres` VALUES (1,'Acción'),(6,'Animación'),(2,'Ciencia ficción'),(4,'Comedia'),(3,'Drama'),(8,'Música'),(9,'Romance'),(7,'Suspense'),(5,'Terror');
/*!40000 ALTER TABLE `genres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie_external_metrics`
--

DROP TABLE IF EXISTS `movie_external_metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie_external_metrics` (
  `movie_id` int NOT NULL,
  `tmdb_movie_id` int NOT NULL,
  `tmdb_vote_average` decimal(4,2) NOT NULL DEFAULT '0.00',
  `tmdb_vote_count` int NOT NULL DEFAULT '0',
  `tmdb_popularity` decimal(8,2) NOT NULL DEFAULT '0.00',
  `fetched_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`movie_id`),
  CONSTRAINT `fk_mem_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_external_metrics`
--

LOCK TABLES `movie_external_metrics` WRITE;
/*!40000 ALTER TABLE `movie_external_metrics` DISABLE KEYS */;
INSERT INTO `movie_external_metrics` VALUES (1,157336,8.47,39357,67.65,'2026-04-13 08:17:10'),(2,603,8.24,27640,27.56,'2026-04-13 08:17:10'),(3,496243,8.50,20402,32.54,'2026-04-13 08:17:10'),(4,862,7.97,19724,20.25,'2026-04-13 08:17:10'),(5,694,8.21,18772,13.73,'2026-04-13 08:17:10'),(6,76341,7.60,23926,22.70,'2026-04-13 08:17:10'),(7,381288,7.34,18228,14.12,'2026-04-13 08:18:41'),(10,114150,7.27,6828,5.42,'2026-04-13 08:18:41');
/*!40000 ALTER TABLE `movie_external_metrics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie_genres`
--

DROP TABLE IF EXISTS `movie_genres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie_genres` (
  `movie_id` int NOT NULL,
  `genre_id` int NOT NULL,
  PRIMARY KEY (`movie_id`,`genre_id`),
  KEY `fk_mg_genre` (`genre_id`),
  CONSTRAINT `fk_mg_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`genre_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mg_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_genres`
--

LOCK TABLES `movie_genres` WRITE;
/*!40000 ALTER TABLE `movie_genres` DISABLE KEYS */;
INSERT INTO `movie_genres` VALUES (2,1),(6,1),(1,2),(2,2),(6,2),(1,3),(3,3),(4,3),(5,3),(3,4),(8,4),(10,4),(5,5),(7,5),(4,6),(7,7),(10,8),(10,9);
/*!40000 ALTER TABLE `movie_genres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movies`
--

DROP TABLE IF EXISTS `movies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movies` (
  `movie_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `release_year` year DEFAULT NULL,
  `director` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `popularity` decimal(5,2) NOT NULL DEFAULT '0.00',
  `poster_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`movie_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movies`
--

LOCK TABLES `movies` WRITE;
/*!40000 ALTER TABLE `movies` DISABLE KEYS */;
INSERT INTO `movies` VALUES (1,'Interestelar','Un grupo de exploradores viaja a través de un agujero de gusano en el espacio. Test Test Test',2014,'Christopher Nolan',9.20,NULL),(2,'Matrix','Un hacker descubre la naturaleza simulada de la realidad.',1999,'Lana y Lilly Wachowski',8.90,NULL),(3,'Parasite','Las vidas de dos familias de distintas clases sociales se entrelazan.',2019,'Bong Joon-ho',8.75,NULL),(4,'Toy Story','Los juguetes cobran vida cuando los humanos no miran.',1995,'John Lasseter',8.50,NULL),(5,'El resplandor','Un escritor acepta cuidar un hotel aislado durante el invierno.',1980,'Stanley Kubrick',8.30,NULL),(6,'Mad Max: Furia en la carretera','En un desierto postapocalíptico, Max se une a un grupo en fuga.',2015,'George Miller',8.60,NULL),(7,'Múltiple','A pesar de que Kevin le ha demostrado a su psiquiatra de confianza, la Dra. Fletcher, que posee 23 personalidades diferentes, aún queda una por emerger, decidida a dominar a todas las demás. Obligado a raptar a tres chicas adolescentes encabezadas por la decidida y observadora Casey, Kevin lucha por sobrevivir contra todas sus personalidades y la gente que le rodea, a medida que las paredes de sus compartimentos mentales se derrumban.',2017,'TMDB',14.12,NULL),(8,'Scary Movie','Tras el asesinato de una bella estudiante, un grupo de adolescentes descubren que hay un asesino entre ellas. La heroína, Cindy, y su grupo de desconcertantes amigos, serán aterrorizados por un singular psicópata enmascarado que pretende vengarse de ellos por haberlo atropellado el pasado Halloween.',2000,'TMDB',16.35,'https://image.tmdb.org/t/p/w342/fHWR3YplPQWciYzxEity2kyDoDn.jpg'),(10,'Dando la nota','Beca es de esas chicas que prefiere escuchar lo que sale de sus auriculares a lo que puede decirle alguien. Al llegar a la universidad no tiene cabida en ningún grupo, pero la obligan a unirse a uno que jamás habría escogido, formado por chicas malas, chicas buenas y chicas raras que solo comparten una cosa: lo bien que suenan cuando están juntas. Beca quiere que el grupo de canto acústico salga del tradicional mundo musical y llegue a alcanzar armonías nuevas y sorprendentes. Las chicas deciden escalar puestos en el despiadado mundo del canto a cappella universitario. Su intento puede acabar siendo lo mejor que han hecho. O, quizá, su mayor locura. Probablemente sea una mezcla de ambas cosas.',2012,'TMDB',5.42,'https://image.tmdb.org/t/p/w342/nuopXlrClCnw85IDZoSFbW1MjbV.jpg');
/*!40000 ALTER TABLE `movies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `preferences`
--

DROP TABLE IF EXISTS `preferences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `preferences` (
  `preference_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `genre_id` int NOT NULL,
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_pref_user_genre` (`user_id`,`genre_id`),
  KEY `fk_pref_genre` (`genre_id`),
  CONSTRAINT `fk_pref_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`genre_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `preferences`
--

LOCK TABLES `preferences` WRITE;
/*!40000 ALTER TABLE `preferences` DISABLE KEYS */;
INSERT INTO `preferences` VALUES (4,1,1),(1,1,2),(2,1,3),(5,1,4),(6,1,5),(7,1,6),(8,1,7);
/*!40000 ALTER TABLE `preferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ratings`
--

DROP TABLE IF EXISTS `ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ratings` (
  `rating_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `movie_id` int NOT NULL,
  `rating` int NOT NULL,
  `review` text COLLATE utf8mb4_unicode_ci,
  `rated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `uk_rat_user_movie` (`user_id`,`movie_id`),
  KEY `fk_rat_movie` (`movie_id`),
  CONSTRAINT `fk_rat_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rat_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_rating_range` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ratings`
--

LOCK TABLES `ratings` WRITE;
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'demo','demo@moviematch.local','demo123','2026-04-13 03:35:53'),(2,'Demo_Real','demo_real@moviematch.com','demo123','2026-04-13 06:03:42');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `watchlist`
--

DROP TABLE IF EXISTS `watchlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `watchlist` (
  `watchlist_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `movie_id` int NOT NULL,
  `status` enum('FAVORITE','WATCH_LATER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`watchlist_id`),
  UNIQUE KEY `uk_wl_user_movie_status` (`user_id`,`movie_id`,`status`),
  KEY `fk_wl_movie` (`movie_id`),
  CONSTRAINT `fk_wl_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `watchlist`
--

LOCK TABLES `watchlist` WRITE;
/*!40000 ALTER TABLE `watchlist` DISABLE KEYS */;
INSERT INTO `watchlist` VALUES (2,1,8,'FAVORITE','2026-04-13 08:03:46');
/*!40000 ALTER TABLE `watchlist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'moviematch'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-13  3:23:57
