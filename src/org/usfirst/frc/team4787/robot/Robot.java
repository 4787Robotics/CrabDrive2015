
package org.usfirst.frc.team4787.robot;


import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

/**
 * This is a demo program showing the use of the RobotDrive class.
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */
public class Robot extends SampleRobot {
    Joystick stick;
    
    final int FLEFT_PWM = 1, BLEFT_PWM = 2, FRIGHT_PWM = 3, BRIGHT_PWM = 4, PERP_PWM = 5; //perp = perpendicular to others
    final int TRIGGER = 1;
    final double DEADZONEX = 0.05, DEADZONEY = 0.05;
    double expX, expY;
    
    double x, y;
    boolean STRAFEMODE;
    Jaguar fLeft = new Jaguar(FLEFT_PWM);
    Jaguar bLeft = new Jaguar(BLEFT_PWM);
    Jaguar fRight= new Jaguar(FRIGHT_PWM);
    Jaguar bRight = new Jaguar(BRIGHT_PWM);
    Jaguar perp = new Jaguar(PERP_PWM);
    
    
    public Robot() {
    	stick = new Joystick(0);
    }

    /**
     * Drive left & right motors for 2 seconds then stop
     */
    public void autonomous() {
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
    	while(true){
    		x = stick.getX();
    		y = stick.getY();
    		STRAFEMODE = !stick.getRawButton(TRIGGER);
	    	if (STRAFEMODE)
	    	{
	    		if(Math.abs(x) < DEADZONEX){
	    			perp.set(x);
	    		}
	    		if(Math.abs(y) < DEADZONEY){
	    			fLeft.set(-y);
	    			bLeft.set(-y);
	    			fRight.set(-y);
	    			bRight.set(-y);
	    		}
	    	}
	    	else
	    	{
	    		if(x>DEADZONEX || x<-DEADZONEX){
	    			expX = Math.pow(x, 3); // x^3 for nonlinear control
	    		}
	    		if(y>DEADZONEY || y<-DEADZONEY){
	    			expY = Math.pow(y, 3); // y^3 for nonlinear control
	    		}
	    	    // Motor power settings
	    		fLeft.set(expX - expY);
	    		bLeft.set(expX - expY);
	    		fRight.set(expX + expY);
	    		bRight.set(expX + expY);
	    	}
    	}
    }

    public void test() {
    	System.out.println("test method");
    }
    
    public void disabled()
    {
    	System.out.println("Ableism is alive");
    }
}
