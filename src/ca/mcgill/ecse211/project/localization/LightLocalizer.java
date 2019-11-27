package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import java.util.Arrays;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Helper.*;
import ca.mcgill.ecse211.project.game.WiFi;
import ca.mcgill.ecse211.project.odometry.Odometer;

/**
 * This class contains methods for light sensor localization
 * The robot uses two light sensors placed at each side at the back of its body 
 * to read grid lines and correct its heading (angle) as necesary
 */
public class LightLocalizer {
  private float[][] lightData;
  private SampleProvider[] sampleProvider;
  private Odometer odometer;
  private int filterSize = 5;
  private int[][] tempLights = new int[2][filterSize];
  private boolean gotInitialSample = false;
  private double[] initialLight = new double[2];
  private double[] offset2 = new double[2];
  private double[] offset1 = new double[2];
  private boolean shouldRight1, shouldRight2;
  private boolean aligned1, aligned2 = false;
  private boolean isLeftSensor1, isLeftSensor2;
  
  public LightLocalizer(Odometer odometer, SampleProvider leftSamp, float[] leftLightData, SampleProvider rightSamp, float[] rightLightData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.lightData = new float[][] {leftLightData, rightLightData};  
    this.odometer = odometer;
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
    //do {
    if (!gotInitialSample) {
      setLRMotorSpeed(120);
      //moveForward(7);
      //moveBackward(3);
      initialLight = meanFilter();
      gotInitialSample = true;
    }
    
    int firstAxis = getAxis()[1];
    int secondAxis = getAxis()[0];
    //System.out.println("currfirst and secon: " + firstAxis + ", " + secondAxis);
//    if (firstAxis == 0) {
//      odometer.setX(0);
//    }
//    else if (firstAxis == 1) {
//      odometer.setY(0);
//    }

//    //first axis
//    while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
//      moveForward();
//    }
//    
//    stopMotors();
//  
//    moveBackward(LS_DISTANCE);
//    turnLeft(90);
    
    //first axis
    while ( meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R ) {
      moveForward();
    }
    stopMotors();
    if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ) {
      offset1[0] = 0;
      offset1[1] = 0;
      isLeftSensor1 = false; // for completeness only
      aligned1 = true;
    }
    //left sensor sees line first
    else if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      shouldRight1 = false;
      //System.out.println("first axis:" + odometer.getXYT()[firstAxis]);
      //System.out.println("second axis:" + odometer.getXYT()[secondAxis]);
      offset1[0] = odometer.getXYT()[firstAxis];
      isLeftSensor1 = true;
    }
    //right sensor sees line first
    else if (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ){
      shouldRight1 = true;
      //System.out.println("first axis:" + odometer.getXYT()[firstAxis]);
      //System.out.println("second axis:" + odometer.getXYT()[secondAxis]);
      offset1[0] = odometer.getXYT()[firstAxis];
      isLeftSensor1 = false;
    }
    
    if (!aligned1) {
      if (isLeftSensor1) {
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
      offset1[1] = odometer.getXYT()[firstAxis];
      //System.out.println("first axis2:" + odometer.getXYT()[firstAxis]);
      //System.out.println("second axis2:" + odometer.getXYT()[secondAxis]);
    }
    
    //System.out.println("offsets" + offset1[0] + ", " + offset1[1]);
    double turnTheta1 = Math.atan(Math.abs(offset1[1] - offset1[0])/WHEEL_BASE);
    double turnTheta1Deg = 180*turnTheta1/Math.PI;

    if (shouldRight1) {
      turnRight(turnTheta1Deg);
    } else {
      turnLeft(turnTheta1Deg);
    }
    
    moveBackward(LS_DISTANCE + WHEEL_BASE*Math.sin(turnTheta1)); //sin takes radians
    turnLeft(90);

    
    
//    //second axis
//    while ( meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R ) {
//      moveForward();
//    }
//    stopMotors();
//    if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ) {
//      offset2[0] = 0;
//      offset2[1] = 0;
//      isLeftSensor2 = false; // for completeness only
//      aligned2 = true;
//    }
//    //left sensor sees line first
//    else if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
//      shouldRight2 = false;
//      offset2[0] = odometer.getXYT()[secondAxis];
//      isLeftSensor2 = true;
//    }
//    //right sensor sees line first
//    else if (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R ){
//      shouldRight2 = true;
//      offset2[0] = odometer.getXYT()[secondAxis];
//      isLeftSensor2 = false;
//    }
//    
//    if (!aligned2) {
//      if (isLeftSensor2) {
//        while (meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) { //now looking for right sensor line
//          moveForward();
//        }
//        stopMotors();
//      } else {
//        while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L ) { //now looking for left sensor line
//          moveForward();
//        }
//        stopMotors();
//      }
//      offset2[1] = odometer.getXYT()[secondAxis];
//    }
//    
//    double turnTheta2 = Math.atan(Math.abs((offset2[1] - offset2[0]))/WHEEL_BASE);
//    double turnTheta2Deg = 180*turnTheta2/Math.PI;
//   
//    if (shouldRight2) {
//      turnRight(turnTheta2Deg);
//    } else {
//      turnLeft(turnTheta2Deg);
//    }
//    setLRMotorSpeed(LS_SPEED_FAST);
//    moveBackward(LS_DISTANCE + WHEEL_BASE*Math.sin(turnTheta2)); //sin takes radians

    //set odometer after localization
    if (WiFi.CORNER == 0) {
      odometer.setXYT(TILE_SIZE, TILE_SIZE, 0); //old = 0
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
    
//    doneLoc = true;
//    
//    } while (Button.waitForAnyPress() != Button.ID_ESCAPE && doneLoc == false);
    
  }
 
 
}