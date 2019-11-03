package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.project.game.SensorController;

public class ColorPoller extends Thread {
  private static final int COLOR_POLLER_PERIOD = 50;
  private float[][] colorData;
  private SampleProvider[] sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;

  public ColorPoller(SampleProvider leftSamp, float[] leftcolorData, SampleProvider rightSamp,float[] rightcolorData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.colorData = new float[][] {leftcolorData, rightcolorData};
  }

  public void run(){
    long updateStart, updateEnd, sleepPeriod;
    float[] colorRGB = new float[3];

    while (true) {

      updateStart = System.currentTimeMillis();

      sampleProvider[0].fetchSample(colorData[0], 0); 
      colorRGB[0] = (colorData[0][0]); 
      colorRGB[1] = (colorData[0][1]);
      colorRGB[2] = (colorData[0][2]);
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