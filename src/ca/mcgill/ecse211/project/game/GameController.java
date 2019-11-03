package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import lejos.hardware.Sound;

import ca.mcgill.ecse211.project.game.SensorController;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.odometry.*;
import ca.mcgill.ecse211.project.game.*;

/**
 * This class runs the entire game
 * It contains the game state machine 
 */
public class GameController implements Runnable {
  private SensorController sensorCont;
  private USLocalizer USLoc;
  private ColorLocalizer colorLoc;
  private OdometryCorrection odoCorrect;
  private ObstacleAvoidance obAvoid;
  private Launcher launcher;
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
                         OdometryCorrection odoCorrect, ObstacleAvoidance obAvoid) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.colorLoc = colorLoc;
    this.odoCorrect = odoCorrect;
    this.obAvoid = obAvoid;
  }
  
  /**
   * Enum used to define game states
   *
   */
  public enum GameState {
    INIT,
    USLOC,
    COLORLOC,
    NAVIGATION,
    NAVWITHOBSTACLE,
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
    changeState(GameState.INIT);
    //TODO: get team color and corresponding boundaries using WiFi
    
    changeState(GameState.USLOC);
    USLoc.localize();
    
    changeState(GameState.COLORLOC);
    colorLoc.localize();
    beep(3);
    
    //travel to tunnel
    changeState(GameState.NAVIGATION);
    //TODO: navigate to tunnel using WiFi info
    
    //travel through tunnel
    changeState(GameState.TUNNEL);
    //TODO: navigate through tunnel
    
    //travel to ideal launch point while avoiding obstacles
    changeState(GameState.NAVWITHOBSTACLE);
    //TODO: navigate to ideal launch point
    beep(3);
    
    //throw balls
    changeState(GameState.LAUNCH);
    for (int i = 0; i< 5; i++) {
      Launcher.launch();
    }
    
    //travel back to tunnel
    changeState(GameState.NAVWITHOBSTACLE);
    //TODO: navigate back to tunnel with obstacle avoidance
    
    //travel through tunnel
    changeState(GameState.TUNNEL);
    //TODO: navigate through tunnel
    
    //travel back to starting grid
    changeState(GameState.NAVIGATION);
    //TODO: navigate back to starting grid
    
    changeState(GameState.DONE);
    beep(5);
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
      case INIT:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case USLOC:
        currUSUsers.add(USLoc);
        sensorCont.resumeUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case COLORLOC:
        currColorUsers.add(colorLoc);
        sensorCont.resumeUSPoller();
        sensorCont.resumeColorPoller();
        break;
        
      case NAVIGATION:
        currUSUsers.remove(USLoc);
        currColorUsers.remove(colorLoc);
        currColorUsers.add(odoCorrect);
        sensorCont.pauseUSPoller();
        sensorCont.resumeColorPoller();
        break;
        
      case NAVWITHOBSTACLE:
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