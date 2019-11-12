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
  private ColorLocalizer colorLoc;
  private ColorTunnelLocalizer colorTunnelLoc;
  private OdometryCorrection odoCorrect;
  private ObstacleAvoidance obAvoid;
  public static GameState state;
  
  /**
   * Constructor for the GameController class
   * @param sensorCont
   * @param USLoc
   * @param colorLoc
   * @param odoCorrect
   * @param obAvoid
   */
  public GameController (SensorController sensorCont, USLocalizer USLoc, ColorLocalizer colorLoc, 
                         ColorTunnelLocalizer colorTunnelLoc, OdometryCorrection odoCorrect, ObstacleAvoidance obAvoid) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.colorLoc = colorLoc;
    this.colorTunnelLoc = colorTunnelLoc;
    this.odoCorrect = odoCorrect;
    this.obAvoid = obAvoid;
  }
  
  /**
   * Enum used to define game states
   *
   */
  public enum GameState {
    US_LOC,
    COLOR_LOC,
    NAVIGATION,
    LOC_BEFORE_TUNNEL,
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
   * Method for game state logic
   */
  public void startGame() {
    //US localization
    changeState(GameState.US_LOC);
    setLRMotorSpeed(US_SPEED);
    USLoc.localize();
    
    // color localization
    changeState(GameState.COLOR_LOC);
    setLRMotorSpeed(CS_SPEED);
    colorLoc.localize();
    beep(1);
    
    //travel to tunnel
    changeState(GameState.NAVIGATION);
    //int[] xyCoords = {tng.ll.x, tng.ll.y};
    //Navigation.travelTo(xyCoords);
    Navigation.travelTo(4,3);
    setLRMotorSpeed(NAV_TURN);
    Navigation.turnTo(-Navigation.turnAngle);
    
    // color localization before tunnel
    changeState(GameState.LOC_BEFORE_TUNNEL);
    setLRMotorSpeed(CS_TUNNEL_SPEED);
    colorTunnelLoc.localize();
    
    //travel through tunnel
//    double backupDist = colorTunnelLoc.backedupDist;
    /*changeState(GameState.TUNNEL);
    //TODO: navigate through tunnel
    
    //travel to ideal launch point while avoiding obstacles
    changeState(GameState.NAV_WITH_OBSTACLE);
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
   * Method to define which threads are running for every state of the game
   * @param newState
   */
  public void changeState(GameState newState) {
    state = newState;
    ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
    ArrayList<ColorUser> currColorUsers = new ArrayList<ColorUser>();
    
    switch (state) { 
      case US_LOC:
        currUSUsers.add(USLoc);
        sensorCont.resumeUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case COLOR_LOC:
        currColorUsers.add(colorLoc);
        sensorCont.pauseUSPoller();
        sensorCont.resumeColorPoller();
        break;
        
      case NAVIGATION:
        currUSUsers.remove(USLoc);
        currColorUsers.remove(colorLoc);
        //currColorUsers.add(odoCorrect);
        sensorCont.pauseUSPoller();
        //sensorCont.resumeColorPoller();
        //sensorCont.pauseColorPoller();
        break;
        
      case LOC_BEFORE_TUNNEL:
        currColorUsers.add(colorTunnelLoc);
        sensorCont.pauseUSPoller();
        sensorCont.resumeColorPoller();
        break;
        
      case NAV_WITH_OBSTACLE:
        currUSUsers.add(obAvoid);
        sensorCont.resumeUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case TUNNEL:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case LAUNCH:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case DONE:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case TEST:
        //TODO: change this as necessary for any test
        sensorCont.resumeUSPoller();
        sensorCont.resumeColorPoller();
        break;
    }
    sensorCont.setCurrUSUsers(currUSUsers);
    sensorCont.setCurrColorUsers(currColorUsers);
  }
  
}