package ca.mcgill.ecse211.project.localization;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.ColorUser;


/**
 * This class contains methods for color sensor localization
 */
public class ColorLocalizer implements ColorUser {
  //class variables
  private SampleProvider colorValue;
  private float[] colorData;
  private float lastColor;
  private float currentColor;
  private double lastPosition[] = new double[3];
  private double correction = 0;
  public int isDetectedBlackLineOfXSide = 0;
  public int isDetectedBlackLineOfYSide = 0;

  /**
   * Runs the logic of the light localizer
   */
  public void localize() {
    long correctionStart, correctionEnd;
    colorValue.fetchSample(colorData, 0);
    lastColor = colorData[0];
    
    while (true) {
      if (isDetectedBlackLineOfXSide == 1 && isDetectedBlackLineOfYSide == 1) {
        break;
      }
      correctionStart = System.currentTimeMillis();
      
      colorValue.fetchSample(colorData, 0);
      currentColor = colorData[0];
      lastPosition = odometer.getXYT();
      
      if (Math.abs(lastColor - currentColor)* 100 > 5) {
        //first we will detect the black line of x-axis side, then the y-axis side.
        if (isDetectedBlackLineOfXSide == 1) {
          isDetectedBlackLineOfYSide = 1;
        }
        isDetectedBlackLineOfXSide = 1;
        Sound.beep();
        if (lastPosition[2] > 350 || lastPosition[2] < 10) {
          correction = TILE_SIZE;
          odometer.setX(correction);
        }
        else if (lastPosition[2] > 250 || lastPosition[2] < 290) {
          correction = TILE_SIZE;
          odometer.setY(correction);
        }
        sleepFor(1000);
      }
      
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }   

  
  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // There is nothing to be done here
    }
  }


  @Override
  public void processColorData(int[] color) {
    // TODO Auto-generated method stub
    
  }

}