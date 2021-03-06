/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * This OpMode scans a single servo back and forwards until Stop is pressed.
 * The code is structured as a LinearOpMode
 * INCREMENT sets how much to increase/decrease the servo position each cycle
 * CYCLE_MS sets the update period.
 * <p>
 * This code assumes a Servo configured with the name "left_hand" as is found on a pushbot.
 * <p>
 * NOTE: When any servo position is set, ALL attached servos are activated, so ensure that any other
 * connected servos are able to move freely before running this test.
 * <p>
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */
@TeleOp(name = "ServoArmTest", group = "Concept")

public class ServoArmTest extends LinearOpMode {

    static final double INCREMENT = 0.01;     // amount to slew servo each CYCLE_MS cycle
    static final int CYCLE_MS = 50;     // period of each cycle
    static final double MAX_POS = 1.0;     // Maximum rotational position
    static final double MIN_POS = 0.0;     // Minimum rotational position

    static final double ARM_LOWER_POSITION = 0.0;
    static final double ARM_HIGHER_POSITION = 0.25;
    static final double TURRET_LEFT_POSITION = 0.17;
    static final double TURRET_MIDDLE_POSITION = 0.50;
    static final double TURRET_RIGHT_POSITION = 0.83;

    // Define class members
    Servo armServo;
    Servo turretServo;
    double position = (MAX_POS - MIN_POS) / 2; // Start at halfway position
    boolean rampUp = true;


    @Override
    public void runOpMode() {

        // Connect to servo (Assume PushBot Left Hand)
        // Change the text in quotes to match any servo name on your robot.
        armServo = hardwareMap.get(Servo.class, "ArmServo");
        turretServo = hardwareMap.get(Servo.class, "TurretServo");

        turretServo.setPosition(TURRET_MIDDLE_POSITION);
        armServo.setPosition(ARM_LOWER_POSITION);


        // Wait for the start button
        telemetry.addData(">", "Press Start to Test Turret and Arm Servos.");
        telemetry.update();
        waitForStart();


        // Scan servo till stop pressed.
        while (opModeIsActive()) {


            // The x, y operate the arm servo
            if (gamepad1.x && armServo.getPosition() >= ARM_HIGHER_POSITION)
                turretServo.setPosition(TURRET_LEFT_POSITION);
            else if (gamepad1.a)
                turretServo.setPosition(TURRET_MIDDLE_POSITION);
            else if (gamepad1.b && armServo.getPosition() >= ARM_HIGHER_POSITION)
                turretServo.setPosition(TURRET_RIGHT_POSITION);

            if (gamepad1.right_trigger > 0)
                armServo.setPosition(ARM_HIGHER_POSITION);
            else if (gamepad1.left_trigger > 0 && turretServo.getPosition() == TURRET_MIDDLE_POSITION)
                armServo.setPosition(ARM_LOWER_POSITION);

            // Display current servo position values
            telemetry.addData("Arm Position", "%5.2f", armServo.getPosition());
            telemetry.addData("Turret Position", "%5.2f", turretServo.getPosition());
            telemetry.addData(">", "Press Stop to end test.");
            telemetry.update();

              /*
                  // slew the servo, according to the rampUp (direction) variable.
                if (rampUp) {
                    // Keep stepping up until we hit the max value.
                    position += INCREMENT ;
                    if (position >= MAX_POS ) {
                        position = MAX_POS;
                        rampUp = !rampUp;   // Switch ramp direction
                    }
                }
                else {
                    // Keep stepping down until we hit the min value.
                    position -= INCREMENT ;
                    if (position <= MIN_POS ) {
                        position = MIN_POS;
                        rampUp = !rampUp;  // Switch ramp direction
                    }
                }
    
                // Display the current value
                telemetry.addData("Servo Position", "%5.2f", position);
                telemetry.addData(">", "Press Stop to end test." );
                telemetry.update();
    
                // Set the servo to the new position and pause;
                armServo.setPosition(position);
                sleep(CYCLE_MS);
                idle();
              
                */

        }

        // Signal done;
        telemetry.addData(">", "Done");
        telemetry.update();
    }
}
