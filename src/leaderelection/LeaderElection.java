/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

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
        
        if((args.length < 4))
        {
            System.out.println("Arg value invalid");
            return;
        }
        
        if((args[2]).equals("1"))
            initiate = true; 
        else
            initiate = false;
        
        
        
        
        Node n = new Node(Integer.parseInt(args[0]),Integer.parseInt(args[1]),initiate);
        
        for(int i=3; i<args.length; i++){
            int toAdd = Integer.parseInt(args[i]);
            //System.out.println("[Main] Adding from " + i + "with " + toAdd);
            n.addNeighbor(Integer.parseInt(args[i]));
            }
        
        System.out.println("Starting State Machine");
        
        n.init();
        
    }

        
    
}
