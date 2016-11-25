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
public class UPDclient implements Runnable{

    private int mcPort;
    private String mcIPStr;
    private int pid;
    private MulticastSocket mcSocket = null;
    private InetAddress mcIPAddress = null;
    private boolean isRunning;
    private DatagramPacket packet;

    
    public UPDclient(int pid, int port, String IPStr) {
        this.pid = pid;
        this.mcIPStr = IPStr;
        this.mcPort = port;
        
    }
    
    public void closeConnection(){
        isRunning = false;
    }
    
    @Override
    public void run() {
        
        try {
            mcIPAddress = InetAddress.getByName(mcIPStr);
            mcSocket = new MulticastSocket(mcPort);
            mcSocket.joinGroup(mcIPAddress);
            
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            isRunning = true;
            
            while(isRunning){
                try {
                mcSocket.receive( packet );
                
                handlePacket( packet, wBuffer );
            } catch ( Exception e ) {
                System.out.println( e.getMessage() );
            }
            }
                
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(UPDclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
