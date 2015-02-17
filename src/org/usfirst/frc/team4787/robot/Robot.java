/** 
 *  FRC ROBOTICS TEAM 4787 - PROTOTYPE CODE
	PIZZA OBFUSCATION PROGRAM
 *  AUTHORS: SOFTWARE SUBMEME
 */

package org.usfirst.frc.team4787.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;

import java.lang.Math;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends SampleRobot 
{

	//Our constants
    Joystick drivestick, mechstick; JoystickButton trigger;
    final int  FLEFT_PWM = 4, BLEFT_PWM = 3, BRIGHT_PWM = 0, FRIGHT_PWM = 1, MECH1_PWM = 2, MECH2_PWM = 5, DRIVESTICK_NUM = 0, MECHSTICK_NUM = 1, UP_LIMIT = 0, DN_LIMIT = 1; // PWM, Stick, and Limiter ports
    int motorSwitch = 0; // Current motor for testing
    final double DEADZONEX = 0.08, DEADZONEY = 0.08, DEADZONEMECH = 0.06; //Deadzones
    double lastTime = 0, expX = 0, expY = 0, x = 0, y = 0, z = 0, mechX = 0, mechY = 0, mechZ = 0; // Motor powers
    boolean topLimited, bottomLimited; // Limiter statuses
    // Vision Shit
    int session; Image frame; Image binaryFrame;
    int autoSelect;
    
    // PWM declarations
    Jaguar bLeft = new Jaguar(BLEFT_PWM);
    Jaguar fLeft = new Jaguar(FLEFT_PWM) { @Override
	public void set(double val){super.set(1.45/3.2 * val); }};
    Jaguar bRight = new Jaguar(BRIGHT_PWM){ @Override
	public void set(double val){super.set(1.45/3.2 * val); }};
	
    Jaguar fRight = new Jaguar(FRIGHT_PWM);
    Jaguar mech1 = new Jaguar(MECH1_PWM);
    Jaguar mech2 = new Jaguar(MECH2_PWM);
    Jaguar[] motorList = {bLeft, fLeft, bRight, fRight, mech1, mech2};
    
    DigitalInput topLimit = new DigitalInput(UP_LIMIT);
    DigitalInput bottomLimit = new DigitalInput(DN_LIMIT);
    
    /**
    * Constructor for a 4787 Robot. 
    * Initializes robot joysticks and camera. 
    */
    public Robot () 
    {
    	drivestick = new Joystick(DRIVESTICK_NUM);
    	mechstick = new Joystick(MECHSTICK_NUM);
    	trigger = new JoystickButton(drivestick, 1);
    	
    	
    	// Vision Dashboard code
    	frame = NIVision.imaqCreateImage(ImageType.IMAGE_RGB, 0);
		binaryFrame = NIVision.imaqCreateImage(ImageType.IMAGE_U8, 0);
		
    	// The camera name (ex "cam0") can be found through the roborio web interface
        session = NIVision.IMAQdxOpenCamera("cam0", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        NIVision.IMAQdxConfigureGrab(session);
        
        //autoSelect = (int)SmartDashboard.getNumber("Autonomous");
        
        System.out.println("setting meme threshold to 75% dank");
    } 
    
    
    @Override
	public void operatorControl() 
    {
    	NIVision.IMAQdxStartAcquisition(session);
		
    	while (isOperatorControl() && isEnabled()) 
    	{
    		//Get input from joysticks
    		x = drivestick.getX();
    		y = drivestick.getY();
    		z = drivestick.getZ();
    		mechY = mechstick.getY();
    		mechZ = mechstick.getZ();
    		expX = Math.pow(x,3);
    		expY = Math.pow(y,3);
    		
    		//Get limiter readings
    		topLimited = !topLimit.get(); // Inverted because it doesn't work otherwise #justsoftwarethings
    		bottomLimited = !bottomLimit.get();
    		
    		//Camera setup
    		NIVision.IMAQdxGrab(session, frame, 1);
            CameraServer.getInstance().setImage(frame);
            
    		//Move the robot depending on joystick input
    		if(Math.abs(x) > DEADZONEX || Math.abs(y) > DEADZONEY){
	    		System.out.println("x: " + x + "  y: " + y + " z: " + z);
	    		bLeft.set(x - y);
	    		fLeft.set(x - y);
	    		bRight.set(x + y);
	    		fRight.set(x + y);
    		}
    		else
    		{
    			bLeft.set(0);
    			fRight.set(0);
    			fLeft.set(0);
    			bRight.set(0);
    		}
    		
    		//Move the mechanism depending on joystick input and check if limiters are activated
    		if(Math.abs(mechY) > DEADZONEMECH && (!(Math.signum(mechY)==-1 && topLimited) && !(Math.signum(mechY)==1 && bottomLimited)))
    		{
    			System.out.println("Mech Y: " + mechY);
    			mech1.set(mechY);
    			mech2.set(mechY);
    		}
    		else
    		{
    			mech1.set(0);
    			mech2.set(0);
    			if (Math.random()>.9) { System.out.println(Math.random()>.5 ? "TOO DANK" : "TOO MANY MEMES" ); } // lower frequency of memes
    		}
    	}
    	NIVision.IMAQdxStopAcquisition(session);
    }
    
    /**
     * Autonomous code.
     * Called once each time the robot enters the autonomous state.
     */
    
    
    @Override
	public void autonomous() 
	{
		System.out.println("I am currently in auto mode.");
		switch(autoSelect)
		{
		case 1: oneAutonomous(); break;
		case 2: case 3: twoThreeAutonomous(); break;
		default: System.out.println("No autonomous mode selected");
		}    
	}
	//Move forward for a short period to move leftmost tote into the score zone. **UNTESTED**
    
    final int TM_RATIO = 1;
    public void moveForward(int meters)
    {
    	bLeft.set(-.5);
    	bRight.set(.5);
    	fLeft.set(-.5);
    	fRight.set(.5);
    	Timer.delay(TM_RATIO*meters);
    	bLeft.set(0);
    	bRight.set(0);
    	fLeft.set(0);
    	fRight.set(0);
    }
    
    final int TR_RATIO = 1;
    public void turnDegrees(int degrees) //degrees. right is pos, left is neg
    {
    	bLeft.set(Math.signum(degrees)*-0.5);
    	bRight.set(Math.signum(degrees)*.5);
    	fLeft.set(Math.signum(degrees)*-0.5);
    	fRight.set(Math.signum(degrees)*.5);
    	Timer.delay(TR_RATIO*degrees/360);
    	bLeft.set(0);
    	bRight.set(0);
    	fLeft.set(0);
    	fRight.set(0);
    }
    
	public void oneAutonomous() 
    {
    	moveForward(10);
    }
    
    public void twoThreeAutonomous() //Grab tote from side, turn right and drive over Steppe
    {
    	mech1.set(0.5); mech2.set(0.5);
    	Timer.delay(0.25);
    	mech1.set(0); mech2.set(0); //picks it up
    	turnDegrees(90);
    	moveForward(8);
    	
    }
    
    /**
     * Robot test mode.
     */
    @Override
	public void test() 
    {
    	SmartDashboard.putNumber("Test Motor: ", motorSwitch);
    	while(isTest() && isEnabled()){
    		y = drivestick.getY();
    		if (trigger.get() || drivestick.getRawButton(1))
    		{
    			if ((Timer.getFPGATimestamp() - lastTime) > 1)
    			{
    				motorList[motorSwitch].set(0);
    				lastTime = Timer.getFPGATimestamp();
    				motorSwitch = (motorSwitch + 1) % 6;
    				System.out.println("Test Motor: " + motorSwitch);
    			}
    		}
    		
    		if(Math.abs(y) > DEADZONEY)
    		{
    			motorList[motorSwitch].set(y);
    		}
    	}
    }
    
    @Override
	public void disabled()
    {
    	System.out.println("I'm differently abled.");
    	NIVision.IMAQdxStopAcquisition(session);
/*    	System.out.println( "　   ＼＼   ＿____"+
							"    　　＼( ͡°_ ͡°)"+
							"    　　　 >　  ヽ"+
							"    　　　/ 　 へ＼"+ // switched encoding to UTF-8 to enable memes
							"    　　 /　　/　＼＼"+ // FUCK it didnt work
							"    　　 ﾚ　ノ　　 ヽ_つ ayy lmao"+
							"    　　/　/"+
							"    　 /　/|"+
							"    　(　(ヽ"+
							"    　|　|、＼"+
							"    　| 丿 ＼ ⌒)"+
							"    　| |　　) /"+
							"    `ノ )　　Lﾉ﻿");*/
    }
}


