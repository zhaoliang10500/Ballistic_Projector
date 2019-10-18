package ca.mcgill.ecse211.project;
/**
 * @author zhaoliang & Brandon Freiberger
 * This Class implemented the light sensor localization
 */
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.Resources.*;

public class LightLocalizer extends Thread{
    //class variables
    private Odometer odometer;
    private SampleProvider colorValue;
    private float[] colorData;
    private float lastColor;
    private float currentColor;
    private double lastPosition[] = new double[3];
    private double correction = 0;
    public int isDetectedBlackLineOfXSide = 0;
    public int isDetectedBlackLineOfYSide = 0;
   
    //motors
    private EV3LargeRegulatedMotor leftMotor, rightMotor;
    Navigation navi; //the navigation class
    
    public LightLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
        Odometer odometer, SampleProvider colorValue, float[] colorData) {
        //get incoming values for variables
        this.odometer = odometer;
        this.colorValue = colorValue;
        this.colorData = colorData;
        
        //set up motors
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.leftMotor.setAcceleration(ACCELERATION);
        this.rightMotor.setAcceleration(ACCELERATION);
    }
    /*
     * Localizes the robot (using the light sensor)
     */
    public void run() {
        long correctionStart, correctionEnd;
        colorValue.fetchSample(colorData, 0);
        lastColor = colorData[0];
        
        while (true) {
          if (isDetectedBlackLineOfXSide == 1 && isDetectedBlackLineOfYSide == 1) {
            break;
          }
          correctionStart = System.currentTimeMillis();
          
          colorValue.fetchSample(colorData, 0);
          currentColor = colorData[0];
          lastPosition = odometer.getXYT();
          
          if (Math.abs(lastColor - currentColor)* 100 > 5) {
            //first we will detect the black line of x-axis side, then the y-axis side.
            if (isDetectedBlackLineOfXSide == 1) {
              isDetectedBlackLineOfYSide = 1;
            }
            isDetectedBlackLineOfXSide = 1;
            Sound.beep();
            if (lastPosition[2] > 350 || lastPosition[2] < 10) {
              correction = TILE_SIZE;
              odometer.setX(correction);
            }
            else if (lastPosition[2] > 250 || lastPosition[2] < 290) {
              correction = TILE_SIZE;
              odometer.setY(correction);
            }
            sleepFor(1000);
          }
          
          correctionEnd = System.currentTimeMillis();
          if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
            sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
          }
        }
    }

    public static void sleepFor(long duration) {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException e) {
        // There is nothing to be done here
      }
    }

}
