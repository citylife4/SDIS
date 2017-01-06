/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

/**
 *
 * @author Afonso Moreira
 * @author José Valverde
 * @author Miguel Carvalho
 */
public class LeaderElection {

    public static void main(String[] args) throws Exception {

        
        boolean initiate;

        //Check arg values
        if ((args.length < 4)) {
            System.out.println("Arg value invalid");
            System.exit(1);
        }

        //check if initiator
        initiate = (args[2]).equals("1");
        

        //Create node object
        Node n = new Node(Integer.parseInt(args[0]), Integer.parseInt(args[1]), initiate);

        //Add neighbor
        for (int i = 3; i < args.length; i++)
            n.addNeighbor(Integer.parseInt(args[i]));
        

        System.out.println("[ID: " + Integer.parseInt(args[0]) + "] Starting");

        //Initiate State Machine
        n.init();

    }

}
