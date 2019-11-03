package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.project.game.SensorController;

public class USPoller extends Thread {
  private final int US_POLLER_PERIOD = 50;
  private float[] USData;
  private SampleProvider sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;
  
  public USPoller(SampleProvider sampleProvider, float[] USData) {
    this.sampleProvider = sampleProvider;
    this.USData = USData;
  }
  
  public void run() {
    long updateStart, updateEnd, sleepPeriod;
    int distance;
    
    while (true) {
      updateStart = System.currentTimeMillis();
      
      if (running) {
          sampleProvider.fetchSample(USData, 0); 
          distance = (int) (USData[0] * 100.0); 
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