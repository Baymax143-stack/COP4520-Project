
/**
 * THE CLASS GameTree WILL PROVIDE THE FOLLOWING FUNCTIONALITY:
 * 
 * Convert a set trade-off matrix, representative of the dynamics of the game, into a tree
 * 
 * Observe time differences in traversing and finding values of the tree, with three different
 * processes (implementation of linear/brute-force, recursive, and parallel methods for traversal)
 * 
 * Citation: https://www.geeksforgeeks.org/construct-complete-binary-tree-given-array/
 * 
 *
 * Geela Margo Ramos
 * Progress Update: 04/19/2022
 */

import java.util.*;
import java.util.Stack; //will comment out for actual simulation
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;

class Node {
    int p1Value, p2Value;
    String agent, action;
    double probability, p1Trade, p2Trade;
    public AtomicBoolean accessGT = new AtomicBoolean(false);
    
    Node left;
    Node right;
    
    Node(String agent, String action, double prob, int p1, int p2) {
        //sets the associated agent and action with trade-off
        this.agent = agent; this.action = action;
        //sets the trade-off value
        this.p1Value = p1; this.p2Value = p2;
        //sets the action probability (of being chosen)
        this.probability = prob;
        //sets the right and left leaves for the node
        left = right = null;
        //helps us keep track of calculations
    }
}


class Backoff {
    final int minDelay, maxDelay;
    int limit;
    
    public Backoff(int min, int max) {
        minDelay = min;
        maxDelay = max;
        limit = minDelay;
    }
    
    public void backoff() throws InterruptedException {
        try{
            int delay = ThreadLocalRandom.current().nextInt(limit);
            limit = Math.min(maxDelay, 2 * limit);
            Thread.sleep(delay);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Exception message: " + e);
        }
    }
}


/* Specifically for traversal through tree in parallel */
class traverseIP extends Thread implements Runnable {
    private Node n; boolean marked;
    GameTree mainThread;
        
    public traverseIP(Node assignedNode, GameTree main){
        this.n = assignedNode;
        this.mainThread = main;
    }
    
    public void lock() throws InterruptedException {
        Backoff wait = new Backoff(mainThread.MIN_DELAY, mainThread.MAX_DELAY);
        while(true){
            while(n.accessGT.get()) {};
            if(!n.accessGT.getAndSet(true)){
                this.marked = true;
                return;
            } else {
                System.out.println("Thread for <" + n.agent + ", " + n.action + "> is waiting for other calculations to finish.");
                wait.backoff();
            }
        }
    }
    
    public void unlock() {
        n.accessGT.set(false);
        return;
    }
    
    public void subgameNE(){
        //if node is empty
        if(n == null) return;
        
        //if node is a leaf
        if(n.right == null && n.left == null) {
            try {
                lock();
                //mainThread.lock.acquire();
                
                n.p1Trade = n.probability * n.p1Value;
                n.p2Trade = n.probability * n.p2Value;
                
                //mainThread.lock.release();
                unlock();
                return;
            }
            catch (Exception e) {
            System.out.println("Exception message: " + e);
            }
        }
        
        //if a node is a branch
        try {
            lock();
            //mainThread.lock.acquire();

            n.p1Trade = n.left.p1Trade + n.right.p1Trade;
            n.p2Trade = n.left.p2Trade + n.right.p2Trade;
            
            //mainThread.lock.release();
            unlock();
        }
        catch(Exception e) {
            System.out.println("Exception message: " + e);
        }
    }
    
    public void subgameNEResult(){
        System.out.println("For <" + n.agent + ", " + n.action + ">, the current calculated subgame NE is: <" + n.p1Trade + ", " + n.p2Trade + ">. ");
    }
        
    @Override
    public void run() {
        try{
            subgameNE();
            subgameNEResult();
        }
        catch(Exception e) {
            System.out.println("Exception Message: " + e);
        }
    }
}


/* Definition of Game Tree and Necessary Functions */
public class GameTree
{   
    //game tree's main root
    Node root;
    
    //instance variables needed
    public Semaphore lock = new Semaphore(1, true);
    private List<Thread> threads; private List<traverseIP> traversals;
    final int MIN_DELAY = 1, MAX_DELAY = 10;
    
    /* Function to Insert Nodes, assuming tradeoff values are given in level order */
    public Node gameTreeFormation(String[] agent, String[] action, double[] probability, int[] p1TradeOff, int[] p2TradeOff, Node root, int i){
        //base case for recursion
        if(i < p1TradeOff.length) {
            Node temp = new Node(agent[i], action[i], probability[i], p1TradeOff[i], p2TradeOff[i]);
            root = temp;
            
            //insert left child
            root.left = gameTreeFormation(agent, action, probability, p1TradeOff, p2TradeOff, root.left, 2 * i + 1);
            //Insert Right Child
            root.right =  gameTreeFormation(agent, action, probability, p1TradeOff, p2TradeOff, root.left, 2 * i + 2);
        }
        return root;
    }    
    
    
    
    /* Traverses through tree without recursion */
    void postOrderPrintNR(Node root) {
        Stack<Node> S = new Stack<Node>();
         
        // Check for empty tree
        if (root == null) return;
        S.push(root);
        
        Node prev = null;
        while (!S.isEmpty())
        {
            Node current = S.peek();
            
            if (prev == null || prev.left == current || prev.right == current){
                if (current.left != null)
                    S.push(current.left);
                else if (current.right != null)
                    S.push(current.right);
                else{
                    S.pop();
                    
                    //print statements for the node
                    System.out.println("This node initially holds a player trade-off of: (" + current.p1Value + ", " + current.p2Value + ").");
                }
 
            }
            else if (current.left == prev){
                if (current.right != null)
                    S.push(current.right);
                else{
                    S.pop();
                    
                    //print statements for the node
                    System.out.println("This node initially holds a player trade-off of: (" + current.p1Value + ", " + current.p2Value + ").");
                }

            }
            else if (current.right == prev){
                S.pop();
                
                //print statements for the node
                System.out.println("This node initially holds a player trade-off of: (" + current.p1Value + ", " + current.p2Value + ").");
            }
 
            prev = current;
        }
    }
    
    /* Traverses through tree with recursion */
    void postOrderPrintWR(Node root) {
        if(root != null) {
            postOrderPrintWR(root.left);
            postOrderPrintWR(root.right);
            
            //print statements for the node
            System.out.println("The " + root.agent + " will " + root.action + ". ");
        }
    }
    
    /* Traverses through tree with multithreading */
    void tPrintIP(Node root, GameTree mainThread) {
        if(root == null) return;
        //handle left child node
        tPrintIP(root.left, mainThread);
            
        //handle right child node
        tPrintIP(root.right, mainThread);
            
        //trigger action
        traverseIP travCurr = new traverseIP(root, mainThread);
        mainThread.traversals.add(travCurr);
        Thread tCurr = new Thread(travCurr);
        mainThread.threads.add(tCurr);
            
        System.out.println("For <" + root.agent + ", " + root.action + ">, the initially set subgame NE is: <" + root.p1Trade + ", " + root.p2Trade + ">. ");
    }
    
    
    
    /* Traverses through tree, calculating subgame perfect equilibria without recursion */
    void postOrderCalculateNR(Node root) {
        Stack<Node> S = new Stack<Node>();
         
        // Check for empty tree
        if (root == null) return;
        S.push(root);
        
        Node prev = null;
        while (!S.isEmpty())
        {
            Node current = S.peek();
            
            if (prev == null || prev.left == current || prev.right == current){
                if (current.left != null)
                    S.push(current.left);
                else if (current.right != null)
                    S.push(current.right);
                else{
                    S.pop();
                    
                    //call to calculate
                    //node is a leaf
                    current.p1Trade = current.probability * current.p1Value;
                    current.p2Trade = current.probability * current.p2Value;
                    //System.out.println("For <" + current.agent + ", " + current.action + ">, the current calculated subgame NE is: <" + current.p1Trade + ", " + current.p2Trade + ">. ");
                }
 
            }
            else if (current.left == prev){
                if (current.right != null)
                    S.push(current.right);
                else{
                    S.pop();
                    
                    //call to calculate
                    //node is a branch
                    current.p1Trade = root.left.p1Trade + current.right.p1Trade;
                    current.p2Trade = root.left.p2Trade + current.right.p2Trade;
                    //System.out.println("For <" + current.agent + ", " + current.action + ">, the current calculated subgame NE is: <" + current.p1Trade + ", " + current.p2Trade + ">. ");
                }

            }
            else if (current.right == prev){
                S.pop();
                
                //call to calculate
                //if node is a branch
                current.p1Trade = current.left.p1Trade + current.right.p1Trade;
                current.p2Trade = current.left.p2Trade + current.right.p2Trade;
                //System.out.println("For <" + current.agent + ", " + current.action + ">, the current calculated subgame NE is: <" + current.p1Trade + ", " + current.p2Trade + ">. ");
            }
 
            prev = current;
        }
    }
    
    
    /* Traverses through tree, calculating subgame perfect equilibria with recursion */
    void postOrderCalculateWR(Node root) {
        if(root != null) {
            postOrderCalculateWR(root.left);
            postOrderCalculateWR(root.right);
            
            //call to calculate
            //if node is a leaf
            if(root.right == null && root.left == null) {
                root.p1Trade = root.probability * root.p1Value;
                root.p2Trade = root.probability * root.p2Value;
                //System.out.println("For <" + root.agent + ", " + root.action + ">, the current calculated subgame NE is: <" + root.p1Trade + ", " + root.p2Trade + ">. ");
                return;
            }
            
            //if node is a branch
            root.p1Trade = root.left.p1Trade + root.right.p1Trade;
            root.p2Trade = root.left.p2Trade + root.right.p2Trade;
            //System.out.println("For <" + root.agent + ", " + root.action + ">, the current calculated subgame NE is: <" + root.p1Trade + ", " + root.p2Trade + ">. ");
        }
    }
    
    /* Traverses through tree, calculating subgame perfect equilibria with multithreading */
    void tCalculateIP(Node root, GameTree mainThread) {
        if(root == null) return;
        //handle left child node
        tCalculateIP(root.left, mainThread);
            
        //handle right child node
        tCalculateIP(root.right, mainThread);
            
        //trigger action
        traverseIP travCurr = new traverseIP(root, mainThread);
        mainThread.traversals.add(travCurr);
        Thread tCurr = new Thread(travCurr);
        mainThread.threads.add(tCurr);
          
        //call to calculate
        tCurr.run();
    }
    
    
    
    /* Driver Function for Testing */
    public static void main(String[] args) {
        GameTree subgame = new GameTree();
        long startTime, endTime;
        
        //set up necessary variables
        String player = "PLAYER", enemy = "ENEMY";
        //String slow = "move at a slow pace"; //will be used for actual simulation
        String fast = "move at a fast pace";
        String wait = "wait, and will not dash", search = "search area to dash towards";
        String wander = "wander; the PLAYER has a chance encounter", hide = "hide; the PLAYER waits if it is safe to move forward";
        String rest = "rest; there is still a chance for an encounter", loot = "loot to find armor; there is still a chance for encounter";
        
        //set up the subgame 
        //in the acutal simulation, is set whenever the player chooses the fast pace
        String[] agent = {player, enemy, enemy, player, player, player, player};
        String[] action = {fast, wait, search, wander, hide, rest, loot}; 
        double[] probability = {0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25}; //set probabilities, with each action having an initial 50% chance of being chosen
        int[] p1TradeOff = {0, 0, 0, 3, 2, 2, 3};
        int[] p2TradeOff = {0, 0, 0, 1, 1, 2, 3};
        subgame.root = subgame.gameTreeFormation(agent, action, probability, p1TradeOff, p2TradeOff, subgame.root, 0);
        
        
        /* FIRST TEST: Observing Traveral with Print */
        System.out.println("FIRST TEST: Observing Traversal with Print");
        
        /* Testing of Linear Method */
        startTime = System.nanoTime();
        subgame.postOrderPrintNR(subgame.root);
        endTime = System.nanoTime();
        
        System.out.println();
        System.out.println("Time for In Order, No-Recursion Traversal for Printing: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
        
        /* Testing of Recursive Method */
        startTime = System.nanoTime();
        subgame.postOrderPrintWR(subgame.root);
        endTime = System.nanoTime();
        
        System.out.println();
        System.out.println("Time for In Order, With-Recursion Traversal for Printing: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
        
        /* Testing of Parallel Method */
        GameTree mainThread = new GameTree();
        int n = p1TradeOff.length;
        
        //spawning of threads
        startTime = System.nanoTime();
        mainThread.threads = new ArrayList<>(); mainThread.traversals = new ArrayList<>();
        subgame.tPrintIP(subgame.root, mainThread);
        for(Thread t : mainThread.threads) {
            try{
                t.join();
            }
            catch(Exception e){
                System.out.println("Exception message: " + e);
            }
        }
        endTime = System.nanoTime();
        for(Thread t: mainThread.threads) {
            t.interrupt();
        }
        
        System.out.println();
        System.out.println("Time for Traversal in Parallel for Printing: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
        
        

        /* SECOND TEST: Observing Traversal with NEQ calculation (conducted through brute force) */
        System.out.println(); System.out.println();
        System.out.println("SECOND TEST: Observing Traversal with Subgame Perfect Equilibria Calculation");
        
        /* Testing of Linear Method */
        startTime = System.nanoTime();
        subgame.postOrderCalculateNR(subgame.root);
        endTime = System.nanoTime();
        
        System.out.println();
        System.out.println("Time for In Order, No-Recursion Traversal for Calculating: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
        
        /* Testing of Recursive Method */
        startTime = System.nanoTime();
        subgame.postOrderCalculateWR(subgame.root);
        endTime = System.nanoTime();
        
        System.out.println();
        System.out.println("Time for In Order, With-Recursion Traversal for Calculating: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
        
        /* Testing of Parallel Method */
        startTime = System.nanoTime();
        subgame.tCalculateIP(subgame.root, mainThread);
        for(Thread t : mainThread.threads) {
            try{
                t.join();
            }
            catch(Exception e){
                System.out.println("Exception message: " + e);
            }
        }
        endTime = System.nanoTime();
        for(Thread t: mainThread.threads) {
            t.interrupt();
        }
        
        System.out.println();
        System.out.println("Time for Traversal in Parallel for Calculating: " + (endTime - startTime) + " nanoseconds ");
        System.out.println();
    }
}
