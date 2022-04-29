
/**
 * Full Game employing aspects of GameTree and Minimax Algorithm for the Enemy's Actions
 *
 * Geela Margo Ramos
 * Due Date: April 28, 2022
 */

import java.lang.Math;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;

class GameNode {
    int p1Value, p2Value;
    String agent, action;
    double probability, p1Trade, p2Trade;
    
    GameNode left, right;
    
    GameNode(String agent, String action, double prob, int p1, int p2) {
        this.agent = agent; this.action = action; //sets the associated agent and action with trade-off
        this.p1Value = p1; this.p2Value = p2; //sets the trade-off value
        this.probability = prob; //sets the action probability (of being chosen)
        left = right = null; //sets the right and left leaves for the node
    }
}

class Agent {
    int health, initiative;
    int x, y; //coordinates on the map
    String name;
    
    Agent(String name, int health) {
        this.health = health;
        this.name = name;
    }
    
    //setter methods for coordinates
    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void move(String direction) {
        if(direction.equals("a")){this.x--;}
        else if(direction.equals("d")){this.x++;}
        else if(direction.equals("w")){this.y--;}
        else {this.y++;}
    }
    
    //getter method for coordinates
    public int getX() {return this.x;}
    public int getY() {return this.y;}
    
    //setter methods for encounters
    public void setInitiative(int i) {this.initiative = i;}
    public void healthDamage(int strike) {this.health -= strike;}
    public void healthGain(int loot) {this.health += loot;}
    
    //getter methods for encounters
    public int getHealth() {return this.health;}
    public int getInit() {return this.initiative;}
    
}

/* For game play : simulation of fighting */
class Encounter {
    NashHunt mainThread; Agent opponent; 
    
    //Fight Strings
    public static final String injured = "You were struck. Your health has decreased: "; //opponent strikes player
    public static final String inflict = "You inflicted damage. Their health has decreased: "; //player strikes opponent
    public static final String flee = " decided to flee, and will be currently vulnerable to any strikes."; //on opportunity attacks
    public static final String bothFlee = "Both of you have decided to flee.";
    
    Encounter(NashHunt mainThread, Agent opponent){
        this.mainThread = mainThread;
        this.opponent = opponent;
    }
    
    public void fight() {
        Scanner f = new Scanner(System.in);
        Random chance = new Random();
        String playerAction, oppAction;
        
        //player chooses action
        System.out.println("What would you like to do? (Type STRIKE or FLEE)");
        playerAction = f.nextLine();
                
        //opponent "chooses" action
        int oppChanceFight = chance.nextInt(10);
        if(mainThread.enemy.health < 7 && opponent.name.equals(mainThread.enemyName)) {
            oppAction = "FLEE"; //will avoid fighting if health is critically low
        } else if(oppChanceFight <= 5){
            oppAction = "FLEE"; //if health is not critically low, OR if the opponent is just a prowler, there will be a 50% probability for either action
        } else {
            oppAction = "STRIKE";
        }
        
        //if both player and opponent want to flee
        if(playerAction.equals("FLEE") && oppAction.equals("FLEE")) {
                mainThread.fightContinue = false;
                System.out.println(bothFlee);
                
                return;
        }
        
        //simulate interaction
        if(opponent.name == "PROWLER"){
            if(mainThread.player.initiative > mainThread.prowler.initiative) {
                //player goes first
                if(playerAction.equals("STRIKE") && oppAction.equals("STRIKE")) {
                    System.out.println("Both of you go to STRIKE one another.");
                    mainThread.prowler.health -= chance.nextInt(6) + 1; //player attacks first
                    System.out.println(inflict + mainThread.prowler.getHealth());
                    if(mainThread.prowler.health <= 0) {return;}         
                
                    mainThread.player.health -= chance.nextInt(6) + 1; //opponent attacks
                    System.out.println(injured + mainThread.player.getHealth());
                } else if(playerAction.equals("STRIKE") && oppAction.equals("FLEE")) {
                    System.out.println(mainThread.prowler.name + flee);
                    mainThread.prowler.health -= chance.nextInt(4) + 1;//opportunity attack on oppponent
                    System.out.println(inflict + mainThread.prowler.getHealth());
                    if(mainThread.prowler.health <= 0) {
                        System.out.println("You defeated the PROWLER currently after you!");
                        mainThread.fightContinue = false;
                        return;
                    }   
                } else {
                    System.out.println(mainThread.player.name + flee);
                    mainThread.player.health -= chance.nextInt(4) + 1;//opportunity attack on player
                    System.out.println(injured + mainThread.player.getHealth());
                    mainThread.fightContinue = false;
                }
            } else {
                //prowler goes first
                if(playerAction.equals("STRIKE") && oppAction.equals("STRIKE")) {
                    System.out.println("Both of you go to STRIKE one another.");
                    mainThread.player.health -= chance.nextInt(6) + 1; //opponent attacks first
                    System.out.println(injured + mainThread.player.getHealth());
                    if(mainThread.player.health <= 0) {return;}    //player loses game     
                
                    mainThread.prowler.health -= chance.nextInt(6) + 1; //player attacks
                    System.out.println(inflict + mainThread.prowler.getHealth());
                } else if(playerAction.equals("STRIKE") && oppAction.equals("FLEE")) {
                    System.out.println(mainThread.prowler.name + flee);
                    mainThread.prowler.health -= chance.nextInt(4) + 1;//opportunity attack on oppponent
                    System.out.println(inflict + mainThread.prowler.getHealth());
                    if(mainThread.prowler.health <= 0) {
                        System.out.println("You defeated the PROWLER currently after you!");
                        mainThread.fightContinue = false;
                        return;
                    }   
                } else {
                    System.out.println(mainThread.player.name + flee);
                    mainThread.player.health -= chance.nextInt(4) + 1;//opportunity attack on player
                    System.out.println(injured + mainThread.prowler.getHealth());
                    mainThread.fightContinue = false;
                } 
            }
            
            return;
        }
        
        //if the opponent is the enemy
        if(mainThread.player.initiative > mainThread.enemy.initiative) {
            //player goes first
            if(playerAction.equals("STRIKE") && oppAction.equals("STRIKE")) {
                System.out.println("Both of you go to STRIKE one another.");
                mainThread.enemy.healthDamage(chance.nextInt(6) + 1); //player attacks first
                System.out.println(inflict + mainThread.enemy.getHealth());
                if(mainThread.enemy.health <= 0) {
                    mainThread.enemyDefeated = true; 
                    return;
                }         
                
                mainThread.player.healthDamage(chance.nextInt(6) + 1); //opponent attacks
                System.out.println(injured + mainThread.player.getHealth());
            } else if(playerAction.equals("STRIKE") && oppAction.equals("FLEE")) {
                System.out.println(mainThread.enemy.name + flee);
                mainThread.enemy.healthDamage(chance.nextInt(4) + 1);//opportunity attack on oppponent
                System.out.println(inflict + mainThread.enemy.getHealth());
                if(mainThread.enemy.health <= 0) {mainThread.enemyDefeated = true;}   
                
                mainThread.fightContinue = false;
                return;
            } else {
                System.out.println(mainThread.player.name + flee);
                mainThread.player.healthDamage(chance.nextInt(4) + 1);//opportunity attack on player
                System.out.println(injured + mainThread.player.getHealth());
                
                mainThread.fightContinue = false;
                return;
            }
        } else {
            //enemy goes first
            if(playerAction.equals("STRIKE") && oppAction.equals("STRIKE")) {
                System.out.println("Both of you go to STRIKE one another.");
                mainThread.player.healthDamage(chance.nextInt(6) + 1); //opponent attacks first
                System.out.println(injured + mainThread.player.getHealth());
                if(mainThread.player.health <= 0) {return;}    //player loses game     
                
                mainThread.enemy.healthDamage(chance.nextInt(6) + 1); //player attacks
                System.out.println(inflict + mainThread.enemy.getHealth());
            } else if(playerAction.equals("STRIKE") && oppAction.equals("FLEE")) {
                System.out.println(mainThread.enemy.name + flee);
                mainThread.enemy.healthDamage(chance.nextInt(4) + 1);//opportunity attack on oppponent
                System.out.println(inflict + mainThread.enemy.getHealth());
                if(mainThread.enemy.health <= 0) {mainThread.enemyDefeated = true;}   
                
                mainThread.fightContinue = false;
                return;
            } else {
                System.out.println(mainThread.player.name + flee);
                mainThread.player.healthDamage(chance.nextInt(4) + 1);//opportunity attack on player
                System.out.println(injured + mainThread.player.getHealth());
                
                mainThread.fightContinue = false;
                return;
            }   
        }
        
        return;
    }
    
    /* Called method for chance encounters */
    public boolean encounterFight() {
        /* Set up opponent and player for encounter */
        mainThread.fightContinue = true;
        Random randInitiative = new Random();
        mainThread.player.setInitiative(randInitiative.nextInt(20) + 1); //roll initiative for player
        System.out.println("Initiative for Player: " + mainThread.player.getInit());
        
        if(opponent.name == "PROWLER") {
            mainThread.prowler.setInitiative(randInitiative.nextInt(20) + 1); //roll initiative for prowler
            System.out.println("Initiative for Prowler: " + mainThread.prowler.getInit());
            
            fight(); //prowler is an instantaneous chance encounter, meant to "stun" player
            
            if(mainThread.player.health <= 0) {return false;} //if fight ends, will return false if player "dies"
        } else {
            mainThread.enemy.setInitiative(randInitiative.nextInt(20) + 1); //roll initiative for enemy
            System.out.println("Initiative for Enemy: " + mainThread.enemy.getInit());
            
            /* FIGHT WILL CONTINUE UNTIL:
             * Both agents want to flee
             * One flees
             * One "dies"
             */
            System.out.println("You hear a shrieking in the distance. The fight begins.");
            while((mainThread.player.health > 0 && mainThread.enemy.health > 0)) {
                if(!mainThread.fightContinue){break;}
                fight();
            }
            if(mainThread.player.health <= 0) {return false;} //when fight ends, will return false if player "dies"
        }
        
        return true; //returns true when fight ends and at least the player "survives"
    }
}

/* Methods involving traversal through tree (representing each game round) */
class GameRound extends Thread implements Runnable {
    private GameNode n; boolean marked;
    int bestValue;
    NashHunt mainThread;
    
    public GameRound(GameNode assignedNode, NashHunt main) {
        this.n = assignedNode;
        this.mainThread = main;
    }
    
    public int minimaxRecursive() {
        int value = 0;
        
        return value;
    }
    
    public int minimaxParallel() {
        int value = 0;
        
        return value;
    }
    
    @Override
    public void run() {
        try {
            bestValue = minimaxRecursive();
            //bestValue = minimaxParallel();
        } catch(Exception e) {
            System.out.println("Exception Message: " + e);
        }
    }
}

public class NashHunt {
    /* Variables for game calculations */    
    GameNode root;
    /* mutex for entire game tree */
    private List<Thread> threads; private List<GameRound> rounds;
    
    /* Variables for game creation */
    //Player
    public static final String p = "PLAYER";
    // Enemy Name
    public static final String e = "ENEMY";
    public static final String enemyName = "Fade"; 
    // Action Strings
    public static final String fast = "You move at a fast pace.", slow = "You move at a slow pace.";
    public static final String wait = " believes fear will find you first. They wait.", search = " calls for Nightfall. They will dash towards an area closer to you.";
    public static final String wander = "You wander towards an eerily empty area. A PROWLER was sent after you.", hide = "You chose to hide. While you can't be found by PROWLERS, you know to tread carefully.";
    public static final String rest = "You decided to rest. Nightfall deafens your senses.", loot = "You try to find armor. Nightfall deafens your senses.";
    public static final String north = "You are moving NORTH", south = "You are moving SOUTH", east = "You are moving EAST", west = "You are moving WEST"; //directional movement
    // Round and Encounter Strings
    public static final String encounter = "The PROWLER has found you. Be prepared to fight!";
    public static final String armor = "You found some armor. Your health has been boosted: ";
    
    /* Variables for game play */
    public static boolean winner, instituteFound, enemyDefeated, fightContinue, playContinue;
    public static Random r; //used to randomize moves and actions throughout game
    
    public static Agent player, enemy, prowler;  //agents in game
    public static String playerName; //name of player
    public static final int tX = 4, tY = 4; //target coordinates on map
    public static double distPE, distTP; //tracks distances between player and enemy, and between target and player respectively
    
    
    /* Function to Insert Nodes, assuming tradeoff values are given in level order */
    public GameNode gameRoundFormation(String[] agent, String[] action, double[] probability, int[] p1TradeOff, int p2TradeOff[], GameNode root, int i) {
        //base case for recursion
        if(i < p1TradeOff.length) {
            GameNode temp = new GameNode(agent[i], action[i], probability[i], p1TradeOff[i], p2TradeOff[i]);
            root = temp;
            
            root.left = gameRoundFormation(agent, action, probability, p1TradeOff, p2TradeOff, root.left, 2 * i + 1); //insert left child
            root.right =  gameRoundFormation(agent, action, probability, p1TradeOff, p2TradeOff, root.left, 2 * i + 2); //insert right child
        }
        return root;
    }
    
    /* Function to Calculate Distance */
    public static double calcDist(int x1, int y1, int x2, int y2) {
        //System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);
        int xDiff = x2 - x1, yDiff = y2 - y1;
        int xDist = xDiff * xDiff, yDist = yDiff * yDiff;
        double dist = Math.sqrt((double)xDist + (double)yDist);
        
        return dist;
    }
    
    /* Function to Check if Player is not at Edge of Map */
    public static boolean canMove(String direction, Agent p) {
        boolean canMove = false;
        int currX = p.getX(), currY = p.getY();
        
        if(direction.equals("w")){
            if(--currY >= 0){canMove = true;}
        } else if(direction.equals("a")){
            if(--currX >= 0){canMove = true;}
        } else if(direction.equals("s")){
            if(++currY < 8){canMove = true;}          
        } else if(direction.equals("d")){
            if(++currX < 8){canMove = true;}   
        } else {System.out.println("Please enter a valid direction, player.");}
        
        if(canMove == false && (!p.name.equals(enemy.name))){System.out.println("A barrier prevents you from moving in this direction. Choose again.");}
        return canMove;
    }
    
    /* Function to Check if Player and Enemy are on the Same Square */
    public static boolean startEnemyEncounter(Agent player, Agent enemy) {
        boolean start = false;
        
        int pX = player.getX(), pY = player.getY(), eX = enemy.getX(), eY = enemy.getY();
        if(pX == eX && pY == eY) {start = true;}

        return start;
    }
    
    /* Game Initializer */
    public static void main(String[] args) {
        NashHunt game = new NashHunt(); 
        game.r = new Random();
        
        /* Game Initialization */
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome to the Hunt. The Fates request for a name. What will your name be, player?");
        game.playerName = input.nextLine();
        game.player = new Agent(playerName, 20);
        
        //Set all variables for game play
        playContinue = true; fightContinue = true;
        winner = instituteFound = enemyDefeated = false;
        game.enemy = new Agent(enemyName, 20);
        
        //Set all variables for in-game calculations
        String[] agent = {p, e, e, p, p, p, p};
        String[] action = {fast, wait, search, wander, hide, rest, loot}; 
        double[] probability = {0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25}; //set probabilities, with each action having an initial 50% chance of being chosen
        int[] p1TradeOff = {0, 0, 0, 3, 2, 2, 3};
        int[] p2TradeOff = {0, 0, 0, 1, 1, 2, 3};
        game.root = game.gameRoundFormation(agent, action, probability, p1TradeOff, p2TradeOff, game.root, 0);
        
        //Map creation
        int pX = game.r.nextInt(8), pY = game.r.nextInt(8);
        int eX = game.r.nextInt(8), eY = game.r.nextInt(8);
        
        while(pX == 4 && pY == 4) {pX = game.r.nextInt(8); pY = game.r.nextInt(8);} //spawn player coordinates
        game.player.setCoordinates(pX, pY);
        game.distTP = calcDist(pX, pY, tX, tY);
        System.out.println("You have spawned " + (int)game.distTP + " squares away from the INSTITUTE.");
        
        while((eX == 4 && eY == 4) && (eX == pX && eY == pY)) {eX = game.r.nextInt(8); eY = game.r.nextInt(8);} //spawn enemy coordinates
        game.enemy.setCoordinates(eX, eY);
        game.distPE = calcDist(pX, pY, eX, eY);
        System.out.println("An ENEMY has spawned " + (int)game.distPE + " meters away from you.");
                
        /* Game Play */
        int roundTrack = 1, eMov; 
        String p1, p2, e1;
        String pDir,eDir; 
        boolean playerMP = false, enemyMP = false; //MP = move possible
        
        //Narrative and Instructions for Player
        System.out.println();
        System.out.println("Instructions: ");
        System.out.println("When asked for the pace you would like to move at, you may choose by typing either SLOW or FAST.");
        System.out.println("Moving SLOW will allow you to move one space in any direction; moving FAST will allow you to move two spaces in any direction.");
        System.out.println("When asked for the direction you would like to move in, type w for NORTH, a for WEST, s for SOUTH, or d for EAST.");
        System.out.println("\n");
        System.out.println("You have entered a clearing that leads towards a barren desert. Traverse across the desert to find the INSTITUTE.");
        System.out.println("Your ENEMY is Fade - a shadow who seeks out those who bask in the light. Find the INSTITUTE before they find you, lest you let fear overcome you.");
        
        //Simulation
        while(playContinue) {
            System.out.println();
            System.out.println("ROUND " + roundTrack + ": ");
            
            System.out.println("What pace would you like to move at? ");
            p1 = input.nextLine();
            if(p1.equals("SLOW")){
                System.out.println(slow + " Type the direction you would like to move in: ");
                pDir = input.nextLine(); playerMP = canMove(pDir, game.player);
                
                /* check if moving in said direction is possible */
                while(!playerMP) {
                    pDir = input.nextLine();
                    playerMP = canMove(pDir, game.player);
                }

                /* player moves in direction */
                game.player.move(pDir); //move player one square in their chosen direction
                if(pDir.equals("w")){System.out.println(north);}
                else if(pDir.equals("a")){System.out.println(west);}
                else if(pDir.equals("s")){System.out.println(south);}
                else{System.out.println(east);}
                //System.out.println("Your coordinates are: " + game.player.getX() + " " + game.player.getY());
                
                /* check if player lands in the same square */
                if(startEnemyEncounter(game.player, game.enemy)){
                    Encounter enemyEncounter = new Encounter(game, game.enemy);
                    enemyEncounter.encounterFight();
                }
                
                /* check if player has found Institute */
                game.distTP = calcDist(game.player.getX(), game.player.getY(), tX, tY);
                if(game.distTP == 0) {instituteFound = true; break;}
                
                /* spawn enemy movement */
                eMov = game.r.nextInt(4);
                if(eMov == 0){enemyMP = canMove("w", game.enemy);}
                else if(eMov == 1){enemyMP = canMove("a", game.enemy);}
                else if(eMov == 2){enemyMP = canMove("s", game.enemy);}
                else{enemyMP = canMove("d", game.enemy);}
                
                while(!enemyMP) {
                    eMov = game.r.nextInt(4);
                    if(eMov == 0){enemyMP = canMove("w", game.enemy);}
                    else if(eMov == 1){enemyMP = canMove("a", game.enemy);}
                    else if(eMov == 2){enemyMP = canMove("s", game.enemy);}
                    else{enemyMP = canMove("d", game.enemy);}
                }
                
                //enemy moves
                if(eMov == 0){game.enemy.move("w");}
                else if(eMov == 1){game.enemy.move("a");}
                else if(eMov == 2){game.enemy.move("s");}
                else{game.enemy.move("d");}
                System.out.println("FADE has moved.");
                //System.out.println("FADE's coordinates are: " + game.enemy.getX() + " " + game.enemy.getY());
                
                /* check if enemy lands in the same square */
                if(startEnemyEncounter(game.player, game.enemy)){
                    Encounter enemyEncounter = new Encounter(game, game.enemy);
                    enemyEncounter.encounterFight();
                }
                
            } else {
                System.out.println(fast + " Type the direction you would like to first move in: ");
                pDir = input.nextLine(); playerMP = canMove(pDir, game.player);
                /* check if moving in said direction is possible */
                while(!playerMP) {
                    playerMP = canMove(pDir, game.player);
                    pDir = input.nextLine();
                }

                /* player moves in first direction */
                game.player.move(pDir);
                if(pDir.equals("w")){System.out.println(north);}
                else if(pDir.equals("a")){System.out.println(west);}
                else if(pDir.equals("s")){System.out.println(south);}
                else{System.out.println(east);}
                
                /* check if player lands in the same square */
                if(startEnemyEncounter(game.player, game.enemy)){
                    Encounter enemyEncounter = new Encounter(game, game.enemy);
                    enemyEncounter.encounterFight();
                }
                
                /* check if player has found Institute */
                game.distTP = calcDist(game.player.getX(), game.player.getY(), tX, tY);
                if(game.distTP == 0) {instituteFound = true; break;}
                
                playerMP = false; //reset playerMP
                
                System.out.println("Type the direction you would like to next move in: ");
                pDir = input.nextLine(); playerMP = canMove(pDir, game.player);
                /* check if moving in said direction is possible */
                while(!playerMP) {
                    playerMP = canMove(pDir, game.player);
                    pDir = input.nextLine();
                }
                
                /* player moves in second direction */
                game.player.move(pDir);
                if(pDir.equals("w")){System.out.println(north);}
                else if(pDir.equals("a")){System.out.println(west);}
                else if(pDir.equals("s")){System.out.println(south);}
                else{System.out.println(east);}
                
                /* check if player lands in the same square */
                if(startEnemyEncounter(game.player, game.enemy)){
                    Encounter enemyEncounter = new Encounter(game, game.enemy);
                    enemyEncounter.encounterFight();
                }
                
                /* check if player has found Institute */
                game.distTP = calcDist(game.player.getX(), game.player.getY(), tX, tY);
                if(game.distTP == 0) {instituteFound = true; break;}
                
                playerMP = false; //reset playerMP
                int additionalMoves = 0;
                
                /* minimax algorithm for enemy's next move */
                int fakeMinimax = game.r.nextInt(2);    //TESTER METHOD
                if(fakeMinimax == 1) {e1 = "search";}   //TESTER METHOD
                else{e1 = "wait";}                    //TESTER METHOD
                
                if(e1.equals("wait")) {
                    System.out.println(enemyName + wait);
                    
                    System.out.println("What would you like to do? (Type WANDER or HIDE)");
                    p2 = input.nextLine(); 
                    if(p2.equals("WANDER")){
                        System.out.println(wander);
                        
                        //new prowler is spawned if a current prowler does not exist OR if the most recent prowler has died
                        if(game.prowler == null || game.prowler.getHealth() == 0){
                            game.prowler = new Agent("PROWLER", 10);
                        }
                        
                        //start encounter
                        Encounter prowlerEncounter = new Encounter(game, game.prowler);
                        System.out.println(encounter);
                        prowlerEncounter.encounterFight();
                    } else {
                        System.out.println(hide);
                    }
                } else {
                    System.out.println(enemyName + search); 
                    
                    System.out.println("What would you like to do? (Type LOOT or REST)");
                    p2 = input.nextLine();
                    if(p2.equals("LOOT")){
                        System.out.println(armor);
                        additionalMoves += game.r.nextInt(2) + 1; //player is making noise and enemy has advantage to get closer
                        game.player.healthGain(game.r.nextInt(2) + 1); //player finds armor to gain health
                    } else {
                        System.out.println(rest); 
                        additionalMoves++; //a normal dash from enemy
                    }
                }
                
                //enemy moves
                for(int i = 0; i < 1 + additionalMoves; i++) {
                    /* spawn enemy movement */
                    eMov = game.r.nextInt(4);
                    if(eMov == 0){enemyMP = canMove("w", game.enemy);}
                    else if(eMov == 1){enemyMP = canMove("a", game.enemy);}
                    else if(eMov == 2){enemyMP = canMove("s", game.enemy);}
                    else{enemyMP = canMove("d", game.enemy);}
                
                    while(!enemyMP) {
                        eMov = game.r.nextInt(4);
                        if(eMov == 0){enemyMP = canMove("w", game.enemy);}
                        else if(eMov == 1){enemyMP = canMove("a", game.enemy);}
                        else if(eMov == 2){enemyMP = canMove("s", game.enemy);}
                        else{enemyMP = canMove("d", game.enemy);}
                    }
                
                    //enemy moves
                    if(eMov == 0){game.enemy.move("w");}
                    else if(eMov == 1){game.enemy.move("a");}
                    else if(eMov == 2){game.enemy.move("s");}
                    else{game.enemy.move("d");}
                    System.out.println("FADE has moved.");
                    //System.out.println("FADE's coordinates are: " + game.enemy.getX() + " " + game.enemy.getY());
                
                    /* check if enemy lands in the same square */
                    if(startEnemyEncounter(game.player, game.enemy)){
                        Encounter enemyEncounter = new Encounter(game, game.enemy);
                        enemyEncounter.encounterFight();
                    }
                }
                
            }
            
            
            //check if game continues
            if(instituteFound) {playContinue = false;}
            else if(enemyDefeated) {playContinue = false;}
            System.out.println("Do you want to keep playing? (Type YES or NO)");
            String response = input.nextLine();
            if(response.equals("NO")){playContinue = false;}
            
            //reset booleans
            playerMP = enemyMP = false;
            
            //print player's health and distance from target
            System.out.println();
            System.out.println("You are currently at a health of: " + game.player.getHealth());
            System.out.println("You are " + (int)game.distTP + " square(s) away from the INSTITUTE.");
            
            roundTrack++;
        }
         
        /* Game Conclusion */
        if(enemyDefeated) {System.out.println("You have slain Fade. A new light shines across the desert, and your newfound bravery will lead the way towards the INSTITUTE.");
        } else if (instituteFound){System.out.println("You have found sanctuary at the INSTITUTE. Fade still roams the desert, waiting for the night to fall across the barren land.");
        } else {
            if(game.player.getHealth() <= 0) {System.out.println("You have been slain. The fear to confront the unknown has overwhelmed you.");}
            else {
                System.out.println("The fear to confront the unknown has overwhelmed you. You are lost in the desert as night falls.");
            }
        }
        
        /* Close Threads */

    }
}
