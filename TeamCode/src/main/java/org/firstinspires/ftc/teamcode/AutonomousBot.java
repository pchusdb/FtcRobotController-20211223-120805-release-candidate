package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AutonomousBot extends LinearOpMode {
    public final static String detectionObjectLabel = "EagleTwo";
    /* Note: This sample uses the all-objects Tensor Flow model (FreightFrenzy_BCDM.tflite), which contains
     * the following 4 detectable objects
     *  0: Ball,
     *  1: Cube,
     *  2: Duck,
     *  3: Marker (duck location tape marker)
     *
     *  Two additional model assets are available which only contain a subset of the objects:
     *  FreightFrenzy_BC.tflite  0: Ball,  1: Cube
     *  FreightFrenzy_DM.tflite  0: Duck,  1: Marker
     */
    /*
    private static final String TFOD_MODEL_ASSET = "FreightFrenzy_BCDM.tflite";
    private static final String[] LABELS = {
            "Ball",
            "Cube",
            "Duck",
            "Marker"
    };
    */

    private static final String TFOD_MODEL_FILE = "model_20220209_155545.tflite";
    private static final String[] LABELS = {
            "BlueAllianceShippingHub",
            "EagleOne",
            "EagleTwo",
            "RedBarcodeMarker1",
            "RedBarcodeMarker3",
            "SharedStorageWallRedStorageTarget"
    };

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY =
            "AZOImLb/////AAABmfzn2uj5DUp8hXLqjZPI08tknrCi8i5Bh3EO5hAASEpOVIbaLggw6xHMN4vGzLS13kmV35Z1bnE9stQafvASuvzPJfHZLXbNegPkIfVqggwvOsoiLItss41X8RiitJE1OTQafU80VtIR93FplVLwAl3/hrLMUz0HRIAJGRB13mx7wHo6TTgvOySzpqDCT3VezG+iHtyXuT749QNbwkosHgwheD9I3yMDOE0bxcdcuFwzurDz2rB3cCttvn4Vfpmlyfn9vmiBJ8pBtW1Nn5DUJ3ab59e5CXk6SFtKixRzbjZ5/XhSR48GKiV74knMED343ST6AV02Aju0cupfflG+g7okhNX7QvZ0Bi7N4vYlo9m0";
    private final ElapsedTime runtime = new ElapsedTime();
    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    protected VuforiaLocalizer vuforia;
    /**
     * {@link #tfod} is the variable we will use to store our instance of the TensorFlow Object
     * Detection engine.
     */
    protected TFObjectDetector tfod;
    /* Declare OpMode members. */
    StandardBot robot = new StandardBot();


    // Constructor
    public AutonomousBot() {

    }

    public void setDefaultMotorDirections() {
        robot.stdLeftFront.setDirection(DcMotorEx.Direction.FORWARD);
        robot.stdLeftRear.setDirection(DcMotorEx.Direction.FORWARD);
        robot.stdRightFront.setDirection(DcMotorEx.Direction.REVERSE);
        robot.stdRightRear.setDirection(DcMotorEx.Direction.REVERSE);

        //robot.stdArmMotor.setDirection(DcMotorEx.Direction.REVERSE);

    }

    public void turnRight() {
        encoderDrive(StandardBot.OPTIMAL_TURN_SPEED, StandardBot.TILE_SIZE - 8, -(StandardBot.TILE_SIZE - 8), 5.0);
    }

    public void turnLeft() {
        encoderDrive(StandardBot.OPTIMAL_TURN_SPEED, -(StandardBot.TILE_SIZE - 8), StandardBot.TILE_SIZE - 8, 5.0);
    }

    public void moveForward(double nTiles) {
        encoderDrive(StandardBot.OPTIMAL_DRIVE_SPEED, nTiles * StandardBot.TILE_SIZE, nTiles * StandardBot.TILE_SIZE, 5.0);
    }

    public void moveForward(double nTiles, double speed) {
        encoderDrive(speed, nTiles * StandardBot.TILE_SIZE, nTiles * StandardBot.TILE_SIZE, 5.0);
    }

    public void moveBackward(double nTiles) {
        moveForward(-nTiles);
    }

    public void moveBackward(double nTiles, double speed) {
        moveForward(-nTiles, speed);
    }

    public void spinCarousel(DcMotorEx.Direction direction, double revolutions, double speed, int timeInMilliSeconds) {
        robot.stdCarouselMotor.setDirection(direction);
        robot.stdCarouselMotor.setTargetPosition((int)Math.round(revolutions * StandardBot.ONE_CAROUSEL_TURN_IN_INCHES * StandardBot.TICKS_PER_INCH));
        robot.stdCarouselMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.stdCarouselMotor.setVelocity(speed);

        while (robot.stdCarouselMotor.isBusy()) {
            telemetry.addData("spinCarousel", "Current wheel position %7d", robot.stdCarouselMotor.getCurrentPosition());
            telemetry.addData("spinCarousel", "Target wheel position %7d", robot.stdCarouselMotor.getTargetPosition());
            telemetry.update();
        }
        sleep(timeInMilliSeconds);
    }

    public void liftArm(int position) {

        telemetry.addData("liftArm", "Setting ExtenderServo to MAX_POSITION");
        robot.stdExtenderServo.setPosition(StandardBot.EXTENDER_MAX_POSITION);

        telemetry.addData("liftArm", "Setting to Stop and Reset Encoder");
        robot.stdArmMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        telemetry.addData("liftArm", "Setting TargetPosition to %5d", position);
        robot.stdArmMotor.setTargetPosition(position);

        telemetry.addData("liftArm", "Setting Run to Position");
        robot.stdArmMotor.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);  // Can't hurt to call this repeatedly

        telemetry.addData("liftArm", "Setting Arm Velocity to %7.2f", StandardBot.ARM_MAX_VELOCITY );
        robot.stdArmMotor.setVelocity(StandardBot.OPTIMAL_ARM_SPEED);

        while (robot.stdArmMotor.isBusy())
        {
          telemetry.addData("liftArm", "ArmMotor targetPosition is %7d", robot.stdArmMotor.getTargetPosition());
          telemetry.addData("liftArm", "ArmMotor currentPosition is %7d", robot.stdArmMotor.getCurrentPosition());
          telemetry.addData("liftArm", "ArmMotor CURRENT is %5.2f milli-amps", robot.stdArmMotor.getCurrent(CurrentUnit.MILLIAMPS));
          telemetry.update();
        
        }

        telemetry.addData("liftArm", "Setting velocity to 0.0 now");
        robot.stdArmMotor.setVelocity(0.0);

        telemetry.addData("liftArm", "ArmMotor targetPosition is %7d", robot.stdArmMotor.getTargetPosition());
        telemetry.addData("liftArm", "ArmMotor currentPosition is %7d", robot.stdArmMotor.getCurrentPosition());
        telemetry.addData("liftArm", "ArmMotor CURRENT is %5.2f milli-amps", robot.stdArmMotor.getCurrent(CurrentUnit.MILLIAMPS));
        telemetry.update();
    }

    public void returnArmPosition() {

        telemetry.addData("returnArmPosition", "Setting ExtenderServo to MAX_POSITION");
        robot.stdExtenderServo.setPosition(StandardBot.EXTENDER_MAX_POSITION);

        int position = robot.stdArmMotor.getCurrentPosition();
        telemetry.addData("returnArmPosition", "Arm Current Position is %5d", position);

        telemetry.addData("returnArmPosition", "Setting TargetPosition to %5d", StandardBot.ARM_LEVEL_REST);
        robot.stdArmMotor.setTargetPosition(StandardBot.ARM_LEVEL_REST);

        telemetry.addData("returnArmPosition", "Setting Run to Position");
        robot.stdArmMotor.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);

        telemetry.addData("returnArmPosition", "Set Velocity to %7.2f", StandardBot.ARM_MAX_VELOCITY);
        robot.stdArmMotor.setVelocity(StandardBot.OPTIMAL_ARM_SPEED);

        while (robot.stdArmMotor.isBusy())
        {
            telemetry.addData("returnArmPosition", "ArmMotor targetPosition is %7d", robot.stdArmMotor.getTargetPosition());
            telemetry.addData("returnArmPosition", "ArmMotor currentPosition is %7d", robot.stdArmMotor.getCurrentPosition());
            telemetry.addData("returnArmPosition", "ArmMotor CURRENT is %5.2f milli-amps", robot.stdArmMotor.getCurrent(CurrentUnit.MILLIAMPS));
            telemetry.update();

        }

        telemetry.addData("returnArmPosition", "Setting velocity to 0.0 now");
        robot.stdArmMotor.setVelocity(0.0);

        telemetry.addData("returnArmPosition", "ArmMotor targetPosition is %7d", robot.stdArmMotor.getTargetPosition());
        telemetry.addData("returnArmPosition", "ArmMotor currentPosition is %7d", robot.stdArmMotor.getCurrentPosition());
        telemetry.addData("returnArmPosition", "ArmMotor CURRENT is %5.2f milli-amps", robot.stdArmMotor.getCurrent(CurrentUnit.MILLIAMPS));

        telemetry.update();
    }

    public void leftStrafe(double nAmounts) {
        encoderLeftStrafe(StandardBot.OPTIMAL_STRAFE_SPEED, nAmounts * StandardBot.TILE_SIZE, nAmounts * StandardBot.TILE_SIZE, 5.0);
    }

    public void rightStrafe(double nAmounts) {
        leftStrafe(-nAmounts);
    }

    public void collectGameElement(double power, int time) {
        robot.stdIntakeServo.setPower(power);
        sleep(time);
        robot.stdIntakeServo.setPower(0);
    }

    public void regurgitateGameElement(double power, int time) {
        robot.stdIntakeServo.setPower(-power);
        sleep(time);
        robot.stdIntakeServo.setPower(0);
    }

    public void encoderDrive(double speed, double leftInches, double rightInches, double timeOut) {
        int newLeftTarget;
        int newRightTarget;

        telemetry.addData("encoderDrive", "Inside econderDrive(...)");

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            telemetry.addData("encoderDrive", "Resetting Drive Train Encoder");
            robot.resetDriveTrainEncoder();

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.stdLeftFront.getCurrentPosition() + (int) (leftInches * StandardBot.TICKS_PER_INCH);
            newRightTarget = robot.stdRightFront.getCurrentPosition() + (int) (rightInches * StandardBot.TICKS_PER_INCH);

            telemetry.addData("encoderDrive", "newLeftTarget = %7d", newLeftTarget);
            robot.stdLeftFront.setTargetPosition(newLeftTarget);
            robot.stdLeftRear.setTargetPosition(newLeftTarget);

            telemetry.addData("encoderDrive", "newRightTarget = %7d", newRightTarget);
            robot.stdRightFront.setTargetPosition(newRightTarget);
            robot.stdRightRear.setTargetPosition(newRightTarget);

            telemetry.addData("encoderDrive", "Setting DriveTrain to RUN_TO_POSITION");
            robot.setDriveTrainToRunToPosition();

            telemetry.addData("encoderDrive", "Setting DriveTrainVelocity to %7.2f", speed);
            robot.setDriveTrainVelocity(speed);

            telemetry.update();


            // reset the timeout time and start motion.
            runtime.reset();
            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the this will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeOut) &&
                    robot.isDriveTrainBusy()) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d, %7d :%7d",
                        robot.stdLeftFront.getCurrentPosition(),
                        robot.stdLeftRear.getCurrentPosition(),
                        robot.stdRightFront.getCurrentPosition(),
                        robot.stdRightRear.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.setDriveTrainVelocity(0.0);

        }
    }

    public void encoderLeftStrafe(double speed, double leftInches, double rightInches, double timeOutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            telemetry.addData("encoderDrive", "Resetting Drive Train Encoder");
            robot.resetDriveTrainEncoder();

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.stdLeftFront.getCurrentPosition() + (int) (leftInches * StandardBot.TICKS_PER_INCH);
            newRightTarget = robot.stdRightFront.getCurrentPosition() + (int) (rightInches * StandardBot.TICKS_PER_INCH);

            robot.stdLeftFront.setTargetPosition(-newLeftTarget);
            robot.stdLeftRear.setTargetPosition(newLeftTarget);
            robot.stdRightFront.setTargetPosition(newRightTarget);
            robot.stdRightRear.setTargetPosition(-newRightTarget);

            // Turn On RUN_TO_POSITION
            robot.setDriveTrainToRunToPosition();

            // reset the timeout time and start motion.
            runtime.reset();

            robot.setDriveTrainVelocity(speed);
            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the this continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeOutS) &&
                    (robot.stdLeftFront.isBusy() && robot.stdLeftRear.isBusy() &&
                            robot.stdRightFront.isBusy() && robot.stdRightRear.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d, %7d :%7d",
                        robot.stdLeftFront.getCurrentPosition(),
                        robot.stdLeftRear.getCurrentPosition(),
                        robot.stdRightFront.getCurrentPosition(),
                        robot.stdRightRear.getCurrentPosition());
                telemetry.update();
            }
            // Stop all motion;
            robot.setDriveTrainVelocity(0.0);
        }
    }

    public int getTargetLevelFromBarcode(String colorSide) {
        //default to one second if waitSecond isn't specified
        return getTargetLevelFromBarcode(colorSide, 1);
    }

    public int getTargetLevelFromBarcode(String colorSide, int waitSeconds) {

        //robot.init(hardwareMap);
        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.

        int preloadTargetLevel = 0;

        /*
         * Activate TensorFlow Object Detection before we wait for the start command.
         * Do it here so that the Camera Stream window will have the TensorFlow annotations visible.
         */
        if (tfod != null) {
            tfod.activate();

            // The TensorFlow software will scale the input images from the camera to a lower resolution.
            // This can result in lower detection accuracy at longer distances (> 55cm or 22").
            // If your target is at distance greater than 50 cm (20") you can adjust the magnification value
            // to artificially zoom in to the center of image.  For best results, the "aspectRatio" argument
            // should be set to the value of the images used to create the TensorFlow Object Detection model
            // (typically 16/9).
            tfod.setZoom(1.0, 16.0 / 9.0);
        }
        long startTime = runtime.time(TimeUnit.SECONDS);
        long currentTime = runtime.time(TimeUnit.SECONDS);
        long counter = 0;
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.

            while (currentTime - startTime < waitSeconds && preloadTargetLevel == 0) {
                counter++;
                telemetry.addData("Counter", "%7d", counter);
                telemetry.addData("startTime", "%7d", startTime);
                telemetry.addData("currentTime", "%7d", currentTime);
                telemetry.update();
                sleep(1000);
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    // step through the list of recognitions and display boundary info.
                    int i = 0;

                    for (Recognition recognition : updatedRecognitions) {
                        if (recognition.getConfidence() >= 0.80) {
                            String objLabel = recognition.getLabel();
                            float objLeft = recognition.getLeft();
                            float objTop = recognition.getTop();
                            float objRight = recognition.getRight();
                            float objBottom = recognition.getBottom();

                            if (recognition.getLabel().equals(detectionObjectLabel)) // From BLUE SIDE
                            {
                                if (colorSide.equals("BLUE")) {
                                    if (objLeft >= 0.00 && objLeft <= 200) // detectionObject is at LEVEL 2 BARCODE
                                    {
                                        preloadTargetLevel = 2;
                                    } else if (objLeft >= 300.00 && objLeft <= 500.00) // detectionObject is at LEVEL 1 BARCODE
                                    {
                                        preloadTargetLevel = 3;
                                    }
                                    else // default to LEVEL 1 if not seen by camera
                                    {
                                        preloadTargetLevel = 1;
                                    }
                                } else if (colorSide.equals("RED")) {
                                    if (objLeft >= 0.00 && objLeft <= 200) // detectionObject is at LEVEL 1 BARCODE
                                    {
                                        preloadTargetLevel = 1;
                                    } else if (objLeft >= 300.00 && objLeft <= 500.00) // detectionObject is at LEVEL 2 BARCODE
                                    {
                                        preloadTargetLevel = 2;
                                    }
                                    else // default to LEVEL 3 BARCODE if detectionObject is not in view of camera
                                    {
                                        preloadTargetLevel = 3;
                                    }
                                }
                            }

                            telemetry.addData(String.format("label (%d)", i), objLabel);
                            telemetry.addData(String.format("  left,top (%d)", i), "%.03f , %.03f",
                                    objLeft, objTop);
                            telemetry.addData(String.format("  right,bottom (%d)", i), "%.03f , %.03f",
                                    objRight, objBottom);
                            telemetry.addData("ObjectDetection", detectionObjectLabel + " is at level %d ", preloadTargetLevel);

                            i++;

                            telemetry.update();
                        }
                    }
                }

                currentTime = runtime.time(TimeUnit.SECONDS);
            } // Ends while(currentTime - startTime < 5)
        }
        /*telemetry.addData("preloadTargetLevel","%7d",preloadTargetLevel);
        telemetry.addData("Counter","%7d",counter);
        telemetry.addData("startTime","%7d",startTime);
        telemetry.addData("currentTime","%7d",currentTime);
        telemetry.update();
        sleep(5000);*/
        return preloadTargetLevel;
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    public void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    public void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 320;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        //tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
        tfod.loadModelFromFile(TFOD_MODEL_FILE, LABELS);
    }

    @Override
    public void runOpMode() {
        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");
        telemetry.update();

        robot.resetDriveTrainEncoder();
        robot.setDriveTrainToRunUsingEncoder();

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Motors", "Current Position at %7d :%7d %7d :%7d",
                robot.stdLeftFront.getCurrentPosition(),
                robot.stdLeftRear.getCurrentPosition(),
                robot.stdRightFront.getCurrentPosition(),
                robot.stdRightRear.getCurrentPosition());

        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        telemetry.addData("Path", "Complete");
        telemetry.update();
    }
}
