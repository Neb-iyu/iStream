# iStream Music Streaming Platform

A modern, feature-rich music streaming application built with JavaFX and RMI.

## Features

- 🎵 Music streaming with high-quality audio playback
- 👥 User authentication and profile management
- 🎨 Modern and responsive UI with smooth animations
- 📱 Cross-platform compatibility
- 🎧 Real-time music controls (play, pause, skip, volume)
- 📋 Queue management and playlist support
- 🔍 Advanced search functionality
- 👤 Artist and album browsing
- ❤️ Like/favorite songs
- 📊 User listening history
- 👑 Admin features for content management

## Technical Stack

- **Frontend**: JavaFX 21
- **Backend**: Java RMI (Remote Method Invocation)
- **Architecture**: Client-Server Model
- **Styling**: CSS with modern design patterns
- **Build Tool**: Maven

## Project Structure

```
music_stream/
├── client/                 # Client application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java source files
│   │   │   └── resources/ # FXML, CSS, and images
│   │   └── test/          # Test files
│   └── pom.xml            # Client dependencies
├── server/                 # Server application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Server implementation
|   |   |   └──resources   # Songs and images
│   │   └── test/          # Test files
│   └── pom.xml            # Server dependencies
└── shared/                 # Shared code between client and server
    ├── src/
    │   └── main/
    │       └── java/      # Shared models and interfaces
    └── pom.xml            # Shared dependencies
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- IDE (recommended: IntelliJ IDEA or Eclipse)
- PostgreSQL

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Neb-iyu/iStream.git
   cd music_stream
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```
3. Create a role and database in PostgreSQL using the credentials from /server/src/main/java/database/DatabaseManager.java
4. Start the server:
   ```bash
   cd server
   mvn exec:java
   ```

5. Start the client:
   ```bash
   cd client
   mvn exec:java
   ```

## Usage

1. Launch the application
2. Create an account or log in
3. Browse music by:
   - Home page recommendations
   - Artist pages
   - Album collections
   - Search functionality
4. Play music using the player controls
5. Create and manage playlists
6. Like your favorite songs

## Features in Detail

### Music Player
- Play/pause control
- Skip forward/backward
- Volume control
- Progress bar with seeking
- Queue management
- Like/unlike songs

### User Interface
- Responsive design
- Dark theme
- Smooth animations
- Intuitive navigation
- Search functionality
- Content browsing

### User Features
- Account management
- Playlist creation
- Listening history
- Favorites management
- Profile customization

### Admin Features
- Content upload
- User management
- Analytics dashboard
- Content moderation


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
