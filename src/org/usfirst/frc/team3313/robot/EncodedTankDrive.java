package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EncodedTankDrive {
	private Spark leftMotor;
	private Spark rightMotor;
	private Encoder leftEncod;
	private Encoder rightEncod;
	private double aproximationLimiter = 0;
	private double RIGHT_SCALE = .978;
	double Kp = 0.03;
	private double m_sensitivity = .5;

	public EncodedTankDrive(Spark left, Spark right, Encoder leftEncod, Encoder rightEncod) {
		leftMotor = left;
		rightMotor = right;
		this.leftEncod = leftEncod;
		this.rightEncod = rightEncod;
		// Gyro on Analog Channel 1
	}

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
		while (!(left==right==true)) {
			if (Math.abs(leftEncod.getDistance()) >= distance) {
				leftMotor.set(0);
				left = true;
			}
			//if (Math.abs(rightEncod.getDistance()) >= distance) {
			//	rightMotor.set(0);
			//	right = true;
			//}
		}
		leftMotor.set(0);
		rightMotor.set(0);
		return true;
	}

	public boolean driveToHeading(double degrees, double speed) {
		Robot.gyro.reset();
		double angle = getAngle();
		while (Math.abs(angle - degrees) > 1) {

			SmartDashboard.putNumber("Euro", getAngle());
			angle = getAngle(); // get current heading
			if (degrees > 0) {
				leftMotor.set(speed);
				rightMotor.set(speed * this.RIGHT_SCALE);
			} else {
				leftMotor.set(-speed);
				rightMotor.set(-speed * this.RIGHT_SCALE);
			}
			// this.drive(-speed, degrees * Kp); // drive towards heading 0
		}
		leftMotor.set(0);
		rightMotor.set(0);
		return true;
	}

	public boolean turnDirectrionInDegrees(double speed, double rotation) {

		return true;
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

	public void drive(double outputMagnitude, double curve) {
		final double leftOutput;
		final double rightOutput;

		if (curve < 0) {
			double value = Math.log(-curve);
			double ratio = (value - m_sensitivity) / (value + m_sensitivity);
			if (ratio == 0) {
				ratio = .0000000001;
			}
			leftOutput = outputMagnitude / ratio;
			rightOutput = outputMagnitude;
		} else if (curve > 0) {
			double value = Math.log(curve);
			double ratio = (value - m_sensitivity) / (value + m_sensitivity);
			if (ratio == 0) {
				ratio = .0000000001;
			}
			leftOutput = outputMagnitude;
			rightOutput = outputMagnitude / ratio;
		} else {
			leftOutput = outputMagnitude;
			rightOutput = outputMagnitude;
		}
		// setLeftRightMotorOutputs(leftOutput, rightOutput); Set motor speeds

		leftMotor.set(leftOutput);
		rightMotor.set(-rightOutput * RIGHT_SCALE);
	}

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

}
