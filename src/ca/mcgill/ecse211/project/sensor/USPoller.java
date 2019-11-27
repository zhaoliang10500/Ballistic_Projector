package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.project.game.SensorController;
import java.util.Arrays;

/**
 * This class contains the ultrasonic poller thread
 *
 */
public class USPoller extends Thread {
  private final int US_POLLER_PERIOD = 0; //50
  private float[] USData;
  private SampleProvider sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;
  public boolean waving = false;
  private int filterSize = 3;
  private int[] tempDists = new int[filterSize];

  /**
   * USPoller class constructor
   * @param sampleProvider
   * @param USData
   */
  public USPoller(SampleProvider sampleProvider, float[] USData) {
    this.sampleProvider = sampleProvider;
    this.USData = USData;
  }

  /**
   * Method to run the USPoller thread, uses a median filter
   */
  public void run() {
    long updateStart, updateEnd, sleepPeriod;
    int distance;

    while (true) {
      updateStart = System.currentTimeMillis();

      if (running) {
        //median filter
        for (int i = 0; i < filterSize; i++) { 
          sampleProvider.fetchSample(USData, 0); 
          tempDists[i] = (int) (USData[0] * 100.0); 
        }
        
        Arrays.sort(tempDists);
        distance = tempDists[filterSize/2]; //java rounds down for int division
        sensorCont.setDistance(distance);
      }

      updateEnd = System.currentTimeMillis();
      sleepPeriod = US_POLLER_PERIOD - (updateEnd - updateStart);
      try {
        if (sleepPeriod >= 0)
          Thread.sleep(sleepPeriod);
      } catch (InterruptedException e) {
        return; 
      }

    }
  }
}