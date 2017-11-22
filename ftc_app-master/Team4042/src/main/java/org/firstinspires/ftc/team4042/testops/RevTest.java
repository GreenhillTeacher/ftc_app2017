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
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.external.samples.HardwarePushbot;

/**
 * This file provides basic Telop driving for a Pushbot robot.
 * The code is structured as an Iterative OpMode
 *
 * This OpMode uses the common Pushbot hardware class to define the devices on the robot.
 * All device access is managed through the HardwarePushbot class.
 *
 * This particular OpMode executes a basic Tank Drive Teleop for a PushBot
 * It raises and lowers the claw using the Gampad Y and A buttons respectively.
 * It also opens and closes the claws slowly using the left and right Bumper buttons.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="RevTest", group="testops")
public class RevTest extends OpMode{

    DcMotor motor1, motor2, motor3, motor4;
    boolean m1, m2, m3, m4;

    @Override
    public void init() {
        try {
            motor1 = hardwareMap.dcMotor.get("motor1");
            motor1.setDirection(DcMotorSimple.Direction.FORWARD);
            motor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m1 = true;
        } catch (Exception ex) { m1 = false; }

        try {
            motor2 = hardwareMap.dcMotor.get("motor2");
            motor2.setDirection(DcMotorSimple.Direction.FORWARD);
            motor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m2 = true;
        } catch (Exception ex) { m2 = false; }

        try {
            motor3 = hardwareMap.dcMotor.get("motor3");
            motor3.setDirection(DcMotorSimple.Direction.FORWARD);
            motor3.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m3 = true;
        } catch (Exception ex) { m3 = false; }

        try {
            motor4 = hardwareMap.dcMotor.get("motor4");
            motor4.setDirection(DcMotorSimple.Direction.FORWARD);
            motor4.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m4 = true;
        } catch (Exception ex) { m4 = false; }
    }

    @Override
    public void loop() {
        if (m1) { motor1.setPower(Range.clip(gamepad1.left_stick_x, -1, 1)); }
        if (m2) { motor2.setPower(Range.clip(gamepad1.left_stick_y, -1, 1)); }
        if (m3) { motor3.setPower(Range.clip(gamepad1.right_stick_x, -1, 1)); }
        if (m4) { motor4.setPower(Range.clip(gamepad1.right_stick_y, -1, 1)); }

        if (m1) { telemetry.addData("1", motor1.getPower()); }
        else { telemetry.addData("1", "Could not find."); }
        if (m2) { telemetry.addData("2", motor1.getPower()); }
        else { telemetry.addData("2", "Could not find."); }
        if (m3) { telemetry.addData("3", motor1.getPower()); }
        else { telemetry.addData("3", "Could not find."); }
        if (m4) { telemetry.addData("4", motor1.getPower()); }
        else { telemetry.addData("4", "Could not find."); }
    }

}
