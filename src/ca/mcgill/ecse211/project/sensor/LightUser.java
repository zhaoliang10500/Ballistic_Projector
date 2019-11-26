package ca.mcgill.ecse211.project.sensor;

/**
 * Interface to be implemented by all classes that use the light sensor(s)
 * @author Liang Zhao & Jessie Tang
 */
public interface LightUser {
  
  public void processLightData(int[] light);
  
}