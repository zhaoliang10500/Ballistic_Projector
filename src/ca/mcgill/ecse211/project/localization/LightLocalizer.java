package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.LightUser;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.WiFi.*;
import java.util.Arrays;
import ca.mcgill.ecse211.project.game.WiFi;

/**
 * This class contains methods for light sensor localization
 * The robot uses two light sensors placed at each side at the back of its body 
 * to read grid lines and correct its heading (angle) as necesary
 */
public class LightLocalizer {
  private float[][] lightData;
  private SampleProvider[] sampleProvider;
  private int filterSize = 5;
  private int[][] tempLights = new int[2][filterSize];
  private boolean gotInitialSample = false;
  private double[] initialLight = new double[2];
  private double[]  offset = new double[2];
  private boolean turnRight;
  private boolean aligned = false;
  private boolean isLeftSensor;
  
  public LightLocalizer(SampleProvider leftSamp, float[] leftLightData, SampleProvider rightSamp, float[] rightLightData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.lightData = new float[][] {leftLightData, rightLightData};  
  }
  
  private double[] meanFilter() {
    double[] lights = new double[2];
    for (int i = 0; i<filterSize; i++) {
      //left sensor
      sampleProvider[0].fetchSample(lightData[0], 0); 
      tempLights[0][i] = (int)(lightData[0][0]*1000.0); 

      //right sensor
      sampleProvider[1].fetchSample(lightData[1], 0); 
      tempLights[1][i] = (int)(lightData[1][0]*1000.0);   
    }
    
    Arrays.sort(tempLights[0]);
    Arrays.sort(tempLights[1]);
    lights[0] = tempLights[0][filterSize/2]; //java rounds down for int division
    lights[1] = tempLights[1][filterSize/2];
    
    return lights;
  }
  
  private int[] getAxis() {
    int[] axis = new int[2];
    
    if (WiFi.CORNER == 0 || WiFi.CORNER == 2) {
      axis[0] = 0; //first displacement is x
      axis[1] = 1;
    }
    else if (WiFi.CORNER == 1 || WiFi.CORNER == 3) {
      axis[0] = 1; //first displacement is y
      axis[1] = 0;
    }
    
    return axis;
  }
  
  public void localize() {
    if (!gotInitialSample) {
      setLRMotorSpeed(120);
      moveForward(5);
      moveBackward(3);
      initialLight = meanFilter();
      gotInitialSample = true;
    }
    
    int firstAxis = getAxis()[0];
    int secondAxis = getAxis()[1];
    //System.out.println("axis; " + firstAxis + ", " + secondAxis);
    if (firstAxis == 0) {
      odometer.setX(0);
    }
    else if (firstAxis == 1) {
      odometer.setY(0);
    }
    
    //first axis
    while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      moveForward();
    }
    stopMotors();
    if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) { //left first, backup more
      moveBackward(LS_DISTANCE + 2);
    }
    else if (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R) { //right first, backup less
      moveBackward(LS_DISTANCE - 2);
    }
    else {
      moveBackward(LS_DISTANCE);
    }
    turnLeft(90);
    
    
    //second axis
    while ( meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R ) {
      moveForward();
    }
    stopMotors();
    if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ) {
      offset[0] = 0;
      offset[1] = 0;
      isLeftSensor = false; // for completeness only
      aligned = true;
    }
    //left sensor sees line first
    else if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      turnRight = false;
      //System.out.println("first left: " + meanFilter()[0]/initialLight[0]);
      offset[0] = odometer.getXYT()[secondAxis];
      isLeftSensor = true;
    }
    //right sensor sees line first
    else if (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ){
      turnRight = true;
      //System.out.println("first right: " + meanFilter()[1]/initialLight[1]);
      offset[0] = odometer.getXYT()[secondAxis];
      isLeftSensor = false;
    }
    
    if (!aligned) {
      if (isLeftSensor) {
        while (meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) { //now looking for right sensor line
          moveForward();
        }
        stopMotors();
      } else {
        while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L ) { //now looking for left sensor line
          moveForward();
        }
        stopMotors();
      }
      offset[1] = odometer.getXYT()[secondAxis];
    }
    
    //System.out.println("delta offset" + offset[0] + ", " + offset[1]);
    double turnTheta = Math.atan(Math.abs((offset[1] - offset[0]))/WHEEL_BASE);
    double turnThetaDeg = 180*turnTheta/Math.PI;
    //System.out.println("turnthetaDeg: " + turnThetaDeg);
    
    if (turnRight) {
      turnRight(turnThetaDeg);
    }
    else {
      turnLeft(turnThetaDeg);
    }
    sleepFor(3000);
    
    //set odometer after localization
    if (WiFi.CORNER == 0) {
      odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
    }
    else if (WiFi.CORNER == 1) {
      odometer.setXYT(14*TILE_SIZE, TILE_SIZE, 270);//270
    }
    else if (WiFi.CORNER == 2) {
      odometer.setXYT(14*TILE_SIZE, 8*TILE_SIZE, 180); //180
    }
    else if (WiFi.CORNER == 3) {
      odometer.setXYT(TILE_SIZE, 8*TILE_SIZE, 90); //90
    } 
    setLRMotorSpeed(LS_SPEED_FAST);
    moveBackward(LS_DISTANCE);

  }
 
 
}






















