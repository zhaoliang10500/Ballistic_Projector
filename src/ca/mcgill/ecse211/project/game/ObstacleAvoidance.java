package ca.mcgill.ecse211.project.game;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Resources.*;
import java.util.Arrays;
import ca.mcgill.ecse211.project.odometry.Odometer;

public class ObstacleAvoidance {
    
     int bandCenter = 11;
     int bandWidth = 3;
    private  SampleProvider us;
    private  float[] usData;
     String Obstacle_Direction = "";
    private  double PI = Math.PI;
    
    private  int filterSize = 3;
    private  int[] tempDists = new int[filterSize];
    private  boolean navigating = true;
    private Odometer odometer;
    private EV3LargeRegulatedMotor leftMotor;
    private EV3LargeRegulatedMotor rightMotor;
    private EV3LargeRegulatedMotor uSMotor;
    
    public ObstacleAvoidance(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
        EV3LargeRegulatedMotor sensorMotor){
    this.odometer = odometer;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.uSMotor = sensorMotor;
}
    
    public void travelTo(double x, double y, double angleOffset, boolean bin) {
      System.out.println("startttttttttt");
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
        
        leftMotor.setSpeed(ROTATE_SPEED);
        rightMotor.setSpeed(ROTATE_SPEED);
        System.out.println("turingnnnnnnnnnn: trajectory" + trajectoryAngle);
       // turnTo(trajectoryAngle);
        
        double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
        
        
        leftMotor.setSpeed(FORWARD_SPEED);
        rightMotor.setSpeed(FORWARD_SPEED);
        
//        leftMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
//        rightMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
       
        
        if (!bin) {
          leftMotor.rotate(Helper.convertDistance(trajectoryLine, WHEEL_RADIUS), true);
          rightMotor.rotate(Helper.convertDistance(trajectoryLine, WHEEL_RADIUS), true);
        } 
        else {
          leftMotor.rotate(Helper.convertDistance(Math.abs(trajectoryLine - (LAUNCH_GRID_DIST*TILE_SIZE)), WHEEL_RADIUS), true);
          rightMotor.rotate(Helper.convertDistance(Math.abs(trajectoryLine- (LAUNCH_GRID_DIST*TILE_SIZE)), WHEEL_RADIUS), true);
        }
        
        
        uSMotor.resetTachoCount();
        uSMotor.setSpeed(SCAN_SPEED);
        
       System.out.println("enterierererererw while");
        while (leftMotor.isMoving() || rightMotor.isMoving()) { // Scan the surrounding when the robot is moving
         System.out.println("dsadsadsadsadsadsad");
            while (!uSMotor.isMoving()){ //Rotate the sensor if it's not already rotating
             System.out.println("sssssssssssssssssssss");
            if (uSMotor.getTachoCount()>=CRITICAL_ANGLE){
                uSMotor.rotateTo(LEFT_ANGLE,true);
            } else {
                uSMotor.rotateTo(RIGHT_ANGLE,true);
            }
            }
            
            if(meanFilter() <= bandCenter){
              if (uSMotor.getTachoCount() > -5 && uSMotor.getTachoCount() < 100) {
                Obstacle_Direction = "ON_THE_RIGHT";
              } else  if (uSMotor.getTachoCount() > -100 && uSMotor.getTachoCount() <= -5) {
                Obstacle_Direction = "ON_THE_LEFT";
              }

                leftMotor.stop(true); // Stop the robot and quit navigation mode
                rightMotor.stop(false);
                navigating = false;
            }
            try { Thread.sleep(50); } catch(Exception e){}      // Poor man's timed sampling
        }
        
        if (!isNavigating() && Obstacle_Direction != null){
            avoidObstacle(Obstacle_Direction); // Implements bangbang controller to avoid the obstacle
            uSMotor.rotateTo(0); // reset sensor position
            navigating = true; // re-enable navigation mode
            travelTo(x,y,angleOffset,bin); // continue traveling to destination
            return;
        }
        uSMotor.rotateTo(0);
        
    }
    
    public  void turnTo(double theta) { //method from navigation program
   
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
    public  boolean isNavigating() {
        return navigating;
    }
    /* parameter: double distance representing the length of the line the vehicle has to run
     * returns: amount of degrees the motors have to turn to traverse this distance
     */
    private  int convertDistanceForMotor(double distance){
        return (int) (360*distance/(2*PI*WHEEL_RADIUS));
    }
    /* parameter: double angle representing the angle heading change in radians
     * returns: amount of degrees the motors have to turn to change this heading
     */
    private  int convertAngleForMotor(double angle){
        return convertDistanceForMotor(WHEEL_BASE*angle/2);
    }
    
    public  void avoidObstacle(String OBSTACLE_POSITION){
      if (OBSTACLE_POSITION == "ON_THE_LEFT") {
        // adjust the robot heading to ensure the avoidance of obstacles
        uSMotor.rotateTo(OBSTACLE_SENSOR_ANGLE);

        leftMotor.rotate(220, true);
        rightMotor.rotate(-220, false);
        
        // define the exit condition of avoidance mode
        double endAngle = odometer.getXYT()[2]/360*2*PI+PI*0.5;
      
        // engage bangbang controller to avoid the obstacle
        while (odometer.getXYT()[2]/360*2*PI<endAngle){
           
            int errorDistance = bandCenter - meanFilter();
            
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
        uSMotor.rotateTo(-OBSTACLE_SENSOR_ANGLE);

        leftMotor.rotate(-220, true);
        rightMotor.rotate(220, false);
        
        // define the exit condition of avoidance mode
        //double endAngle = odometer.getXYT()[2]/360*2*PI+PI*0.8;
        double endAngle = odometer.getXYT()[2]/360*2*PI-PI*0.5;
      
        // engage bangbang controller to avoid the obstacle
        while (odometer.getXYT()[2]/360*2*PI>endAngle){
            int errorDistance = bandCenter - meanFilter();
            
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
       
        leftMotor.stop();
        rightMotor.stop();
        
    }

    private  int meanFilter() {
      int distance;
      for (int i = 0; i < filterSize; i++) { 
        us.fetchSample(usData, 0); 
        tempDists[i] = (int) (usData[0] * 100.0); 
      }
      
      Arrays.sort(tempDists);
      distance = tempDists[filterSize/2]; //java rounds down for int division
      return distance;
    }
}