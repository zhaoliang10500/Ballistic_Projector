package ca.mcgill.ecse211.project.sensor;

import lejos.robotics.SampleProvider;

/**
 * Interface to be implemented by all classes that use the color sensor(s)
 *
 */
public interface ColorUser {
  
  public void processColorData(int[] color);
  
}