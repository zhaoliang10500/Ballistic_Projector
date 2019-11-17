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
  private LightTunnelLocalizer lightTunnelLoc;
  private OdometryCorrection odoCorrect;
  private ObstacleAvoidance obAvoid;
  private final int[] testCoords = {4,3};
  public static GameState state;
  
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
                         LightTunnelLocalizer lightTunnelLoc, OdometryCorrection odoCorrect, ObstacleAvoidance obAvoid) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.lightLoc = lightLoc;
    this.lightTunnelLoc = lightTunnelLoc;
    this.odoCorrect = odoCorrect;
    this.obAvoid = obAvoid;
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
    TUNNEL_LOC,
    NAV_WITH_OBSTACLE,
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
    Navigation.travelTo(tng.ll.x-0.15, tng.ll.y, 7);
    //Navigation.travelTo(testCoords[0],testCoords[1]);
    setLRMotorSpeed(NAV_TURN);
    Navigation.turnTo(-Navigation.turnAngle); //turn robot back to 0°
    
    // light localization before tunnel
    changeState(GameState.TUNNEL_LOC);
    setLRMotorSpeed(CS_TUNNEL_SPEED);
    lightTunnelLoc.localize(true); //boolean before = true
    
    //travel through tunnel
    //double backupDist = lightTunnelLoc.backedupDist;
    
    //TODO: Keep track of back up distance to fool proof tunnel localization (eg in case backs up one extra tile)
    
    changeState(GameState.TUNNEL);
    setLRMotorSpeed(TUNNEL_SPEED);
    Navigation.travelThroughTunnel();
    
    // light localization after tunnel
    changeState(GameState.TUNNEL_LOC);
    setLRMotorSpeed(CS_TUNNEL_SPEED);
    //lightTunnelLoc.localize(false); //boolean before = false -> after
    
    // navigation: travel to launch point
    changeState(GameState.NAVIGATION);
    Navigation.travelTo(bin.x - 1.0, bin.y - 1.5, 0);
    setLRMotorSpeed(NAV_TURN2);
    Navigation.turnTo(targetAngle); //turn to specified orientation
    beep(3);
    
    // throw balls
    changeState(GameState.LAUNCH);
    for (int i = 0; i<5; i++) {
      Launcher.launch();
    }
    
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
    ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
    ArrayList<LightUser> currLightUsers = new ArrayList<LightUser>();
    
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
        
      case TUNNEL_LOC:
        currLightUsers.add(lightTunnelLoc);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case NAV_WITH_OBSTACLE:
        currUSUsers.add(obAvoid);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case TUNNEL:
        currLightUsers.remove(lightTunnelLoc);
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