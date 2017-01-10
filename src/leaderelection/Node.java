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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alunos
 */
//TODO: No fim de uma eleição e antes de voltar para o INIT, meter o messageFifo e variáveis auxiliares a null.
public class Node {

    private final boolean DEBUG;
    private static int id;
    private final int value;
    private boolean delta;
    private Integer parent;
    private boolean ackSent;
    private static int lid;
    private boolean initiator; //define se este é o node que vai iniciar a comunicação
    private UDPclient client;
    private final int port = 12345;
    private final String IP = "225.1.2.3";
    private static final ConcurrentLinkedQueue messageFifo = new ConcurrentLinkedQueue();
    private static final ConcurrentLinkedQueue replyFifo = new ConcurrentLinkedQueue();
    private static final ConcurrentLinkedQueue probeFifo = new ConcurrentLinkedQueue();

    public int nMessage = 0;
    public int toSendDestination = 0;

    //Defines for the Message types
    private final String election = "ELECTION";
    private final String ack = "ACK";
    private final String lead = "LEAD";

    private static boolean notEnd = false;

    public static ReentrantLock messageFifoLock;
    public static ReentrantLock replyFifoLock;
    public static ReentrantLock probeFifoLock;

    //
    public static boolean add = false;
    long startTime;
    private String[] expectedAck = null;

    ArrayList<Integer> nList;
    final ArrayList<Integer> sList;
    ArrayList<Integer> ackValues;

    static int state = 1;

    public Node(int id, int value, boolean initiator) {
        this.DEBUG = true;
        this.id = id;
        this.value = value;
        this.delta = false;
        this.parent = null;
        this.ackSent = false;
        this.lid = 0;
        this.nList = new ArrayList<>();
        this.sList = new ArrayList<>();
        this.ackValues = new ArrayList<>();
        this.initiator = initiator;
        Node.messageFifoLock = new ReentrantLock();
        Node.replyFifoLock = new ReentrantLock();
        Node.probeFifoLock = new ReentrantLock();
        //this.stateMachine();
    }

    public void init() {
        //Create UDP client
        this.client = new UDPclient(id, port, IP);
        Thread rec = new Thread(this.client);
        rec.start();

        //Start sendPing thead
        this.receiveReply();

        //Start State machine
        this.stateMachine();

    }

    public void addNeighbor(int Ni) {  //TODO:  
        System.out.println("[NODE] New neighbor: " + Ni);
        nList.add(Ni);
        sList.add(Ni);
    }

    public int getId() {
        return this.id;
    }

    public static void handlePacket(String message) {

        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("handlePacketThread");

                if (message.contains("REPLY")) {
                    replyFifoLock.lock();
                    try {
                        replyFifo.add(message);
                    } finally {
                        replyFifoLock.unlock();
                    }
                } else if (message.contains("PROBE")) {

                    probeFifoLock.lock();
                    try {
                        probeFifo.add(message);
                    } finally {
                        probeFifoLock.unlock();

                    }

                } else {
                    messageFifoLock.lock();
                    try {
                        messageFifo.add(message);
                    } finally {
                        messageFifoLock.unlock();
                    }
                }

            }

        }.start();

    }

    public void receiveReply() {

        new Thread() {

            @Override
            public void run() {
                String message = null;
                Thread.currentThread().setName("receiveReply");
                while (true) {
                    Boolean flag = false;
                    synchronized (probeFifo) {
                        if (probeFifo != null && !probeFifo.isEmpty()) {
                            flag = true;
                            message = probeFifo.remove().toString();
                        }
                    }
                    if (flag) {
                        if (Integer.parseInt(message.split("\\@")[2]) == id) {
                            System.out.println("[PROBE] Sending reply: " + Integer.parseInt(message.split("\\@")[0]));
                            client.sendMessage(id, "REPLY", Integer.parseInt(message.split("\\@")[0]), 0);

                        } else {
                            //System.out.println(Integer.parseInt(message.split("\\@")[2]) + " != " + id);
                        }
                    }
                }

            }

        }.start();

    }

    public void sendPing() {

        new Thread() {
            @Override
            public void run() {

                String message;
                long initialTime;
                //long timout = 1000000000; //1SECOND
                long timout = 10000; //1SECOND

                Thread.currentThread().setName("sendPing");

                while (true) {
                    //send Ping
                    if (lid != 0) {
                        return;
                    }

                    synchronized (sList) {
                        for (Integer S1 : sList) {
                            //for each Si
                            client.sendMessage(id, "PROBE", S1, 0);
                        }
                    }
                    initialTime = System.nanoTime();

                    while (true) {
                        try {
                            Thread.sleep(timout);

                            if (lid != 0) {
                                return;
                            }

                            ArrayList<Integer> toCheck = new ArrayList();

                            if (System.nanoTime() > initialTime + timout) {
                                System.err.println("TIMOUT = " + lid);
                                replyFifoLock.lock();
                                try {
                                    for (Iterator it = replyFifo.iterator(); it.hasNext();) {
                                        Object messages = it.next();
                                        int toAdd = Integer.parseInt(String.valueOf(messages).split("\\@")[0]);
                                        int receivedId = Integer.parseInt(String.valueOf(messages).split("\\@")[2]);
                                        if (id == receivedId) {
                                            toCheck.add(toAdd);
                                        }
                                    }
                                } finally {
                                    replyFifoLock.unlock();
                                }

                                System.out.println("Slist after: ");
                                sList.stream().forEach(System.out::println);

                                System.out.println("Tocheck: ");
                                toCheck.stream().forEach(System.out::println);
                                synchronized (sList) {
                                    //S.removeAll(toCheck);
                                    sList.retainAll(toCheck);
                                    System.out.println("Slist before: ");
                                    sList.forEach(idtmp -> {
                                        System.out.println(idtmp);
                                    });
                                }
                                toCheck.removeAll(toCheck);
                                System.out.println("asd: ");

                                break;

                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }

        }.start();

    }

    public String[] processFIFO() {

        String[] toProcess;
        int messageId;
        int toMe;
        String message;

        System.out.println("[Process] Starting");

        while (true) {

            while (true) {

                messageFifoLock.lock();
                try {
                    if (messageFifo != null && !messageFifo.isEmpty()) {
                        message = messageFifo.remove().toString();
                        break;
                    }
                } finally {
                    messageFifoLock.unlock();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR!");
                    Logger
                            .getLogger(Node.class
                                    .getName()).log(Level.SEVERE, null, ex);
                    //break;
                }

                if (sList.isEmpty() && (state == 3 || state == 10)) {
                    return null;
                }

            }

            /*
            
            while( messageFifo == null || messageFifo.isEmpty())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR!");
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    //break;
                }
            }
            
                //System.err.println("OLA:" + messageFifo + " " +  messageFifo.isEmpty());
                
                if(DEBUG)
                    System.out.println("[NODE, processFIFO] Last in FIFO: " + messageFifo.peek()
                        .toString()+ " Size: " + messageFifo.size()); 

             */
            toProcess = message.split("@");
            messageId = Integer.parseInt(toProcess[0]);
            toMe = Integer.parseInt(toProcess[2]);
            System.out.println("[PARSE all] Misc:" + message);
            if (toProcess == null) {
                break;
            }

            for (Integer N1 : nList) {
                // Se for meu vizinho e for para mim ou broadcast
                if (N1 == messageId && (toMe == 0 || toMe == Node.id)) {
                    System.out.println("[PARSE] Misc:" + message);
                    add = false;
                    nMessage++;
                    return toProcess;
                } else {
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
                                System.out.println("[STATE -" + state + "]");
                            }

                            if (initiator == true) {
                                if (DEBUG) {
                                    System.out.println("[STATE -" + state + "] I'm the Initiator");
                                }

                                client.sendMessage(id, election, 0, 0);
                                state = 9;

                            } else {
                                sList.stream().forEach(System.out::print);

                                if (DEBUG) {
                                    System.out.println("[STATE -" + state + "] I'm not the Initiator");
                                }
                                if (sList.isEmpty()) {
                                    nList.stream().forEach(System.out::print);
                                    sList.addAll(nList);
                                    sList.stream().forEach(System.out::print);
                                }

                                String[] receivedMessage = processFIFO();
                                if (DEBUG) {
                                    System.out.println("[STATE -" + state + "] Received: " + receivedMessage[1]);
                                }
                                if (receivedMessage[1].equals(election)) {
                                    parent = Integer.parseInt(receivedMessage[0]);

                                    //Remove parent from Si
                                    for (Integer S1 : sList) {
                                        if (S1 == Integer.parseInt(receivedMessage[0])) {
                                            sList.remove(S1);
                                            break;
                                        }
                                    }
                                    state = 2;
                                }
                                if (receivedMessage[1].equals(lead)) {
                                    if (Integer.parseInt(receivedMessage[3]) != lid) {
                                        client.sendMessage(id, lead, 0, lid);
                                    }
                                } else {
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 0");
                                }
                            }
                            sendPing();
                            break;
                        case 2:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            //Start probe

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

                            if (receivedMessage == null) {
                                System.err.println("[STATE -" + state + "] Slist is empty: " + sList.isEmpty());
                                state = 5;
                                break;
                            }

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
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 3: " + null);
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

                            synchronized (sList) {

                                sList.remove(auxReceivedId);

                                if (sList.isEmpty()) //Nao e empty
                                {
                                    state = 6;
                                } else {
                                    if (DEBUG) {
                                        for (int i = 0; i < sList.size(); i++) {

                                            System.err.println(sList.get(i) + " ");
                                        }
                                    }
                                    state = 3;

                                }
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
                                    toSendDestination = Integer.parseInt(expectedLead[0]);
                                    state = 71;
                                    break;
                                } else {
                                    System.err.println("[STATE -" + state + "] Received Unexpected Message in state 7 : " + Arrays.deepToString(expectedLead));
                                }

                            }

                            break;

                        /*case 71:
                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }
                            client.sendMessage(id, ack, toSendDestination, mostValuedAck);
                            state = 7;
                            break;
*/
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

                            initiator = false;
                            state = 1;
                            //return;
                            break;
                        case 9:

                            startTime = System.nanoTime();

                            if (DEBUG) {
                                System.err.println("[BEGIN STATE -" + state + "]");
                            }

                            while (true) {
                                expectedAck = processFIFO();

                                if (expectedAck == null) {
                                    System.err.println("[STATE -" + state + "] Breaking because ping: ");
                                    state = 10;
                                    break;
                                }
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
                            if (expectedAck != null) {
                                ackValues.add(auxReceivedMostValued);
                                synchronized (sList) {
                                    sList.remove(auxReceivedId);

                                    if (sList.isEmpty()) {
                                        state = 11;
                                    } else {
                                        state = 9;
                                    }
                                    break;
                                }
                            } else if (expectedAck == null && sList.isEmpty()) {
                                state = 11;
                            } else {
                                System.err.println("Problems in state " + state);
                            }

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
                                Files.write(Paths.get("/usr/users2/mieec2012/ee12061/NetBeansProjects/sdis/dist/myfile.txt"), (Integer.toString(nMessage) + ' ' + time / 1000000 + '\n').getBytes(), StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                System.out.println("Nao ha ficheiro");
                            }

                            System.out.println("[Node " + id + "] ACABOU LIDER É :" + lid + "\n Elapsed Time: " + time / 1000000);

                            initiator = false;
                            state = 1;
                        //return;

                    }

                }
            }

        }.start();
    }
}
