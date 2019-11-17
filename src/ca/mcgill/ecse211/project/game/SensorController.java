package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import ca.mcgill.ecse211.project.sensor.*;

/**
 * This class contains methods that controls the ultrasonic and light sensors
 *
 */
public class SensorController {
  private static SensorController sensorCont = null;
  private LightPoller lightPoll;
  private USPoller usPoll;
  
  // array lists to hold the current US and light users, should be two each
  private ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
  private ArrayList<LightUser> currLightUsers = new ArrayList<LightUser>();
  
  /**
   * Private SensorController constructor
   * @param usPoll
   * @param lightPoll
   */
  private SensorController(USPoller usPoll, LightPoller lightPoll) {
    this.usPoll = usPoll;
    this.lightPoll = lightPoll;
    this.usPoll.sensorCont = this;
    this.lightPoll.sensorCont = this;
  }
  
  /**
   * Synchronized method used to only allow the next sensor thread to enter after the previous has completed
   * This prevents problems such as the game actions beginning before the localization completes
   * @param usPoll
   * @param lightPoll
   * @return SensorController object
   */
  public synchronized static SensorController getSensorController(USPoller usPoll, LightPoller lightPoll) {
    if (sensorCont != null) {
      return sensorCont;
    }
    else {
      sensorCont = new SensorController(usPoll, lightPoll);
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
   * Pause Light Poller
   */
  public void pauseLightPoller() {
    lightPoll.running = false;
  }
  
  /**
   * Resume Light Poller
   */
  public void resumeLightPoller() {
    lightPoll.running = true;
  }
  
  
  /**
   * Add a USUser to the currUSUsers list
   * @param currUSUsers
   */
  public void setCurrUSUsers (ArrayList<USUser> currUSUsers) {
    this.currUSUsers = currUSUsers;
  }
  
  /**
   * Add a lightUser to the currLightUsers list
   * @param currLightUsers
   */
  public void setCurrLightUsers (ArrayList<LightUser> currLightUsers) {
    this.currLightUsers = currLightUsers;
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
   * Set light for all light sensor users
   * @param lightArray
   */
  public void setLight(int[] lights) { 
    for(LightUser user : currLightUsers) {
      user.processLightData(lights);
    }
  }

}



