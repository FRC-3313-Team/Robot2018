package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Spark;

public class EncodedTankDrive {
	private Spark t1;
	private Spark t2;
	private Encoder leftEncod;
	private Encoder rightEncod;
	private double compRight = 0, compLeft = 0;
	private double aproximationLimiter = 0;
	private double RIGHT_SCALE = .978;

	public EncodedTankDrive(Spark left, Spark right, Encoder leftEncod, Encoder rightEncod) {
		t1 = left;
		t2 = right;
		this.leftEncod = leftEncod;
		this.rightEncod = rightEncod;
	}

	public void tankDrive(double speedLeft, double speedRight) {
		t1.set(speedLeft);
		t2.set(speedRight);
	}

	/**
	 * 
	 * @param speed between -1.0 and 1.0
	 * @param distance in inches, limit to 1 decimal place
	 * @return true
	 */
	public boolean driveStraight(double speed, double distance) {
		leftEncod.reset();
		rightEncod.reset();
		t1.set(speed);
		t2.set(-speed * RIGHT_SCALE);
		boolean str = true;
		while (str) {
			if (leftEncod.getDistance() >= distance) {
				str = false;
				break;
			}
			//System.out.print("" + leftEncod.getDistance(float));
		}
		t1.set(0);
		t2.set(0);
		return true;
	}

	public void setRightSidedCompensation(double d) {
		compRight = d;
	}

	public void setLeftSidedCompensation(double d) {
		compLeft = d;
	}

	public void setAproximationLimiter(double d) {
		aproximationLimiter = d;
	}

	private boolean appox(double trueValue, double valueToAproximate) {
		if ((Math.abs(valueToAproximate - trueValue)) <= this.aproximationLimiter) {
			return true;
		} else {
			return false;
		}
	}

}
