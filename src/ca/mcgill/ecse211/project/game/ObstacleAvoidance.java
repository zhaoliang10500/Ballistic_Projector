package ca.mcgill.ecse211.project.game;
import ca.mcgill.ecse211.project.sensor.*;
import static ca.mcgill.ecse211.project.game.Resources.*;

/**
 * This class contains methods for obstacle avoidance
 *
 */
public class ObstacleAvoidance implements USUser {
  private int distance;
  
  /**
   * Uses the bang-bang controller to avoid obstacles if the distance < OBS_THRESHOLD
   */
  public void avoid() {
    
  }
  
  /**
   * Method to process US Poller data
   */
  @Override
  public void processUSData(int distance) {
    this.distance = distance;
  }
  
}