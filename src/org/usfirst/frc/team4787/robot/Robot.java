
package org.usfirst.frc.team4787.robot;


import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.CameraServer;

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
    
    final int FLEFT_PWM = 1, BLEFT_PWM = 2, FRIGHT_PWM = 3, BRIGHT_PWM = 4, PERP_PWM = 5, MECH1_PWM = 6, MECH2_PWM = 7; //perp = perpendicular to others
    final int MECH_UP = 3, MECH_DOWN = 2; // buttons 
    final int TRIGGER = 1;
    final double DEADZONEX = 0.05, DEADZONEY = 0.05;
    double expX, expY;
    double m = 0.5;
    double x, y, z;
    boolean STRAFEMODE;
    Jaguar fLeft = new Jaguar(FLEFT_PWM);
    Jaguar bLeft = new Jaguar(BLEFT_PWM);
    Jaguar fRight= new Jaguar(FRIGHT_PWM);
    Jaguar bRight = new Jaguar(BRIGHT_PWM);
    Jaguar perp = new Jaguar(PERP_PWM);
    Jaguar mech1 = new Jaguar(MECH1_PWM);
    Jaguar mech2 = new Jaguar (MECH2_PWM);
    
    
    public Robot() {
    	stick = new Joystick(0);
    	CameraServer camera = CameraServer.getInstance();
    	camera.setQuality(100);
    	camera.startAutomaticCapture("cam0");
    }

    /**
     * Drive left & right motors for 2 seconds then stop
     */
    public void autonomous() {
    	while(true){
    		System.out.println();
    	}
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
    	while(true){
    		x = stick.getX();
    		y = stick.getY();
    		z = -(stick.getZ()); // Throttle control
    		STRAFEMODE = !stick.getRawButton(TRIGGER);
	    	if (STRAFEMODE)
	    	{
	    		if(Math.abs(x) < DEADZONEX){
	    			perp.set(x * z);
	    		}
	    		if(Math.abs(y) < DEADZONEY){
	    			fLeft.set(-y * z);
	    			bLeft.set(-y * z);
	    			fRight.set(-y * z);
	    			bRight.set(-y * z);
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
	    		fLeft.set((expX - expY) * z);
	    		bLeft.set((expX - expY) * z);
	    		fRight.set((expX + expY) * z);
	    		bRight.set((expX + expY) * z);
	    	}
	    	if (stick.getRawButton(MECH_UP)){
	    		mech1.set(m);
	    		mech2.set(m);
	    	}else if(stick.getRawButton(MECH_DOWN)){
	    		mech1.set(-m);
	    		mech2.set(-m);
	    	}else{
	    		mech1.set(0);
	    		mech2.set(0);
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
