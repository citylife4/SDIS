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
    boolean ackSent;
    int lidid;
    
    ArrayList<Node> N;
    ArrayList<Node> S;
    ArrayList<Node> ackValues;
    
    static boolean input;
    static int state = 1;
    
    InetAddress group;
    String inetAddress = "228.5.6.7";
    int port = 6789;
    

    
    public Node(int id, int value){
        this.id = id;
        this.value = value;
        this.delta = false;
        this.parent = null;
        this.ackSent = false;
        this.N = null;
        this.S = null;
        
        this.stateMachine();
    }
    
    public void joinGroup() {
        
        try {
            group = InetAddress.getByName(inetAddress);
            MulticastSocket s = new MulticastSocket(port);
            DatagramSocket socket = new DatagramSocket();
            
            s.joinGroup(group);
            
        } catch (Exception ex) {
            //TODO : cenas
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addNeighbor(Node Ni){  //TODO:  
        N.add(Ni);
        S.add(Ni);
    }
    private 
    
    private boolean changeInput(){
        //TODO : 
        new Thread(){
            @Override
            public void run(){
                
            }
        }
        return input;
    }
    
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
    
    

