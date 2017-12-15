package org.firstinspires.ftc.team4042;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by Hazel on 9/22/2017.
 */

public abstract class Drive {
    //Initializes a factor for the speed of movement to a position when driving with encoders
    public static final double BASE_SPEED = .3;
    //The deadzone size for the joystick inputs
    public static final double DEADZONE_SIZE = .01;
    //The largest speed factor possible
    public static final double FULL_SPEED = 1;
    //The power to put to the motors to stop them
    public static final double STOP_SPEED = 0;

    //Use gyro - true/false
    public static boolean useGyro = true;

    //Set to false to just get outputs as telemetry
    public static boolean useMotors = true;

    //adjusted power for power levels

    /***instance variables**/
    DcMotor motorLeftFront;
    DcMotor motorRightFront;
    DcMotor motorLeftBack;
    DcMotor motorRightBack;

    Telemetry telemetry;

    RevGyro gyro;

    AnalogSensor[] ir = new AnalogSensor[5];

    boolean verbose;

    Telemetry.Log log;

    //Require drive() in subclasses
    public abstract void drive(boolean useEncoders, Gamepad gamepad1, Gamepad gamepad2, double speedFactor);

    public Drive() {
        if (useGyro) {
            gyro = new RevGyro();
        }

        for(int i = 0; i < ir.length; i++){
            ir[i] = new AnalogSensor("infrared" + i, false);
        }

        verbose = false;
    }

    public Drive(boolean verbose) {
        this();
        this.verbose = verbose;
    }

    public void initialize(Telemetry telemetry, HardwareMap hardwareMap) {
        this.telemetry = telemetry;
        this.log = telemetry.log();
        gyro.initialize(telemetry, hardwareMap);

        telemetry.addData("useGyro", useGyro);

        if (useGyro) {
            gyro.initialize(telemetry, hardwareMap);
        }
        for (int i = 0; i < ir.length; i++) {
            ir[i].initialize(hardwareMap);
        }

        try {
            motorLeftFront = hardwareMap.dcMotor.get("front left");
        } catch (IllegalArgumentException ex) {
            telemetry.addData("Front Left", "Could not find.");
            useMotors = false;
        }

        try {
            motorRightFront = hardwareMap.dcMotor.get("front right");
        } catch (IllegalArgumentException ex) {
            telemetry.addData("Front Right", "Could not find.");
            useMotors = false;
        }

        try {
            motorRightBack = hardwareMap.dcMotor.get("back right");
        } catch (IllegalArgumentException ex) {
            telemetry.addData("Back Right", "Could not find.");
            useMotors = false;
        }

        try {
            motorLeftBack = hardwareMap.dcMotor.get("back left");
        } catch (IllegalArgumentException ex) {
            telemetry.addData("Back Left", "Could not find.");
            useMotors = false;
        }
    }

    public void setUseGyro(boolean useGyro) {
        Drive.useGyro = useGyro;
    }

    /**
     * sets all the motors to run using the PID algorithms and encoders
     */
    public void runWithEncoders(){
        if (verbose || !useMotors) { telemetry.addData("Encoders", "true"); }

        if (useMotors) {
            if (motorLeftBack != null) {
                motorLeftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if (motorLeftFront != null) {
                motorLeftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if (motorRightBack != null) {
                motorRightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if (motorRightFront != null) {
                motorRightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
        }
    }

    /**
     * sets all the motors to run NOT using the PID algorithms and encoders
     */
    public void runWithoutEncoders(){
        if (verbose || !useMotors) { telemetry.addData("Encoders", "false"); }

        if (useMotors) {
            if (motorLeftBack != null) {
                motorLeftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            if (motorLeftFront != null) {
                motorLeftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            if (motorRightBack != null) {
                motorRightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            if (motorRightFront != null) {
                motorRightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        }
    }

    /**
     * resents the encoder counts of all motors
     */
    public void resetEncoders() {
        if (verbose || !useMotors) { telemetry.addData("Encoders", "reset"); }

        if (useMotors) {
            if (motorLeftBack != null) {
                motorLeftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            if (motorLeftFront != null) {
                motorLeftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            if (motorRightBack != null) {
                motorRightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            if (motorRightFront != null) {
                motorRightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
        }
    }

    /**
     * Sets the motors to run with or without motors
     * @param useEncoders Whether or not to use encoders for the motors
     */
    public void setEncoders(boolean useEncoders) {
        if (useEncoders) {
            this.runWithEncoders();
        } else {
            this.runWithoutEncoders();
        }
    }

    /**
     * Returns val, unless it's within DEAD_ZONE of 0, then returns 0
     * @param val A value to test
     * @return val, adjusted for the deadzone
     */
    public double deadZone(double val) {
        if(Math.abs(val - DEADZONE_SIZE) <= 0) {return 0;}
        else { return val; }
    }

    /**
     * Sets motor power to four wheels from an array of the values for each of the four wheels.
     * The wheels should be clockwise from the top left:
     * 0 - leftFront
     * 1 - rightFront
     * 2 - rightBack
     * 3 - leftBack
     * @param speedWheel An array of the power to set to each motor
     */
    public void setMotorPower(double[] speedWheel, double speedFactor) {
        //Scales wheel speeds to fit motors
        double max = max(speedWheel[0], speedWheel[1], speedWheel[2], speedWheel[3]);
        //Since max is an absolute value function, this also accounts for a data set like [3, 1, 0, -5], since max will be 5
        for(int i = 0; i < 4; i++) {
            speedWheel[i] *= speedFactor;
            if (max > 1) {
                speedWheel[i] /= max;
            }
        }

        if (useMotors) {
            //Sets the power
            if (motorLeftFront != null) {
                motorLeftFront.setPower(deadZone(speedWheel[0]));
            }
            if (motorRightFront != null) {
                motorRightFront.setPower(deadZone(-speedWheel[1]));
            } //The right motors are mounted "upside down", which is why we have to inverse this
            if (motorRightBack != null) {
                motorRightBack.setPower(deadZone(-speedWheel[2]));
            }
            if (motorLeftBack != null) {
                motorLeftBack.setPower(deadZone(speedWheel[3]));
            }
        }

        if (verbose || !useMotors) {
            //Prints power
            telemetry.addData("Left Front", speedWheel[0]);
            telemetry.addData("Right Front", -speedWheel[1]);
            telemetry.addData("Right Back", -speedWheel[2]);
            telemetry.addData("Left Back", speedWheel[3]);
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void toggleVerbose() {
        verbose = !verbose;
    }

    /**
     * finds and returns the largest of four doubles
     *
     * @param a first value
     * @param b second value
     * @param c third value
     * @param d fourth value
     * @return return the maximum of all decimals
     */
    public double max(double a, double b, double c, double d) {

        a = Math.abs(a);
        b = Math.abs(b);
        c = Math.abs(c);
        d = Math.abs(d);
        
        return Math.max(Math.max(Math.max(a, b), c), d);
    }

    public double min(double a, double b, double c, double d) {
        return Math.min(a, (Math.min(b, (Math.min(c, d)))));
    }
}
