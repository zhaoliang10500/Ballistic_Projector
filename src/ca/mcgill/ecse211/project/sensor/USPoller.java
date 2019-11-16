package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.project.game.SensorController;
import static ca.mcgill.ecse211.project.game.Resources.USMotor;
import java.util.Arrays;

/**
 * This class contains the ultrasonic poller thread
 *
 */
public class USPoller extends Thread {
  private final int US_POLLER_PERIOD = 50;
  private float[] USData;
  private SampleProvider sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;
  public boolean waving = false;

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
   * Method to run the USPoller thread
   */
  public void run() {
    long updateStart, updateEnd, sleepPeriod;
    int distance;
    int filterSize = 5;
    int[] tempDists = new int[filterSize];

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

        //TODO: this might not work, need to test
        if (waving) {
          USMotor.rotate(130);
          USMotor.rotate(-130);     
        }
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