package ca.mcgill.ecse211.project;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class Resources {
  
  //Setup two motors here
  public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor leftThrowMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
  public static final EV3LargeRegulatedMotor rightThrowMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
  public static final Port usPort = LocalEV3.get().getPort("S2");        
  public static final Port colorPort = LocalEV3.get().getPort("S1");   
  
  //Setup LCD display here
  public static final TextLCD LCD = LocalEV3.get().getTextLCD();

  //constants
  public static final double WHEEL_RADIUS = 2.13;
  public static final double WHEEL_BASE = 10.8;
  public static final int ROTATION_SPEED = 60;
  public static final int ACCELERATION = 600;
  public static final int FORWARD_SPEED = 60;
  public static final double TILE_SIZE = 30.48;
  
  //Change these experimentally for USLocalizer
  public static final int DISTANCE_TO_WALL = 25;
  public static final int NOISE_OFFSET = 2;
  
  //Change these experimentally for LightLocalizer
  public static final int DISTANCE_FROM_EDGE = 18;
  public static final double significantPercentThreshold = 20;
  public static final int lightSensorDistance = 15;
  
  //change for the LightLocalizer
  public static final int CORRECTION_PERIOD = 10;
  
  //For throwing the balls
  public static final int LAUNCHING_ACCELERATION = 10000;
}
