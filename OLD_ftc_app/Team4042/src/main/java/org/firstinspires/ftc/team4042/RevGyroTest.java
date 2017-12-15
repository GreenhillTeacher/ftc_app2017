package org.firstinspires.ftc.team4042;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous ;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import java.util.Locale;

/**
 * Gets all the data from a gyro and prints it to telemetry
 */
@Autonomous(name="Rev_Gyro_test", group="K9bot")
public class RevGyroTest extends LinearOpMode {

    private RevGyro revGyro = new RevGyro();

    @Override
    public void runOpMode() {

        revGyro.initialize(telemetry, hardwareMap);


// Set up our telemetry dashboard
        while (opModeIsActive()) {
            revGyro.updateAngles();
            telemetry.addData("heading", revGyro.getHeading());
            telemetry.addData("pitch", revGyro.getPitch());
            telemetry.addData("roll", revGyro.getRoll());
            telemetry.update();
        }
    }
}