// This class gemerates multiple images for testing purposes
// including getting the rgb values of a black and white image
/* Bing Li
 * SPH4U0
 * One Bit Camera
 */
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.awt.*;

public class ImageTesting
{
    public static void generate() throws Exception
    {
        // Blank: Generate a blank canvas
        File file = new File("DATA\\Blank.dat");
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		
		for(int col=0; col<3; col++) {
            for(int y=0; y<300; y++) {
                for(int x=0; x<300; x++) {
                    printWriter.println(1023); // Outputs intensity (0 or 1023) for checkerboard
                }
            }
        }
        
        fileWriter.flush();
		fileWriter.close();
        
        // Test 1: 9x9 checkerboard pattern
        file = new File("DATA\\Checkerboard.dat");
		fileWriter = new FileWriter(file);
		printWriter = new PrintWriter(fileWriter);
		
		for(int col=0; col<3; col++) {
            for(int y=0; y<300; y++) {
                for(int x=0; x<300; x++) {
                    boolean white = ((y/30)%2==0 || (x/30)%2==0)&&!((y/30)%2==0 && (x/30)%2==0);
                    printWriter.println(white ? "1023":"0"); // Outputs intensity (0 or 1023) for checkerboard
                }
            }
        }
        
        fileWriter.flush();
		fileWriter.close();
        
        // Test 2: pixellated black and white image
        BufferedImage bi=ImageIO.read(new File("DATA\\Sample.png"));
        
        file = new File("DATA\\Sample.dat");
		fileWriter = new FileWriter(file);
		printWriter = new PrintWriter(fileWriter);
		
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getRed()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getGreen()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getBlue()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        
		fileWriter.flush();
		fileWriter.close();
		
		// Test 3: pixellated colour image
        bi=ImageIO.read(new File("DATA\\SampleColour.png"));
        
        file = new File("DATA\\SampleColour.dat");
		fileWriter = new FileWriter(file);
		printWriter = new PrintWriter(fileWriter);
		
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getRed()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getGreen()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        for (int y=0; y < bi.getHeight(); y++) {
            for (int x=0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y)); // Gets RGB of current pixel
                //System.out.println("("+x+","+y+")\t"+"red=="+c.getRed()+" green=="+c.getGreen()+"    blue=="+c.getBlue());
                printWriter.println((int)((c.getBlue()/255.0)*1023)); // Outputs intensity (0-1023) from RGB (0-255)
            }
        }
        
		fileWriter.flush();
		fileWriter.close();
    }
}
