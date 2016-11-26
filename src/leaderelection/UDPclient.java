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

/**
 *
 * @author ee12038
 */
public class UDPclient implements Runnable{

    private int mcPort;
    private String mcIPStr;
    private int pid;
    private MulticastSocket mcSocket = null;
    private InetAddress mcIPAddress = null;
    private boolean isRunning;
    byte[] buffer = new byte[2048];

    
    public UDPclient(int pid, int port, String IPStr) {
        this.pid = pid;
        this.mcIPStr = IPStr;
        this.mcPort = port;
        
    }
    
    public void closeConnection(){
        if(isRunning) {
            isRunning = false;
            mcSocket.close();
        }
        else
            System.err.println("Is not Running");
    }
    
    @Override
    public void run() {
        
        try {
            mcIPAddress = InetAddress.getByName(mcIPStr);
            mcSocket = new MulticastSocket(mcPort);
            mcSocket.joinGroup(mcIPAddress);
            
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            isRunning = true;
            
            while(isRunning){
                try {
                    
                    //Wait to receive message
                    System.out.println( "A Receber ...." );
                    mcSocket.receive( receivePacket );
                    String msg = new String(buffer, 0, receivePacket.getLength());
                    //Node.handlePacket( msg );
                    System.err.println( "Recebido: " + msg );
                    receivePacket.setLength(buffer.length);
                    
                } catch ( Exception e ) {
                    System.out.println( e.getMessage() );
                }
            }
                
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(UDPclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(int id, String message, int destination, int mostValued){
        
        if(isRunning) {
            String messageFormated = id + "@" + message + "@" + destination + "@" + mostValued;
            byte[] toSend = new byte[2048];
            toSend = messageFormated.getBytes();
        
            DatagramPacket sendPacket =
                  new DatagramPacket(toSend, toSend.length);
            try {
                mcSocket.send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(UDPclient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
            System.err.println("Connection not Started");
    }
    
}
