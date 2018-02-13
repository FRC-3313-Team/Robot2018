/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.						*/
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.															   */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.cscore.UsbCamera;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {

	// Camera
	// +CheesyVisionServer server = CheesyVisionServer.getInstance();
	// public final int listenPort = 1180;

	// Encoder stuff
	Encoder ecodRight = new Encoder(9, 8, true, Encoder.EncodingType.k1X);
	Encoder ecodLeft = new Encoder(7, 6, false, Encoder.EncodingType.k1X);
	// Digital Channel Port, Digital Channel Port, invert, ignore

	// Drive
	EncodedTankDrive drive = new EncodedTankDrive(new Spark(8), new Spark(9), ecodLeft, ecodRight);

	// Limit Switch
	DigitalInput stage1UpLimit, stage1DownLimit, stage2UpLimit, stage2DownLimit;

	// Joystick
	Joystick controller = new Joystick(0);
	Joystick funcJoystick = new Joystick(1);

	//Gyro that is attached directly to the RIO
	static ADXRS450_Gyro gyro;

	// Talons
	Talon T3 = new Talon(3); // Stage 2 lift
	Talon T4 = new Talon(4); // Stage 1 lift
	Talon T5 = new Talon(5); // Intake L
	Talon T6 = new Talon(6); // Intake R
	Talon T7 = new Talon(7); // Intake Tilt
	Servo tom = new Servo(0); // Controls the stroller handle servo

	// Accelerated Movement May or may not work IDK
	double incrementSpeed = 0; // DO NOT TOUCH
	int currentSpeed = 0; // DO NOT TOUCH
	int noMovement = 0; // DO NOT TOUCH
	int ticksToWaitAfterNoMovement = 40;
	int ticksTillFullSpeed = 7; // 20 ~= 1 sec
	double maxSpeed = 100; // value where 100 is 100% of motor speed
	boolean respectMax = true; // Whether or not to respect full movement of
	// joystick or not, meaning max movement on joystick is the same as the maximum
	// speed versus
	// End

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		drive.tankDrive(0, 0);
		// Camera
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		camera.setResolution(640, 480);
		// CameraServer.getInstance().startAutomaticCapture();

		// Limit Switch
		stage1UpLimit = new DigitalInput(0);
		stage1DownLimit = new DigitalInput(1);
		stage2UpLimit = new DigitalInput(3);
		stage2DownLimit = new DigitalInput(2);
		// Magic numbers - just ignore this
		// double x = new Float(.05236111111);
		this.ecodLeft.setDistancePerPulse(.05236111111);
		this.ecodRight.setDistancePerPulse(.05236111111); // .05236111111

		gyro = new ADXRS450_Gyro();
		gyro.reset();
		// ShuffleBoard Stuff

		// Autonomous Stuff

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString line to get the
	 * auto name from the text box below the Gyro
	 *
	 * <p>
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the SendableChooser
	 * make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		first = true;
		tom.setAngle(0);
		drive.driveStraight(.25, 12); // Drive straight for speed, distance
		tom.setAngle(180);
		drive.driveToHeading(90, .25);
		drive.driveStraight(.25, 24);
	}

	@Override
	public void autonomousPeriodic() {
		while (isAutonomous()) {

		}
	}

	@Override
	public void teleopInit() {
		tom.set(20);
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {

		// Button map: A-1 B-2 X-3 Y-4 BumperR-5 BumperL-6 Back-7 Start-8
		this.advancedDrive(-controller.getX(), controller.getRawAxis(5));
		// this.advancedDrive(controller.getY(), controller.getRawAxis(4));
		// Shuffleboard
		SmartDashboard.putBoolean("Stage one: Up", stage1UpLimit.get());
		SmartDashboard.putBoolean("Stage one: Down", stage1DownLimit.get());
		SmartDashboard.putBoolean("Stage two: Up", stage2UpLimit.get());
		SmartDashboard.putBoolean("Stage two: Down", stage2DownLimit.get());
		SmartDashboard.putNumber("Left Pulses", ecodLeft.get());
		SmartDashboard.putNumber("Right Pulses", ecodRight.get());
		SmartDashboard.putNumber("Euro", drive.getAngle());
		SmartDashboard.putNumber("Tom Servo", tom.getAngle());

		// Intake
		if (controller.getRawButton(6)) {
			T5.set(1);
			T6.set(1);
		} else if (controller.getRawButton(5)) {
			T5.set(-1);
			T6.set(-1);
		} else {
			T5.set(0);
			T6.set(0);
		}
		// Tilt
		if (funcJoystick.getRawButton(7)) {
			T7.set(.5);
		} else if (funcJoystick.getRawButton(6)) {
			T7.set(-.5);
		} else {
			T7.set(0);

		}

		// Stage one lift
		if (funcJoystick.getRawButton(3) && stage1UpLimit.get()) { // Digital Input 0
			T4.set(-1); // Up
		} else if (funcJoystick.getRawButton(2) && stage1DownLimit.get()) { // Digital Input 2
			T4.set(1); // Down
		} else {
			T4.set(.15); // Change this value as needed to hold power in the motor
		}

		// Stage two lift
		if (funcJoystick.getRawButton(4) && stage2UpLimit.get()) { // Digital Output 3
			T3.set(1); // Up
		} else if (funcJoystick.getRawButton(5) && stage2DownLimit.get()) {
			T3.set(-.6); // Down
		} else {
			T3.set(0); // Change this value as needed to hold power in the motor
		}

		if (funcJoystick.getRawButton(10)) {
			tom.setAngle(180);
		}

	}

	// FIX DEADZONES
	private void advancedDrive(double rightStick, double leftStick) {
		// rightStick uses Y axis, leftStick uses rawAxis(5)
		if (rightStick == 0 && leftStick == 0) {
			if (noMovement == ticksToWaitAfterNoMovement) {
				currentSpeed = 0; // Reset the speed when no movement
				noMovement = 0;
			} else {
				noMovement++;
			}
			return;
		}
		rightStick = -rightStick * .5; // Invert
		if (respectMax) {
			double respectedValue = ((rightStick / 100) * maxSpeed); // New Respected speed
			// if (controller.getRawButton(5)) { // Ignore the advanced drive
			// drive.tankDrive(-(controller.getY() / 1.25) + (-controller.getRawAxis(5) /
			// 2),
			// (-controller.getY() / 1.25) + -(-controller.getRawAxis(5) / 2));
			// }
			if (currentSpeed != ticksTillFullSpeed) {
				currentSpeed++; // Calculate the next tick speed based off maxSpeed / ticksTillFullSpeed
				if (respectedValue <= (incrementSpeed * currentSpeed)) {
					drive.tankDrive(respectedValue + (-leftStick / 2), respectedValue + (leftStick / 2));
				} else {
					drive.tankDrive((incrementSpeed * currentSpeed) + (-leftStick / 2),
						(incrementSpeed * currentSpeed) + (leftStick / 2));
				}
			} else {
				drive.tankDrive(respectedValue + (-leftStick / 2), respectedValue + (leftStick / 2));
			}
			// double acclerationValue = (respectedValue / ticksTillFullSpeed) *
			// currentSpeed;
		}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {}
}