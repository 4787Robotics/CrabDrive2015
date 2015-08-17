package org.usfirst.frc.team4787.robot;

import edu.wpi.first.wpilibj.Jaguar;

public class RampingMotorController extends Jaguar {
	public RampingMotorController(int channel) {
		super(channel);
	}

	public static double TICKVAL = .05; // Every tick, change the scale by this much.  Modify if needed.  Ticks are usually every 30ms, look in Robot.java
	public static double DEADZONE = 0.06; // Anything less than this magnitude is "dead".
	
	private double currentValue = 0;
	private double scaleFactor = 0;
	
	public void set ( double input) {
//		System.out.println(scaleFactor);
		if (alive(input)) { // input past the dead zones
			
		
			if ( alive(currentValue) && Math.signum(input)!=Math.signum(currentValue)) { // switching directions
				scaleFactor=0;
				currentValue = 0; // set power to zero when we flip.  Should make it a little more responsive at the cost of some brown-out protection.
			} else { // continuing onward
				scaleFactor+=TICKVAL;
				if (scaleFactor>1) { scaleFactor = 1;}
			}
		
		} else { // no input
			scaleFactor-=3*TICKVAL; // slow down faster than we ramp up
			if (scaleFactor<0) { scaleFactor = 0;}
		}
		currentValue = input*scaleFactor;
//		if (currentValue!=0 ) System.out.println(currentValue);
		super.set(currentValue);
	}
	
	private static boolean alive (double in ){ // got tired of typing this out
		return Math.abs(in)>DEADZONE;
	}
}
