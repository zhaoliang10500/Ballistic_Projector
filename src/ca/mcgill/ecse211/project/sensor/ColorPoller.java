package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.project.game.SensorController;

public class ColorPoller extends Thread {
  private static final int COLOR_POLLER_PERIOD = 50;
  private float[][] data;
  private SampleProvider[] sampleProvider;
  public SensorController sensorCont;
  public boolean running;

  public ColorPoller(SampleProvider leftSamp, float[] leftData, SampleProvider rightSamp,float[] rightData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.data = new float[][] {leftData, rightData};
  }

  public void run(){
    long updateStart, updateEnd, sleepPeriod;
    float[] colorRGB = new float[3];

    while (true) {

      updateStart = System.currentTimeMillis();

      sampleProvider[0].fetchSample(data[0], 0); 
      colorRGB[0] = (data[0][0]); 
      colorRGB[1] = (data[0][1]);
      colorRGB[2] = (data[0][2]);
      sensorCont.setColor(colorRGB);

      updateEnd = System.currentTimeMillis();
      sleepPeriod = COLOR_POLLER_PERIOD - (updateEnd - updateStart);
      try {
        if (sleepPeriod >= 0)
          Thread.sleep(COLOR_POLLER_PERIOD - (updateEnd - updateStart));
      } catch (InterruptedException e) {
        return; // end thread
      }
    }
  }

}