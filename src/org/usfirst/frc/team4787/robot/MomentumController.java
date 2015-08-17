package org.usfirst.frc.team4787.robot;

import edu.wpi.first.wpilibj.Jaguar;

public class MomentumController extends Jaguar {

	public MomentumController(int channel) {
		super(channel);
	}
	
	public static double TICKVAL = .07; // originally .1
	public static double DEADZONE = 0.02; // originally 0.06
	public static double MAXLOW = -.06; // originally -.5
	public static double MAXHIGH = .17;
	
	
	/* 
	 * TEMP CHANGES: Mech securing mechanism broke on 7/18/2015 (MSI).  MAXLOW has been changed, MAXHIGH has been added.  In addition, TICKVAL has been halved.
	 * Please remove this note when the mech is fixed with something other than zip ties.
	 */
	
	private double velocity = 0;
	
	public void set (double in, boolean precise) {
		if (precise) { // return to direct control if button is pressed		// PRECISION MODE HAS BEEN DISABLED WHILE THE MECH IS NOT STABLE - REPLACE WHEN FIXED
			super.set(in*.3); //constant added instead of outright removal
			velocity = 0;
			return;
		}
		
		
		// MAXIC NUMBERS EVERYWHERE
		
		if (alive(in)) {
			velocity+=(2*TICKVAL*in);
		}
		else {
			if (alive(velocity)) { // DECAY bullshit
					if (velocity>0) {
						velocity-=TICKVAL*2; // magic number changed from 25 to allow low-speed up movements
						if (velocity<0) { velocity = 0; } // if we went too far go back to 0
					}
					if (velocity<0) {
						velocity+=TICKVAL*25;
						if (velocity>0) { velocity = 0; } // if we went too far go back to 0
					}
			}
			else { // if velocity is dead() kill it dead all the way until it is dead
				velocity = 0;
			}
		}
		velocity = stand_limit(velocity);
		velocity = applyMax(velocity);
		super.set(velocity);
		System.out.println(velocity);
	}
	
	public static double stand_limit ( double in) { // clamp a value to [-1,1]
		if (in<-1) {in = -1;}
		if (in>1) {in=1;}
		return in;
	}
	
	private static boolean alive (double in ){ // got tired of typing this out
		return Math.abs(in)>DEADZONE;
	}
	
	private static double applyMax( double in) {
		if (in<MAXLOW) { in = MAXLOW;}
		if (in>MAXHIGH) { in = MAXHIGH;}
		return in;
	}

	
	/* Full speed after 3/4 of a second
	 * 30ms per tick  --> 33 ticks per second
	 *  1 / (.75s) = change/time
	 *  
	 *  25 ticks for full power
	 *  tickval = .04
	 */
	
}
