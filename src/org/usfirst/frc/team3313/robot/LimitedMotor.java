package org.usfirst.frc.team3313.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Talon;

public class LimitedMotor {
	DigitalInput upperLimit, lowerLimit;
	Talon motor;

	public LimitedMotor(int motorPort, int upperPort, int lowerPort) {
		motor = new Talon(motorPort);
		upperLimit = new DigitalInput(upperPort);
		lowerLimit = new DigitalInput(lowerPort);
	}

	public void set(double value) {
		if (value > 0 && !upperLimit.get()) {
			motor.set(value);
		} else if (value < 0 && !lowerLimit.get()) {
			motor.set(value);
		} else {
			motor.set(0);
		}

	}

	public boolean getUpper() {
		return upperLimit.get();
	}

}
