# The Board Bird

The Board Bird is a small Java desktop game built with **Java Swing**. It was
created for a university project whose requirement was to use Swing as the
main UI technology. The game idea is inspired by the Nokia game **Forbidden
Treasure**, while the implementation, artwork integration, and gameplay code
are original to this project.

## Video




https://github.com/user-attachments/assets/24cc60ad-1617-4782-a85d-465c86d087ff







## Gameplay

Control the bird while it moves through a vertically scrolling board:

- Collect corn to earn money.
- Reach the money target to advance to the next level.
- Collect hearts to restore lost lives.
- Avoid fire, which removes a life and shakes the camera.
- Break green blocks when they are in front of the bird.
- A life is also lost automatically every 30 seconds.
- The current best level and player name are stored in `scores.json`.

## Controls

| Key / Button | Function |
| --- | --- |
| `Left Arrow` | Move the bird one cell to the left. |
| `Right Arrow` | Move the bird one cell to the right. |
| `Down Arrow` | Break the block below the bird and fall through it. |
| `Space` | Break the two blocks in front of the bird. |
| `Play again` | Save a new record when applicable and restart the current level after losing. |
| `Exit` | Close the game-over dialog. |

## Project Structure

The code keeps the game in a small number of focused Swing-oriented
components:

```text
src/game_s1/
├── Main.java             # Application entry point and Swing startup
├── Design.java           # Main game window, board construction, and game flow
├── BoardButton.java      # A typed cell on the game board
├── ScoreRecord.java      # Immutable score data
└── ScoreRepository.java  # Reading and writing scores.json
```

`Design` remains the coordinator for the game loop and Swing screens, while
board cells and score persistence are isolated into their own files. This
keeps the project simple enough for a university assignment without adding
frameworks or unnecessary abstraction layers.

## Requirements

- Java 17 or newer
- Java Swing (included with the JDK)
- Eclipse, or any Java IDE/build tool configured with `src` as the source
  directory

## Running

Run `game_s1.Main` from the project root so the image assets and
`scores.json` are found using their relative paths.

In Eclipse, import the project and run **`Main.java`** as a Java application.
