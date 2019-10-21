package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.project.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class Resources {
  
  //Setup two motors here
  public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor leftThrowMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
  public static final EV3LargeRegulatedMotor rightThrowMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
  
  //Sensors
  public static final EV3UltrasonicSensor US_SENSOR = new EV3UltrasonicSensor(SensorPort.S2);
  public static final EV3ColorSensor L_SENSOR = new EV3ColorSensor(SensorPort.S1);
  
  // Odometer
  public static Odometer odometer = Odometer.getOdometer();
  
  //Setup LCD display here
  public static final TextLCD LCD = LocalEV3.get().getTextLCD();

  //constants
  public static final double WHEEL_RADIUS = 2.13;
  public static final double WHEEL_BASE = 11.7; //11.7
  public static final int ROTATION_SPEED = 60;
  public static final int ACCELERATION = 600;
  public static final int FORWARD_SPEED = 60;
  public static final double TILE_SIZE = 30.48;
  public static final int RISING = Button.ID_LEFT; //for USLocalizer
  public static final int FALLING = Button.ID_RIGHT; //for USLocalizer
  
  //Change these experimentally for USLocalizer
  public static final int US_SPEED = 100; //80
  public static final int RISE_THRESHOLD = 37; //38, 40
  public static final int RISE_ANGLE = 225;
  
  public static final int FALL_THRESHOLD = 25; //25
  public static final int FALL_ANGLE = 45; //45
  public static final int MEDIAN_FILTER = 3; //number of sample taken 
  
  //Change these experimentally for LightLocalizer
  public static final double INTENSITY_THRESHOLD = 0.62;//0.67  //change in intensity for line detection
  public static final double LS_DISTANCE = 7; // sensor distance from center of rotation
  public static final int LS_SPEED = 80; //70
  public static final int MEAN_FILTER = 5;

  
  //change for the LightLocalizer
  public static final int CORRECTION_PERIOD = 10;
  
  //for navigation
  public static int NAV_FORWARD = 160;
  public static double NAV_OFFSET = 4.5*TILE_SIZE; //min offset = 4.5
  public static int NAV_ROTATE = 90;
  
}
