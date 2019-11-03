package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import lejos.hardware.Sound;

import ca.mcgill.ecse211.project.game.SensorController;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.odometry.*;

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
  public static GameState state;
  
  public GameController (SensorController sensorCont, USLocalizer USLoc, ColorLocalizer colorLoc, 
                         OdometryCorrection odoCorrect, ObstacleAvoidance obAvoid) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.colorLoc = colorLoc;
    this.odoCorrect = odoCorrect;
    this.obAvoid = obAvoid;
  }
  
  public enum GameState {
    INIT,
    USLOC,
    COLORLOC,
    NAVIGATION,
    OBSTACLE,
    TUNNEL,
    THROW,
    DONE,
    TEST
  }
  
  @Override
  public void run() {
    startGame();
  }
  
  public void startGame() {
    changeState(GameState.USLOC);
    USLoc.localize();
    
    changeState(GameState.COLORLOC);
    colorLoc.localize();
    
  }
  
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
        currColorUsers.add(odoCorrect);
        sensorCont.pauseUSPoller();
        sensorCont.resumeColorPoller();
        break;
        
      case OBSTACLE:
        currUSUsers.add(obAvoid);
        sensorCont.resumeUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case TUNNEL:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case THROW:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case DONE:
        sensorCont.pauseUSPoller();
        sensorCont.pauseColorPoller();
        break;
        
      case TEST:
        sensorCont.resumeUSPoller();
        sensorCont.resumeColorPoller();
        break;
    }
    sensorCont.setCurrUSUsers(currUSUsers);
    sensorCont.setCurrColorUsers(currColorUsers);
  }
  
  
  
  
  
  
  
}