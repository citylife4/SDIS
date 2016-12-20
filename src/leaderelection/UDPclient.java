/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leaderelection;

import java.io.IOException;
import static java.lang.Math.log;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import leaderelection.Node;

/**
 *
 * @author ee12038
 */
public final class UDPclient implements Runnable{

    private int mcPort;
    private String mcIPStr;
    public int pid;
    private MulticastSocket mcSocket = null;
    private InetAddress mcIPAddress = null;
    public boolean isRunning;
    byte[] buffer = new byte[2048];
    
    //DEBUG:
    private final String log;
    
    public UDPclient(int pid, int port, String IPStr) {
        super();
        this.pid = pid;
        this.mcIPStr = IPStr;
        this.mcPort = port;
        log  = "[UDP, Thread: " + Thread.currentThread().getId() + "] ";
        Thread.currentThread().setName("UDPThread");
        startConnection();
    }
    
    public void startConnection(){
        try {
            System.out.println("[UDP, Node: " + Thread.currentThread().getId() + "] " + "Starting Connection ");
            mcIPAddress = InetAddress.getByName(mcIPStr);
            mcSocket = new MulticastSocket(mcPort);
            mcSocket.joinGroup(mcIPAddress);
            isRunning = true;
        } catch (IOException ex) {
            System.err.println("[UDP, Node: " + Thread.currentThread().getId() + "] " + "Problem on creating socket");
            Logger.getLogger(UDPclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeConnection(){
        if(isRunning) {
            isRunning = false;
            mcSocket.close();
        }
        else
            System.err.println("[UDP, " + Thread.currentThread().getId() + "] " + "Is not Running");
    }
    
    @Override
    public void run() {

        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        if(!isRunning){
            startConnection();
        }
        while(isRunning){
            try {
                
                
                //Wait to receive message
                //this.sendMessage(1, "2", 3);
                //System.out.println("[UDP, Thread: " + Thread.currentThread().getId() + "] " +  "A Receber .... " );
                
                mcSocket.receive( receivePacket );
                String msg = new String(buffer, 0, receivePacket.getLength());
                Node.handlePacket( msg );
                //System.err.println("[UDP, Thread: " + Thread.currentThread().getId() + "] " +  "Recebido: " + msg );
                receivePacket.setLength(buffer.length);

            } catch ( IOException e ) {
                System.err.println("[UDP, Thread: " + Thread.currentThread().getId() + "] " + "Problem on receive");
                System.out.println( e.getMessage() );
            }
        }
                
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }
    
    public void sendMessage(int id, String message, int destination, int mostValued){
        
        new Thread() {
            
            @Override
                public void run() {
                    
                    
                  if(!isRunning) {
                    startConnection();
                }

                String messageFormated = id + "@" + message + "@" + destination + "@" + mostValued;
                byte[] toSend = new byte[2048];
                toSend = messageFormated.getBytes();
                System.out.println("[UDP, Thread: " + Thread.currentThread().getId() + "] "
                        + "Sending: " + messageFormated + " _ "+  Thread.currentThread().getId());
                
                DatagramPacket sendPacket = new DatagramPacket(toSend, toSend.length);
                sendPacket.setAddress(mcIPAddress);
                sendPacket.setPort(mcPort);
                
                try {
                    
                    mcSocket.send(sendPacket);
                    /*
                    System.out.println("[UDP, Thread: " + Thread.currentThread().getId() + "] " 
                            + messageFormated + " Sent.");
                    */
                    Thread.currentThread().interrupt();
                    
                } catch (IOException ex) {
                    System.err.println("[UDP, Thread: " + Thread.currentThread().getId() + "] " + "Problem Sending");
                    Logger.getLogger(UDPclient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();     
        
    } 
}
