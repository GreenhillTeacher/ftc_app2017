/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.team4042.testops;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a PushBot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="jimbobjow", group="testops")  // @Autonomous(...) is the other common choice
@Disabled
public class EncoderTest extends OpMode
{
    /* Declare OpMode members. */
    private ElapsedTime timer = new ElapsedTime();
    private DcMotor motor;
    private Servo servo;
    private DigitalChannel sensor;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        try {
            motor = hardwareMap.dcMotor.get("motor");
        } catch (IllegalArgumentException ex) {

        }

        try {
            servo = hardwareMap.servo.get("servo");
        } catch (IllegalArgumentException ex) {

        }

        try {
            sensor = hardwareMap.digitalChannel.get("sensor");
        } catch (IllegalArgumentException ex) {

        }

        if(sensor != null) { sensor.setMode(DigitalChannel.Mode.INPUT); }
        if (motor != null) {
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    @Override
    public void loop() {
        if (motor != null) {
            motor.setPower(Range.clip(gamepad1.left_stick_y, -1, 1));
            telemetry.addData("where is jimbobjow?", motor.getCurrentPosition());
        } else {
            telemetry.addData("could not find", "motor");
        }

        if (servo != null) {
            servo.setPosition(Range.scale(gamepad1.right_stick_y, -1, 1, 0, 1));
            telemetry.addData("where be jimbobjowjunior?", servo.getPosition());
        } else {
            telemetry.addData("could not find", "servo");
        }

        if (sensor != null) {
            telemetry.addData("sensor", sensor.getState());
        }

        telemetry.addData("time", timer.milliseconds());
        timer.reset();
    }
}
