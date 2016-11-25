/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alunos
 */



public class Node {
    private final int id;
    private final int value;
    private boolean delta;
    private Node parent;
    private boolean ackSent;
    private int lidid;
    private boolean initiator; //define se este é o node que vai iniciar a comunicação
    
    //UDPclient client; = new UDPclient();
    
    ArrayList<Node> N;
    ArrayList<Node> S;
    ArrayList<Node> ackValues;
  
    static int state = 1;
     
    public Node(int id, int value, boolean initiator){
        this.id = id;
        this.value = value;
        this.delta = false;
        this.parent = null;
        this.ackSent = false;
        this.N = null;
        this.S = null;
        this.initiator = initiator;
        
        this.stateMachine();
    }
     
    public void addNeighbor(Node Ni){  //TODO:  
        N.add(Ni);
        S.add(Ni);
    }
    private 
    
    private final void stateMachine(){
            

        
        new Thread(){
   
            @Override
            public void run(){
                while(true) {
                    switch (state){
                        case 1: //espera por input ou election
                }
                    
                    
                }
                    
                    
                    //state machine were if input from PC or received election
                    
                    
            }
        }.start();
    
    
    }
}
    
    
