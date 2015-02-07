/** 
 *  FRC ROBOTICS TEAM 4787 - PROTOTYPE CODE
 *  AUTHORS: SOFTWARE SUBTEAM MEMBERS
 */

package org.usfirst.frc.team4787.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;

import java.lang.Math;
import java.util.Comparator;

import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;



public class Robot extends SampleRobot {
	
	public class ParticleReport implements Comparator<ParticleReport>, Comparable<ParticleReport>{
		double PercentAreaToImageArea;
		double Area;
		double BoundingRectLeft;
		double BoundingRectTop;
		double BoundingRectRight;
		double BoundingRectBottom;
		
		@Override
		public int compareTo(ParticleReport r)
		{
			return (int)(r.Area - this.Area);
		}
		@Override
		public int compare(ParticleReport r1, ParticleReport r2)
		{
			return (int)(r1.Area - r2.Area);
		}
	}

	//Structure to represent the scores for the various tests used for target identification
	public class Scores {
		double Area;
		double Aspect;
	}
	
	
	//Vision constants
	NIVision.Range TOTE_HUE_RANGE = new NIVision.Range(101, 64);	//Default hue range for yellow tote
	NIVision.Range TOTE_SAT_RANGE = new NIVision.Range(88, 255);	//Default saturation range for yellow tote
	NIVision.Range TOTE_VAL_RANGE = new NIVision.Range(134, 255);	//Default value range for yellow tote
	double AREA_MINIMUM = 0.5; //Default Area minimum for particle as a percentage of total image area
	double LONG_RATIO = 2.22; //Tote long side = 26.9 / Tote height = 12.1 = 2.22
	double SHORT_RATIO = 1.4; //Tote short side = 16.9 / Tote height = 12.1 = 1.4
	double SCORE_MIN = 75.0;  //Minimum score to be considered a tote
	double VIEW_ANGLE = 49.4; //View angle fo camera, set to Axis m1011 by default, 64 for m1013, 51.7 for 206, 52 for HD3000 square, 60 for HD3000 640x480
	NIVision.ParticleFilterCriteria2 criteria[] = new NIVision.ParticleFilterCriteria2[1];
	NIVision.ParticleFilterOptions2 filterOptions = new NIVision.ParticleFilterOptions2(0,0,1,1);
	Scores scores = new Scores();
	
	//Our constants
    Joystick drivestick, mechstick; JoystickButton trigger;
    final int RIGHT_PWM = 0, PERP_PWM = 1, LEFT_PWM = 2, MECH1_PWM = 3, MECH2_PWM = 4, MECH_UP = 3, MECH_DOWN = 2, DRIVESTICK_NUM = 0, MECHSTICK_NUM = 1;
    final double DEADZONEX = 0.05, DEADZONEY = 0.05;
    double lastTime = 0, expX = 0, expY = 0, x = 0, y = 0, z = 0, mechY = 0;
    boolean STRAFEMODE = true; 
    int session; Image frame; Image binaryFrame; int imaqError;
    String modestr;
    
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
    	

    	// Vision Dashboard code
    	frame = NIVision.imaqCreateImage(ImageType.IMAGE_RGB, 0);
		binaryFrame = NIVision.imaqCreateImage(ImageType.IMAGE_U8, 0);
		criteria[0] = new NIVision.ParticleFilterCriteria2(NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA, AREA_MINIMUM, 100.0, 0, 0);
		
		
    	// the camera name (ex "cam0") can be found through the roborio web interface
        session = NIVision.IMAQdxOpenCamera("cam0", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        NIVision.IMAQdxConfigureGrab(session);
        
        SmartDashboard.putString("Drive Mode:", modestr);
    } 

    /**
     * Move forward.
     * Called once each time the robot enters the autonomous state.
     */
	@Override
	public void autonomous() {
    	left.set(0.1);
    	right.set(0.1);
    }
    
    public void moveForwardAutonomous() //simplest auto. move forward for like 2.5 seconds to move leftmost tote.
    {
    	left.set(.5);
    	right.set(.5);
    	Timer.delay(2);
    	left.set(0);
    	right.set(0);
    }
    

    /**
     * A version of crab drive.
     * Called once each time the robot enters the operator-controlled state.
     */
    @Override
	public void operatorControl() {
    	NIVision.IMAQdxStartAcquisition(session);
		
    	while (isOperatorControl() && isEnabled()) {
    		x = drivestick.getX();
    		y = drivestick.getY();
    		mechY = mechstick.getY();
    		//camera setup
    		NIVision.IMAQdxGrab(session, frame, 1);
            CameraServer.getInstance().setImage(frame);
            
            //Trigger toggles drivemodes
    		if (trigger.get() || drivestick.getRawButton(1))
    		{
    			
    			if ((Timer.getFPGATimestamp() - lastTime) > 1)
    			{
    				lastTime = Timer.getFPGATimestamp();
    				STRAFEMODE = !STRAFEMODE;
    				//System.out.println("Strafemode: " + STRAFEMODE);
    			}
    		}
    		modestr = STRAFEMODE ? "Strafe" : "Car"; //refresh SmartDashboard value for modestr
    		 
    		
    		if(!STRAFEMODE)
    		{
    			System.out.println("Car mode | x: " + x + "  y:" + y);
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
    		
    		if(Math.abs(mechY) < DEADZONEY)
    		{
    			mech1.set(mechY/2);
    			mech2.set(mechY/2);
    		}
    	}
    	NIVision.IMAQdxStopAcquisition(session);
    }

    /**
     * Robot test mode.
     */
    @Override
	public void test() {
    	while (true) {
			/*left.set(0.2); Timer.delay(2);
			right.set(0.2); Timer.delay(2);
			perp.set(0.2); Timer.delay(2);
			mech1.set(0.2); Timer.delay(2);
			mech2.set(0.2); Timer.delay(2);*/
    		System.out.println( (drivestick.getRawButton(1) ? "1" : " ") + (drivestick.getRawButton(2) ? "2" : " ") + (drivestick.getRawButton(3) ? "3" : " ") );
    	}
  
    }
    
    @Override
	public void disabled()
    {
    	System.out.println("I'm disabled.");
    	NIVision.IMAQdxStopAcquisition(session);
    }

//Vision func dump

//Comparator function for sorting particles. Returns true if particle 1 is larger
	static boolean CompareParticleSizes(ParticleReport particle1, ParticleReport particle2)
	{
		//we want descending sort order
		return particle1.PercentAreaToImageArea > particle2.PercentAreaToImageArea;
	}

	/**
	 * Converts a ratio with ideal value of 1 to a score. The resulting function is piecewise
	 * linear going from (0,0) to (1,100) to (2,0) and is 0 for all inputs outside the range 0-2
	 */
	double ratioToScore(double ratio)
	{
		return (Math.max(0, Math.min(100*(1-Math.abs(1-ratio)), 100)));
	}

	double AreaScore(ParticleReport report)
	{
		double boundingArea = (report.BoundingRectBottom - report.BoundingRectTop) * (report.BoundingRectRight - report.BoundingRectLeft);
		//Tape is 7" edge so 49" bounding rect. With 2" wide tape it covers 24" of the rect.
		return ratioToScore((49/24)*report.Area/boundingArea);
	}

	/**
	 * Method to score if the aspect ratio of the particle appears to match the retro-reflective target. Target is 7"x7" so aspect should be 1
	 */
	double AspectScore(ParticleReport report)
	{
		return ratioToScore(((report.BoundingRectRight-report.BoundingRectLeft)/(report.BoundingRectBottom-report.BoundingRectTop)));
	}

	/**
	 * Computes the estimated distance to a target using the width of the particle in the image. For more information and graphics
	 * showing the math behind this approach see the Vision Processing section of the ScreenStepsLive documentation.
	 *
	 * @param image The image to use for measuring the particle estimated rectangle
	 * @param report The Particle Analysis Report for the particle
	 * @param isLong Boolean indicating if the target is believed to be the long side of a tote
	 * @return The estimated distance to the target in feet.
	 */
	double computeDistance (Image image, ParticleReport report) {
		double normalizedWidth, targetWidth;
		NIVision.GetImageSizeResult size;

		size = NIVision.imaqGetImageSize(image);
		normalizedWidth = 2*(report.BoundingRectRight - report.BoundingRectLeft)/size.width;
		targetWidth = 7;

		return  targetWidth/(normalizedWidth*12*Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
	}
}

//Garbage code dump (we may need this stuff later so w/e)
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

