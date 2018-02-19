/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.						*/
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.															   */
/*----------------------------------------------------------------------------*/
package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.*;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.*;

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
	Encoder encodeRight = new Encoder(9, 8, true, Encoder.EncodingType.k1X);
	Encoder encodeLeft = new Encoder(7, 6, false, Encoder.EncodingType.k1X);
	// Digital Channel Port, Digital Channel Port, invert, ignore

	// Drive
	EncodedTankDrive drive = new EncodedTankDrive(new Spark(8), new Spark(9), encodeLeft, encodeRight);

	// Limit Switch
	DigitalInput stage1UpLimit, stage1DownLimit, stage2UpLimit, stage2DownLimit;

	// Joystick
	Joystick controller = new Joystick(0);
	Joystick funcJoystick = new Joystick(1);

	// Gyro that is attached directly to the RIO
	static ADXRS450_Gyro gyro;

	// Talons
	Talon stage2 = new Talon(3); // Stage 2 lift
	Talon stage1 = new Talon(4); // Stage 1 lift
	Talon intakeL = new Talon(5); // Intake L
	Talon intakeR = new Talon(6); // Intake R
	Talon tilt = new Talon(7); // Intake Tilt

	// Servos
	Servo drop = new Servo(0);

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

	private SendableChooser<Integer> autoChoosePosition = new SendableChooser<>();
	private int selectedAutoPosition;
	private SendableChooser<Integer> autoChooseDistance = new SendableChooser<>();
	private int selectedAutoDistance;

	private DriverStation ds = DriverStation.getInstance();

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		drive.tankDrive(0, 0);

		autoChoosePosition.addDefault("Position 1 Auto", 1);
		autoChoosePosition.addObject("Position 2 Auto", 2);
		autoChoosePosition.addObject("Drive Forward", 3);
		SmartDashboard.putData("Auto Position", autoChoosePosition);

		autoChooseDistance.addDefault("Auto to Switch", 1);
		autoChooseDistance.addObject("Auto to Scale", 2);
		autoChooseDistance.addObject("Drive Forward", 3);
		SmartDashboard.putData("Auto Distance", autoChooseDistance);

		// Camera
		// UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		// camera.setResolution(640, 480);
		// CameraServer.getInstance().startAutomaticCapture();

		// Limit Switch
		stage1UpLimit = new DigitalInput(0);
		stage1DownLimit = new DigitalInput(1);
		stage2UpLimit = new DigitalInput(3);
		stage2DownLimit = new DigitalInput(2);
		// Magic numbers - just ignore this
		// double x = new Float(.05236111111);
		encodeLeft.setDistancePerPulse(.05236111111);
		encodeRight.setDistancePerPulse(.05236111111); // .05236111111

		gyro = new ADXRS450_Gyro();
		gyro.reset();
		// ShuffleBoard Stuff
		// Autonomous Stuff
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard.
	 */
	@Override
	public void autonomousInit() {
		selectedAutoPosition = autoChoosePosition.getSelected();
		selectedAutoDistance = autoChooseDistance.getSelected();

		tilt.set(-.75);
		Timer.delay(.2);
		tilt.set(.3);

		String message = ds.getGameSpecificMessage();
		char switchSide = message.charAt(0);
		char scaleSide = message.charAt(1);

		if (selectedAutoPosition == 1) {// If we are in position 1 (right side)
			if (selectedAutoDistance == 1) {// If we want to target the switch
				if (switchSide == 'R') {// If our color is on the right
					drive.driveStraight(.50, 150);
					drive.rotateByDegrees(-90, .25);
					autoRaiseStage2();
					// drive.driveStraight(.25, 20);
					autoShoot();
				} else {// If our color is on the left
					drive.driveStraight(.25, 181);
					drive.rotateByDegrees(-90, .25);
					drive.driveStraight(.25, 175);
					drive.rotateByDegrees(-90, .25);
					// TODO maybe drive forward a few inches
					autoRaiseStage2();
					drive.driveStraight(.25, 20);
					autoShoot();
				}
			} else {// If we want to target the scale
				if (scaleSide == 'R') {// If our color is on the right
					drive.driveStraight(.55, 261.47);
					drive.rotateByDegrees(-45, .25);
					autoRaiseStage1();
					autoRaiseStage2();
					autoShoot();
				} else {// If our color is on the left
					drive.driveStraight(.5, 181);
					drive.rotateByDegrees(-90, .25);
					drive.driveStraight(.25, 193);
					drive.rotateByDegrees(90, .25);
					drive.driveStraight(.25, 18);
					autoRaiseStage1();
					autoRaiseStage2();
					autoShoot();
				}
			}
		} else {
			if (selectedAutoDistance == 1) {// If we want to target the switch DONE
				if (switchSide == 'L') {// If our color is on the left
					drive.driveStraight(.50, 150);
					drive.rotateByDegrees(90, .25);
					autoRaiseStage2();
					// drive.driveStraight(.25, 20);
					autoShoot();
				} else {// If our color is on the right
					drive.driveStraight(.5, 200);
					drive.rotateByDegrees(90, .25);
					drive.driveStraight(.5, 100);
					drive.rotateByDegrees(90, .25);
					// TODO maybe drive forward a few inches
					autoRaiseStage2();
					drive.driveStraight(.25, 20);
					// autoShoot();
				}
			} else {// If we want to target the scale
				if (scaleSide == 'L') {// If our color is on the left
					drive.driveStraight(.55, 261.47);
					drive.rotateByDegrees(45, .25);
					autoRaiseStage1();
					autoRaiseStage2();
					autoShoot();
				} else {// If our color is on the right
					drive.driveStraight(.5, 181);
					drive.rotateByDegrees(90, .25);
					drive.driveStraight(.25, 193);
					drive.rotateByDegrees(-90, .25);
					drive.driveStraight(.25, 18);
					autoRaiseStage1();
					autoRaiseStage2();
					autoShoot();
				}
			}
		}
	}

	@Override
	public void autonomousPeriodic() {

	}

	/**
	 * Used to shoot in auto
	 */
	public void autoShoot() {
		intakeL.set(-.5);
		intakeR.set(-.5);
		Timer.delay(.25);
		intakeL.set(0);
		intakeR.set(0);
		tilt.set(0);
	}

	public void autoRaiseStage2() {
		stage2.set(1);
		while (stage2UpLimit.get()) {
		}
		stage2.set(0);
	}

	public void autoRaiseStage1() {
		stage1.set(.5);
		while (stage1UpLimit.get()) {
		}
		stage1.set(0);
	}

	@Override
	public void teleopInit() {
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {

		// Button map: A-1 B-2 X-3 Y-4 BumperR-5 BumperL-6 Back-7 Start-8
		advancedDrive(-controller.getX(), controller.getRawAxis(5));
		// Shuffleboard
		SmartDashboard.putBoolean("Stage one: Up", stage1UpLimit.get());
		SmartDashboard.putBoolean("Stage one: Down", stage1DownLimit.get());
		SmartDashboard.putBoolean("Stage two: Up", stage2UpLimit.get());
		SmartDashboard.putBoolean("Stage two: Down", stage2DownLimit.get());
		SmartDashboard.putNumber("Left Pulses", encodeLeft.get());
		SmartDashboard.putNumber("Right Pulses", encodeRight.get());
		SmartDashboard.putNumber("Euro", drive.getAngle());
		SmartDashboard.putString("Game Message", ds.getGameSpecificMessage());
		SmartDashboard.putNumber("ATK Z", -funcJoystick.getZ());

		double joyZ = (funcJoystick.getZ() * -0.5) + 0.5;
		// Intake
		if (controller.getRawButton(6)) {
			intakeL.set(joyZ);
			intakeR.set(joyZ);
		} else if (controller.getRawButton(5)) {
			intakeL.set(-joyZ);
			intakeR.set(-joyZ);
		} else {
			intakeL.set(0);
			intakeR.set(0);
		}
		// Tilt
		if (funcJoystick.getRawButton(5)) {
			tilt.set(.5);
		} else if (funcJoystick.getRawButton(4)) {
			tilt.set(-.5);
		} else {
			tilt.set(0.1);

		}

		// Stage one lift
		if (funcJoystick.getRawButton(6) && stage1UpLimit.get()) { // Digital Input 0
			stage1.set(-1); // Up
		} else if (funcJoystick.getRawButton(7) && stage1DownLimit.get()) { // Digital Input 2
			stage1.set(1); // Down
		} else {
			if (stage1DownLimit.get()) {
				stage1.set(0.15); // Change this value as needed to hold power in the motor
			}else {
				stage1.set(0);
			}
		}

		// Stage two lift
		if (funcJoystick.getRawButton(3) && stage2UpLimit.get()) { // Digital Output 3
			stage2.set(1); // Up
		} else if (funcJoystick.getRawButton(2) && stage2DownLimit.get()) {
			stage2.set(-.6); // Down
		} else {
			stage2.set(0); // Change this value as needed to hold power in the motor
		}

		// Drop Hang Bar
		if (funcJoystick.getRawButton(10)) {
			drop.set(1);
			drop.setAngle(180);
		} else {
			drop.set(0);
			drop.setAngle(0);
		}

	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
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
}
