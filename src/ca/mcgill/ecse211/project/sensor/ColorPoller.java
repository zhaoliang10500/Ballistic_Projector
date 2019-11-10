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
  public ColorPoller(SampleProvider leftSamp, float[] leftcolorData, SampleProvider rightSamp, float[] rightcolorData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.colorData = new float[][] {leftcolorData, rightcolorData};
  }

  /**
   * Method to run the color poller thread
   */
  public void run(){
    long updateStart, updateEnd, sleepPeriod;
    int[] colors = new int[2];

    while (true) {

      updateStart = System.currentTimeMillis();

      if (running) {
        //left sensor
        sampleProvider[0].fetchSample(colorData[0], 0); 
        int R0 = (int)(colorData[0][0]*100000.0); 
        int G0 = (int)(colorData[0][1]*100000.0);
        int B0 = (int)(colorData[0][2]*100000.0);
        colors[0] = (int)Math.sqrt(R0^2 + G0^2 + B0^2);     

        //right sensor
        sampleProvider[1].fetchSample(colorData[1], 0); 
        int R1 = (int)(colorData[1][0]*100000.0); 
        int G1 = (int)(colorData[1][1]*100000.0);
        int B1 = (int)(colorData[1][2]*100000.0);
        colors[1] = (int)Math.sqrt(R1^2 + G1^2 + B1^2);     
        
        System.out.println("color0: " + colors[0]);
        System.out.println("color1: " + colors[1]);
        
        //set colors
        sensorCont.setColor(colors);
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