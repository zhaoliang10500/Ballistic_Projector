package ca.mcgill.ecse211.project;
import java.text.DecimalFormat;
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * This class is used to display the content of the odometer variables (x, y, Theta) and distance
 */
public class Display extends Thread {

  private final long DISPLAY_PERIOD = 25;
  private long timeout = Long.MAX_VALUE;
  
  private Odometer odometer;
  private USLocalizer usLocalizer;
  
  public Display (Odometer odometer, USLocalizer usLocalizer) {
    this.odometer = odometer;
    this.usLocalizer = usLocalizer;
  }

  public void run() {
    
    LCD.clear();
    
    long updateStart, updateEnd;

    long tStart = System.currentTimeMillis();
    do {
      updateStart = System.currentTimeMillis();
      
      // Print x,y, and theta information
      DecimalFormat numberFormat = new DecimalFormat("######0.00");
      LCD.drawString("X: " + numberFormat.format(odometer.getXYT()[0]), 0, 0);
      LCD.drawString("Y: " + numberFormat.format(odometer.getXYT()[1]), 0, 1);
      LCD.drawString("T: " + numberFormat.format(odometer.getXYT()[2]), 0, 2);
      LCD.drawString("Distance: " + usLocalizer.filter(), 0, 3);
      // this ensures that the data is updated only once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < DISPLAY_PERIOD) {
        try {
          Thread.sleep(DISPLAY_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while ((updateEnd - tStart) <= timeout);

  }
  
  /**
   * Sets the timeout in ms.
   * 
   * @param timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  /**
   * Shows the text on the LCD, line by line.
   * 
   * @param strings comma-separated list of strings, one per line
   */
  public static void showText(String... strings) {
    LCD.clear();
    for (int i = 0; i < strings.length; i++) {
      LCD.drawString(strings[i], 0, i);
    }
  }

}
