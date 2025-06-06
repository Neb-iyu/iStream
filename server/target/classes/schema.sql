-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

-- Admin table
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id)
);
-- Albums table
CREATE TABLE IF NOT EXISTS albums (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist_id INT REFERENCES artists(id),
    cover_art_path VARCHAR(255)
);
-- Artists table
CREATE TABLE IF NOT EXISTS artists (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    album_id INT REFERENCES albums(id),
    song_id INT REFERENCES songs(id),
);
-- Songs table
CREATE TABLE IF NOT EXISTS songs (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    album VARCHAR(255),
    duration INT,
    genre VARCHAR(255),
    file_path VARCHAR(255),
    cover_art_path VARCHAR(255),
    year INT
);

-- Playlists table
CREATE TABLE IF NOT EXISTS playlists (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    name VARCHAR(255) NOT NULL
);

-- Playlist items table
CREATE TABLE IF NOT EXISTS playlist_items (
    playlist_id INT REFERENCES playlists(id),
    song_id INT REFERENCES songs(id),
    position INT,
    PRIMARY KEY (playlist_id, song_id)
);

-- Play history table
CREATE TABLE IF NOT EXISTS play_history (
    user_id INT REFERENCES users(id),
    song_id INT REFERENCES songs(id),
    play_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, song_id)
);

-- Likes table
CREATE TABLE IF NOT EXISTS likes (
    user_id INT REFERENCES users(id),
    song_id INT REFERENCES songs(id),
    PRIMARY KEY (user_id, song_id)
);

-- Sessions table
CREATE TABLE sessions (
    token VARCHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_sessions_expires ON sessions(expires_at);
