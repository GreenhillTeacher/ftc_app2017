package org.firstinspires.ftc.team4042.drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Mecanum", group="drive")
public class TeleOpMecanum extends OpMode {

    private double adjustedSpeed;

    //CONTROL BOOLEANS START
    // We have these booleans so we only register a button press once.
    // You have to let go of the button and push it again to register a new event.
    private boolean aA = false;
    private boolean aY = false;
    private boolean aX = false;

    private boolean aDown = false;
    private boolean aUp = false;
    private boolean manual = false;

    private boolean bUp;
    private boolean bDown;
    private boolean bLeft;
    private boolean bRight;

    private boolean bA;
    private boolean bX;
    private boolean bB;
    private boolean bY;
    //CONTROL BOOLEANS END

    private Drive drive = new MecanumDrive(true);

    /**
    GAMEPAD 1:
      Joystick 1 X & Y - movement
      Joystick 2 X - rotation
      Bumpers - external intakes backwards
      Triggers - external intakes forwards
      Dpad up/down - speed modes
      A - toggle verbose
      B -
      X - toggle crawl
      Y - toggle extendo
      Start -
      Back -

    GAMEPAD 2:
      Joystick 1 Y - controls placer vertical in manual
      Joystick 2 X - controls place horizontal in manual
      Bumpers - internal intakes backwards
      Triggers - internal intakes forwards
      Dpad - placer
      A - places glyph
      B - manual hand toggle
      X - toggles manual placement mode
      Y - resets the glyph placer
      Start -
      Back -
     */

    @Override
    public void init() {
        drive.initialize(telemetry, hardwareMap);
        drive.runWithEncoders();

        telemetry.update();

        drive.targetY = GlyphPlacementSystem.Position.TOP;
        drive.targetX = GlyphPlacementSystem.HorizPos.LEFT;
        drive.stage = GlyphPlacementSystem.Stage.HOME;
        drive.glyph.setHomeTarget();

        adjustedSpeed = MecanumDrive.FULL_SPEED;
    }

    @Override
    public void start() {
        //Moves the servo to the up position
        drive.jewelUp();
        //Raises the brakes
        drive.raiseBrakes();
        //Locks the catches
        drive.lockCatches();
    }
    
    @Override
    public void loop() {

        drive.glyph.updateEncoderDeriv();

        //First controller pushing Y - toggle extendo
        if (gamepad1.y && !aY) {
            toggleExtendo();
        }
        aY = gamepad1.y;

        //The X button on the first controller - toggle crawling to let us adjust the back of the robot too
        if (gamepad1.x && !aX) {
            Drive.crawl = !Drive.crawl;
        }
        aX = gamepad1.x;

        //1 A - toggle verbose
        if (gamepad1.a && !aA) {
            drive.toggleVerbose();
        }
        aA = gamepad1.a;

        //Drives the robot
        drive.drive(false, gamepad1, gamepad2, adjustedSpeed * MecanumDrive.FULL_SPEED);

        if (Drive.useGyro) {
            drive.useGyro();
        }

        speedModes();

        /*if (!bY && gamepad1.y) { //When you first push the button

        }
        else if (bY && !gamepad2.y) { //When you release the button, reset the utrack
            drive.resetUTrack();
        }
        else if (gamepad2.y) { //Runs the utrack downwards
            drive.setVerticalDrive(-0.5);
        }
        //If you're at the bottom, haven't been pushing a, and now are pushing a
        else*/ if (drive.uTrackAtBottom && !bA && gamepad2.a) {
            drive.uTrack();
        }
        //If you're not at the bottom and are pushing a
        else if (!drive.uTrackAtBottom && gamepad2.a) {
            drive.uTrack();
        }
        bA = gamepad2.a;
        bY = gamepad2.y;

        //Right trigger of the b controller runs the right intake forward
        intakes();

        if (gamepad2.x && !bX) {
            manual = !manual;

            if (!manual) {
                drive.resetEncoders();
                drive.runWithEncoders();
            }

            if (drive.getVerticalDriveMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) {
                drive.setVerticalDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
            } else {
                drive.stage = GlyphPlacementSystem.Stage.RETURN1;
                drive.setVerticalDriveMode(DcMotor.RunMode.RUN_TO_POSITION);
            }
        }
        bX = gamepad2.x;

        //TODO: manual is done broked
        if(!manual) {
            //Glyph locate
            glyphLocate();

            drive.glyph.runToPosition();
        } else {
            drive.setVerticalDrive(gamepad2.left_stick_y);
            drive.setHorizontalDrive(gamepad2.left_stick_x);

            if (gamepad2.b && ! bB) {
                drive.toggleHand();
            }
            bB = gamepad2.b;
        }
        telemetryUpdate();
    }

    private void speedModes() {
        //If you push the left bumper, dials the speed down
        if (gamepad1.dpad_down && !aDown && (adjustedSpeed - 0.25) >= 0) {
            adjustedSpeed -= 0.25;
        }
        aDown = gamepad1.dpad_down;
        //Right bumper - dial speed up
        if (gamepad1.dpad_up && !aUp && (adjustedSpeed + 0.25) <= MecanumDrive.FULL_SPEED)
        {
            adjustedSpeed += 0.25;
        }
        aUp = gamepad1.dpad_up;
    }

    private void toggleExtendo() {
        //It's extendo, so put it back together
        ElapsedTime timer = new ElapsedTime();
        if (Drive.isExtendo) {
            timer.reset();

            //do {
                //drive.pushRobotTogether();
                drive.raiseBrakes();
                drive.lockCatches();
            //} while (timer.seconds() < 2);

            Drive.isExtendo = false;
        }
        //Not extendo, so take it apart
        else {
            timer.reset();

            //do {
                //drive.pushRobotTogether();
                drive.lowerBrakes();
                drive.unlockCatches();
            //} while (timer.seconds() < 2);

            Drive.isExtendo = true;
        }
    }

    private void glyphLocate() {
        if (gamepad2.dpad_up && !bUp) { drive.targetY = GlyphPlacementSystem.Position.values()[drive.glyph.up() + 2]; }
        bUp = gamepad2.dpad_up;
        if (gamepad2.dpad_down && !bDown) { drive.targetY = GlyphPlacementSystem.Position.values()[drive.glyph.down() + 2]; }
        bDown = gamepad2.dpad_down;
        if (gamepad2.dpad_left && !bLeft) { drive.targetX = GlyphPlacementSystem.HorizPos.values()[drive.glyph.left()]; }
        bLeft = gamepad2.dpad_left;
        if (gamepad2.dpad_right && !bRight) { drive.targetX = GlyphPlacementSystem.HorizPos.values()[drive.glyph.right()]; }
        bRight = gamepad2.dpad_right;
    }

    private void intakes() {
        if (Drive.isExtendo) {
            double bRightTrigger = drive.deadZone(gamepad2.right_trigger);
            if (bRightTrigger > 0) {
                drive.internalIntakeRight(bRightTrigger);
            }
            //Right bumper of the b controller runs the right intake backwards
            else if (gamepad2.right_bumper) {
                drive.internalIntakeRight(-1);
            } else {
                drive.internalIntakeRight(0);
            }

            //Left trigger of the b controller runs the left intake forward
            double bLeftTrigger = drive.deadZone(gamepad2.left_trigger);
            if (bLeftTrigger > 0) {
                drive.internalIntakeLeft(bLeftTrigger);
            }
            //Left bumper of the b controller runs the left intake backwards
            else if (gamepad2.left_bumper) {
                drive.internalIntakeLeft(-1);
            } else {
                drive.internalIntakeLeft(0);
            }
        }

        double aRightTrigger = drive.deadZone(gamepad1.right_trigger);
        if (aRightTrigger > 0) {
            drive.intakeRight(aRightTrigger);
            if (!Drive.isExtendo) {
                drive.internalIntakeRight(aRightTrigger);
            }
        }
        else if (gamepad1.right_bumper) {
            drive.intakeRight(-1);
            if (!Drive.isExtendo) {
                drive.internalIntakeRight(-1);
            }
        }
        else {
            drive.intakeRight(0);
            if (!Drive.isExtendo) {
                drive.internalIntakeRight(0);
            }
        }

        double aLeftTrigger = drive.deadZone(gamepad1.left_trigger);
        if (aLeftTrigger > 0) {
            drive.intakeLeft(aLeftTrigger);
            if (!Drive.isExtendo) {
                drive.internalIntakeLeft(aLeftTrigger);
            }
        }
        else if (gamepad1.left_bumper) {
            drive.intakeLeft(-1);
            if (!Drive.isExtendo) {
                drive.internalIntakeLeft(-1);
            }
        }
        else {
            drive.intakeLeft(0);
            if (!Drive.isExtendo) {
                drive.internalIntakeLeft(0);
            }
        }
    }

    private void telemetryUpdate() {
        telemetry.addData("Manual", manual);
        telemetry.addData("Crawl", Drive.crawl);
        telemetry.addData("Glyph", drive.glyph.getTargetPositionAsString());
        telemetry.addData("Speed factor", adjustedSpeed);
        if (drive.verbose) {
            telemetry.addData("encoder currentY pos", drive.verticalDriveCurrPos());
            telemetry.addData("hand is open", drive.isHandOpen());
            telemetry.addData("limit switch", drive.getCenterState());
            telemetry.addData("targetY", drive.targetY.toString());
            telemetry.addData("Current pos", drive.glyph.currentY.toString());
            telemetry.addData("encoder targetY pos", drive.verticalDriveTargetPos());
            telemetry.addData("stage", drive.stage);
            telemetry.addData("gamepad2.a", gamepad2.a);
            telemetry.addData("handDropTimer seconds", drive.handDropTimer.seconds());
            telemetry.addData("horizontal u", drive.getHorizontalDrive());
            telemetry.addData("horizontal translate timer", drive.glyph.horizontalTimer.seconds());
            telemetry.addData("target pos",drive.targetX + " horiz time " + drive.glyph.horizontalTimer.seconds() + " horiz trans time " + drive.glyph.HORIZONTAL_TRANSLATION_TIME);
            telemetry.addData("boolean",((drive.targetX.equals(GlyphPlacementSystem.HorizPos.LEFT) ||
                    drive.targetX.equals(GlyphPlacementSystem.HorizPos.RIGHT)) + " bool " + (drive.glyph.horizontalTimer.seconds() >= drive.glyph.HORIZONTAL_TRANSLATION_TIME)));
        }
        if (Drive.useGyro) {
            telemetry.addData("gyro", drive.gyro.updateHeading());
        }
        telemetry.update();
    }
}