package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import java.util.concurrent.CountDownLatch;

public class Main {
  public static Display display;
  public static USLocalizer usLoc;
  public static LightLocalizer lightLoc;
  public static CountDownLatch latch = new CountDownLatch(1);
  /**
   * Main entry point - instantiate objects used and set up sensor
   * @param args
   */
  public static void main(String[] args) {
    
    SampleProvider usSampleProvider = US_SENSOR.getMode("Distance");
    float[] usData = new float[usSampleProvider.sampleSize()];
   
    SampleProvider lsSampleProvider = L_SENSOR.getMode("Red");
    float[] lsData = new float[lsSampleProvider.sampleSize()];
    
    SimpleThrow simpleThrow = new SimpleThrow(leftThrowMotor, rightThrowMotor);
    
    int buttonChoice;
    do {
      LCD.clear();
      LCD.drawString("< Left  | Right >", 0, 0);
      LCD.drawString("        |        ", 0, 1);
      LCD.drawString(" Mobile | Fixed  ", 0, 2);
      LCD.drawString(" Launch | Launch ", 0, 3);

      buttonChoice = Button.waitForAnyPress();

    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_ESCAPE);


    if (buttonChoice == Button.ID_LEFT) {
      usLoc = new USLocalizer(FALLING, usSampleProvider, usData, latch); //use rising edge 
      Display display = new Display();
      
      // create threads
      Thread odoThread = new Thread(odometer);
      Thread usLocalizerThread = new Thread(usLoc);
      Thread displayThread = new Thread(display);
      
      // start odometer, display, and ultrasonic localization
      odoThread.start();
      displayThread.start();
      usLocalizerThread.start(); 

      // start light localization 
      lightLoc = new LightLocalizer(lsSampleProvider, lsData, latch);
      Thread lightlocThread = new Thread(lightLoc);
      lightlocThread.start();
      
      /**
       * TO DO: Navigate to ideal coordinates
       */
      /**
       * TO DO: Throw after calculation
       */
    }
    
    else if (buttonChoice == Button.ID_RIGHT) {
      simpleThrow.doSimpleThrow();
    }
    
    else {
      System.exit(0);
    }
    
    while(Button.waitForAnyPress() != Button.ID_ESCAPE) {
      // keep program from ending unless esc is pressed
    }
    System.exit(0);

  }
    
    
}
  
  
  
  
  
  
  
  
  