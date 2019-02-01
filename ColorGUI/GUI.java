// This class is the entire GUI for the one bit camera
/* Bing Li
 * SPH4U0
 * One Bit Camera
 */
import java.awt.*;
import java.io.*; // File IO (Saving and Loading)
import javax.swing.*;
import javax.swing.filechooser.*; // File Chooser
import java.awt.geom.*; // Rotating text
import java.awt.image.*; // BufferedImage
import java.awt.event.*;  // Needed for ActionListener
import javax.swing.event.*;  // Needed for ActionListener
import javax.swing.text.*;
import javax.swing.border.*;
import javax.imageio.*; // Allows exporting to a png

class GUI extends JFrame
{
    private static final double RED_WEIGHT=0.2989,BLUE_WEIGHT=0.5870,GREEN_WEIGHT=0.1141; // For rgb to greyscale conversion
    private static final int RED=0,GREEN=1,BLUE=2;
    
    private JComboBox mode = new JComboBox(new String[]{"All","Red Only","Green Only","Blue Only"});
    private JButton scanBtn,saveBtn,loadBtn,exportBtn,configBtn;
    private JLabel contourStep,contourMode;
    private JSpinner steps;
    private int mouseX,mouseY;
    
    private Arduino main; // Arduino connection
    
    private int scannedImage[][][] = new int[3][300][300]; // Supposedly scanned image
    
    // UI components
    private DrawArea board;
    private JPanel currentIntensity;
    private Contour contour;
    private Histogram histogram;
    private JLabel cameraXpos,cameraYpos,cameraIntensity,cameraLux,mouseXpos,mouseYpos,mouseIntensity,mouseLux;

    public GUI(Arduino ard)
    {
        // Initialize components
        load(new File("DATA\\Blank.dat"));
        main = ard;
        BtnListener btnListener = new BtnListener(); // listener for all buttons
        mouseX = mouseY = 0;
        
        scanBtn = new JButton("Start Scan");
        scanBtn.addActionListener(btnListener);
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(btnListener);
        loadBtn = new JButton("Load");
        loadBtn.addActionListener(btnListener);
        exportBtn = new JButton("Export");
        exportBtn.addActionListener(btnListener);
        configBtn = new JButton("Calibrate");
        configBtn.addActionListener(btnListener);

        // Create content pane, set layout
        JPanel content = new JPanel();        // Create a content pane
        content.setLayout(new BorderLayout(5,0)); // Use BorderLayout for panel
        JPanel north = new JPanel();
        north.setLayout(new FlowLayout()); // Use FlowLayout for input area
        JPanel east = new JPanel();
        east.setLayout(new BorderLayout(0,5)); // Use BorderLayout for easy organization
        contourStep = new JLabel("Contour Step:");
        contourMode = new JLabel("Contour Mode:");
        steps = new JSpinner();
        steps.setModel(new SpinnerNumberModel(100,1,1023,20)); // Initial, min, max, step
        // Makes user conform to format
        JFormattedTextField txt = ((JSpinner.NumberEditor) steps.getEditor()).getTextField();
        ((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);
        
        JPanel currentIntensity = new JPanel(); // Current Intensity
        currentIntensity.setLayout(new GridLayout(5,2)); // Grid of 4 rows and 2 columns
        cameraXpos = new JLabel(" X: -");
        cameraYpos = new JLabel(" Y: -");
        cameraIntensity = new JLabel(" Intensity: -");
        cameraLux = new JLabel(" Lux: -");
        mouseXpos = new JLabel("X: -");
        mouseYpos = new JLabel("Y: -");
        mouseIntensity = new JLabel("Intensity: -");
        mouseLux = new JLabel("Lux: -");
        // Add in the components to the current intensity
        currentIntensity.add(new JLabel(" Camera"));
        currentIntensity.add(new JLabel("Mouse"));
        currentIntensity.add(cameraXpos);
        currentIntensity.add(mouseXpos);
        currentIntensity.add(cameraYpos);
        currentIntensity.add(mouseYpos);
        currentIntensity.add(cameraIntensity);
        currentIntensity.add(mouseIntensity);
        currentIntensity.add(cameraLux);
        currentIntensity.add(mouseLux);
        currentIntensity.setPreferredSize(new Dimension(302,68));
        currentIntensity.setBorder(BorderFactory.createLineBorder(new Color(100,100,100)));

        board = new DrawArea(602,602); // Main image
        contour = new Contour(302,314); // Contour plot
        histogram = new Histogram(302,210); // Histogram
         
         // Mouse over event: update intensity
        board.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {} // Not needed
            public void mouseMoved(MouseEvent e) {
                if(e.getX()>0 && e.getX()<601 && e.getY()>0 && e.getY()<601) {
                    mouseX = (e.getX()-1)/2; mouseY = (e.getY()-1)/2; // Movement
                    
                    // Visual updates
                    mouseXpos.setText("X: "+mouseX);
                    mouseYpos.setText("Y: "+mouseY);
                    mouseIntensity.setText("Intensity: (" + scannedImage[RED][mouseY][mouseX] + "," +
                                                            scannedImage[GREEN][mouseY][mouseX] + "," +
                                                            scannedImage[BLUE][mouseY][mouseX] + ")");
                    mouseLux.setText("Lux: "+(int)(0.343252*grey(mouseX,mouseY)));
                }
            }
        });
        
        contour.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {} // Not needed
            public void mouseMoved(MouseEvent e) {
                if(e.getX()>0 && e.getX()<301 && e.getY()>12 && e.getY()<313) {
                    mouseX = (e.getX()-1); mouseY = (e.getY()-13); // Movement
                    
                    // Tool tip updates
                    int currentContourStep = (Integer)steps.getValue();
                    String currentContourMode = (String)mode.getSelectedItem();
                    int intensity=0;
                    
                    if(currentContourMode.equals("All")) intensity = grey(mouseX,mouseY);
                    else if(currentContourMode.equals("Red Only")) intensity = scannedImage[RED][mouseY][mouseX];
                    else if(currentContourMode.equals("Green Only")) intensity = scannedImage[GREEN][mouseY][mouseX];
                    else if(currentContourMode.equals("Blue Only")) intensity = scannedImage[BLUE][mouseY][mouseX];
                    
                    contour.setToolTipText("<html>Blue indicates low intensity while orange indicate high intensity<br>" + 
                                           "X: " + mouseX + "  Y: " + mouseY + 
                                           "<br>Current step: " + intensity/currentContourStep*currentContourStep);
                }
            }
        });
        
        // Update UI when inputs updated
        steps.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                contour.updateAll();
            }
        });
        mode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                contour.updateAll();
            }
        });
        
        // Tool tips
        scanBtn.setToolTipText("Start scanning");
        configBtn.setToolTipText("Calibration tool");
        saveBtn.setToolTipText("Save the current image");
        loadBtn.setToolTipText("Load a saved image");
        steps.setToolTipText("Change contour step (0-1023)");
        mode.setToolTipText("Select a contour mode");
        exportBtn.setToolTipText("Exports the image, contour plot, and histogram to a folder of your choice");
        mouseIntensity.setToolTipText("(R,G,B)");

        // Add components to content area
        north.add(scanBtn);
        north.add(configBtn);
        north.add(saveBtn);
        north.add(loadBtn);
        north.add(contourStep);
        north.add(steps);
        north.add(contourMode);
        north.add(mode);
        north.add(exportBtn);
        east.add(currentIntensity,"North");
        east.add(contour,"Center");
        east.add(histogram,"South");
        
        // Input area
        content.add(north,"North");
        // Output areas
        content.add(board,"Center");
        content.add(east,"East");
        content.setBorder(BorderFactory.createEmptyBorder(0,5,5,5)); // Add a 5 pixel margin on left, right, and bottom of window

        // Set window attributes
        setContentPane(content);
        pack();
        setTitle("Single Pixel Camera");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);           // Center window.
    }
    
    class BtnListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getActionCommand().equals("Start Scan")) {
                scanBtn.setText("Stop"); // Set the text to read Stop
                
                // Load up a blank canvas
                load(new File("DATA\\Blank.dat"));
                 // Update everything
                board.updateAll();
                contour.updateAll();
                repaint();
                
                // Do not let the user do any saving or lading until program has completed
                saveBtn.setEnabled(false);
                scanBtn.setToolTipText("Stop scanning, other options unavailable while scanning");
                configBtn.setEnabled(false);
                loadBtn.setEnabled(false);
                contourStep.setEnabled(false);
                steps.setEnabled(false);
                contourMode.setEnabled(false);
                mode.setEnabled(false);
                exportBtn.setEnabled(false);
                
                // Write to arduino to turn it on
                main.resetPosition();
                main.write("y");
            } else if(e.getActionCommand().equals("Stop")) {
                scanBtn.setText("Start Scan"); // Set the text to read Start Scan
                
                // Re-enable everything
                saveBtn.setEnabled(true);
                scanBtn.setToolTipText("Start scanning");
                configBtn.setEnabled(true);
                loadBtn.setEnabled(true);
                contourStep.setEnabled(true);
                steps.setEnabled(true);
                contourMode.setEnabled(true);
                mode.setEnabled(true);
                exportBtn.setEnabled(true);
                
                // Write to Arduino to turn it off
                main.write("n");
            } else if(e.getActionCommand().equals("Save")) {
                save();
            } else if(e.getActionCommand().equals("Load")) {
                load();
                // Refresh display
                board.updateAll();
                contour.updateAll();
                repaint();
            } else if(e.getActionCommand().equals("Export")) {
                export();
            } else if(e.getActionCommand().equals("Contrast")) {
                process();
                
                 // Update everything
                board.updateAll();
                contour.updateAll();
                repaint();
            } else if(e.getActionCommand().equals("Calibrate")) {
                JFrame f = new JFrame();
                
                JOptionPane.showMessageDialog(f, "Please do the physical calibration as \ndetailed in the operation manual.\n\nPress OK when complete.","Device Calibration",JOptionPane.INFORMATION_MESSAGE);
                
                JOptionPane.showMessageDialog(f, "Please place the dark cloth over the sensor \nensuring no light is able to reach the sensor.\n\nPress OK when complete.","Device Calibration",JOptionPane.INFORMATION_MESSAGE);
                
                JOptionPane.showMessageDialog(f, "Thank you.\n\nPlease place the laser calibrator over \nthe lens and activate the laser.\n\nPress OK when complete.","Device Calibration",JOptionPane.INFORMATION_MESSAGE);
                
                JOptionPane.showMessageDialog(f, "Thank you.\n\nCalibration complete.","Device Calibration",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    // Convert RGB to greyscale
    public int grey(int x, int y) 
    {
        return (int)(scannedImage[RED][y][x]*RED_WEIGHT+scannedImage[GREEN][y][x]*GREEN_WEIGHT+scannedImage[BLUE][y][x]*BLUE_WEIGHT);
    }
    
    // Programmatically click the scan button, used from Arduino class
    public void clickScan()
    {
        if(scanBtn.getText().equals("Stop")) scanBtn.doClick();
    }
    
    // Save the image
    private void save()
    {
        // Save by writing to file
        try {
            // Use file chooser for better UI
            JFileChooser fc = new JFileChooser();
            // Set Default directory and file filters
            fc.setCurrentDirectory(new File("DATA\\"));
            fc.setFileFilter(new FileNameExtensionFilter("Data Files", "dat"));
            fc.setAcceptAllFileFilterUsed(false); // Do not let user read in other file types
            
            int returnVal = fc.showSaveDialog(null);
            
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                // Make sure to add on the extension if it;s not there so it can load
                String filePath = file.getAbsolutePath();
                if(!filePath.endsWith(".dat")) {
                    file = new File(filePath + ".dat");
                }
                
                FileWriter fileWriter = new FileWriter(file);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                
                // Write to the file one value per line
                for(int i=0; i<3; i++)
                    for(int j=0; j<300; j++)
                        for(int k=0; k<300; k++)
                            printWriter.println(scannedImage[i][j][k]);
                
                // Close the file
                fileWriter.flush();
                fileWriter.close();
            }
        } catch(Exception e) { // If somehow unable to save show message
            JOptionPane.showMessageDialog(null, "Unable To Save To File","Error:",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Load by reading from file
    private void load()
    {
        // Use file chooser for better UI
        JFileChooser fc = new JFileChooser();
        // Set Default directory and file filters
        fc.setCurrentDirectory(new File("DATA\\"));
        fc.setFileFilter(new FileNameExtensionFilter("Data Files", "dat"));
        fc.setAcceptAllFileFilterUsed(false); // Do not let user read in other file types
            
        int returnVal = fc.showOpenDialog(null);
            
        if(returnVal == JFileChooser.APPROVE_OPTION) load(fc.getSelectedFile());
    }
    
    private void load(File file)
    {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
        
            // Read from the file
            for(int i=0; i<3; i++)
                for(int j=0; j<300; j++)
                    for(int k=0; k<300; k++)
                        scannedImage[i][j][k] = Integer.parseInt(bufferedReader.readLine());
                            
            fileReader.close(); // close the file
        } catch(Exception e) { // If it does not exist, output error message in dialog box
            JOptionPane.showMessageDialog(null, "File Does Not Exist","Error:",JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exports the current image to a png
    public void export()
    {
        // Save by writing to file
        try {
            // Use file chooser for better UI
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // User can only select a directory
            fc.setCurrentDirectory(new File("EXPORT\\")); // Set default directory
            fc.setAcceptAllFileFilterUsed(false); // Do not let user read in other file types
            
            int returnVal = fc.showSaveDialog(null);
            
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                String filePath = file.getAbsolutePath();
                
                // Create directory if non-existent
                if(!file.exists()) {
                    file.mkdir();
                }
                
                // Create images
                file = new File(filePath + "\\Image.png");
                ImageIO.write(board.getImg(), "png", file);
                
                file = new File(filePath + "\\Contour Plot.png");
                ImageIO.write(contour.getImg(), "png", file);
                
                file = new File(filePath + "\\Histogram.png");
                ImageIO.write(histogram.getImg(), "png", file);
            }
        } catch(Exception e) { // If somehow unable to save show message
            JOptionPane.showMessageDialog(null, "Unable To Save To File","Error:",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Sends in an intensity from the arduino and displays
    public void sendIntensity(int col, int x, int y, int intensity)
    {
        if(x<300 && y<300) {
            scannedImage[col][y][x] = intensity;
            
            if(col==RED) scannedImage[GREEN][y][x] = scannedImage[BLUE][y][x] = 0;
            
            // Update additional information
            cameraXpos.setText(" X: "+x);
            cameraYpos.setText(" Y: "+y);
            cameraIntensity.setText(" Intensity: "+intensity);
            cameraLux.setText(" Lux: "+(int)(0.343252*intensity));
            
            board.updatePixel(x,y);
            
            int currentContourStep = (Integer)steps.getValue();
            contour.updatePixel(x,y,intensity,currentContourStep);
            
            repaint();
        }
    }
    
    // Sends in an intensity from the arduino and displays (greyscale only)
    public void sendAllIntensity(int x, int y, int intensity)
    {
        scannedImage[RED][y][x] = intensity;
        scannedImage[GREEN][y][x] = intensity;
        scannedImage[BLUE][y][x] = intensity;
        
        // Update additional information
        cameraXpos.setText(" X: "+x);
        cameraYpos.setText(" Y: "+y);
        cameraIntensity.setText(" Intensity: "+intensity);
        cameraLux.setText(" Lux: "+(int)(0.343252*intensity));
        
        board.updatePixel(x,y);
        
        int currentContourStep = (Integer)steps.getValue();
        contour.updatePixel(x,y,intensity,currentContourStep);
        
        repaint();
    }
    
    // Processes the image to make the contrast greater
    public void process()
    {
        for(int i=0; i<3; i++) {
            int min = 1023;
            int max = 0;
            for(int j=0; j<300; j++) {
                for(int k=0; k<300; k++) {
                    if(scannedImage[i][j][k]!=1023) {
                        max = Math.max(max,scannedImage[i][j][k]);
                        min = Math.min(min,scannedImage[i][j][k]);
                    }
                }
            }
            
            if(max!=min) {
                for(int j=0; j<300; j++) {
                    for(int k=0; k<300; k++) {
                        if(scannedImage[i][j][k]!=1023) scannedImage[i][j][k] = (int)((1.0*scannedImage[i][j][k]-min)/(max-min)*1023.00);
                    }
                }
            }
        }
    }
    
    // Main image that is being scanned
    // For 300 x 300 image, each "pixel" is a 2x2 rectangle
    class DrawArea extends JPanel
    {
        private BufferedImage img = new BufferedImage(602 ,602, BufferedImage.TYPE_INT_ARGB);
        private Graphics2D g2d = img.createGraphics();
        
        public DrawArea(int width, int height)
        {
            this.setPreferredSize(new Dimension(width,height)); // size
            updateAll();
        }
        
        public void updatePixel(int x, int y) 
        {
            // Colour in a pixel
            int r = (int)((scannedImage[RED][y][x]/1023.0)*255);
            int g = (int)((scannedImage[GREEN][y][x]/1023.0)*255);
            int b = (int)((scannedImage[BLUE][y][x]/1023.0)*255);
            
            g2d.setColor(new Color(r,g,b));
            g2d.fillRect(1+2*x,1+2*y,2,2);
            
            repaint();
        }
        
        public void updateAll()
        {
            // Border
            g2d.setColor(new Color(100,100,100));
            g2d.drawRect(0,0,601,601);
            
            // Show the current image
            for(int i=0; i<300; i++) { // Row: y-pos
                for(int j=0; j<300; j++) { // Col: x-pos
                    updatePixel(j,i);
                }
            }
            
            repaint();
        }

        public void paintComponent(Graphics g)
        {
            g.drawImage(img,0,0,null);
        }
        
        public BufferedImage getImg()
        {
            return img;
        }
    }
    
    // Contour plot based on image intensities
    class Contour extends JPanel
    {
        private BufferedImage img = new BufferedImage(302 ,326, BufferedImage.TYPE_INT_ARGB);
        private Graphics2D g2d = img.createGraphics();
        
        public Contour(int width, int height)
        {
            this.setPreferredSize(new Dimension(width,height)); // size
            updateAll();
        }
        
        public void updatePixel(int x, int y, int intensity, int currentContourStep) 
        {
            int r = (int)((intensity/currentContourStep*currentContourStep)/1023.0*255.0);
            int g = (int)((intensity/currentContourStep*currentContourStep)/1023.0*255.0*0.5);
            int b = 255-(int)((intensity/currentContourStep*currentContourStep)/1023.0*255.0);
            
            g2d.setColor(new Color(r,g,b));
                    
            g2d.fillRect(1+x,13+y,1,1);
            
            repaint();
        }
        
        public void updateAll()
        {
            FontMetrics fontMetrics = g2d.getFontMetrics(); // Needed for right and center alignment
            
            // White Background
            g2d.setColor(new Color(255,255,255));
            g2d.fillRect(0,0,302,326);
            // Title
            g2d.setColor(new Color(0,0,0));
            g2d.drawString("Contour Plot (Mouse over for details)",151-fontMetrics.stringWidth("Contour Plot (Mouse over for details)")/2,11); // Centered text
            // Border
            g2d.setColor(new Color(100,100,100));
            g2d.drawRect(0,0,301,313);
            
            int currentContourStep = (Integer)steps.getValue();
            String currentContourMode = (String)mode.getSelectedItem();
            
            // Show the current image, if difference between point and surrounding points is according to filter, draw a pixel
            for(int i=0; i<300; i++) { // Row: y-pos
                for(int j=0; j<300; j++) { // Col: x-pos
                    /*
                    // The following draws contours as lines
                    // Check surrounding pixels, only have to check 3 otherwise there would be double lines
                    // Checks right, bottom, and bottom right
                    if((i+1<300 && scannedImage[i][j]/currentContourStep!=scannedImage[i+1][j]/currentContourStep) ||
                       (i+1<300 && j+1<300 && scannedImage[i][j]/currentContourStep!=scannedImage[i+1][j+1]/currentContourStep) ||
                       (j+1<300 && scannedImage[i][j]/currentContourStep!=scannedImage[i][j+1]/currentContourStep)
                       ) g.setColor(new Color(0,0,0));
                    else g.setColor(new Color(255,255,255));
                    */
                    
                    // Draw contour as colours
                    if(currentContourMode.equals("All")) updatePixel(j,i,grey(j,i),currentContourStep);
                    else if(currentContourMode.equals("Red Only")) updatePixel(j,i,scannedImage[RED][i][j],currentContourStep);
                    else if(currentContourMode.equals("Green Only")) updatePixel(j,i,scannedImage[GREEN][i][j],currentContourStep);
                    else if(currentContourMode.equals("Blue Only")) updatePixel(j,i,scannedImage[BLUE][i][j],currentContourStep);
                }
            }
            
            // Will only show up on exported images
            g2d.setColor(new Color(0,0,0));
            g2d.drawString("Contour Step: "+currentContourStep,2,325); // Left Align
            g2d.drawString("Color: "+currentContourMode,300-fontMetrics.stringWidth("Color: "+currentContourMode),325); // Right Align
            
            repaint();
        }

        public void paintComponent(Graphics g)
        {
            g.drawImage(img,0,0,null);
        }
        
        public BufferedImage getImg()
        {
            return img;
        }
    }
    
    // Histogram, Class interval: 64 (16 intervals)
    class Histogram extends JPanel
    {
        private BufferedImage img = new BufferedImage(302, 210, BufferedImage.TYPE_INT_ARGB);
        private Graphics2D g2d = img.createGraphics();
        private FontMetrics fontMetrics = g2d.getFontMetrics(); // Needed for right and center alignment
        
        public Histogram(int width, int height)
        {
            this.setPreferredSize(new Dimension(width,height)); // size
        }

        public void paintComponent(Graphics g)
        {
            Font font = new Font(null, Font.PLAIN, 12);
            AffineTransform affineTransform = new AffineTransform();
            
            // White Background
            g2d.setColor(new Color(255,255,255));
            g2d.fillRect(0,0,302,210);
            // Title
            g2d.setColor(new Color(0,0,0));
            g2d.drawString("Histogram",151-fontMetrics.stringWidth("Histogram")/2,12); // Centered text
            // Border
            g2d.setColor(new Color(100,100,100));
            g2d.drawRect(0,0,301,209);
            
            int counter[] = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // Count for each range
            int max = 0; // Used to dynamically scale vertical axis, highest should be 145 pixels
            for(int i=0; i<300; i++) {
                for(int j=0; j<300; j++) {
                    max = Math.max(max,++counter[grey(j,i)/64]);
                }
            }
            
            // Draw axes ticks
            // Make font smaller
            font = new Font(null, Font.PLAIN, 10);
            g2d.setFont(font);
            
            for(int i=0; i<=10; i++) { // 10 ticks on the vertical axis
                g2d.setColor(new Color(0,0,0));
                int vtick = (int)((i*max/10.0)/max*145); // Distance of current tick from first tick
                g2d.drawLine(59,165-vtick,62,165-vtick);
                g2d.drawString(""+i*max/10,60-fontMetrics.stringWidth(""+i*max/10),170-vtick); // Right aligned string
                // Horizontal grey lines from vertical axis
                g2d.setColor(new Color(210,210,210));
                g2d.drawLine(62,165-vtick,286,165-vtick);
            }
            
            // Rotate text for horizontal axis
            affineTransform.rotate(Math.toRadians(-90), 0, 0);
            Font rotatedFont = font.deriveFont(affineTransform);
            g2d.setFont(rotatedFont);
            
            for(int i=0; i<=16; i++) { // 16 ticks on the horizontal axis
                g2d.setColor(new Color(0,0,0));
                g2d.drawLine(62+i*14,165,62+i*14,168);
                g2d.drawString(""+i*64,66+i*14,168+fontMetrics.stringWidth(""+i*64)); // Right aligned string
            }
            
            // Undo rotation after completion
            font = new Font(null, Font.PLAIN, 12);
            affineTransform.rotate(Math.toRadians(90), 0, 0);
            rotatedFont = font.deriveFont(affineTransform);
            g2d.setFont(rotatedFont);
            
            // Draw bars alternating in red and blue, start with red
            for(int i=0; i<counter.length; i++) {
                g2d.setColor(i%2==0 ? Color.RED:Color.BLUE); // Alternate colours
                int height = (int)((1.0*counter[i]/max)*145); // Height of bar
                g2d.fillRect(62+i*14,165-height,14,height);
            }
            
            // Draw axes
            g2d.setColor(new Color(0,0,0));
            g2d.drawLine(62,165,286,165); // Horizontal axis
            g2d.drawLine(62,20,62,165); // Vertical axis
            
            // Label Horizaxes
            g2d.drawString("Intensity Range",151-fontMetrics.stringWidth("Intensity Range")/2,203); // Centered horizontal axis label
            
            // Rotate
            affineTransform.rotate(Math.toRadians(-90), 0, 0);
            rotatedFont = font.deriveFont(affineTransform);
            g2d.setFont(rotatedFont);
            g2d.drawString("Number of Pixels",16,105+fontMetrics.stringWidth("Number of Pixels")/2); // Centered vertical axis label
            affineTransform.rotate(Math.toRadians(90), 0, 0);
            rotatedFont = font.deriveFont(affineTransform);
            g2d.setFont(rotatedFont);
            
            g.drawImage(img,0,0,null);
        }
        
        public BufferedImage getImg()
        {
            return img;
        }
    }
}
