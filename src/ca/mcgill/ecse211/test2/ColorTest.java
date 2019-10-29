package ca.mcgill.ecse211.test2;
import static ca.mcgill.ecse211.test2.Resources.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

//adjust motor forward speed in resources
//get accuracy % (number of lines detected / actual number of lines) compared to speed

public class ColorTest extends Thread{
  private SampleProvider sampleProvider;
  private float[] colorData;
  private float lastColor, currentColor;
  private int lineCount;
  
  public ColorTest() {
    sampleProvider = colorSensor.getMode("RGB"); // Set sensor mode
    lineCount = 0;
  }

  public void run() {
    sampleProvider.fetchSample(colorData, 0);
    lastColor = colorData[0];
    while (true) {
      sampleProvider.fetchSample(colorData, 0);
      currentColor = colorData[0];

      if (Math.abs(lastColor - currentColor)* 100 > 5) {
        Sound.beep();
        lineCount++;
      }
    }
  }
}
