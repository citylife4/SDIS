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
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author alunos
 */



public class Node {
    private final int id;
    private final int value;
    private boolean delta;
    private Integer parent;
    private boolean ackSent;
    private int lidid;
    private boolean initiator; //define se este é o node que vai iniciar a comunicação
    private UDPclient client; 
    private final int port = 12345;
    private final String IP = "225.1.2.3";
    private static Queue message_fifo =new LinkedList();
    
    //Defines for the Message types
    private final String election = "ELECTION";
    private final String ack = "ACK";
    private final String lead = "LEAD"; 
    
    
    ArrayList<Integer> N;
    ArrayList<Integer> S;
    ArrayList<Integer> ackValues;
  
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
        
        this.client = new UDPclient(id,port,IP);
        Thread rec = new Thread(this.client);
        rec.start();
      
        //this.stateMachine();
    }
     
    public void addNeighbor(Integer Ni){  //TODO:  
        N.add(Ni);
        S.add(Ni);
    }
    
    public int getId(){
        return this.id;
    }
    
    public static void handlePacket(String message){
          
        boolean add = message_fifo.add(message); 
    }
    
    public String[] processFIFO(){

        String[] toProcess = null;
        int messageId;
        
        while(true){
            toProcess = ((message_fifo.remove()).toString()).split("@");
            messageId = Integer.parseInt(toProcess[0]);
            
            if(toProcess == null)
                break;
            
            for (Integer N1 : N) {
                if(N1 != messageId){
                       continue;
                    }
                else{
                    return toProcess;
                }
                    
                /*if((toProcess[1]).equals(election)){
                        if( parent == null){
                            parent = N1;
                            S.remove(N1);
                        }
                        else{
                            
                        }*/
                    
                }
           
            }
        
        return null;
  
        }
        
        
    


    
    
    public void stateMachine(){
            

        
        new Thread(){
   
            @Override
            public void run(){
                while(true) {
                    switch (state){
                        case 1: //espera por input ou election
                            if( initiator == true ){
                                client.sendMessage(id,election);
                                state = 8;
                            }
                            else{ 
                                String[] receivedMessage = processFIFO();
                                
                                if( receivedMessage[1].equals(election)){
                                    
                                    parent = Integer.parseInt(receivedMessage[0]);
                                   
                                    for (Integer S1 : S) {
                                        if(S1 == Integer.parseInt(receivedMessage[0])){
                                            S.remove(S1);
                                            break;
                                        }
                                            
                                     }
                                
                                
                                state = 2;
                                
                                }
                                else
                                  System.err.println("Received Unexpected Message in state 0");
                            }
                        case 2:
                            for(Integer S1: S){
                                client.sendMessage(id, election);
                            }
                            state = 3;
                        case 3:
                            String[] receivedMessage = processFIFO();
                            
                            if( receivedMessage[1].equals(election))
                                state = 4;
                            else if(receivedMessage[1].equals(ack))
                                state = 5;
                            else
                                System.err.println("Received Unexpected Message in state 3");
                        case 4:
                            client.sendMessage(id,ack,);
                            
                            
                            
                                
                                 
                                
                            
                            
                            
                            
                }
                    
                    
                }
                    
                    
                    //state machine were if input from PC or received election
                    
                    
            }
        }.start();
    
    
    }
}
    
    
