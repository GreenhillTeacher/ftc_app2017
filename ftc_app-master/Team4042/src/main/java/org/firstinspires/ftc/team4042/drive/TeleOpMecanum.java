package org.firstinspires.ftc.team4042.drive;

import android.hardware.Camera;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.team4042.autos.C;

@TeleOp(name = "Mecanum", group="drive")
public class TeleOpMecanum extends OpMode {

    private double adjustedSpeed;

    private boolean placerModeInstant = true;
    private boolean manual = false;
    private boolean onBalancingStone = false;

    private boolean noAutoIntakes = false;

    private double oops = 1; //switch to -1 if runs in wrong direction when going for balance

    //CONTROL BOOLEANS START
    // We have these booleans so we only register a button press once.
    // You have to let go of the button and push it again to register a new event.
    private boolean aBack = false;
    private double aBackTime = 0;
    private boolean bBack = false;
    private boolean bStart = false;
    private boolean lJoyBtn = false;
    private boolean rJoyBtn = false;

    private boolean aY = false;
    private boolean aX = false;
    private boolean aB = false;

    private boolean aUp = false;
    private boolean aDown = false;

    private boolean aLeftStick = false;
    private boolean aRightStick = false;

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

    private double startRoll;
    private double startPitch;

    private Camera cam;
    private Camera.Parameters onParams;
    private Camera.Parameters offParams;
    private boolean flashOn;

    /**
    GAMEPAD 1:
      Joystick 1 X & Y      movement
      Joystick 1 button     fast
      Joystick 2 X          rotation
      Joystick 2 button     medium
      Bumpers               (extendo) external intakes backwards    (normal) both intakes backwards
      Triggers              (extendo) external intakes forwards     (normal) both intakes forwards
      Dpad up               turn off auto intake
      A
      B                     toggle tank
      X                     toggle crawl
      Y                     toggle extendo
      Start
      Back                  balance

    GAMEPAD 2:
     Manual mode:
      Joystick 1 Y          controls placer vertical
      Joystick 1 btn        toggle Ryan v Brendan uTrack
      Joystick 2 X          controls place horizontal
      Joystick 2 btn        toggle manual
      Bumpers               (extendo) internal intakes backwards
      Triggers              (extendo) internal intakes forwards
      B                     manual hand toggle
      Y                     resets the glyph placer
      Back                  toggle verbose

     Ryan control:
      Joystick 1 btn        toggle Ryan v Brendan uTrack
      Joystick 2 btn        toggle manual
      Bumpers               (extendo) internal intakes backwards
      Triggers              (extendo) internal intakes forwards
      A                     Move uTrack
      dpad                  Target uTrack
      Back                  toggle verbose

     Brendan control:
     Joystick 1 btn        toggle Ryan v Brendan uTrack
     Joystick 2 btn        toggle manual
     Bumpers               (extendo) internal intakes backwards
     Triggers              (extendo) internal intakes forwards
     A,B,X,Y               Move x trget uTrack
     dpad                  Move y trget uTrack
     Back                  toggle verbose
     */

    @Override
    public void init() {
        try {
            drive.initialize(telemetry, hardwareMap);
            drive.runWithEncoders();

            drive.initializeGyro(telemetry, hardwareMap);
            //gyro();

            telemetry.update();

            drive.targetY = GlyphPlacementSystem.Position.TOP;
            drive.targetX = GlyphPlacementSystem.HorizPos.LEFT;
            drive.stage = GlyphPlacementSystem.Stage.HOME;
            drive.glyph.setHomeTarget();

            adjustedSpeed = MecanumDrive.FULL_SPEED;
        } catch (Exception ex) {
            telemetry.addData("Exception", Drive.getStackTrace(ex));
        }
        cam = Camera.open();

        onParams = cam.getParameters();
        onParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        offParams = cam.getParameters();
        offParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    }

    @Override
    public void start() {
        try {
            //Moves the servo to the up position
            drive.jewelUp();
            //Raises the brakes
            drive.raiseBrakes();
            //Locks the catches
            drive.lockCatches();
        } catch (Exception ex) {
            telemetry.addData("Exception", Drive.getStackTrace(ex));
        }
    }
    
    @Override
    public void loop() {
        try {
            if (gamepad1.dpad_up && !aUp) {
                noAutoIntakes = !noAutoIntakes;
            }
            aUp = gamepad1.dpad_up;

            if (gamepad1.dpad_down && !aDown) {
                drive.toggleWinch();
            }
            aDown = gamepad1.dpad_down;
            telemetry.addData("winch", drive.winchOpen);

            //Toggles verbose
            if (gamepad2.back && !bBack) {
                drive.toggleVerbose();
            }
            bBack = gamepad2.back;

            if (gamepad2.left_stick_button && !lJoyBtn) {
                placerModeInstant = !placerModeInstant;
            }
            lJoyBtn = gamepad2.left_stick_button;

            //The first time you hit back, it establishes how long you've been pushing it for
            if (gamepad1.back && !aBack) {
                aBackTime = System.nanoTime();
            }
            //If you're pushing back and have been for longer than "nano", then run the full balance code
            if (gamepad1.back && aBack) {
                if (System.nanoTime() - aBackTime > C.get().getDouble("nano")) {
                    balance();
                }
            }
            //If you've released back and did so for a shorter time than "nano", then toggle whether you're on the stone
            if (!gamepad1.back && aBack) {
                if (System.nanoTime() - aBackTime < C.get().getDouble("nano")) {
                    onBalancingStone = !onBalancingStone;
                }
            }
            aBack = gamepad1.back;

            //Adjust drive modes, speeds, etc
            setUpDrive();

            //Sets up speed modes
            speedModes();

            //Drives the robot
            drive.drive(false, gamepad1, gamepad2, adjustedSpeed * MecanumDrive.FULL_SPEED);

            //Runs the intakes
            intakes();

            //Runs the glyph placer
            glyphPlacer();

            //Updates the telemetry output
            telemetryUpdate();
        } catch (Exception ex) {
            telemetry.addData("Exception", Drive.getStackTrace(ex));
        }
    }

    @Override
    public void stop() {
        super.stop();
        cam.release();
    }

    public void gyro() {
        ElapsedTime timer = new ElapsedTime();
        timer.reset();
        do {
            drive.gyro.updateAngles();
            startRoll = drive.gyro.getRoll();
            startPitch = drive.gyro.getPitch();
        } while (startRoll == 0 && startPitch == 0 && timer.seconds() < .5);
    }

    private void balance() {
        drive.gyro.updateAngles();
        double currRoll = drive.gyro.getRoll();
        double currPitch = drive.gyro.getPitch();
        telemetry.addData("currRoll", currRoll);
        telemetry.addData("currPitch", currPitch);

        boolean flat = Math.abs(currRoll - startRoll) < 2 && Math.abs(currPitch - startPitch) < 2;
        boolean veryTipped = Math.abs(currRoll - startRoll) > 8 || Math.abs(currPitch - startPitch) > 8;
        telemetry.addData("flat", flat);
        if (!onBalancingStone && !flat) {
            //If you get tipped, you must be on the balancing stone and we flag you as such
            onBalancingStone = true;
        } else if (!onBalancingStone && flat) {
            //If you're just getting on or you're on the ground, run back hard
            drive.driveXYR(1, 0, -1, 0, true);
        } else if (veryTipped) {
            //Move away from which way you're tipped (should go towards the center)
            double y = oops*2*(Math.ceil((currRoll-startRoll)/100)-.5);
            telemetry.addData("y", y);
            drive.driveXYR(1, 0, y, 0, true);
        } else if (!flat) {
            //adjust
            //double degreeP = .05;
            //If you're on the balancing stone and not quite flat, then adjust
            double degreeP = C.get().getDouble("degree");
            double x = degreeP * (startPitch - currPitch);
            double y = degreeP * (startRoll - currRoll);
            drive.driveXYR(1, x, y, 0, true);
        }
    }

    private void setUpDrive() {
        drive.uTrackUpdate();
        //drive.updateRates();

        //First controller pushing Y - toggle extendo
        if (gamepad1.y && !aY) {
            toggleExtendo();
        }
        aY = gamepad1.y;

        //The X button on the first controller - toggle crawling to let us adjust the back of the robot too
        if (gamepad1.x && !aX) {
            Drive.crawl = !Drive.crawl;
            if (Drive.crawl) {
                drive.freezeBack();
            } else {
                drive.runBackWithEncoders();
            }
        }
        aX = gamepad1.x;

        if (gamepad1.b && !aB) {
            Drive.tank = !Drive.tank;
        }
        aB = gamepad1.b;

        if (Drive.useGyro) {
            drive.useGyro(0);
        }
    }

    private void glyphPlacer() {
        //If you're at the bottom, haven't been pushing a, and now are pushing a
        if (!placerModeInstant && drive.uTrackAtBottom && !bA && gamepad2.a) {
            drive.uTrack();
        }
        //If you're not at the bottom and are pushing a
        else if (!placerModeInstant && !drive.uTrackAtBottom && gamepad2.a) {
            drive.uTrack();
        }
        bA = gamepad2.a;

        if (gamepad2.right_stick_button && !rJoyBtn) {
            manual = !manual;

            if (!manual) {
                drive.resetEncoders();
                drive.runWithEncoders();
                drive.glyph.setHomeTarget();

                // Turn off flashlight
                if (flashOn) {
                    cam.setParameters(offParams);
                    cam.stopPreview();
                    flashOn = false;
                }
            } else if (manual) {
                // Turn on flashlight
                if (!flashOn) {
                    cam.setParameters(onParams);
                    cam.startPreview();
                    flashOn = true;
                }
            }

            if (drive.getVerticalDriveMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) {
                drive.setVerticalDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
            } else {
                drive.stage = GlyphPlacementSystem.Stage.RETURN2;
                drive.setVerticalDriveMode(DcMotor.RunMode.RUN_TO_POSITION);
            }
        }
        rJoyBtn = gamepad2.right_stick_button;

        if (manual) {
            if (gamepad2.b && !bB) {
                drive.toggleHand();
            }
            bB = gamepad2.b;

            if (bY && !gamepad2.y) { //When you release Y, reset the utrack
                drive.resetUTrack();
                drive.glyph.setHomeTarget();
            } else if (gamepad2.y) { //If you're holding y, run the utrack downwards
                drive.setVerticalDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
                drive.setVerticalDrive(-0.5);
            } else {
                drive.setVerticalDrive(gamepad2.left_stick_y);
                drive.setHorizontalDrive(gamepad2.right_stick_x);
            }
            bY = gamepad2.y;
        }
        else if(!manual) {
            //Glyph locate
            if (!placerModeInstant) {
                glyphUI();
            } else {
                glyphTarget();
            }

            if (!drive.stage.equals(GlyphPlacementSystem.Stage.RETURN2) && !drive.stage.equals(GlyphPlacementSystem.Stage.RESET)) {
                drive.glyph.runToPosition();
            }
        }
    }

    private void speedModes() {
        //Left and right stick together = super slow
        if (gamepad1.left_stick_button && !aLeftStick && gamepad1.right_stick_button && !aRightStick) {
            adjustedSpeed = .5;
        }
        //Left stick = fast
        if (gamepad1.left_stick_button && !aLeftStick && !gamepad1.right_stick_button) {
            adjustedSpeed = 1;
        }
        //Right stick = med
        if (gamepad1.right_stick_button && !aRightStick && !gamepad1.left_stick_button) {
            adjustedSpeed = .75;
        }
        aLeftStick = gamepad1.left_stick_button;
        aRightStick = gamepad1.right_stick_button;
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
            drive.freezeBack();
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
            drive.runBackWithEncoders();
            //} while (timer.seconds() < 2);

            Drive.isExtendo = true;
        }
    }

    private boolean done = true;

    private void glyphTarget() {
        int targetX = gamepad2.x ? 0 : (gamepad2.y || gamepad2.a ? 1 : (gamepad2.b ? 2 : -1));
        int targetY = gamepad2.dpad_up ? 0 : (gamepad2.dpad_left || gamepad2.dpad_right ? 1 : (gamepad2.dpad_down ? 2 : -1));
        if (targetX != -1 && targetY != -1 &&
                (!done || ((done && !bRight && !bLeft && !bUp && !bDown) || (done && !bA && !bB && !bX && !bY)))) {
            drive.glyph.uiTarget(targetX, targetY);
            drive.glyphLocate();
            done = drive.uTrack();
            telemetry.addData("done", done);
        }
        bRight = gamepad2.dpad_right;
        bLeft = gamepad2.dpad_left;
        bUp = gamepad2.dpad_up;
        bDown = gamepad2.dpad_down;
        bA = gamepad2.a;
        bB = gamepad2.b;
        bX = gamepad2.x;
        bY = gamepad2.y;
    }

    private void glyphUI() {
        if (gamepad2.dpad_up && !bUp) { drive.glyph.uiUp(); }
        bUp = gamepad2.dpad_up;
        if (gamepad2.dpad_down && !bDown) { drive.glyph.uiDown(); }
        bDown = gamepad2.dpad_down;
        if (gamepad2.dpad_left && !bLeft) { drive.glyph.uiLeft(); }
        bLeft = gamepad2.dpad_left;
        if (gamepad2.dpad_right && !bRight) { drive.glyph.uiRight(); }
        bRight = gamepad2.dpad_right;
    }

    private void intakes() {
        double aRightTrigger = drive.deadZone(gamepad1.right_trigger);
        boolean aRightBumper = gamepad1.right_bumper;
        double aLeftTrigger = drive.deadZone(gamepad1.left_trigger);
        boolean aLeftBumper = gamepad1.left_bumper;

        double bRightTrigger = drive.deadZone(gamepad2.right_trigger);
        boolean bRightBumper = gamepad2.right_bumper;
        double bLeftTrigger = drive.deadZone(gamepad2.left_trigger);
        boolean bLeftBumper = gamepad2.left_bumper;

        drive.shortIr[1].addReading();
        double backDistance = drive.shortIr[1].getCmAvg();
        boolean isGlyphBack = Math.abs(backDistance - C.get().getDouble("glyphIn")) < Math.abs(backDistance - C.get().getDouble("glyphOut"));

        if (noAutoIntakes && isGlyphBack) {
            drive.intakeLeft(0);
            drive.intakeRight(0);
        } else {
            if (Drive.isExtendo) {
                if (bRightTrigger > 0) { drive.internalIntakeRight(bRightTrigger); }
                //Right bumper of the b controller runs the right intake backwards
                else if (bRightBumper) { drive.internalIntakeRight(-1); }
                else { drive.internalIntakeRight(0); }

                //Left trigger of the b controller runs the left intake forward
                if (bLeftTrigger > 0) { drive.internalIntakeLeft(bLeftTrigger); }
                //Left bumper of the b controller runs the left intake backwards
                else if (bLeftBumper) { drive.internalIntakeLeft(-1); }
                else { drive.internalIntakeLeft(0); }
            }

            if (aRightTrigger > 0) {
                drive.intakeRight(aRightTrigger);
                if (!Drive.isExtendo) { drive.internalIntakeRight(aRightTrigger); }
            } else if (aRightBumper) {
                drive.intakeRight(-1);
                if (!Drive.isExtendo) { drive.internalIntakeRight(-1); }
            } else {
                drive.intakeRight(0);
                if (!Drive.isExtendo) { drive.internalIntakeRight(0); }
            }

            if (aLeftTrigger > 0) {
                drive.intakeLeft(aLeftTrigger);
                if (!Drive.isExtendo) { drive.internalIntakeLeft(aLeftTrigger); }
            } else if (aLeftBumper) {
                drive.intakeLeft(-1);
                if (!Drive.isExtendo) { drive.internalIntakeLeft(-1); }
            } else {
                drive.intakeLeft(0);
                if (!Drive.isExtendo) { drive.internalIntakeLeft(0); }
            }
        }
    }

    private void telemetryUpdate() {
        telemetry.addData("Manual", manual);
        telemetry.addData("Crawl", Drive.crawl);
        telemetry.addData("Glyph", drive.glyph.getTargetPositionAsString());
        telemetry.addData("Glyph pos", drive.verticalDriveCurrPos());
        telemetry.addData("some pos", drive.motorLeftBack.getCurrentPosition());
        telemetry.addData("Speed factor", adjustedSpeed);
        telemetry.addData("Tank", Drive.tank);
        telemetry.addData("Placer Mode Instant", placerModeInstant);
        telemetry.addData("start pitch", startPitch);
        telemetry.addData("start roll", startRoll);
        telemetry.addData("onBalancingStone", onBalancingStone);
        //drive.uTrackAtBottom && !bA && gamepad2.a
        //!drive.uTrackAtBottom && gamepad2.a
        if (drive.verbose) {
            telemetry.addData("gamepad1.dpad_up", gamepad1.dpad_up);
            telemetry.addData("bottom", drive.getBottomState());
            telemetry.addData("center", drive.getCenterState());
        }
        if (Drive.useGyro) {
            telemetry.addData("gyro", drive.gyro.updateHeading());
        }
        telemetry.update();
    }
}