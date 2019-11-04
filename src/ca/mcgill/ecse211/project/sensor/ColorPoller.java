package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;
import java.awt.Color;
import ca.mcgill.ecse211.project.game.SensorController;

/**
 * This class contains the color poller thread
 *
 */
public class ColorPoller extends Thread {
  private static final int COLOR_POLLER_PERIOD = 50;
  private float[][] colorData;
  private SampleProvider[] sampleProvider;
  public SensorController sensorCont;
  public boolean running = false;

  /**
   * ColorPoller class constructor
   * @param leftSamp
   * @param leftcolorData
   * @param rightSamp
   * @param rightcolorData
   */
  public ColorPoller(SampleProvider leftSamp, float[] leftcolorData, SampleProvider rightSamp,float[] rightcolorData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.colorData = new float[][] {leftcolorData, rightcolorData};
  }

  /**
   * Method to run the color poller thread
   */
  public void run(){
    long updateStart, updateEnd, sleepPeriod;
    float[] colors = new float[2];
    int[] scaledColors = new int[2];

    while (true) {

      updateStart = System.currentTimeMillis();

      if (running) {
        //left sensor
        sampleProvider[0].fetchSample(colorData[0], 0); 
        float R0 = (colorData[0][0]); 
        float G0 = (colorData[0][1]);
        float B0 = (colorData[0][2]);
        colors[0] = new Color(R0, G0, B0).getRGB(); //range = 0-1.0
        scaledColors[0] = (int) (colors[0] * 100.0); //scale up by 100

        //right sensor
        sampleProvider[1].fetchSample(colorData[0], 0); 
        float R1 = (colorData[1][0]); 
        float G1 = (colorData[1][1]);
        float B1 = (colorData[1][2]);
        colors[1] = new Color(R1, G1, B1).getRGB(); //range = 0-1.0
        scaledColors[1] = (int) (colors[1] * 100.0); //scale up by 100

        //set colors
        sensorCont.setColor(scaledColors);
      }

      updateEnd = System.currentTimeMillis();
      sleepPeriod = COLOR_POLLER_PERIOD - (updateEnd - updateStart);
      try {
        if (sleepPeriod >= 0)
          Thread.sleep(COLOR_POLLER_PERIOD - (updateEnd - updateStart));
      } catch (InterruptedException e) {
        return; 
      }
    }
  }

}