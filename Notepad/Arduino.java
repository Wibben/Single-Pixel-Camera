// This class allows for communication to the Arduino
// through the serial ports
/* Bing Li
 * SPH4U0
 * One Bit Camera
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.*; // RXTX Arduino communication
import java.util.Enumeration;
import java.io.*; // File IO (Saving and Loading)

public class Arduino implements SerialPortEventListener {
    SerialPort serialPort;
    /** The port we're normally going to use. */
    private static final String PORT_NAMES[] = { 
            "/dev/tty.usbserial-A9007UX1", // Mac OS X
            "/dev/ttyACM0", // Raspberry Pi
            "/dev/ttyUSB0", // Linux
            "COM3", // Windows
            "COM4", // Windows
            "COM13", // Windows
            "COM9" // Windows
        };
    /**
     * A BufferedReader which will be fed by a InputStreamReader 
     * converting the bytes into characters 
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 115200;
    
    private FileWriter fileWriter;
    private PrintWriter printWriter;

    public Arduino() {
        // Initialize values
        try{
            fileWriter = new FileWriter(new File("DATA\\NOTEPAD.dat"));
            printWriter = new PrintWriter(fileWriter);
        } catch (Exception e) {System.err.print(e.toString());}
        
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    System.out.println("Port: "+portId);
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(
                DATA_RATE, // 9600 baud
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine=input.readLine();
                
                // Write to the file one value per line
                printWriter.println(inputLine);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
    
    // Write to arduino
    public void write(String data)
    {
        // To turn on the Arduino send in 'y'
        try {
            output.write(data.getBytes());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static void main(String args[]) throws Exception 
    {
        // Create and initialize the Arduino communicator
        Arduino m = new Arduino();
        
        m.write("y");
        
        Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Started");
    }
    
    public void closeFile()
    {
        // Close the file
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {System.err.print(e.toString());}
    }
}