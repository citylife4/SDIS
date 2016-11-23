/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.util.ArrayList;

/**
 *
 * @author alunos
 */
public class Node {
    private final int id;
    private final int value;
    private boolean delta;
    private Node parent;
    boolean ack_sent;
    int lidid;
    ArrayList<Node> N;
    ArrayList<Node> S;
    
    
    
    
    public Node(int id, int value){
        this.id = id;
        this.value = value;
        this.delta = false;
        this.parent = null;
        this.ack_sent = false;
        this.lidid = 0;
        this.N = null;
        this.S = null;
    }
    
    private void addNeighbor(Node Ni){
     
        N.add(Ni);
        (Ni.N).add();
        
        
    
    }
           
    
}
