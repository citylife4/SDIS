/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author alunos
 */



public class LeaderElection {

    public static void main(String[] args) throws Exception {
    
        //start connection
        // - new UDPclient
        
        //init nodes values for leader election
        
        // new node
        
        //start leader election...
        
        // new statemachine (or node method, just do node.state_machine();)
        boolean initiate;
        
        if((args[2]).equals("1"))
            initiate = true; 
        else
            initiate = false;
        
        
        
        
        Node n = new Node(Integer.parseInt(args[0]),Integer.parseInt(args[1]),initiate);
        
        for(int i=3; i<args.length; i++){
            n.addNeighbor(Integer.parseInt(args[i]));
            }
        
        System.out.println("Starting State Machine");
        
        n.stateMachine();
        
    }

        
    
}
