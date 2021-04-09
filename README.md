# Connect5Game
Two applications (Client &amp; Server) for a Connect 5 Game

## Running the game
### Method 1 (Preferable)
1. Open the two projects in Intellj
2. Pull in the maven dependencies specified in the pom.xml
3. Run both applications (2 separate instances of the client app are required for two the players)
4. Follow the instructions in the terminal from the client apps

### Method 2
1. Open command prompt, 2 separate windows for the client and 1 for the server
2. Navigate to the root folders of each application
3. run 'mvn clean package' to run unit tests and build the jar files
4. navigate to the target folder where the jar is located
5. run 'java -jar (jarName)' to start the app, 1 instance for the server, 2 instances for the client

## Assumptions Made
- Only two players can join the server at a time, and so there can only be 1 active game.
- HTTP based interaction, so the no state between client and server
- Two separate clients are required (same app run in separate instances)
- Two colors allowed for the player discs, Blue and Red. If the first player to joing picks a color and the second player picks the same color, he will be automatically assigned the other color
- Player name is unique, not allowed two players with the same name
- Player name cannot be empty, disc color must be either Blue or Red
- First player to move is selected at random
- If a move is requested on a column that is full, the player is asked to make another move
- A winning state can be in 4 directions, horizontal, vertical and diagonal (left and right)
- If a player disconnects after the game has started, the game ends and the other player wins
- Once a game ends, the players are removed from the game and two new players can join and start a new game
- If a player is not in the game he wont be authorized to make moves, get game status or disconnect
- Since the server only accepts HTTP requests and doesn't send them to the clients, the clients continuosly send a game status requests to determine if it's their move or if the game has ended

## Game execution
1. Client asks the player to enter in a name and choose one of the two disc colors
2. If the server is running, the player joins the game and receives a confirmation message
3. If another player has already joined, the game begins. One of the two players is picked at random and asked to make his move
4. Player to move is presented with the gameboard and asked to select the column to insert his disc in between 1 and 9. Select 0 to disconnect from the game
5. If the disc has been successfully inserted to the board, the other player will be presented with the updated board and asked to make his move
6. This back and forth continues until one of the players wins or leaves
7. If a player requests to insert a disc into a column that is full, he will be asked to make another move
8. If a player makes a winnig move or his opponent disconnects he will receive a message stating he won
9. If the other player makes a winning move, the losing player will receive a message stating he lost

## Server Test Coverage
Only testable code exists in the GameController and GameService.

GameController: Line Coverage = 100%
GameService: Line Coverage = 92%
