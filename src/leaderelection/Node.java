/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author alunos
 */

//TODO: No fim de uma eleição e antes de voltar para o INIT, meter o message_fifo e variáveis auxiliares a null.

public class Node {
    private final int id;
    private final int value;
    private boolean delta;
    private Integer parent;
    private boolean ackSent;
    private int lid;
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
        this.lid = 0;
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
                
               Integer auxReceivedId = null;
               Integer auxReceivedMostValued = null;
               Integer auxReceivedLeaderId = null;
               
                while(true) {
                    switch (state){
                        case 1: //espera por input ou election
                            if( initiator == true ){
                                client.sendMessage(id,election,0,0);
                                state = 9;
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
                            delta = true;
                            for(Integer S1: S){
                                client.sendMessage(id, election,0,0);
                            }
                            state = 3;
                        case 3:
                            String[] receivedMessage = processFIFO();
                            
                            if( receivedMessage[1].equals(election)){
                                auxReceivedId = Integer.parseInt(receivedMessage[2]);
                                state = 4;
                            }
                            else if(receivedMessage[1].equals(ack)){
                                auxReceivedId = Integer.parseInt(receivedMessage[2]);
                                auxReceivedMostValued = Integer.parseInt(receivedMessage[3]);
                                state = 5;
                            }
                            else
                                System.err.println("Received Unexpected Message in state 3");
                        case 4:
                            client.sendMessage(id,ack,auxReceivedId,id);
                            state = 3;
                        case 5:
                            
                            ackValues.add( auxReceivedMostValued);
                            S.remove(auxReceivedId);
                        
                            if(S == null)
                                state = 6;
                            else 
                                state = 3;
                        case 6:
                            
                            Integer mostValuedAck = 0;
                            
                            for(Integer N1: N){
                                if(!N1.equals(parent)){
                                    if(mostValuedAck < N1)
                                        mostValuedAck = N1;
                                }
                            }
                            
                            client.sendMessage(id, ack, parent, mostValuedAck);
                            state = 7;
                            
                        case 7:
                           
                            while(true){
                                
                                String[] expectedLead = processFIFO();
                            
                            if((expectedLead[1]).equals(lead)){
                                
                                auxReceivedLeaderId = Integer.parseInt(expectedLead[3]);
                                state = 8;
                                break;
                
                            }
                            else
                               System.err.println("Received Unexpected Message in state 7, trying again...");
                            }
                        case 8:
                            lid = auxReceivedLeaderId;
                            delta = false;
                            state = 1;
                            
                            client.sendMessage(id, lead, 0, lid);
                            
                        case 9:
                            
                            String[] expectedAck = null;
                            
                            while(true){
                                expectedAck = processFIFO();
                                if((expectedAck[1]).equals(ack)){
                                    auxReceivedId = Integer.parseInt(expectedAck[2]);
                                    auxReceivedMostValued = Integer.parseInt(expectedAck[3]);
                                    state = 10;
                                    break;
                                }
                            }
                            
                        case 10:
                            ackValues.add( auxReceivedMostValued);
                            S.remove(auxReceivedId);
                        
                            if(S == null)
                                state = 11;
                            else 
                                state = 9;
                            
                        case 11:
                            Integer mostValuedAck2 = 0;
                            
                            for(Integer N1: N){
                                if(!N1.equals(parent)){
                                    if(mostValuedAck2 < N1)
                                        mostValuedAck2 = N1;
                                }
                            }
                            
                            client.sendMessage(id, lead, 0, mostValuedAck2);
                            state = 1;
                }     
            }
        }
    
    
    }.start();
}
}
    
