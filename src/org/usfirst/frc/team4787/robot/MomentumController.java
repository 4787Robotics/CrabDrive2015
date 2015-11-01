package org.usfirst.frc.team4787.robot;

import edu.wpi.first.wpilibj.Jaguar;

/* This code is untested and you probably shouldn't use it.  If you want to use *only the structure*
*  for a future robot, make sure you check all those stupid magic numbers, and probably do the
*  timing math over again for good measure.
*/
public class MomentumController extends Jaguar {

	public MomentumController(int channel) {
		super(channel);
	}
	
	public static double TICKVAL = .1;
	public static double DEADZONE = 0.06;
	public static double MAXLOW = -.4;
	public static double MAXHIGH = .17;
	
	
	/* 
	 * TEMP CHANGES: Mech securing mechanism broke on 7/18/2015 (MSI).  MAXLOW has been changed, MAXHIGH has been added.  In addition, TICKVAL has been halved.
	 * Please remove this note when the mech is fixed with something other than zip ties.
	 * 
	 *  Mechanism has been repaired; changes have been reverted - but this code isn't used anymore anyway.
	 */
	
	private double velocity = 0;
	
	public void set (double in, boolean precise) {
		if (precise) { // return to direct control if button is pressed
			super.set(in*.5); // Increased, but still less powerful than true direct control.
			velocity = 0;
			return;
		}
		
		
		// MAGIC NUMBERS EVERYWHERE
		
		if (alive(in)) {
			velocity+=(2*TICKVAL*in);
		}
		else {
			if (alive(velocity)) { // DECAY bullshit
					if (velocity>0) {
						velocity-=TICKVAL*25; // value changed from 25 to 2 to 25 again
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
