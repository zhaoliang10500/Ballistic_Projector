package ca.mcgill.ecse211.test2;

import static ca.mcgill.ecse211.test2.Resources.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private float blackThreshold = 83;
  private float colorData[]; // Array to store sensor data
  private SampleProvider sampleProvider;
  
  private boolean onLine; // Boolean to prevent false positives
  private int count; // Number of lines crossed, from 0 to 8
  private double[] xyt;  // Adjusted x,y,theta positions to pass into odometer
  private double xOffset, yOffset; //Offsets from origin
  
  //For calculating x and y Offset by taking (temp1 + temp2) / 2
  double tempXOffset1, tempXOffset2, tempYOffset1, tempYOffset2; 

  public OdometryCorrection() {
    sampleProvider = colorSensor.getMode("Red"); // Set sensor mode
    colorData = new float[colorSensor.sampleSize()]; // Current color data
    count = 0;
    xyt = new double[2];
    onLine = false;
  }

  public void run() {
    long correctionStart, correctionEnd;
    
    while (true) {
      correctionStart = System.currentTimeMillis();
      sampleProvider.fetchSample(colorData, 0);
      colorData[0] *= 1000; // Scale up color intensity
   
      xyt[0] = odometer.getXYT()[0]; // Set x
      xyt[1] = odometer.getXYT()[1]; // Set y 
      
      // Calculate and update odometer with new accurate positions
      if (colorData[0] < blackThreshold) {
        count++;
        Sound.playNote(Sound.FLUTE, 440, 250); 
        onLine = true;
        if (count == 0) {
          xyt[0] = 0; 
          xyt[1] = 0; 
        }
        
        // Set y
        else if (count > 0 && count <= 3) {
          if (count == 1) {
          tempYOffset1 = TILE_SIZE -odometer.getXYT()[1]; //getting bottom of offset
        }
          xyt[1] = count*TILE_SIZE;
        }
        else if (count > 6 && count <= 9) {
          xyt[1] = 3*TILE_SIZE - ((count - 7)*TILE_SIZE);
        }
        
        // Set x 
        else if (count > 3 && count <= 6) {
          if (count == 4) {
            tempXOffset1 = TILE_SIZE - odometer.getXYT()[0]; //getting left side offset
          }
          xyt[0] = (count-3)*TILE_SIZE;
        }
        else if (count > 9 && count <= 12) {
          if (count == 12) {
            tempYOffset2 = odometer.getXYT()[1]; //getting bottom of offset
            tempXOffset2 = odometer.getXYT()[0] - TILE_SIZE; // getting left of offset
          }  
          xyt[0] = 3*TILE_SIZE - ((count -10)*TILE_SIZE);
        }
        else {
          break;
        }
        odometer.setXY(xyt[0], xyt[1]); 
      }
      onLine = false; // Update boolean
      
      if (count == 12) {
        xOffset = (tempXOffset1 + tempXOffset2)/2;
        yOffset = (tempYOffset1 + tempYOffset2)/2;
        xyt[0] = xOffset;
        xyt[1] = yOffset;
      }
      
      odometer.setXY(xyt[0], xyt[1]);

      // This ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }

}
