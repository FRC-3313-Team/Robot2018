package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.*;

public class EncodedTankDrive {

	private Spark leftMotor;
	private Spark rightMotor;
	private Encoder leftEncod;
	private Encoder rightEncod;
	private double RIGHT_SCALE = .978;
	double Kp = 0.03;

	public EncodedTankDrive(Spark left, Spark right, Encoder leftEncod, Encoder rightEncod) {
		this.leftMotor = left;
		this.rightMotor = right;
		this.leftEncod = leftEncod;
		this.rightEncod = rightEncod;
	}

	/**
	 * Manual tank drive method. Use AdvancedDrive in Robot.java during TeleOp
	 */
	public void tankDrive(double speedLeft, double speedRight) {
		leftMotor.set(speedLeft);
		rightMotor.set(speedRight);
	}

	/**
	 *
	 * @param speed
	 *            between -1.0 and 1.0
	 * @param distance
	 *            in inches, limit to 1 decimal place
	 * @return true
	 */
	public boolean driveStraight(double speed, double distance) {
		leftEncod.reset();
		rightEncod.reset();
		leftMotor.set(speed);
		rightMotor.set(-speed * RIGHT_SCALE);
		boolean left = false;
		boolean right = true;
		while (!(left == right == true)) {
			if (Math.abs(leftEncod.getDistance()) >= ((distance / 4) * 3)) {
				if (Math.abs(leftEncod.getDistance()) >= ((distance / 10) * 9)) {
					if (speed > .75) {
						leftMotor.set(speed / 3);
						rightMotor.set(speed / 3);
					}
				} else {
					if (speed > .75) {
						leftMotor.set(speed / 2);
						rightMotor.set(speed / 2);
					}
				}
			}
			if (Math.abs(leftEncod.getDistance()) >= distance) {
				leftMotor.set(0);
				left = true;
			}
			// if (Math.abs(rightEncod.getDistance()) >= distance) {
			// rightMotor.set(0);
			// right = true;
			// }
		}
		leftMotor.set(0);
		rightMotor.set(0);

		while (!this.leftEncod.getStopped() && !rightEncod.getStopped()) {
		}
		return true;
	}

	/**
	 * Drives to a specific rotation at a specified speed.
	 *
	 * @param degrees
	 *            How much to turn by. Positive values are clockwise; negative are
	 *            anti-clockwise
	 * @param speed
	 *            How fast to turn. We usually use .25
	 * @return
	 */
	public boolean rotateByDegrees(double degrees, double speed) {
		Robot.gyro.reset();
		double angle = getAngle();
		while (Math.abs(angle - degrees) > 1) {
			angle = getAngle(); // get current heading
			if (degrees > 0) {
				leftMotor.set(speed);
				rightMotor.set(speed * this.RIGHT_SCALE);
			} else {
				leftMotor.set(-speed);
				rightMotor.set(-speed * this.RIGHT_SCALE);
			}
		}
		leftMotor.set(0);
		rightMotor.set(0);
		while (!this.leftEncod.getStopped() && !rightEncod.getStopped()) {
		}
		return true;
	}

	/**
	 * Wraps the gyro output to a range of -360 to +360
	 * 
	 * @return the wrapped angle
	 */
	public double getAngle() {
		double angle = Robot.gyro.getAngle();
		while (angle > 360) {
			angle -= 360;
		}
		while (angle < -360) {
			angle += 360;
		}
		return angle;
	}

	/**
	 * Attempts to adjust the robots heading.
	 * @param The angle that was attempted to be reached
	 * @return Weather an adjustment was needed or not
	 */
	public boolean fixHeading(double AngleAttempted) {
		double d = Robot.gyro.getAngle() - AngleAttempted;
		if (d < 0) {
			// Robot Needs to turn positive degrees.
			rotateByDegrees(Math.abs(d), .25);
			return true;
		} else if (d > 0) {
			// Robot needs to turn negative degrees.
			rotateByDegrees((d *= -1), .25);
			return true;
		} else {
			// Robot somehow managed to stay straight?
			return false;
		}
	}

	/**
	 * Attempt to break the motors by setting the speed in the opposite direction
	 * for .2 seconds.
	 * 
	 * @param The
	 *            about of force to push back against the motors, too much jerk
	 *            could case harm to the motors. USE WITH CAUTION.
	 */
	public void brakeMotors(double force) {
		rightMotor.set(rightMotor.get() > 0 ? -force : force);
		leftMotor.set(leftMotor.get() > 0 ? -force : force);
		Timer.delay(.2);
		leftMotor.set(0);
		rightMotor.set(0);
	}

}
