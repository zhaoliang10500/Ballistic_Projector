package ca.mcgill.ecse211.project.game;
import ca.mcgill.ecse211.project.odometry.Odometer;
import ca.mcgill.ecse211.project.sensor.USUser;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Resources.*;

public class ObstacleAvoidance extends Thread implements USUser{
    
    private Odometer odometer;
    private EV3LargeRegulatedMotor leftMotor, rightMotor, sensorMotor;
    int bandCenter = 11;
    int bandWidth = 3;
    static int distance;
    private SampleProvider us;
    private float[] usData;
    String Obstacle_Direction = "";
    private static double PI = Math.PI;
    
    
    public ObstacleAvoidance(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
            EV3LargeRegulatedMotor sensorMotor, SampleProvider us, float[] usData){
        this.odometer = odometer;
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.sensorMotor = sensorMotor;
        this.us = us;
        this.usData = usData;
    }
    
    //constants
   
 

    private static boolean navigating = true;
    
    public void travelTo(double x, double y) {
      Obstacle_Direction = null;
        //reset motors
        for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
            motor.stop();
            motor.setAcceleration(3000);
        }
        
        //get distance and angle, and then move
        double trajectoryX = x - odometer.getXYT()[0];
        double trajectoryY = y - odometer.getXYT()[1];
        double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
        
        Sound.beepSequenceUp();
        leftMotor.setSpeed(ROTATE_SPEED);
        rightMotor.setSpeed(ROTATE_SPEED);
        turnTo(trajectoryAngle);
        
        double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
        
        Sound.beepSequence();
        
        leftMotor.setSpeed(FORWARD_SPEED);
        rightMotor.setSpeed(FORWARD_SPEED);
        
        leftMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
        rightMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
       
        
        int distance;
        sensorMotor.resetTachoCount();
        sensorMotor.setSpeed(SCAN_SPEED);
        
       
        while (leftMotor.isMoving() || rightMotor.isMoving()) { // Scan the surrounding when the robot is moving
         
            while (!sensorMotor.isMoving()){ //Rotate the sensor if it's not already rotating
             
            if (sensorMotor.getTachoCount()>=CRITICAL_ANGLE){
                sensorMotor.rotateTo(LEFT_ANGLE,true);
            } else {
                sensorMotor.rotateTo(RIGHT_ANGLE,true);
            }
            }
            
            us.fetchSample(usData,0);                           // acquire data
            distance=(int)(usData[0]*100.0);                    // extract from buffer, cast to int
            
            if(distance <= bandCenter){
              if (sensorMotor.getTachoCount() > -5 && sensorMotor.getTachoCount() < 100) {
                Obstacle_Direction = "ON_THE_RIGHT";
              } else  if (sensorMotor.getTachoCount() > -100 && sensorMotor.getTachoCount() <= -5) {
                Obstacle_Direction = "ON_THE_LEFT";
              }
                Sound.beep();
                leftMotor.stop(true); // Stop the robot and quit navigation mode
                rightMotor.stop(false);
                navigating = false;
            }
            try { Thread.sleep(50); } catch(Exception e){}      // Poor man's timed sampling
        }
        
        if (!this.isNavigating() && Obstacle_Direction != null){
            avoidObstacle(Obstacle_Direction); // Implements bangbang controller to avoid the obstacle
            sensorMotor.rotateTo(0); // reset sensor position
            navigating = true; // re-enable navigation mode
            travelTo(x,y); // continue traveling to destination
            return;
        }
        sensorMotor.rotateTo(0);
        
    }
    
    public void turnTo(double theta) { //method from navigation program
   
      double angle = getMinAngle(theta/360*2*PI- odometer.getXYT()[2]/360*2*PI);
     
      leftMotor.rotate(convertAngleForMotor(angle),true);
      rightMotor.rotate(-convertAngleForMotor(angle),false);
    }
    
    public double getMinAngle(double angle){
        if (angle > PI) {
            angle = 2*PI - angle;
        } else if (angle < -PI) {
            angle = angle + 2*PI;
        }
        return angle;
    }
    
    /* returns: whether or not the vehicle is currently navigating
     */
    public boolean isNavigating() {
        return navigating;
    }
    /* parameter: double distance representing the length of the line the vehicle has to run
     * returns: amount of degrees the motors have to turn to traverse this distance
     */
    private int convertDistanceForMotor(double distance){
        return (int) (360*distance/(2*PI*WHEEL_RADIUS));
    }
    /* parameter: double angle representing the angle heading change in radians
     * returns: amount of degrees the motors have to turn to change this heading
     */
    private int convertAngleForMotor(double angle){
        return convertDistanceForMotor(WHEEL_BASE*angle/2);
    }
    
    public void avoidObstacle(String OBSTACLE_POSITION){
      if (OBSTACLE_POSITION == "ON_THE_LEFT") {
        // adjust the robot heading to ensure the avoidance of obstacles
        sensorMotor.rotateTo(OBSTACLE_SENSOR_ANGLE);

        leftMotor.rotate(220, true);
        rightMotor.rotate(-220, false);
        
        // define the exit condition of avoidance mode
        double endAngle = odometer.getXYT()[2]/360*2*PI+PI*0.5;
        System.out.println(endAngle + "   EndAngle    "   + odometer.getXYT()[2]/360*2*PI + "   odometersssssssssss");
        // engage bangbang controller to avoid the obstacle
        while (odometer.getXYT()[2]/360*2*PI<endAngle){
          System.out.println(endAngle + "   EndAngle    "   + odometer.getXYT()[2]/360*2*PI + "   odometer");
            us.fetchSample(usData,0);                           // acquire data
            distance=(int)(usData[0]*100.0);                    // extract from buffer, cast to int
            int errorDistance = bandCenter - distance;
            System.out.println(distance + "disttttttttce");
            if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
                leftMotor.setSpeed(100 * 2);
                rightMotor.setSpeed(100 * 2);
                leftMotor.forward();
                rightMotor.forward();
            } else if (errorDistance > 0){ //too close to wall
                if (errorDistance > 10) {
                  leftMotor.setSpeed(100);// Setting the outer wheel to reverse
                  rightMotor.setSpeed(120); 
                  leftMotor.forward();
                  rightMotor.backward();
                } else {
                  leftMotor.setSpeed(200 * 3/5);
                  rightMotor.setSpeed(20 * 3/5); 
                  leftMotor.forward();
                  rightMotor.forward();
              }
            } else if (errorDistance < 0){ // getting too far from the wall
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(150);
                leftMotor.forward();
                rightMotor.forward();
            }
        }
      }
      else {
        // adjust the robot heading to ensure the avoidance of obstacles
        sensorMotor.rotateTo(-OBSTACLE_SENSOR_ANGLE);

        leftMotor.rotate(-220, true);
        rightMotor.rotate(220, false);
        
        // define the exit condition of avoidance mode
        //double endAngle = odometer.getXYT()[2]/360*2*PI+PI*0.8;
        double endAngle = odometer.getXYT()[2]/360*2*PI-PI*0.5;
        System.out.println(endAngle + "   EndAngle    "   + odometer.getXYT()[2]/360*2*PI + "   odometersssssssssss");
        // engage bangbang controller to avoid the obstacle
        while (odometer.getXYT()[2]/360*2*PI>endAngle){
          System.out.println(endAngle + "   EndAngle    "   + odometer.getXYT()[2]/360*2*PI + "   odometer");
            us.fetchSample(usData,0);                           // acquire data
            distance=(int)(usData[0]*100.0);                    // extract from buffer, cast to int
            int errorDistance = bandCenter - distance;
            
            if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
                leftMotor.setSpeed(100 * 2);
                rightMotor.setSpeed(100 * 2);
                leftMotor.forward();
                rightMotor.forward();
            } else if (errorDistance > 0){ //too close to wall
                if (errorDistance > 10) {
                  leftMotor.setSpeed(120);// Setting the outer wheel to reverse
                  rightMotor.setSpeed(100); 
                  leftMotor.backward();
                  rightMotor.forward();
                } else {
                  leftMotor.setSpeed(20 * 3/5);
                  rightMotor.setSpeed(200 * 3/5); 
                  leftMotor.forward();
                  rightMotor.forward();
              }
            } else if (errorDistance < 0){ // getting too far from the wall
                leftMotor.setSpeed(150);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
            }
        }
      }
       
        Sound.beep();
        leftMotor.stop();
        rightMotor.stop();
        
    }

    @Override
    public void processUSData(int distance) {
      ObstacleAvoidance.distance = distance;
      
    }

}