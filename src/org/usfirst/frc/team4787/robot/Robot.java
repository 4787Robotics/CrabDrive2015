/** 
 *  FRC ROBOTICS TEAM 4787 - PROTOTYPE CODE
 *  AUTHORS: SOFTWARE SUBTEAM MEMBERS
 */

package org.usfirst.frc.team4787.robot;

import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

public class Robot extends SampleRobot {
    Joystick drivestick, mechstick; JoystickButton trigger;
    final int LEFT_PWM = 0, RIGHT_PWM = 1, PERP_PWM = 2, MECH1_PWM = 3, MECH2_PWM = 4, MECH_UP = 3, MECH_DOWN = 2, DRIVESTICK_NUM = 0, MECHSTICK_NUM = 1;
    final double DEADZONEX = 0.05, DEADZONEY = 0.05;
    
    double lastTime = 0, expX = 0, expY = 0, x = 0, y = 0, z = 0, mechY = 0;
    boolean STRAFEMODE = true;
    
    Jaguar left = new Jaguar(LEFT_PWM);
    Jaguar right = new Jaguar(RIGHT_PWM);
    Jaguar perp = new Jaguar(PERP_PWM);
    Jaguar mech1 = new Jaguar(MECH1_PWM);
    Jaguar mech2 = new Jaguar (MECH2_PWM);

    /**
    * Constructor for a 4787 Robot. 
    * Initializes robot joysticks and camera. 
    */
    public Robot () {
    	drivestick = new Joystick(DRIVESTICK_NUM);
    	mechstick = new Joystick(MECHSTICK_NUM);
    	trigger = new JoystickButton(drivestick, 1);
    	CameraServer camera = CameraServer.getInstance();
    	camera.setQuality(100);
    	camera.startAutomaticCapture("cam0");
    }

    /**
     * Move forward.
     * Called once each time the robot enters the autonomous state.
     */
    @Override
	public void autonomous() {
    	System.out.println("I believe I am currently in autonmous mode.");
    	left.set(0.1); right.set(0.1);
    }

    /**
     * A version of crab drive.
     * Called once each time the robot enters the operator-controlled state.
     */
    @Override
	public void operatorControl() {
		/*
 		if (x > DEADZONEX || x < -DEADZONEX) {
			expX = Math.pow(x, 3); // x^3 for nonlinear control
			System.out.println("deadzonex");
		}
		if (y > DEADZONEY || y < -DEADZONEY) {
			expY = Math.pow(y, 3); // y^3 for nonlinear control
			System.out.println("deadzoney");
		}
		
	    // Motor power settings
		System.out.println((expX - expY)*z);
		left.set((expX - expY) * z); 
		System.out.println((expX + expY)*z);
		right.set((expX + expY) * z);
    	                             
    	mechY = mechstick.getY();
	    	                     
	    	if (mechY >= DEADZONEY) {
		    	mech1.set(mechY/2);
		    	mech2.set(mechY/2);
	    	}
	    	
		if (Math.abs(x) < DEADZONEX) {
			perp.set(x * z);
		}
		if (Math.abs(y) < DEADZONEY) {
			left.set(-y * z);
			right.set(-y * z);

		}
		
		*/
    	
    	while (true) {
    		x = drivestick.getX();
    		y = drivestick.getY();
    		
    		/*
    	
    		if(trigger.get())
    		{
    			if(lastTime - Timer.getFPGATimestamp() > 1)
    			{
    				lastTime = Timer.getFPGATimestamp();
    				STRAFEMODE = !STRAFEMODE;
    			}
    		} 
    		
    		*/
    		
    		if(!STRAFEMODE)
    		{
    			System.out.println("Gnomes mode | x: " + x + "  y:" + y);
    			left.set(x - y);
    			right.set(x + y);
    		}
    		else
    		{
    			System.out.println("Strafe mode | x: " + x + "   y:" + y);
    			left.set(-y);
    			right.set(y);
    			perp.set(-x);
    		}
    	}
    }

    /**
     * Robot test mode.
     */
    @Override
	public void test() {
    	while (true) {
			left.set(0.2); Timer.delay(2);
			right.set(0.2); Timer.delay(2);
			perp.set(0.2); Timer.delay(2);
			mech1.set(0.2); Timer.delay(2);
			mech2.set(0.2); Timer.delay(2);
    	}
  
    }
    
    @Override
	public void disabled()
    {
    	System.out.println("I'm disabled.");
    }
}
