package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import java.util.Arrays;
import ca.mcgill.ecse211.project.game.SensorController;

/**
 * This class contains the color poller thread
 *
 */
public class LightPoller extends Thread {
  private static final int LIGHT_POLLER_PERIOD = 50;
  private float[][] lightData;
  private SampleProvider[] sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;
  private int filterSize = 5;
  private int[][] tempLights = new int[2][filterSize];

  /**
   * LightPoller class constructor
   * @param leftSamp
   * @param leftcolorData
   * @param rightSamp
   * @param rightcolorData
   */
  public LightPoller(SampleProvider leftSamp, float[] leftLightData, SampleProvider rightSamp, float[] rightLightData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.lightData = new float[][] {leftLightData, rightLightData};
  }

  /**
   * Method to run the color poller thread
   */
  public void run(){
    long updateStart, updateEnd, sleepPeriod;
    int[] lights = new int[2];

    while (true) {

      updateStart = System.currentTimeMillis();

      if (running) {
        //median filter
        for (int i = 0; i<filterSize; i++) {
          //left sensor
          sampleProvider[0].fetchSample(lightData[0], 0); 
          tempLights[0][i] = (int)(lightData[0][0]*1000.0); 
  
          //right sensor
          sampleProvider[1].fetchSample(lightData[1], 0); 
          tempLights[1][i] = (int)(lightData[1][0]*1000.0);   
        }
        
        Arrays.sort(tempLights[0]);
        Arrays.sort(tempLights[1]);
        lights[0] = tempLights[0][filterSize/2]; //java rounds down for int division
        lights[1] = tempLights[1][filterSize/2];
        
        //set lights
        sensorCont.setLight(lights);
        
      }

      updateEnd = System.currentTimeMillis();
      sleepPeriod = LIGHT_POLLER_PERIOD - (updateEnd - updateStart);
      try {
        if (sleepPeriod >= 0)
          Thread.sleep(LIGHT_POLLER_PERIOD - (updateEnd - updateStart));
      } catch (InterruptedException e) {
        return; 
      }
    }
  }

}