package ca.mcgill.ecse211.project;
import lejos.hardware.Button;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.Resources.*;
/**
 * @author zhaoliang & Brandon
 * Main function
 */
public class Main {
  
  public static void main (String[] args) throws InterruptedException {
    //Setup the Ultrasonic Sensor
    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
    SampleProvider usValue = usSensor.getMode("Distance");
    float[] usData = new float[usValue.sampleSize()];   
    
    //Setup the Odometer and the Display
    Odometer odometer = Odometer.getOdometer();
    USLocalizer usLocalizer = new USLocalizer(leftMotor, rightMotor, odometer, usSensor, usData);
    SimpleThrow simpleThrow = new SimpleThrow(leftThrowMotor, rightThrowMotor);
    Display display = new Display(odometer, usLocalizer);
    
    //Setup the Color Sensor
    @SuppressWarnings("resource")
    SensorModes colorSensor = new EV3ColorSensor(colorPort);
    SampleProvider colorValue = colorSensor.getMode("RGB");
    float[] colorData = new float[colorValue.sampleSize()];
    
    
    //Display on the screen defines here
    int pressForStage1;
    do {
      LCD.clear();
      
      LCD.drawString("< Left       | Right       >", 0, 0);
      LCD.drawString("             |              ", 0, 1);
      LCD.drawString(" do          | do           ", 0, 2);
      LCD.drawString(" localization| Throw        ", 0, 3);
      LCD.drawString(" & Throw     | only         ", 0, 4);
      pressForStage1 = Button.waitForAnyPress();
    } while (pressForStage1 != Button.ID_LEFT && pressForStage1 != Button.ID_RIGHT);
    //Do localization and throw if left button is pressed
    if (pressForStage1 == Button.ID_LEFT) {
      //DO RISING_EDGE
      odometer.start();
      display.start();
      usLocalizer.doLocalization("RISING_EDGE");
      //DO LOCALIZATION TO (1,1)
      LightLocalizer lightLocalizer = new LightLocalizer(leftMotor, rightMotor, odometer, colorValue, colorData);
      Navigation navigation = new Navigation(leftMotor, rightMotor, odometer, lightLocalizer);//Navigating to grid point (1,1)
      lightLocalizer.start();
      navigation.doNavigation();
      //DO THROW
      
      
      
    }
    //Do simple throw directly
    else if (pressForStage1 == Button.ID_RIGHT) {
      simpleThrow.doSimpleThrow();
    }
  }
}
