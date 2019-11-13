package ca.mcgill.ecse211.project.game;

import ca.mcgill.ecse211.project.odometry.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

/**
 *Class of constants
 */
public class Resources {
  
  //Setup two motors here
  public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3LargeRegulatedMotor throwMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
  public static final EV3LargeRegulatedMotor USMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
  
  //Sensors
  public static final EV3UltrasonicSensor US_SENSOR = new EV3UltrasonicSensor(SensorPort.S3);
  public static final EV3ColorSensor COLOR_SENSOR_L = new EV3ColorSensor(SensorPort.S1);
  public static final EV3ColorSensor COLOR_SENSOR_R = new EV3ColorSensor(SensorPort.S4);
  
  // Odometer
  public static Odometer odometer = Odometer.getOdometer();
  
  //Setup LCD display here
  public static final TextLCD LCD = LocalEV3.get().getTextLCD();

  //General Constants
  public static final double WHEEL_RADIUS = 2.13;
  public static final double WHEEL_BASE = 11.7; //11.7
  public static final int ROTATION_SPEED = 60;
  public static final int ACCELERATION = 600;
  public static final int FORWARD_SPEED = 60;
  public static final double TILE_SIZE = 30.48;
  public static final int RISING = Button.ID_LEFT; //for USLocalizer
  public static final int FALLING = Button.ID_RIGHT; //for USLocalizer
  
  //USLocalizer
  public static final int US_SPEED = 150; //80
  public static final int RISE_THRESHOLD = 37; //38, 40
  public static final int RISE_ANGLE = 225;
  
  public static final int FALL_THRESHOLD = 25; //25
  public static final int FALL_ANGLE = 45; //45
  
  //ColorLocalizer
  public static final double COLOR_THRESHOLD_R = 0.55; //0.55
  public static final double COLOR_THRESHOLD_L = 0.58; //0.60
  
  public static final double CS_DISTANCE = 12; // sensor distance from center of rotation
  public static final int CS_SPEED = 120; //70
  public static final int CS_TUNNEL_SPEED = 120;//120
  
  //Navigation
  public static final int NAV_FORWARD = 200;
  public static final double NAV_OFFSET = 4.5*TILE_SIZE; //min offset = 4.5
  public static final int NAV_TURN = 90;
  public static final int NAV_TURN2 = 120;
  public static final int TUNNEL_SPEED = 200;
  
  //Obstacle Avoidance
  public static final int OBS_THRESHOLD = 30;
}
