/** 
 *  FRC ROBOTICS TEAM 4787 - PROTOTYPE CODE
	CRAB DRIVE
 *  AUTHORS: SOFTWARE SUBTEAM MEMBERS
 */

package org.usfirst.frc.team4787.robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;

import java.lang.Math;
import java.util.Comparator;
import java.util.Vector;

import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends SampleRobot 
{
	public class ParticleReport implements Comparator<ParticleReport>, Comparable<ParticleReport> 
	{
		double PercentAreaToImageArea, Area;
		double BoundingRectLeft, BoundingRectTop, BoundingRectRight, BoundingRectBottom;
		
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
	public class Scores 
	{
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
	
	
	//Our constants
    Joystick drivestick, mechstick; JoystickButton trigger;
    final int PERP_PWM = 0, RIGHT_PWM = 1, LEFT_PWM = 2, MECH1_PWM = 3, MECH2_PWM = 4, MECH_UP = 3, MECH_DOWN = 2, DRIVESTICK_NUM = 0, MECHSTICK_NUM = 1;
    final double DEADZONEX = 0.08, DEADZONEY = 0.08, DEADZONEMECH = 0.06;
    double lastTime = 0, expX = 0, expY = 0, x = 0, y = 0, z = 0, mechY = 0, mechZ = 0;
    boolean STRAFEMODE = true; 
    int session; Image frame; Image binaryFrame; int imaqError;
    int autoSelect;
    String modestr;
    
    Jaguar left = new Jaguar(LEFT_PWM);
    Jaguar right = new Jaguar(RIGHT_PWM);
    Jaguar perp = new Jaguar(PERP_PWM);
    Jaguar mech1 = new Jaguar(MECH1_PWM);
    Jaguar mech2 = new Jaguar(MECH2_PWM);
    
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
		criteria[0] = new NIVision.ParticleFilterCriteria2(NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA, AREA_MINIMUM, 100.0, 0, 0);
		
    	// The camera name (ex "cam0") can be found through the roborio web interface
        session = NIVision.IMAQdxOpenCamera("cam0", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        NIVision.IMAQdxConfigureGrab(session);
        
        //SmartDashboard.putString("Drive Mode:", modestr);
        //autoSelect = (int)SmartDashboard.getNumber("Autonomous");
    } 

    /**
     * Autonmous code.
     * Called once each time the robot enters the autonomous state.
     */
	@Override
	public void autonomous() 
	{
		System.out.println("I am currently in auto mode.");
		switch(autoSelect)
		{
		case 1: moveForwardAutonomous(); break;
		case 2: middleToteAutonomous(); break;
		case 3: finalToteAutonomous(); break; 
		case 4: visionStackAutonomous(); break;
		default: System.out.println("No autonomous mode selected");
		}

    }
    
	public void visionStackAutonomous() //Stack 3 totes w/ vision
	{
		while (isAutonomous() && isEnabled()) 
		{
			NIVision.imaqReadFile(frame, "/home/lvuser/SampleImages/image21.jpg"); 
			
			//Update threshold values from SmartDashboard. For performance reasons it is recommended to remove this after calibration is finished.
			TOTE_HUE_RANGE.minValue = (int)SmartDashboard.getNumber("Tote hue min", TOTE_HUE_RANGE.minValue);
			TOTE_HUE_RANGE.maxValue = (int)SmartDashboard.getNumber("Tote hue max", TOTE_HUE_RANGE.maxValue);
			TOTE_SAT_RANGE.minValue = (int)SmartDashboard.getNumber("Tote sat min", TOTE_SAT_RANGE.minValue);
			TOTE_SAT_RANGE.maxValue = (int)SmartDashboard.getNumber("Tote sat max", TOTE_SAT_RANGE.maxValue);
			TOTE_VAL_RANGE.minValue = (int)SmartDashboard.getNumber("Tote val min", TOTE_VAL_RANGE.minValue);
			TOTE_VAL_RANGE.maxValue = (int)SmartDashboard.getNumber("Tote val max", TOTE_VAL_RANGE.maxValue);
			
			//Threshold the image looking for yellow (tote color)
			NIVision.imaqColorThreshold(binaryFrame, frame, 255, NIVision.ColorMode.HSV, TOTE_HUE_RANGE, TOTE_SAT_RANGE, TOTE_VAL_RANGE);
			
			int numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
			
			//Send masked image to dashboard to assist in tweaking mask.
			CameraServer.getInstance().setImage(binaryFrame);
			
			//Filter out small particles
			float areaMin = (float)SmartDashboard.getNumber("Area min %", AREA_MINIMUM);
			criteria[0].lower = areaMin;
			imaqError = NIVision.imaqParticleFilter4(binaryFrame, binaryFrame, criteria, filterOptions, null);

			//Send particle count after filtering to dashboard
			numParticles = NIVision.imaqCountParticles(binaryFrame, 1);
			SmartDashboard.putNumber("Filtered particles", numParticles);
			
			if (numParticles > 0) 
			{
				Vector<ParticleReport> particles = new Vector<ParticleReport>();
				
				for (int particleIndex = 0; particleIndex < numParticles; particleIndex++) 
				{
					ParticleReport par = new ParticleReport();
					
					par.PercentAreaToImageArea = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA);
					par.Area = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_AREA);
					par.BoundingRectTop = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_BOUNDING_RECT_TOP);
					par.BoundingRectLeft = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_BOUNDING_RECT_LEFT);
					par.BoundingRectBottom = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_BOUNDING_RECT_BOTTOM);
					par.BoundingRectRight = NIVision.imaqMeasureParticle(binaryFrame, particleIndex, 0, NIVision.MeasurementType.MT_BOUNDING_RECT_RIGHT);
					
					particles.add(par);
				}
				
				particles.sort(null);
				Scores[] scoresList = new Scores[numParticles];
				
				//Score each particle and store scores into scoresList[]
				for(int i = 0; i < numParticles; i++) 
				{
					scoresList[i].Aspect = AspectScore(particles.elementAt(i));
					scoresList[i].Area = AreaScore(particles.elementAt(i));
				}
				
				//Output the scores of particle 0 to the smart dashboard
				SmartDashboard.putNumber("Aspect 0 (supposedly the Largest Particle)", scoresList[0].Aspect);
				SmartDashboard.putNumber("Area 0 (supposedly the Largest Particle)", scoresList[0].Area);
				
				//Output the scores of particle 1 to the smart dashboard
				SmartDashboard.putNumber("Aspect 1 (supposedly the 2nd Largest Particle)", scoresList[1].Aspect);
				SmartDashboard.putNumber("Area 1 (supposedly the 2nd Largest Particle)", scoresList[1].Area);

				
				//Determine if we are looking at a tote based on the scores
				boolean isTote = scoresList[0].Aspect > SCORE_MIN && scoresList[0].Area > SCORE_MIN;
				
				//Send distance and tote status to dashboard. The bounding rect, particularly the horizontal center (left - right) may be useful for rotating/driving towards a tote
				SmartDashboard.putBoolean("IsTote", isTote);
				SmartDashboard.putNumber("Distance 0", computeDistance(binaryFrame, particles.elementAt(0)));
				SmartDashboard.putNumber("Distance 1", computeDistance(binaryFrame, particles.elementAt(1)));
				SmartDashboard.putNumber ("Distance (Mean of 0 and 1)", ( ( computeDistance(binaryFrame, particles.elementAt(0)) + computeDistance(binaryFrame, particles.elementAt(1)) ) / 2 ));
				
		
			}
			else
			{
				SmartDashboard.putBoolean("IsTote", false);
			}
			
			Timer.delay(0.005);
		}
		
	}
	//Move forward for a short period to move leftmost tote into the score zone. **UNTESTED**
    public void moveForwardAutonomous() 
    {
    	left.set(-0.3); right.set(0.3); //Move forward for 2 seconds
    	Timer.delay(4);
    	left.set(0); right.set(0);
    }
    
    public void middleToteAutonomous() //Grab tote from side, strafe right then strafe forward and right again.
    {
    	mech1.set(0.5); mech2.set(0.5);
    	Timer.delay(0.5);
    	mech1.set(0); mech2.set(0); //picks it up
    	perp.set(-0.5);
    	Timer.delay(0.5);
    	perp.set(0); //hopefully, strafes right
    	left.set(-0.3); right.set(-0.3);
    	Timer.delay(1);
    	left.set(0); right.set(0); //moves forward
    	perp.set(-0.5);
    	Timer.delay(0.5);
    	perp.set(0); //hopefully, strafes right again
    }
    
    public void finalToteAutonomous() //Grab tote from side, strafe right then strafes forward longer and right again.
    {
    	mech1.set(0.5); mech2.set(0.5);
    	Timer.delay(0.5);
    	mech1.set(0); mech2.set(0); //picks it up
    	perp.set(-0.5);
    	Timer.delay(0.5);
    	perp.set(0); //hopefully, strafes right
    	left.set(-0.3); right.set(-0.3);
    	Timer.delay(3);
    	left.set(0); right.set(0); //moves forward
    	perp.set(-0.5);
    	Timer.delay(0.5);
    	perp.set(0); //hopefully, strafes right again
    }
    
    /**
     * A version of crab drive.
     * Called once each time the robot enters the operator-controlled state.
     */
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
    		
    		//Camera setup
    		NIVision.IMAQdxGrab(session, frame, 1);
            CameraServer.getInstance().setImage(frame);
            
            //Toggles drivemode depending on trigger input
    		if (trigger.get() || drivestick.getRawButton(1))
    		{
    			if ((Timer.getFPGATimestamp() - lastTime) > 1)
    			{
    				lastTime = Timer.getFPGATimestamp();
    				STRAFEMODE = !STRAFEMODE;
    			}
    		}
    		modestr = STRAFEMODE ? "Strafe" : "Car"; //refresh SmartDashboard value for modestr
    		
    		//Move the robot depending on joystick input
    		if(Math.abs(x) > DEADZONEX || Math.abs(y) > DEADZONEY){
	    		if(!STRAFEMODE)
	    		{
	    			System.out.println("Car mode | x: " + x + "  y:" + y + "z:" + z);
	    			left.set(x - y);
	    			right.set(x + y);
	    		}
	    		else
	    		{
	    			System.out.println("Strafe mode | x: " + x + "   y:" + y + "z:" + z);
	    			left.set(-expY/2);
	    			right.set(expY/2);
	    			perp.set(-expX/2);
	    		}
    		}
    		else
    		{
    			left.set(0);
    			right.set(0);
    			perp.set(0);
    		}
    		
    		//Move the mechanism depending on joystick input
    		if(Math.abs(mechY) > DEADZONEMECH)
    		{
    			System.out.println("Mech Y: " + mechY);
    			mech1.set(mechY);
    			mech2.set(mechY);
    		}
    		else
    		{
    			mech1.set(0);
    			mech2.set(0);
    		}
    	}
    	
    	
    	NIVision.IMAQdxStopAcquisition(session);
    }

    /**
     * Robot test mode.
     */
    @Override
	public void test() 
    {
		//left.set(0.2); Timer.delay(2);
		//right.set(0.2); Timer.delay(2);
		perp.set(0.5); Timer.delay(3);
		perp.set(-0.5); Timer.delay(3);
		
		perp.set(0.5); Timer.delay(3);
		perp.set(-0.5); Timer.delay(3);
		
		//left.set(0.3); right.set(0.3); Timer.delay(1.5);
		
		//left.set(-0.3); right.set(-0.3); Timer.delay(1.5);
		
		//left.set(0.3); right.set(-0.3); Timer.delay(1.5);
		
		//left.set(-0.3); right.set(0.3); Timer.delay(1.5);
		
		//mech1.set(0.1); Timer.delay(0.4);
		
		//mech2.set(-0.1); Timer.delay(0.4);
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
		//We want descending sort order
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
	double computeDistance (Image image, ParticleReport report) 
	{
		double normalizedWidth, targetWidth;
		NIVision.GetImageSizeResult size;

		size = NIVision.imaqGetImageSize(image);
		normalizedWidth = 2*(report.BoundingRectRight - report.BoundingRectLeft)/size.width;
		targetWidth = 7;

		return  targetWidth/(normalizedWidth*12*Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
	}
}


