package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import ca.mcgill.ecse211.project.sensor.*;

/**
 * This class contains methods that controls the ultrasonic and color sensors
 *
 */
public class SensorController {
  private static SensorController sensorCont = null;
  private ColorPoller colorPoll;
  private USPoller usPoll;
  
  // array lists to hold the current US and color users, should be two each
  private ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
  private ArrayList<ColorUser> currColorUsers = new ArrayList<ColorUser>();
  
  /**
   * Private SensorController constructor
   * @param usPoll
   * @param colorPoll
   */
  private SensorController(USPoller usPoll, ColorPoller colorPoll) {
    this.usPoll = usPoll;
    this.colorPoll = colorPoll;
    this.usPoll.sensorCont = this;
    this.colorPoll.sensorCont = this;
  }
  
  /**
   * Synchronized method used to only allow the next sensor thread to enter after the previous has completed
   * This prevents problems such as the game actions beginning before the localization completes
   * @param usPoll
   * @param colorPoll
   * @return SensorController object
   */
  public synchronized static SensorController getSensorController(USPoller usPoll, ColorPoller colorPoll) {
    if (sensorCont != null) {
      return sensorCont;
    }
    else {
      sensorCont = new SensorController(usPoll, colorPoll);
      return sensorCont;
    }
  }
  
  /**
   * Pause US Poller
   */
  public void pauseUSPoller() {
    usPoll.running = false;
  }
  
  /**
   * Resume US Poller
   */
  public void resumeUSPoller() {
    usPoll.running = true;
  }
  
  /**
   * rotates US sensor motor
   */
  public void pauseUSMotor() {
    usPoll.waving = false;
  }
  
  /**
   * Stops US sensor motor
   */
  public void resumeUSMotor() {
    usPoll.waving = true;
  }
  
  /**
   * Pause Color Poller
   */
  public void pauseColorPoller() {
    colorPoll.running = false;
  }
  
  /**
   * Resume Color Poller
   */
  public void resumeColorPoller() {
    colorPoll.running = true;
  }
  
  
  /**
   * Add a USUser to the currUSUsers list
   * @param currUSUsers
   */
  public void setCurrUSUsers (ArrayList<USUser> currUSUsers) {
    this.currUSUsers = currUSUsers;
  }
  
  /**
   * Add a colorUser to the currColorUsers list
   * @param currColorUsers
   */
  public void setCurrColorUsers (ArrayList<ColorUser> currColorUsers) {
    this.currColorUsers = currColorUsers;
  }
  
  /**
   * Set distance for all ultrasonic sensor users
   * @param distance
   */
  public void setDistance (int distance) {
    for(USUser user : currUSUsers) {
      user.processUSData(distance);
    }
  }
  
  /**
   * Set color for all color sensor users
   * @param colorArray
   */
  public void setColor(double[] colors) { 
    for(ColorUser user : currColorUsers) {
      user.processColorData(colors);
    }
  }

}



