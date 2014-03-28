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
    //private Window window;
    private Window window;
    
    //lista de puertos que se encontraran
    private Enumeration ports = null;
    //para mapear los portnames de CommPortIdentifiers
    private HashMap portMap = new HashMap();
    
    //contiene el puerto a utilizar
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;
    
    //in and output data streams
    private InputStream input = null;
    private OutputStream output = null;
    //para des/habilitar botones y cosillas dependiendo del estado del puerto
    private boolean isPortConnected = false;
    //timeout for connecting with the port
    final static int TIMEOUT = 2000;
    //ASCII values for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
    
    public SerialComm()
    {
        super();
        window = new Window() {

            @Override
            public void guiConnect() {
                connect();
                if(getConnected() == true)
                {
                    if(initIOStream() == true)
                    {
                        initListener();
                    }
                }
            }

            @Override
            public void guiDisconnect() {
                disconnect();
            }

            @Override
            public void guiSendData(byte data) {
                System.out.print("Sending: ");
                System.out.printf("0x%02X\n",data);
                writeData(data);
            }

            @Override
            public void guiRefreshPorts() {
                searchForPorts();
            }
        };
    }
    
    @Override
    public void serialEvent(SerialPortEvent spe) {
        //What to do when data is recieved
          if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            /*
            try
            {
                byte singleData = (byte)input.read();

                if (singleData != NEW_LINE_ASCII)
                {
                    logText = new String(new byte[] {singleData});
                    window.txtLog.append(logText);
                }
                else
                {
                    window.txtLog.append("\n");
                }
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                window.txtLog.setForeground(Color.red);
                window.txtLog.append(logText + "\n");
            }
            */
        }
    }
    
    public void searchForPorts ()
    {
        window.cleanPorts();
        ports= CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements())
        {
            CommPortIdentifier currentPort = (CommPortIdentifier)ports.nextElement();
            if(currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                //TODO actualizar el drop-down menu de la GUI. eso es trabajo de la clase window
                //podria poner un callback
                //window.cboxPorts.addItem(curPort.getName());
                window.addPort(currentPort.getName());
                portMap.put(currentPort.getName(), currentPort);
            }
        }
    }

    public void connect()
    {
        String selectedPort =  window.getSelectedPortName();
        selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
        
        CommPort commPort = null;
        
        try
        {
            //devuelve un objeto CommPort
            commPort = selectedPortIdentifier.open("LED_Driver",TIMEOUT);
            //se puede castear commPort a SerialPort
            serialPort = (SerialPort)commPort;
            setConnected (true);
            //habilitar boton para enviar
            window.setConnected(true);
        }catch (PortInUseException e)
        {
            JOptionPane.showMessageDialog (null,"Port in use.");
             window.setConnected(false);
            /*
            logText = selectedPort + " is in use. (" + e.toString() + ")";

            window.txtLog.setForeground(Color.RED);
            window.txtLog.append(logText + "\n");
            */
        }catch (Exception e)
        {
            Logger.getLogger(SerialComm.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog (null,"Something else happened. Please check if any ports are available and relaunch this app");
            window.setConnected(false);
            System.exit(-1);
            /*
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            window.txtLog.append(logText + "\n");
            window.txtLog.setForeground(Color.RED);
            */
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
           //writeData(0,0);
           
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
            window.setConnected(isPortConnected);
            
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
        comm.window.setVisible(true);
        comm.searchForPorts();
    }
    
}
