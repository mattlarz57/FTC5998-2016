package org.firstinspires.ftc.teamcode.Blue;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * Created by Ethan Schaffer on 1/11/2017.
 */
@Autonomous(group = "Blue", name = "2BS (B)  ")
public class Blue2BShoot extends LinearOpMode {
    Robot robot = new Robot();

    @Override
    public void runOpMode() throws InterruptedException {
        robot.initialize(Blue2BShoot.this, hardwareMap, telemetry, true);
        waitForStart();
        robot.Move(40, - 1.00);
        robot.TurnRight(35, 0.15);
        robot.Move(240, - 1.00);
        robot.AlignToWithin(3, 0.05);
        //Line up with the wall
        robot.StrafeToWall(15, 0.10);

        robot.AlignToWithin(2.5, 0.05);
        robot.AlignToWithin(2.5, 0.05);
        robot.LineSearch(2, 0.10);
        robot.LineSearch(2, - 0.05);
        robot.AlignToWithin(2.5, 0.05);
        robot.AlignToWithin(1.0, 0.04);
        robot.StrafeToWall(9, 0.10);

        robot.Move(2, - 1.00);
        robot.LineSearch(2, 0.05);
        robot.PressBeacon(Robot.team.Blue );
        //Press the first beacon

        robot.StrafeFromWall(15, 1.0);
        robot.AlignToWithin(3, 0.05);
        robot.Move(140, - 1.00);
        robot.LineSearch(2, - 0.11);
        robot.AlignToWithin(2.5, 0.05);
        robot.AlignToWithin(2.5, 0.05);
        robot.StrafeToWall(10, 0.10);
        robot.Move(1, 1.00);
        robot.LineSearch(2, - 0.10);
        robot.LineSearch(2, 0.05);
        robot.AlignToWithin(5, 0.05);
        robot.StrafeToWall(8, 0.10);
        robot.AlignToWithin(2.5, 0.05);
        robot.AlignToWithin(1, 0.04);
        robot.PressBeacon(Robot.team.Blue);
        //Press the second beacon

        robot.StrafeFromWall(13, 1.00);
        robot.ShootSmart();
//        robot.ShootAtPower(0, 0.80); //Turn on the shooter so it can speed up without wasting time
        robot.TurnRight(35, 0.10);
        robot.Move(145, 1.00);
        sleep(250);
        robot.EnableShot(850, 1.00);
        //Shoot the particles into the vortex
        robot.StopShooter();
//        robot.ShootAtPower(0, 0.00);
        robot.Move(60, 1.00);
        //Go park on the wooden part of the field
    }
}
