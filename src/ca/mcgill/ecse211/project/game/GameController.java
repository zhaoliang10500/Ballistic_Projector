package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import lejos.hardware.Sound;

import ca.mcgill.ecse211.project.game.SensorController;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.odometry.*;

import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.localization.Navigation.*;
import static ca.mcgill.ecse211.project.game.WifiResources.*;

/**
 * This class runs the entire game
 * It contains the game state machine 
 */
public class GameController implements Runnable {
  private SensorController sensorCont;
  private USLocalizer USLoc;
  private LightLocalizer lightLoc;
  private LightTunnelLocalizer lightTunnelLoc1, lightTunnelLoc2, lightTunnelLoc3, lightTunnelLoc4;
  private ObstacleAvoidance obAvoid1, obAvoid2;
  private final int[] testCoords = {4,3};
  public static GameState state;
  public volatile ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
  public volatile ArrayList<LightUser> currLightUsers = new ArrayList<LightUser>();
  
  /**
   * Constructor for the GameController class
   * @param sensorCont The sensor controller
   * @param USLoc The ultrasonic localizer
   * @param lightLoc The light localizer
   * @paran lightTunnelLoc The light localizer for localizing before and after a tunnel
   * @param odoCorrect The odometer correction 
   * @param obAvoid The obstacle avoidance
   */
  public GameController (SensorController sensorCont, USLocalizer USLoc, LightLocalizer lightLoc, 
                         LightTunnelLocalizer lightTunnelLoc1, LightTunnelLocalizer lightTunnelLoc2, 
                         LightTunnelLocalizer lightTunnelLoc3, LightTunnelLocalizer lightTunnelLoc4,
                         ObstacleAvoidance obAvoid1, ObstacleAvoidance obAvoid2) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.lightLoc = lightLoc;
    this.lightTunnelLoc1 = lightTunnelLoc1;
    this.lightTunnelLoc2 = lightTunnelLoc2;
    this.lightTunnelLoc3 = lightTunnelLoc3;
    this.lightTunnelLoc4 = lightTunnelLoc4;
    this.obAvoid1 = obAvoid1;
    this.obAvoid2 = obAvoid2;
  }
  
  /**
   * Enum used to define game states
   * Each state turns on/off the ultrasonic poller and the light poller
   *
   */
  public enum GameState {
    US_LOC,
    COLOR_LOC,
    NAVIGATION,
    TUNNEL_LOC1,
    TUNNEL_LOC2,
    TUNNEL_LOC3,
    TUNNEL_LOC4,
    NAV_WITH_OBSTACLE1,
    NAV_WITH_OBSTACLE2,
    TUNNEL,
    LAUNCH,
    DONE,
    TEST
  }
  
  /**
   * Run method for thread
   */
  @Override
  public void run() {
    startGame();
  }
  
  /**
   * This method runs the logic of the game by changing 
   * the GameState and calling the corresponding methods for each state
   */
  public void startGame() {
    //US localization
    changeState(GameState.US_LOC);
    setLRMotorSpeed(US_SPEED);
    USLoc.localize();
    
    // light localization
    changeState(GameState.COLOR_LOC);
    setLRMotorSpeed(CS_SPEED);
    lightLoc.localize();
    beep(3);
    
    //navigation: travel to tunnel
    changeState(GameState.NAVIGATION);
    Navigation.travelTo(WiFi.TUNNEL_LL.x, WiFi.TUNNEL_LL.y, 0);
    //Navigation.travelTo(tng.ll.x-0.15, tng.ll.y, 7);
    //Navigation.travelTo(testCoords[0],testCoords[1]);
    //setLRMotorSpeed(NAV_TURN);
    if (WiFi.TUNNEL_LL.x + 1 == WiFi.TUNNEL_UR.x) {//vertical tunnel = turn left to enter tunnel straight
      Navigation.turnTo(-Navigation.turnAngle); //turn left
    }
    else if (WiFi.TUNNEL_LL.x + 2 == WiFi.TUNNEL_UR.x) { //horizontal tunnel = turn right to enter tunnel straight
      Navigation.turnTo(90-Navigation.turnAngle); //turn right
    }
      
    
//    // light localization before tunnel
//    changeState(GameState.TUNNEL_LOC1);
//    setLRMotorSpeed(CS_TUNNEL_SPEED);
//    lightTunnelLoc1.localize(true); //boolean before = true
//    
//    //travel through tunnel
//    //double backupDist = lightTunnelLoc.backedupDist;
//    
//    //TODO: Keep track of back up distance to fool proof tunnel localization (eg in case backs up one extra tile)
//    
//    changeState(GameState.TUNNEL);
//    setLRMotorSpeed(TUNNEL_SPEED);
//    Navigation.travelThroughTunnel();
//    
//    // light localization after tunnel
//    changeState(GameState.TUNNEL_LOC2);
//    setLRMotorSpeed(CS_TUNNEL_SPEED);
//    lightTunnelLoc2.localize(false); //boolean before = false -> after
    
//    // navigation: travel to launch point
//    changeState(GameState.NAVIGATION);
//    Navigation.travelTo(bin.x - 1.0, bin.y - 1.5, 0);
//    setLRMotorSpeed(NAV_TURN2);
//    Navigation.turnTo(targetAngle); //turn to specified orientation
//    beep(3);
//    
//    // throw balls
//    changeState(GameState.LAUNCH);
//    for (int i = 0; i<5; i++) {
//      Launcher.launch();
//    }
    
    //travel back to tunnel
    //TODO
    
    //localize
    //TODO
    
    //travel through tunnel
    //TODO
    
    //localize
    //TODO
    
    // navigation: back to starting point
    //TODO
    //beep(5);
    
    
    //travel to ideal launch point while avoiding obstacles
    /*changeState(GameState.NAV_WITH_OBSTACLE);
    //TODO: navigate to ideal launch point
    beep(3);
    
    //throw balls
    changeState(GameState.LAUNCH);
    for (int i = 0; i< 5; i++) {
      Launcher.launch();
    }
    
    //travel back to tunnel
    changeState(GameState.NAV_WITH_OBSTACLE);
    //TODO: navigate back to tunnel with obstacle avoidance
    
    //travel through tunnel
    changeState(GameState.TUNNEL);
    //TODO: navigate through tunnel
    
    //travel back to starting grid
    changeState(GameState.NAVIGATION);
    //TODO: navigate back to starting grid
    
    changeState(GameState.DONE);
    beep(5);*/
  }
  
  /**
   * Method to beep num times 
   * @param num
   */
  public void beep(int num) {
    for (int i = 0; i < num; i++) {
      Sound.beep(); 
    }
  }
  
  /**
   * Method that defines which threads are running for every state of the game
   * @param newState GameState enum 
   */
  public void changeState(GameState newState) {
    state = newState;
    
    switch (state) { 
      case US_LOC:
        currUSUsers.add(USLoc);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case COLOR_LOC:
        currLightUsers.add(lightLoc);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case NAVIGATION:
        currUSUsers.remove(USLoc);
        currLightUsers.remove(lightLoc);
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case TUNNEL_LOC1:
        currLightUsers.add(lightTunnelLoc1);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
      
      case TUNNEL_LOC2:
        currLightUsers.remove(lightTunnelLoc1);
        currLightUsers.add(lightTunnelLoc2);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
      
      case TUNNEL_LOC3:
        currLightUsers.remove(lightTunnelLoc2);
        currLightUsers.add(lightTunnelLoc3);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case TUNNEL_LOC4:
        currLightUsers.remove(lightTunnelLoc3);
        currLightUsers.add(lightTunnelLoc4);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case NAV_WITH_OBSTACLE1:
        currUSUsers.add(obAvoid1);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;
      
      case NAV_WITH_OBSTACLE2:
        currUSUsers.remove(obAvoid1);
        currUSUsers.add(obAvoid2);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;

      case TUNNEL:
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case LAUNCH:
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case DONE:
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case TEST:
        //TODO: change this as necessary for any test
        sensorCont.resumeUSPoller();
        sensorCont.resumeLightPoller();
        break;
    }
    sensorCont.setCurrUSUsers(currUSUsers);
    sensorCont.setCurrLightUsers(currLightUsers);
  }
  
}