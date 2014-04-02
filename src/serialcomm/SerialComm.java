/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serialcomm;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Rodrigo
 */
public class SerialComm implements SerialPortEventListener{
      
    //list of available port names
    ArrayList <String> portNames = new ArrayList();
    //list of ports found
    private Enumeration ports = null;
    //to map CommPortIdentifiers with portnames
    private HashMap portMap = new HashMap();
    //^ this two should be static...
    
    //port to use
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;
    
    //in and output data streams
    private InputStream input = null;
    private OutputStream output = null;
    //for dis/abling buttons and stuff depending on port status
    private boolean isPortConnected = false;
    //timeout for connecting with the port
    final static int TIMEOUT = 2000;
    
    public SerialComm(){}
    
    public ArrayList <String> getPortNames()
    {
        return portNames;
    }
    
    @Override
    public void serialEvent(SerialPortEvent spe) {
        //What to do when data is recieved
          if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
           
        }
    }
    
    //this method should be static...
    public void searchForPorts ()
    {
        portNames.clear();
        portMap.clear();
        ports= CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements())
        {
            CommPortIdentifier currentPort = (CommPortIdentifier)ports.nextElement();
            if(currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                portMap.put(currentPort.getName(), currentPort);
                portNames.add(currentPort.getName());
            }
        }
    }

    public void connect(String portName)
    {
        //String selectedPort =  window.getSelectedPortName();
        selectedPortIdentifier = (CommPortIdentifier) portMap.get(portName);
        
        CommPort commPort = null;
        
        try
        {
            //devuelve un objeto CommPort
            commPort = selectedPortIdentifier.open("LED_Driver",TIMEOUT);
            //se puede castear commPort a SerialPort
            serialPort = (SerialPort)commPort;
            setConnected (true);
        }catch (PortInUseException e)
        {
            JOptionPane.showMessageDialog (null,"Port in use.");
        }catch (Exception e)
        {
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog (null,"Something else happened. Please check if any ports are available and relaunch this app");
            System.exit(-1);
        }
        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException ex) {
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public boolean initIOStream()
    {
        boolean successful= false;
        try {
           input = serialPort.getInputStream();
           output = serialPort.getOutputStream();
           
           successful = true;
           return successful;
        }catch (IOException e)
        {
            JOptionPane.showMessageDialog (null,"Initialization failed. Please try again.");
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, e);
            return successful;
        }
    }
    
    //start the event listener
    public void initListener()
    {
        try{
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }catch (TooManyListenersException e)
        {
            JOptionPane.showMessageDialog (null,"Too many listeners, something wrong.");
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, e);
        }
        serialPort.setDTR(false);
    }
    
    //disconnect port
    public void disconnect()
    {
        //closing serial port
        try
        {
            //writeData(0,0);
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            
        }catch(Exception e)
        {
            JOptionPane.showMessageDialog (null,"Failed to close, you may have to reconnect your serial adapter.");
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void writeData (byte toSend)
    {
        try {
            output.write(toSend);
            output.flush();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog (null,"Failed to write, you may have to reconnect your serial adapter.");
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void setConnected(boolean b) {
        isPortConnected = b;
    }
    public boolean getConnected ()
    {
        return isPortConnected;
    }
    /**
     * @param args the command line arguments
    *  
    */
    public static void main(String[] args) {
        SerialComm comm = new SerialComm();
        Window window = new Window(comm);
        window.setVisible(true);
    }
    
}
