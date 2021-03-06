# COP4520-Project Team: Geela Margo Ramos
<pre>
The following README.md file is provided for updates regarding the project topic and description and specific details on progress for this project.
</pre>

## Project Topic: Implementation of Parallel Computation of Multiple Subgame Perfect Equilibrium in N-Player Role-Playing Games
When we consider its definition, a role-playing game (RPG) is a game in which participants assume the roles of characters in a fictional setting; its definition, however, can be extended by analysis of randomness or unpredictability in the game. In this project, we wil/ conduct the following steps:

1. Delve into how we can define a role-playing game by stochastic randomness, referencing that a stochastic game is denoted as a game where "the play proceeds by steps from position to position, according to transition probabilities controlled jointly by two players." [^1]
2. Observe the processes required to calculate equilibrium given the dynamic nature of the game, where a set of choices conducted by each player not only affects the amount and type of payoff another player receives but affects the environment they are interacting within.
3. Determine how parallel structures and algorithms can be implemented in a virtual simulation of *Dungeons and Dragons* (DnD) to support calculation of Nash Equilibrium throughout each state of the game and assist action-choice selection by the program to act similarly to the role of a *Dungeon Master* (DM).
4. Analyze if implementation of parallel structures and algorithms affects performative randomness conducted by the participant interacting with the program, and how efficiency achieved by the program is affected when more than 2 players are introduced in the simulation.


[^1]: LS Shapley, Stochastic games. Proc Natl Acad Sci USA 39, 1095–1100 (1953)


## Installation and Execution of Experimental Evaluation:
**GameTree must be downloaded in order to run this program through the terminal:**

1. Open a terminal and navigate to the location of ```GameTree```
2. Compile the program with the following command: ```javac GameTree.java```
3. Run the compiled program with the following command: ```java GameTree```

## Installation and Execution of Actual Game:
**NashHunt must be downloaded in order to run this program through the terminal:**

1. Open a terminal and navigate to the location of ```NashHunt```
2. Compile the program with the following command: ```javac NashHunt.java```
3. Run the compiled program with the following command: ```java NashHunt```

**Instructions for Game Play:**
You will be spawned onto a random square within an 8x8 map. Your goal is to traverse across the map
and find the target location. While traversing across the map, an enemy will be seeking you out. To win
this game, you must either find the target location or defeat the enemy in combat. Please note that
when prompted for input, follow the instructions on what to type. You will always have two choices for
input.

## Presentation Slides:
For the presentation slides, located in the Midterm Report Folder, slides 1 - 20 were used for the short
presentation during class. The remainder slides were used for the longer version of the presentation, posted
in the discussion board for video presentations.
