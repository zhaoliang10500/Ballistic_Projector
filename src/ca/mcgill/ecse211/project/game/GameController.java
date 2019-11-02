package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import lejos.hardware.Sound;

import ca.mcgill.ecse211.project.game.SensorController;

/**
 * This class runs the entire game
 * It contains the game state machine 
 */
public class GameController implements Runnable {
  private SensorController sensorCont;
  
  public enum GameState {
    INITIALIZATION,
    USLOCALIZATION,
    COLORLOCALIZATION,
    NAVIGATION,
    TUNNEL,
    THROW,
    DONE,
    TESTING;
  }
  
  @Override
  public void run() {
    
  }
  
  public void changeState(GameState state) {
    
  }
}