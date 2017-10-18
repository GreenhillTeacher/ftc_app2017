package org.firstinspires.ftc.team4042;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="RedTopAuto", group="K9bot")
public class RedTopAuto extends LinearOpMode {

    MecanumDrive drive;

    @Override
    public void runOpMode() {
        drive = new MecanumDrive(hardwareMap, telemetry, false, true);
        telemetry.update();

        waitForStart();

        //TODO: TEST THIS
        drive.resetEncoders();
        drive.setEncoders(true);
        autoDrive(new Direction(1, .5), Drive.FULL_SPEED, 1000);

        //autoSensorMove(Direction.Forward, Drive.FULL_SPEED / 4, 7, drive.ir);

        //check sensor sums
        //robot starts facing right
        //scan vision patter
        //go to front of jewels
        //cv scan
        //knock off other jewel
        //head right
        //whisker sensor hits cryptobox
        //back up
        //repeat ^ until whisker disengages
        //move right until we see -^-^-| from ultrasonic
        //place block
        //detach and extend robot towards glyph
    }

    /**
     * Drives in the given Direction at the given speed until targetTicks is reached
     * @param direction The direction to head in
     * @param speed The speed to move at
     * @param targetTicks The final distance to have travelled, in encoder ticks
     */
    private void autoDrive(Direction direction, double speed, double targetTicks) {
        boolean done = false;
        while (opModeIsActive() && !done) {
            done = drive.driveWithEncoders(direction, speed, targetTicks);
            telemetry.update();
        }
    }

    private void autoRotate(Direction.Rotation rotation, double speed, double targetTicks) {
        boolean done = false;
        while (opModeIsActive() && !done) {
            done = drive.rotateWithEncoders(rotation, speed, targetTicks);
            telemetry.update();
        }
    }

    private void autoSensorMove(Direction direction, double speed, double targetDistance, AnalogSensor ir) {
        boolean done = false;
        while (opModeIsActive() && !done) {
            done = drive.driveWithSensor(direction, speed, targetDistance, ir);
            telemetry.update();
        }
    }
}