/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alunos
 */
//TODO: No fim de uma eleição e antes de voltar para o INIT, meter o message_fifo e variáveis auxiliares a null.
public class Node {

    private final boolean DEBUG;
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
    private static Queue message_fifo = new LinkedList();
    public int nMessage = 0;

    //Defines for the Message types
    private final String election = "ELECTION";
    private final String ack = "ACK";
    private final String lead = "LEAD";

    public static ReentrantLock lock;

    //
    public static boolean add = false;
    long startTime;

    ArrayList<Integer> N;
    ArrayList<Integer> S;
    ArrayList<Integer> ackValues;

    static int state = 1;

    public Node(int id, int value, boolean initiator) {
        this.DEBUG = false;
        this.id = id;
        this.value = value;
        this.delta = false;
        this.parent = null;
        this.ackSent = false;
        this.lid = 0;
        this.N = new ArrayList<>();
        this.S = new ArrayList<>();
        this.ackValues = new ArrayList<>();
        this.initiator = initiator;
        Node.lock = new ReentrantLock();
        //this.stateMachine();
    }

    public void init() {
        this.client = new UDPclient(id, port, IP);
        Thread rec = new Thread(this.client);
        rec.start();

        this.stateMachine();

    }

    public void addNeighbor(int Ni) {  //TODO:  
        System.out.println("[NODE] New neighbor: " + Ni);
        N.add(Ni);
        S.add(Ni);
    }

    public int getId() {
        return this.id;
    }

    public static void handlePacket(String message) {

        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("handlePacketThread-" + Thread.currentThread().getId());
                lock.lock();
                try {
                    //System.out.println("[NODE, handlePacket] Received message: "
                    //        + message);
                    add = message_fifo.add(message);
                    /*
                    System.out.println("[NODE, handlePacket] added is " + add + ": "
                       + message_fifo.peek().toString());
                     */
                } finally {
                    lock.unlock();
                }

            }

        }.start();

    }

    public String[] processFIFO() {

        String[] toProcess;
        int messageId;
        int toMe;
        String message;

        while (true) {

            while (true) {

                lock.lock();
                try {
                    if (message_fifo != null && !message_fifo.isEmpty()) {
                        message = message_fifo.remove().toString();
                        break;
                    }
                } finally {
                    lock.unlock();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR!");
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    //break;
                }

            }

            /*
            
            while( message_fifo == null || message_fifo.isEmpty())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR!");
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    //break;
                }
            }
            
                //System.err.println("OLA:" + message_fifo + " " +  message_fifo.isEmpty());
                
                if(DEBUG)
                    System.out.println("[NODE, processFIFO] Last in FIFO: " + message_fifo.peek()
                        .toString()+ " Size: " + message_fifo.size()); 

             */
            toProcess = message.split("@");
            messageId = Integer.parseInt(toProcess[0]);
            toMe = Integer.parseInt(toProcess[2]);

            if (toProcess == null) {
                break;
            }

            for (Integer N1 : N) {
                // Se for meu vizinho e for para mim ou broadcast
                if (N1 == messageId && (toMe == 0 || toMe == this.id)) {
                    System.out.println("Returning" + message);
                    add = false;
                    nMessage++;
                    return toProcess;
                } else {
                    continue;
                }
            }

            /*if((toProcess[1]).equals(election)){
                            if( parent == null){
                                parent = N1;
                                S.remove(N1);
                            }
                            else{

                            }*/
        }

        return null;

    }

    public void stateMachine() {

        new Thread() {

            @Override
            public void run() {
                Thread.currentThread().setName("stateMachineThread-" + Thread.currentThread().getId());

                Integer auxReceivedId = null;
                Integer auxReceivedMostValued = null;
                Integer auxReceivedLeaderId = null;
                Integer mostValuedAck = 0;
                while (true) {
                    OUTER:
                    switch (state) {
                        case 1: //espera por input ou election
                            if (DEBUG) {
                                System.out.println("[STATE -" + state + "] initator? = " + initiator);
                            }

                            if (initiator == true) {
                                client.sendMessage(id, election, 0, 0);
                                state = 9;

                            } else {

                                String[] receivedMessage = processFIFO();
                                if (DEBUG) {
                                    System.out.println("[STATE -" + state + "] Received: " + receivedMessage[1]);
                                }
                                if (receivedMessage[1].equals(election)) {
                                    parent = Integer.parseInt(receivedMessage[0]);

                                    for (Integer S1 : S) {
                                        if (S1 == Integer.parseInt(receivedMessage[0])) {
                                            S.remove(S1);
                                            break;
                                        }
                                    }
                                    state = 2;
                                } else {
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 0");
                                }
                            }
                            break;
                        case 2:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            delta = true;
                            //for(Integer S1: S){
                            client.sendMessage(id, election, 0, 0);
                            //}
                            state = 3;
                            break;
                        case 3:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            String[] receivedMessage = processFIFO();

                            switch (receivedMessage[1]) {
                                case election:
                                    auxReceivedId = Integer.parseInt(receivedMessage[0]);
                                    state = 4;
                                    break OUTER;
                                case ack:
                                    auxReceivedId = Integer.parseInt(receivedMessage[0]);
                                    auxReceivedMostValued = Integer.parseInt(receivedMessage[3]);
                                    if (DEBUG) {
                                        System.err.println("auxID: " + auxReceivedId + " " + auxReceivedMostValued);
                                    }
                                    state = 5;
                                    break OUTER;
                                default:
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 3");
                                    break OUTER;
                            }
                        case 4:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            client.sendMessage(id, ack, auxReceivedId, id);
                            state = 3;
                            break;
                        case 5:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }

                            ackValues.add(auxReceivedMostValued);
                            S.remove(auxReceivedId);

                            if (S.isEmpty()) //Nao e empty
                            {
                                state = 6;
                            } else {
                                for (int i = 0; i < S.size(); i++) {
                                    if (DEBUG) {
                                        System.err.println(S.get(i) + " ");
                                    }
                                }
                                state = 3;

                            }
                            break;
                        case 6:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }

                            if (!ackValues.isEmpty()) {
                                for (Integer N1 : ackValues) {
                                    if (!N1.equals(parent)) {
                                        if (mostValuedAck < N1) {
                                            mostValuedAck = N1;
                                        }
                                    }
                                }
                            }

                            if (mostValuedAck < value) {
                                mostValuedAck = value;
                            }

                            client.sendMessage(id, ack, parent, mostValuedAck);
                            state = 7;
                            break;
                        case 7:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }

                            while (true) {
                                String[] expectedLead = processFIFO();

                                if ((expectedLead[1]).equals(lead)) {

                                    auxReceivedLeaderId = Integer.parseInt(expectedLead[3]);
                                    state = 8;
                                    break;

                                } else if ((expectedLead[1].equals(election))) {
                                    state = 71;
                                    break;
                                } else {
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 7 : " + Arrays.deepToString(expectedLead));
                                }

                            }

                            break;

                        case 71:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            client.sendMessage(id, ack, mostValuedAck, id);
                            state = 7;
                            break;

                        case 8:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            lid = auxReceivedLeaderId;
                            delta = false;
                            //state = 1;
                            if (DEBUG) {
                                System.out.println("The Leader is" + lid + "!");
                            }

                            client.sendMessage(id, lead, 0, lid);
                            System.out.println("[Node " + id + "] ACABOU LIDER É :" + lid);
                            try {
                                Files.write(Paths.get("/usr/users2/mieec2012/ee12061/NetBeansProjects/sdis/dist/myfile.txt"), (Integer.toString(nMessage) + '\n').getBytes(), StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                System.out.println("nao ha ficheiro");
                                //exception handling left as an exercise for the reader
                            }
                            return;
                        case 9:

                            startTime = System.nanoTime();

                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            String[] expectedAck;

                            while (true) {
                                expectedAck = processFIFO();
                                if ((expectedAck[1]).equals(ack)) {
                                    if (DEBUG) {
                                        System.out.println("expectedACK: " + expectedAck[0] + " " + expectedAck[1] + " " + expectedAck[2] + " " + expectedAck[3] + " ");
                                    }
                                    auxReceivedId = Integer.parseInt(expectedAck[0]);
                                    auxReceivedMostValued = Integer.parseInt(expectedAck[3]);
                                    state = 10;
                                    break;
                                }
                            }
                            break;
                        case 10:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            ackValues.add(auxReceivedMostValued);
                            S.remove(auxReceivedId);

                            if (S.isEmpty()) {
                                state = 11;
                            } else {
                                state = 9;
                            }
                            break;
                        case 11:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            Integer mostValuedAck2 = 0;

                            delta = false;
                            if (DEBUG) {
                                System.out.println("[STATE -" + state + "] The Leader is" + lid + "!");
                            }

                            for (Integer N1 : ackValues) {
                                if (!N1.equals(parent)) {
                                    if (mostValuedAck2 < N1) {
                                        mostValuedAck2 = N1;
                                    }
                                }
                            }
                            if (mostValuedAck2 < value) {
                                mostValuedAck2 = value;
                            }
                            lid = mostValuedAck2;

                            client.sendMessage(id, lead, 0, mostValuedAck2);
                            state = 1;
                            long estimatedTime = System.nanoTime() - startTime;
                            float time = estimatedTime;
                            try {
                                Files.write(Paths.get("/usr/users2/mieec2012/ee12061/NetBeansProjects/sdis/dist/myfile.txt"), (Integer.toString(nMessage) + ' ' + time/1000000 + '\n').getBytes(), StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                System.out.println("Nao ha ficheiro");
                            }
                            
                            System.out.println("[Node " + id + "] ACABOU LIDER É :" + lid + "\n Elapsed Time: " + time / 1000000);
                            return;

                    }

                }
            }

        }.start();
    }
}
