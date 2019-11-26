package ca.mcgill.ecse211.project.sensor;

/**
 * Interface to be implemented by all classes that use the ultrasonic sensor
 * @author Liang Zhao & Jessie Tang
 *
 */
public interface USUser {
  public void processUSData(int distance);
}