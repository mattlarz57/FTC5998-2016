package org.firstinspires.ftc.teamcode.Current;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.navX.ftc.AHRS;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Ethan Schaffer and team 5998
 * All references to anything with the word "Left" in it works similarly or identically for any similarly named method "Right".
 */

public class Robot {
    public enum team {
        Red, Blue, NotSensed
    }
    public static final String DIMNAME = "dim"; //second DIM, reserved for NavX
    public LinearOpMode l;
    private boolean infeedOn = false, shooterOn = false;
    public final double ticksPerRev = 7;
    public final double gearBoxOne = 40.0;
    public final double gearBoxTwo = 24.0 / 16.0;
    public final double gearBoxThree = 1.0;
    public final double wheelDiameter = 4.0 * Math.PI;
    public final double cmPerInch = 2.54;
    public final double cmPerTick = (wheelDiameter / (ticksPerRev * gearBoxOne * gearBoxTwo * gearBoxThree)) * cmPerInch;
    //Allows us to drive our roobt with accuracy to the centimeter

    public final double width = 31.75;
    // The wheel's width is used to calculate turn distance by Encoder ticks, using our turnLeftEnc method.

    public final double distance = 2000/(172*cmPerInch);
    //This value isn't too accurate, but it theoretically works.

    //The Above Values lets us convert encoder ticks to centimeters per travelled, as shown below.

    double targPerSecond = 1950; //This is our target RPM
    double timeWait = .33; //in seconds
    /*
    * Our tests showed us that an update rate of 3Hz was the most effective for accuracy.
    * Update Rate (Hz) Made/Shot
    * 5                4   / 10
    * 3                9   / 10
    * 2                9   / 10
    * 1                10  / 10
    * */
    double target = targPerSecond * timeWait; //Target Rotations per second


    public static final String LEFT1NAME = "l1"; //LX Port 2
    public static final String LEFT2NAME = "l2"; //LX Port 1
    public static final String RIGHT1NAME = "r1";//0A Port 1
    public static final String RIGHT2NAME = "r2";//0A Port 2
    public static final String BALLBLOCKNAME = "s"; //MO Port 4
    public static final double BALLBLOCKOPEN = .65, BALLBLOCKCLOSED = 0;
    public static final String SHOOT1NAME = "sh1";//PN Port 1
    public static final String SHOOT2NAME = "sh2";//PN Port 2
    public static final String INFEEDNAME = "in"; //2S Port 2
    public static final String LIFTNAME = "l"; //2S Port 1
    public static final String LEFTPUSHNAME = "lp";//MO Port 1
    public static final String RIGHTPUSHNAME = "rp";//MO Port 2
    public static final String RANGENAME = "r"; //Port 0
    public static final String COLORSIDENAME = "cs"; //Port 1
    public static final String COLORLEFTBOTTOMNAME = "cb";//Port 2
    public static final String COLORRIGHTBOTTOMNAME = "cb2"; //Port 4
    public static final String GYRONAME = "g"; //Port 4
    public VoltageSensor voltageGetter;

    public DcMotor leftFrontWheel, leftBackWheel, rightFrontWheel, rightBackWheel, shoot1, shoot2, infeed, lift;
    public Servo leftButtonPusher, rightButtonPusher, ballBlock;
    public ColorSensor colorSensorOnSide, colorSensorBottom;
    public ModernRoboticsI2cGyro gyroSensor;
    public DeviceInterfaceModule dim;
    public ModernRoboticsI2cRangeSensor range;
    public AHRS navX;
    public static final double LEFT_SERVO_OFF_VALUE = .25;
    public static final double LEFT_SERVO_ON_VALUE = 1;
    public static final double RIGHT_SERVO_ON_VALUE = 1;
    public static final double RIGHT_SERVO_OFF_VALUE = .25;
    Telemetry t;

    // This method prints out sensor readings to the driver's station.
    // This is most used for debugging, but it also lets us check the status
    // of sensors before the start of a match
    public void sensorsInfo(){
        t.clear();
        t.addData("NavX", navX.isConnected() ? navX.getYaw() : "Disconnected");
        t.addData("Color Bottom", colorSensorBottom.alpha());
        t.addData("Color Side Red", colorSensorOnSide.red());
        t.addData("Color Side Blue", colorSensorOnSide.blue());
        t.addData("Range CM", range.getDistance(DistanceUnit.CM));
        t.update();
    }
    // We initialize all of our Motors and Servos locally. This helps our other files maintain readability,
    // and allows us to change how the hardware map works here or otherwise.
    public void initialize(LinearOpMode lInput, HardwareMap hardwareMap, Telemetry telemetry, boolean navXOn){
        l = lInput;
        t = telemetry;
        leftFrontWheel = hardwareMap.dcMotor.get(LEFT1NAME);
        leftBackWheel = hardwareMap.dcMotor.get(LEFT2NAME);
        rightFrontWheel = hardwareMap.dcMotor.get(RIGHT1NAME);
        rightBackWheel = hardwareMap.dcMotor.get(RIGHT2NAME);
        rightBackWheel.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFrontWheel.setDirection(DcMotorSimple.Direction.REVERSE);

        shoot1 = hardwareMap.dcMotor.get(SHOOT1NAME);
        shoot1.setDirection(DcMotorSimple.Direction.REVERSE);
        shoot2 = hardwareMap.dcMotor.get(SHOOT2NAME);
        infeed = hardwareMap.dcMotor.get(INFEEDNAME);
        infeed.setDirection(DcMotorSimple.Direction.REVERSE);

        ballBlock = hardwareMap.servo.get(BALLBLOCKNAME);
        leftButtonPusher = hardwareMap.servo.get(LEFTPUSHNAME);
        rightButtonPusher = hardwareMap.servo.get(RIGHTPUSHNAME);

        leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
        rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
        ballBlock.setPosition(BALLBLOCKCLOSED);
        voltageGetter = hardwareMap.voltageSensor.get("Motor Controller 1");

        range = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "r");
        colorSensorBottom = hardwareMap.colorSensor.get(COLORLEFTBOTTOMNAME);

        colorSensorBottom.setI2cAddress(I2cAddr.create8bit(0x4c));
        colorSensorOnSide = hardwareMap.colorSensor.get(COLORSIDENAME);
        colorSensorOnSide.setI2cAddress(I2cAddr.create8bit(0x3c));
        colorSensorBottom.enableLed(true);
        colorSensorOnSide.enableLed(false);

        dim = hardwareMap.get(DeviceInterfaceModule.class, DIMNAME);
        // If the navXOn boolean is input as false, we can skip this step.
        // This will only be the case in a fraction of our routes,
        // but it is a good fallback if the navX is broken.
        if(navXOn){
            navX = new AHRS(dim, hardwareMap.i2cDevice.get("n").getPort(), AHRS.DeviceDataType.kProcessedData, (byte)50);
            navX.zeroYaw();
            while ( navX.isCalibrating() && !l.isStopRequested()) {
                telemetry.addData("Gyro", "Calibrating");
                telemetry.addData("Yaw", navX.getYaw());
                if(!navX.isConnected()){
                    telemetry.addData("NavX", "DISCONNECTED!");
                } else {
                    telemetry.addData("NavX", "Connected!");}
                telemetry.update();
            }
            // Allow the NavX to calibrate before exiting the
            telemetry.addData("Yaw", navX.getYaw());
            if(!navX.isConnected()){
                telemetry.addData("NavX", "DISCONNECTED!");
            } else {
                telemetry.addData("NavX", "Connected!");}
            telemetry.update();
        } else {
            telemetry.addData("NavX", "None");
        }

        //We found that if you don't set the LED until after the waitForStart(), it doesn't work as well.
        colorSensorBottom.enableLed(true);
        colorSensorOnSide.enableLed(false);
    }

    // This function resets the encoders of the 4 drive wheels.
    // We call it at the start of each method that uses drive encoders,
    // to ensure that our encoders are accurate and in the correct mode.
    public void ResetDriveEncoders(){
        leftBackWheel.setMode(DcMotor.RunMode.RESET_ENCODERS);
        leftFrontWheel.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightBackWheel.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightFrontWheel.setMode(DcMotor.RunMode.RESET_ENCODERS);
        while(leftFrontWheel.getCurrentPosition() != 0 || leftBackWheel.getCurrentPosition() != 0
                || rightBackWheel.getCurrentPosition() != 0 || rightFrontWheel.getCurrentPosition() != 0){

        }
        leftBackWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFrontWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // This method lets us set strafing power to the motors
    // such that we strafe either left or right.
    public void setStrafePower(String Direction, double Speed) {
        if(Objects.equals(Direction, "Left"))
        {
            rightFrontWheel.setPower(Speed);
            rightBackWheel.setPower(-Speed);
            leftFrontWheel.setPower(-Speed);
            leftBackWheel.setPower(Speed);
        }
        if(Objects.equals(Direction, "Right"))
        {
            rightFrontWheel.setPower(-Speed);
            rightBackWheel.setPower(Speed);
            leftFrontWheel.setPower(Speed);
            leftBackWheel.setPower(-Speed);
        }
    }

    // This is our most used, and most useful method.
    // We use it to turn to within our  threshold angle of 0.
    // The default power is .05 as that power performed with the best accuracy in our testing.
    public void AlignToWithin(double threshold){
        AlignToWithin(threshold, .05);
    }

    // The align to within algorithm uses our team's unique method of non-recovery turning.
    // Because the robot exits the control loop once the reading is past the target,
    // we can apply this logic and use it to control our turning to a precise degree of accuracy.
    public void AlignToWithin(double threshold, double power){
        TurnRightAbsolute(- threshold, power);
        TurnLeftAbsolute(threshold, power);
        TurnRightAbsolute(- threshold, power);
    }

    // The AlignToWithinOf method aligns to within threshold degrees of expected.
    // To do this, we subtract or add threshold to the expected reading,
    // following a similar structure to that of AlignToWithin
    public void AlignToWithinOf(double expected, double threshold, double power){
        TurnRightAbsolute(expected - threshold, power);
        TurnLeftAbsolute(expected + threshold, power);
        TurnRightAbsolute(expected - threshold, power);
    }

    // Sets the same power to all four wheels. This lets us stop and start the robot easily,
    // and in a more _ way.
    public void setDrivePower(double power) {
        leftBackWheel.setPower(power);
        leftFrontWheel.setPower(power);
        rightBackWheel.setPower(power);
        rightFrontWheel.setPower(power);
    }

    //Using the encoders, we move to a certain distance
    public void Move(double sensor, double power) {
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(!shooterOn){
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double ticks = sensor / cmPerTick;
        int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
        int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
        int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
        int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
        double avg = (RBPos + LBPos + RFPos + LFPos)/4;
        double lastTime = l.getRuntime(),
                deltaEnc1 = 0,
                deltaEnc2 = 0,
                pastEnc1 = shoot1.getCurrentPosition(),
                pastEnc2 = shoot2.getCurrentPosition(),
                percentError,
                DeltaAvg;
        while(avg < ticks && l.opModeIsActive()) {
            if(shooterOn){
                if(shoot1.getPower() == 0) {
                    shoot1.setPower(power);
                    shoot2.setPower(power);
                    break;
                } else if( !((l.getRuntime() - timeWait) < lastTime)){
                    // We set up RPM handling within the running of our control loop.
                    // This lets us get more accuracy from our flywheel shooter,
                    // while saving time by letting us tweak the shooter power
                    deltaEnc1 = Math.abs(Math.abs(shoot1.getCurrentPosition())-pastEnc1);
                    deltaEnc2 = Math.abs(Math.abs(shoot2.getCurrentPosition())-pastEnc2);
                    DeltaAvg = (deltaEnc1 + deltaEnc2) / 2;
                    // We take the average change in encoder reading ...
                    percentError = ((DeltaAvg - target) / DeltaAvg);
                    // And calculate the percent error based on an expected change,
                    // which is based on the target speed value.
                    // Based on this, we add our percent error to the current power ...
                    power = Range.clip( power - percentError / 5 , -1, 1);
                    shoot1.setPower(power);
                    shoot2.setPower(power);
                    l.telemetry.clear();
                    l.telemetry.addData("Delta Avg", DeltaAvg);
                    l.telemetry.addData("Target", target);
                    l.telemetry.addData("Power", shoot1.getPower());
                    l.telemetry.addData("Delta 1", deltaEnc1);
                    l.telemetry.addData("Delta 2", deltaEnc2);
                    l.telemetry.addData("Percent Error", percentError);
                    l.telemetry.update();
                    pastEnc1 = Math.abs(shoot1.getCurrentPosition());
                    pastEnc2 = Math.abs(shoot2.getCurrentPosition());
                    lastTime = l.getRuntime();
                    // And then store out old times and encoder positions
                }
            }
            sensorsInfo();
            setDrivePower(power);
            RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + LBPos + RFPos + LFPos) / 4;
        }
        setDrivePower(0);
    }

    //Drive in a direction until the robot's pitch changes.
    public void MoveToPitch(double pitch, double power){
        setDrivePower(power);
        while(l.opModeIsActive() && (Math.abs(navX.getPitch()) < pitch) ){
            sensorsInfo();
        }
        setDrivePower(0);
    }

    // MoveCoast functions like Move, but the drive wheels are not stopped at the end.
    // This allows for more fluid motion while driving autonomously,
    // especially on routes like our main blue one where we are aiming for
    // more fluid motion.
    public void MoveCoast(double sensor, double power) {
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double ticks = sensor / cmPerTick;
        int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
        int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
        int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
        int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
        double avg = (RBPos + LBPos + RFPos + LFPos)/4;
        while(avg < ticks && l.opModeIsActive()) {
            sensorsInfo();
            setDrivePower(power);
            RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + LBPos + RFPos + LFPos) / 4;
        }
    }


    // The ForwardsPLoop method slows the robot down
    // to give us more control of our distance travelled.
    public void ForwardsPLoop(double sensor, double maxPower) {
        //Max Power should be normally set to 1, but for very precise Movements a value of .diagonalBrokeThreshold or lower is recommended.
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double ticks = sensor / cmPerTick;
        int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
        int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
        int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
        int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
        double avg = (RBPos + LBPos + RFPos + LFPos)/4;
        double power;
        while(avg < ticks && l.opModeIsActive()) {
            sensorsInfo();
            power = Range.clip((ticks-avg)/ticks, .1, Math.abs(maxPower));
            setDrivePower(power);
            RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + LBPos + RFPos + LFPos) / 4;
        }
        setDrivePower(0);
    }

    // The default max power is 1.0, or 100%
    public void ForwardsPLoop(double sensor){
        ForwardsPLoop(sensor, 1.0);
    }
    //Same as forwardsPLoop, but backwards
    public void BackwardsPLoop(double sensor, double maxPower) {
        //Max Power should be normally set to 1, but for very precise Movements a value of .diagonalBrokeThreshold or lower is reccomended.
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double ticks = Math.abs(sensor) / cmPerTick;
        int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
        int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
        int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
        int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
        double avg = (RBPos + LBPos + RFPos + LFPos)/4;
        double power;
        while(avg < ticks && l.opModeIsActive()) {
            sensorsInfo();
            power = - Range.clip((ticks-avg)/ticks, .1, Math.abs(maxPower));
            setDrivePower(power);
            RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + LBPos + RFPos + LFPos) / 4;
        }
        setDrivePower(0);
    }
    // The default max power is 1.0, or 100%
    public void BackwardsPLoop(double sensor){
        BackwardsPLoop(sensor, 1.0);
    }

    // TurnLeftPLoop is designed to use a P style control loop to
    // turn a bit faster. It doesn't work too well,
    // but is good if we are in a rush but need more accuracy than
    // normal turning provides.
    public void TurnLeftPLoop(double degrees, double maxPower){
        //Max Power should be normally set to .5, but for very precise turns a value of .05 is reccomended.
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double power;
        while(navX.getYaw() >= degrees && l.opModeIsActive()){
            sensorsInfo();
            power = Range.clip((navX.getYaw() - degrees)/degrees, .05, maxPower);
            rightBackWheel.setPower(power);
            rightFrontWheel.setPower(power);
            leftBackWheel.setPower(-power);
            leftFrontWheel.setPower(-power);
        }
        setDrivePower(0);
    }

    // Turns left to a precise angle, regardless of current lineup.
    public void TurnLeftAbsolute(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        while(navX.getYaw() >= sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(power);
            rightFrontWheel.setPower(power);
            leftBackWheel.setPower(-power);
            leftFrontWheel.setPower(-power);
        }
        setDrivePower(0);

    }

    // Turns left to an angle based on the current reading.
    // This is most useful when we are using our file reading code.
    public void TurnLeftRelative(double sensor, double power){
        if(!navX.isConnected()){
            TurnLeftEnc(sensor, .10);
            return;
        }
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double yaw = navX.getYaw();
        sensor = -Math.abs(sensor);
        while(Math.abs(yaw - navX.getYaw()) < sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(power);
            rightFrontWheel.setPower(power);
            leftBackWheel.setPower(-power);
            leftFrontWheel.setPower(-power);
        }
        setDrivePower(0);

    }

    //  Functions similarly to TurnLeftAbsolute.
    public void TurnLeft(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        sensor = - Math.abs(sensor);
        while(navX.getYaw() >= sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(power);
            rightFrontWheel.setPower(power);
            leftBackWheel.setPower(-power);
            leftFrontWheel.setPower(-power);
        }
        setDrivePower(0);

    }

    // Uses Motor Encoders to turn faster, without the reliance on a gyroscope.
    // This turning tends to be less accurate,
    // but is good for faster turns that we are making
    // towards the end of a route.
    public void TurnLeftEnc(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double changeFactor = 90;
        double modified = changeFactor + sensor;

        double circleFrac = modified/360;
        double cm = width * circleFrac * Math.PI * 2;
        double movement = cm / cmPerTick;
        double ticks = movement/2;

        rightBackWheel.setPower(power);
        rightFrontWheel.setPower(power);
        leftBackWheel.setPower(-power);
        leftFrontWheel.setPower(-power);

        double avg = 0;
        do {
            int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + RFPos + LBPos + LFPos)/4;
            sensorsInfo();
        } while(avg < ticks && l.opModeIsActive());
        setDrivePower(0);
    }

    public void TurnRightPLoop(double degrees, double maxPower){
        //Max Power should be normally set to .5, but for very precise turns a value of .05 is reccomended.
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double power;
        while(navX.getYaw() <= degrees && l.opModeIsActive()){
            sensorsInfo();
            power = Range.clip((degrees - navX.getYaw())/degrees, .05, maxPower);
            rightBackWheel.setPower(power);
            rightFrontWheel.setPower(power);
            leftBackWheel.setPower(-power);
            leftFrontWheel.setPower(-power);
        }
        setDrivePower(0);
    }
    public void TurnRightAbsolute(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        while(navX.getYaw() <= sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(-power);
            rightFrontWheel.setPower(-power);
            leftBackWheel.setPower(power);
            leftFrontWheel.setPower(power);
        }
        setDrivePower(0);
    }
    public void TurnRight(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        sensor = Math.abs(sensor);
        while(navX.getYaw() <= sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(-power);
            rightFrontWheel.setPower(-power);
            leftBackWheel.setPower(power);
            leftFrontWheel.setPower(power);
        }
        setDrivePower(0);
    }
    public void TurnRightRelative(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double yaw = navX.getYaw();
        while(Math.abs(yaw - navX.getYaw()) < sensor && l.opModeIsActive()){
            sensorsInfo();
            rightBackWheel.setPower(-power);
            rightFrontWheel.setPower(-power);
            leftBackWheel.setPower(power);
            leftFrontWheel.setPower(power);
        }
        setDrivePower(0);
    }
    public void TurnRightEnc(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        ResetDriveEncoders();
        double changeFactor = 90;
        double modified = changeFactor + sensor;

        double circleFrac = modified/360;
        double cm = width * circleFrac * Math.PI * 2;
        double movement = cm / cmPerTick;
        double ticks = movement/2;

        rightBackWheel.setPower(-power);
        rightFrontWheel.setPower(-power);
        leftBackWheel.setPower(power);
        leftFrontWheel.setPower(power);

        double avg = 0;
        do {
            int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + RFPos + LBPos + LFPos)/4;
            sensorsInfo();
        } while(avg < ticks && l.opModeIsActive());
        setDrivePower(0);
    }

    // The StrafeLeft and StrafeRight methods
    public void StrafeLeft(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double ticks = sensor / cmPerTick;
        ticks *= distance;
        int avg = 0;
        rightFrontWheel.setPower(power);
        rightBackWheel.setPower(-power);
        leftFrontWheel.setPower(-power);
        leftBackWheel.setPower(power);
        do {
            int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + RFPos + LBPos + LFPos)/4;
            sensorsInfo();
        } while(avg < ticks && l.opModeIsActive());
        setDrivePower(0);
    }
    public void StrafeRight(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        double ticks = sensor / cmPerTick;
        ticks *= distance;
        int avg = 0;
        setStrafePower("Right", power);
        do {
            int RBPos = Math.abs(rightBackWheel.getCurrentPosition());
            int RFPos = Math.abs(rightFrontWheel.getCurrentPosition());
            int LBPos = Math.abs(leftBackWheel.getCurrentPosition());
            int LFPos = Math.abs(leftFrontWheel.getCurrentPosition());
            avg = (RBPos + RFPos + LBPos + LFPos)/4;
            sensorsInfo();
        } while(avg < ticks && l.opModeIsActive());
        setDrivePower(0);
    }

    // The getRange method filters out bogus range values of 255.
    // We found that these values were common around other robots
    // using similar sensors, so wee filter them out.
    public double getRange(double previous){
        double c = range.getDistance(DistanceUnit.CM);
        if(c == 255){
            return previous;
        } else {
            return c;
        }
    }

    // The ShootByVoltage method uses the battery voltage
    // to set the shooter to the right power.
    public void ShootByVoltage(){
        shooterOn = true;
        double volts = voltageGetter.getVoltage();
        double power = 1.00;
        if(volts > 13.3){
            power = 0.40;
        } else if(volts > 13.1){
            power = 0.50;
        } else if(volts > 12.9){
            power = 0.55;
        } else if(volts > 12.6){
            power = 0.60;
        } else if(volts > 12.3) {
            power = 0.70;
        } else if(volts > 12.0) {
            power = 0.80;
        } else {
            power = 0.90;
        }
        shoot1.setPower(power);
        shoot2.setPower(power);
    }

    // The StopShooter method sets the power of the shooter to zero.
    // It's use is pretty self-evident
    public void StopShooter(){
        shoot1.setPower(0);
        shoot2.setPower(0);
        shooterOn = false;
    }


    public void StrafeToWall(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            setStrafePower("Left", power);
        }
        if(range.getDistance(DistanceUnit.CM) == 255){
            StrafeToWall(sensor, power);
        }
        setDrivePower(0);
    }

    public void StrafeFromWall(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 0;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange < sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            setStrafePower("Right", power);
        }
        if(range.getDistance(DistanceUnit.CM) == 255){
            StrafeFromWall(sensor, power);
        }
        setDrivePower(0);
    }

    // Finds the white tape line beneath the robot.
    public void LineSearch(double power){
        LineSearch(2, power);
    }
    // The sensor value is the expected value of
    // the Color Sensor's Alpha reading.
    public void LineSearch(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        if(!l.opModeIsActive())
            Finish();
        while(colorSensorBottom.red() < sensor && colorSensorBottom.blue() < sensor && colorSensorBottom.alpha() < sensor && l.opModeIsActive()){
            sensorsInfo();
            setDrivePower(power);
        }
        setDrivePower(0);
    }

    // Follows the same logic as AlignToWithinOf,
    public void StrafeToPrecise(double cm, double threshold, double power){
        StrafeToWall(cm+threshold, power);
        StrafeFromWall(cm-threshold, power);
        StrafeToWall(cm+threshold, power);
    }

    // Presses the beacon without correction.
    public void PressBeaconSimple(team t){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        team colorReading;
        if(colorSensorOnSide.red() == 255){
            return;
        }
        if(colorSensorOnSide.red() > colorSensorOnSide.blue()){
            colorReading = team.Red;
        } else {
            colorReading = team.Blue;
        }
        if(colorReading == t){
            Move(1, - .25);
            rightButtonPusher.setPosition(RIGHT_SERVO_ON_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
        } else {
            rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_ON_VALUE);
        }
        try {
            Thread.sleep(1250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
        leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
    }

    // Presses the beacon so that the color is the same as the team input.
    // Will correct the beacon if it is the wrong color.
    public void PressBeacon(team t){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        team colorReading;
        if(colorSensorOnSide.red() == 255){
            return;
        }
        if(colorSensorOnSide.red() > colorSensorOnSide.blue()){
            colorReading = team.Red;
        } else {
            colorReading = team.Blue;
        }
        if(colorReading == t){
            Move(1, - .25);
            rightButtonPusher.setPosition(RIGHT_SERVO_ON_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
        } else {
            rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_ON_VALUE);
        }
            try {
                Thread.sleep(1250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        try {
            Thread.sleep(1250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
        leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
        if(!l.opModeIsActive())
        {
            Finish();
        }
        if(colorSensorOnSide.red() > colorSensorOnSide.blue()){
            colorReading = team.Red;
        } else {
            colorReading = team.Blue;
        }
        if(colorReading == t){
            return;
        } else {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(colorSensorOnSide.red() > colorSensorOnSide.blue()){
                colorReading = team.Red;
            } else {
                colorReading = team.Blue;
            }
            if(colorReading == t){
                rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
                leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
                return;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_ON_VALUE);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rightButtonPusher.setPosition(RIGHT_SERVO_OFF_VALUE);
            leftButtonPusher.setPosition(LEFT_SERVO_OFF_VALUE);
        }

    }

    // Sets the shooter to a power based on the parameter input.
    public void ShootAtPower(double power){
        shooterOn = true;
        if(!l.opModeIsActive())
            Finish();
        shoot1.setPower(power);
        shoot2.setPower(power);
    }

    // Turns on the infeed motor, and shoots two particles.
    public void EnableShot(double sensor, double power){
        if(!l.opModeIsActive())
            Finish();
        infeed.setPower(power);
        double timeStart = l.getRuntime();
        double lastTime = l.getRuntime();
        double deltaEnc1 = 0,
                deltaEnc2 = 0,
                pastEnc1 = shoot1.getCurrentPosition(),
                pastEnc2 = shoot2.getCurrentPosition(),
                DeltaAvg = 0, percentError;
        while ( ((l.getRuntime() - timeStart) < .750) && l.opModeIsActive()){
            if(shoot1.getPower() == 0) {
                shoot1.setPower(power);
                shoot2.setPower(power);
                break;
            } else if( !((l.getRuntime() - timeWait) < lastTime)){
                // We set up RPM handling within the running of our control loop.
                // This lets us get more accuracy from our flywheel shooter,
                // while saving time by letting us tweak the shooter power
                deltaEnc1 = Math.abs(Math.abs(shoot1.getCurrentPosition())-pastEnc1);
                deltaEnc2 = Math.abs(Math.abs(shoot2.getCurrentPosition())-pastEnc2);
                DeltaAvg = (deltaEnc1 + deltaEnc2) / 2;
                // We take the average change in encoder reading ...
                percentError = ((DeltaAvg - target) / DeltaAvg);
                // And calculate the percent error based on an expected change,
                // which is based on the target speed value.
                // Based on this, we add our percent error to the current power ...
                power = Range.clip( power - percentError / 5 , -1, 1);
                shoot1.setPower(power);
                shoot2.setPower(power);
                l.telemetry.clear();
                l.telemetry.addData("Delta Avg", DeltaAvg);
                l.telemetry.addData("Target", target);
                l.telemetry.addData("Power", shoot1.getPower());
                l.telemetry.addData("Delta 1", deltaEnc1);
                l.telemetry.addData("Delta 2", deltaEnc2);
                l.telemetry.addData("Percent Error", percentError);
                l.telemetry.update();
                pastEnc1 = Math.abs(shoot1.getCurrentPosition());
                pastEnc2 = Math.abs(shoot2.getCurrentPosition());
                lastTime = l.getRuntime();
                // And then store out old times and encoder positions
            }
        }
        ballBlock.setPosition(BALLBLOCKOPEN);
        while ( ((l.getRuntime() - timeStart) < 1.250) && l.opModeIsActive()){
            if(shoot1.getPower() == 0) {
                shoot1.setPower(power);
                shoot2.setPower(power);
                break;
            } else if( !((l.getRuntime() - timeWait) < lastTime)){
                // We set up RPM handling within the running of our control loop.
                // This lets us get more accuracy from our flywheel shooter,
                // while saving time by letting us tweak the shooter power
                deltaEnc1 = Math.abs(Math.abs(shoot1.getCurrentPosition())-pastEnc1);
                deltaEnc2 = Math.abs(Math.abs(shoot2.getCurrentPosition())-pastEnc2);
                DeltaAvg = (deltaEnc1 + deltaEnc2) / 2;
                // We take the average change in encoder reading ...
                percentError = ((DeltaAvg - target) / DeltaAvg);
                // And calculate the percent error based on an expected change,
                // which is based on the target speed value.
                // Based on this, we add our percent error to the current power ...
                power = Range.clip( power - percentError / 5 , -1, 1);
                shoot1.setPower(power);
                shoot2.setPower(power);
                l.telemetry.clear();
                l.telemetry.addData("Delta Avg", DeltaAvg);
                l.telemetry.addData("Target", target);
                l.telemetry.addData("Power", shoot1.getPower());
                l.telemetry.addData("Delta 1", deltaEnc1);
                l.telemetry.addData("Delta 2", deltaEnc2);
                l.telemetry.addData("Percent Error", percentError);
                l.telemetry.update();
                pastEnc1 = Math.abs(shoot1.getCurrentPosition());
                pastEnc2 = Math.abs(shoot2.getCurrentPosition());
                lastTime = l.getRuntime();
                // And then store out old times and encoder positions
            }
        }
        ballBlock.setPosition(BALLBLOCKCLOSED);
        infeed.setPower(0);
        infeedOn = false;
    }

    // Runs the super.stop() method, and turns off all of the sensors
    public void Finish(){
        navX.close();
        colorSensorOnSide.close();
        colorSensorBottom.close();
        range.close();
        l.stop();
    }

    // If the NavX is offset by more than this angle,
    // the robot will exit the strafing control loop and
    // run the handleCollision code.
    double diagonalBrokeThreshold = 17.5;

    // We want to back away from whatever dislodged us,
    // and then strafe to the wall to the same distance as the
    // DiagonalForwardsLeft was told to.
    public void handleCollision(double sensor){
        boolean turnedTooFarRight;
        if(navX.getYaw() < 0){
            turnedTooFarRight = true;
            Move(15, 1.0);
        } else{
            turnedTooFarRight = false;
            Move(15, -1.0);
        }
        StrafeToWall(sensor, .20);
        AlignToWithin(1.5, .05);
        // Line back up to center.
        if(!turnedTooFarRight){
            Move(25, 1.0);
        } else {
            Move(25, -1.0);
        }
        // This will give us a similar outcome to the expected outcome
        // of the call to DiagonalForwards
    }

    // DiagonalForwardsLeft will strafe at a diagonal until the
    public boolean DiagonalForwardsLeft(double distance, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        double angStart = navX.getYaw();
        while(pastRange > distance && l.opModeIsActive() && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(1, -1, 0);
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(distance);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }

    // This method functions like DiagonalForwardsLeft,
    // but it takes separate input for the
    // x and y directions of motion.
    public boolean DiagonalForwardsLeft(double distance, double yIn, double xIn) {
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();

        double angStart = navX.getYaw();
        while(pastRange > distance && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(yIn, -xIn, 0);
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(distance);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }

    // Functions like the above method, but coasts instead of stopping. This is good for curved motion.
    public boolean DiagonalForwardsLeftCoast(double distance, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        double angStart = navX.getYaw();
        while(pastRange > distance && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(power, -power, 0);
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(distance);
            setDrivePower(0);
            return true;
        }
        return false;
    }
    public boolean DiagonalForwardsRight(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        double angStart = navX.getYaw();
        while(pastRange > sensor && l.opModeIsActive() && (Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold)){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(power, power, 0);
            return true;
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(sensor);
            return true;
        }

        setDrivePower(0);
        return false;
    }
    public boolean DiagonalForwardsRight(double sensor, double yIn, double xIn){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(yIn, xIn, 0);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }
    public boolean DiagonalForwardsRightCoast(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(power, power, 0);
            return true;
        }
        return false;
    }
    public boolean DiagonalBackwardsRight(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-power, power, 0);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }
    public boolean DiagonalBackwardsRight(double sensor, double yIn, double xIn){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-yIn, xIn, 0);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }
    public boolean DiagonalBackwardsRightCoast(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        while(pastRange > sensor && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-power, power, 0);
            return true;
        }
        return false;
    }
    public boolean DiagonalBackwardsLeft(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        double angStart = navX.getYaw();
        while(pastRange > sensor && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-power, -power, 0);
        }

        if(Math.abs(navX.getYaw() - angStart) > 25){
            handleCollision(sensor);
            setDrivePower(0);
            return true;
        }
        setDrivePower(0);
        return false;
    }
    public boolean DiagonalBackwardsLeft(double sensor, double yIn, double xIn){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();
        double angStart = navX.getYaw();
        while(pastRange > sensor && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-yIn, -xIn, 0);
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(sensor);
            return true;
        }
        setDrivePower(0);
        return false;
    }
    public boolean DiagonalBackwardsLeftCoast(double sensor, double power){
        if(infeedOn){
            infeed.setPower(1);
        } else {
            infeed.setPower(0);
        }
        if(shooterOn){
            ShootByVoltage();
        } else {
            shoot1.setPower(0);
            shoot2.setPower(0);
        }
        double pastRange = 254;
        if(!l.opModeIsActive())
            Finish();

        double angStart = navX.getYaw();
        while(pastRange > sensor && Math.abs(navX.getYaw() - angStart) < diagonalBrokeThreshold && l.opModeIsActive()){
            sensorsInfo();
            pastRange = getRange(pastRange);
            arcadeMecanum(-power, -power, 0);
        }
        if(Math.abs(navX.getYaw() - angStart) > diagonalBrokeThreshold){
            handleCollision(sensor);
            return true;
        }
        return false;
    }

    public void ArcadeToAngleLeft(double yIn, double xIn, double cIn, double angleTarget){
        arcadeMecanum(yIn, xIn, cIn);
        while(navX.getYaw() >= -Math.abs(angleTarget) && l.opModeIsActive()){
            sensorsInfo();
        }
        setDrivePower(0);
    }
    public void ArcadeToAngleRight(double yIn, double xIn, double cIn, double angleTarget){
        arcadeMecanum(yIn, xIn, cIn);
        while(navX.getYaw() <= Math.abs(angleTarget) && l.opModeIsActive()){
            sensorsInfo();
        }
        setDrivePower(0);
    }
    public void ArcadeToAngleLeftCoast(double yIn, double xIn, double cIn, double angleTarget){
        arcadeMecanum(yIn, xIn, cIn);
        while(navX.getYaw() >= -Math.abs(angleTarget) && l.opModeIsActive()){
            sensorsInfo();
        }
    }
    public void ArcadeToAngleRightCoast(double yIn, double xIn, double cIn, double angleTarget){
        arcadeMecanum(yIn, xIn, cIn);
        while(navX.getYaw() <= Math.abs(angleTarget) && l.opModeIsActive()){
            sensorsInfo();
        }
    }


    // This is the same method as we use in TeleOp.
    // We use it for our diagonal strafing methods.
    public void arcadeMecanum(double y, double x, double c) {
        double leftFrontVal = y + x + c;
        double rightFrontVal = y - x - c;
        double leftBackVal = y - x + c;
        double rightBackVal = y + x - c;

        //Move range to between -1 and +1, if not there already
        double[] wheelPowers = {rightFrontVal, leftFrontVal, leftBackVal, rightBackVal};
        Arrays.sort(wheelPowers);
        if (wheelPowers[3] > 1) {
            leftFrontVal /= wheelPowers[3];
            rightFrontVal /= wheelPowers[3];
            leftBackVal /= wheelPowers[3];
            rightBackVal /= wheelPowers[3];
        }
        leftFrontWheel.setPower(leftFrontVal);
        leftBackWheel.setPower(leftBackVal);
        rightFrontWheel.setPower(rightFrontVal);
        rightBackWheel.setPower(rightBackVal);
    }

}
