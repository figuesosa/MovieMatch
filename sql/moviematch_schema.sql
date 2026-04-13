-- Script de creación de la base de datos MovieMatch (MySQL)
-- Motor de recomendación de películas - aplicación Java MovieMatch

CREATE DATABASE IF NOT EXISTS moviematch
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE moviematch;

-- ---------------------------------------------------------------------------
-- 1. Usuarios
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 2. Géneros
-- ---------------------------------------------------------------------------
CREATE TABLE genres (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 3. Películas
-- ---------------------------------------------------------------------------
CREATE TABLE movies (
    movie_id      INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    release_year  YEAR,
    director      VARCHAR(100),
    popularity    DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    poster_url    VARCHAR(255) NULL
) ENGINE=InnoDB;

-- Si ya tenías la tabla creada en una versión anterior sin poster_url, ejecuta:
-- ALTER TABLE movies ADD COLUMN poster_url VARCHAR(255) NULL;

-- Métricas externas cacheadas (TMDB) para recomendaciones híbridas
CREATE TABLE movie_external_metrics (
    movie_id            INT PRIMARY KEY,
    tmdb_movie_id       INT NOT NULL,
    tmdb_vote_average   DECIMAL(4, 2) NOT NULL DEFAULT 0.00,
    tmdb_vote_count     INT NOT NULL DEFAULT 0,
    tmdb_popularity     DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    fetched_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mem_movie FOREIGN KEY (movie_id)
        REFERENCES movies (movie_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Si la base ya existe, crear también:
-- CREATE TABLE movie_external_metrics (
--   movie_id INT PRIMARY KEY,
--   tmdb_movie_id INT NOT NULL,
--   tmdb_vote_average DECIMAL(4,2) NOT NULL DEFAULT 0.00,
--   tmdb_vote_count INT NOT NULL DEFAULT 0,
--   tmdb_popularity DECIMAL(8,2) NOT NULL DEFAULT 0.00,
--   fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   CONSTRAINT fk_mem_movie FOREIGN KEY (movie_id) REFERENCES movies (movie_id) ON DELETE CASCADE
-- );

-- ---------------------------------------------------------------------------
-- 4. Relación películas ↔ géneros (N:M)
-- ---------------------------------------------------------------------------
CREATE TABLE movie_genres (
    movie_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_mg_movie FOREIGN KEY (movie_id)
        REFERENCES movies (movie_id) ON DELETE CASCADE,
    CONSTRAINT fk_mg_genre FOREIGN KEY (genre_id)
        REFERENCES genres (genre_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 5. Calificaciones
-- ---------------------------------------------------------------------------
CREATE TABLE ratings (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id   INT NOT NULL,
    movie_id  INT NOT NULL,
    rating    INT NOT NULL,
    review    TEXT,
    rated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rat_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_rat_movie FOREIGN KEY (movie_id)
        REFERENCES movies (movie_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_range CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uk_rat_user_movie UNIQUE (user_id, movie_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 6. Preferencias de género por usuario
-- ---------------------------------------------------------------------------
CREATE TABLE preferences (
    preference_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    genre_id      INT NOT NULL,
    CONSTRAINT fk_pref_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_pref_genre FOREIGN KEY (genre_id)
        REFERENCES genres (genre_id) ON DELETE CASCADE,
    CONSTRAINT uk_pref_user_genre UNIQUE (user_id, genre_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 7. Lista personal (favoritos / ver más tarde)
-- ---------------------------------------------------------------------------
CREATE TABLE watchlist (
    watchlist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    movie_id     INT NOT NULL,
    status       ENUM('FAVORITE', 'WATCH_LATER') NOT NULL,
    added_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wl_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_movie FOREIGN KEY (movie_id)
        REFERENCES movies (movie_id) ON DELETE CASCADE,
    CONSTRAINT uk_wl_user_movie_status UNIQUE (user_id, movie_id, status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Datos de ejemplo: géneros
-- ---------------------------------------------------------------------------
INSERT INTO genres (name) VALUES
    ('Acción'),
    ('Ciencia ficción'),
    ('Drama'),
    ('Comedia'),
    ('Terror'),
    ('Animación');

-- ---------------------------------------------------------------------------
-- Datos de ejemplo: películas
-- ---------------------------------------------------------------------------
INSERT INTO movies (title, description, release_year, director, popularity) VALUES
    ('Interestelar', 'Un grupo de exploradores viaja a través de un agujero de gusano en el espacio.', 2014, 'Christopher Nolan', 9.20),
    ('Matrix', 'Un hacker descubre la naturaleza simulada de la realidad.', 1999, 'Lana y Lilly Wachowski', 8.90),
    ('Parasite', 'Las vidas de dos familias de distintas clases sociales se entrelazan.', 2019, 'Bong Joon-ho', 8.75),
    ('Toy Story', 'Los juguetes cobran vida cuando los humanos no miran.', 1995, 'John Lasseter', 8.50),
    ('El resplandor', 'Un escritor acepta cuidar un hotel aislado durante el invierno.', 1980, 'Stanley Kubrick', 8.30),
    ('Mad Max: Furia en la carretera', 'En un desierto postapocalíptico, Max se une a un grupo en fuga.', 2015, 'George Miller', 8.60);

-- Asociar películas con géneros (movie_genres)
INSERT INTO movie_genres (movie_id, genre_id) VALUES
    (1, 2), (1, 3),
    (2, 1), (2, 2),
    (3, 3), (3, 4),
    (4, 3), (4, 6),
    (5, 3), (5, 5),
    (6, 1), (6, 2);

-- ---------------------------------------------------------------------------
-- Usuario de prueba (ventana Swing): usuario demo / contraseña demo123
-- ---------------------------------------------------------------------------
INSERT INTO users (username, email, password) VALUES
    ('demo', 'demo@moviematch.local', 'demo123');

INSERT INTO preferences (user_id, genre_id)
SELECT u.user_id, g.genre_id
FROM users u
JOIN genres g ON g.name IN ('Ciencia ficción', 'Drama')
WHERE u.username = 'demo';
