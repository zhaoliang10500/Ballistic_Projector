package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import java.util.concurrent.CountDownLatch;

public class Main {
  public static Display display;
  public static USLocalizer usLoc;
  public static LightLocalizer lightLoc;
  public static Navigation nav;
  public static CountDownLatch latch = new CountDownLatch(1);
  public static CountDownLatch latch2 = new CountDownLatch(1);
  
  public static int isTimeForMulThrow = 0;
  
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
      // create threads
      LCD.clear();
      Thread odoThread = new Thread(odometer);
      odoThread.start();
      
      usLoc = new USLocalizer(FALLING, usSampleProvider, usData, latch); 
      Thread usLocalizerThread = new Thread(usLoc);

      usLocalizerThread.start(); 

      // start light localization 
      lightLoc = new LightLocalizer(lsSampleProvider, lsData, latch, latch2);
      Thread lightlocThread = new Thread(lightLoc);
      lightlocThread.start();
      
      nav = new Navigation(latch2);
      Thread navThread = new Thread(nav);
      navThread.start();
      
      if (Button.waitForAnyPress() == Button.ID_ENTER) {
        simpleThrow.doSimpleThrow();   
      }
    }
    
    else if (buttonChoice == Button.ID_RIGHT) {
      simpleThrow.doSimpleThrow();
    }
    
   LCD.drawString("Press to throw again", 0, 5);
    //Do throw another 4 times
    for (int i = 0; i < 4; i++) {
      int buttonpressed;
      do {
        LCD.drawString("Press to Left or Right Button", 0, 6);
        buttonpressed = Button.waitForAnyPress();
      }
      while (buttonpressed != Button.ID_LEFT && buttonpressed != Button.ID_RIGHT); 
        
      simpleThrow.doSimpleThrow();
    }
    
    
    System.exit(0);

  }
    
    
}
  
  
  
  
  
  
  
  
  