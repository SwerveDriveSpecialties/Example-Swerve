/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.swervedrivespecialties.exampleswerve.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.swervedrivespecialties.exampleswerve.RobotMap;
import com.swervedrivespecialties.exampleswerve.commands.infeed.InfeedSubsystemCommands;
import com.swervedrivespecialties.exampleswerve.commands.infeed.runConveyorMotors;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Infeed extends SubsystemBase {

  private final double kEncoderCountsPerBall = 7500;
  private final double kConveyorTalonConstantVBus = -0.3;
  private final double kFeederTalonConstantVBus = 0.7;

  private static Infeed _instance = new Infeed();

  public static Infeed get_instance() {
    return _instance;
  }

  private TalonSRX _conveyorTalon;
  private DigitalInput _beamSensor;
  private DigitalInput _beamSensorStopBall;
  private boolean _isFinished = false;
  private boolean _sensorlast;
  private boolean _sensorthis;
  private boolean _isFirstCycle;

  public enum CONVEYORPHASES {
    PHASE1, PHASE2;
  }

  CONVEYORPHASES phase = CONVEYORPHASES.PHASE1;

  /**
   * Creates a new Infeed.
   */
  private Infeed() {
    _conveyorTalon = new TalonSRX(RobotMap.CONVEYOR_MOTOR);
    _conveyorTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
    _beamSensor = new DigitalInput(RobotMap.BEAM_SENSOR);
    _beamSensorStopBall = new DigitalInput(RobotMap.BEAM_SENSOR_STOP_BALL);
  }

  public void setDefault() {
    phase = CONVEYORPHASES.PHASE1;
    _isFinished = false;
    _conveyorTalon.setSelectedSensorPosition(RobotMap.BEAM_SENSOR);
  }

  public void outputToSDB() {
    SmartDashboard.putBoolean("Bobo", _beamSensorStopBall.get());
    SmartDashboard.putNumber("ConveyorVAL", _conveyorTalon.getSelectedSensorPosition());
  }

  public void vbusFeederWheel(){
    _conveyorTalon.set(ControlMode.PercentOutput, kFeederTalonConstantVBus);
  }
  public void setConveyorZero()
  {
    _conveyorTalon.set(ControlMode.PercentOutput, 0.0);
  }

  public void conveyorPhases() {
    if (_conveyorTalon.getSelectedSensorPosition() <= kEncoderCountsPerBall) {
      _conveyorTalon.set(ControlMode.PercentOutput, kConveyorTalonConstantVBus);
      _isFinished = false;
      phase = CONVEYORPHASES.PHASE2;
    }

    else if (phase == CONVEYORPHASES.PHASE2) {
      _conveyorTalon.set(ControlMode.PercentOutput, 0);
      _isFinished = true;
    }

    else {
      System.out.println("you're bad");
    }
  }

  

  public void PrintSmartDash()
  {
    SmartDashboard.putBoolean("SensorVAl", _beamSensor.get());
  }
  public void endConveyorPhases() {
    _conveyorTalon.set(ControlMode.PercentOutput, 0);
  }

  public boolean isSensorTrue() {
    if (_isFirstCycle) {
      _sensorthis = _beamSensor.get();
      return false;
    } else {
      _sensorlast = _sensorthis;
      _sensorthis = _beamSensor.get();
      return !_sensorthis && _sensorlast;
    }
  }

  public boolean isConveyorFinished() {
    return _isFinished;
  }

  public boolean STOPTHEFREAKINGBALLJIMBO() {
    return !_beamSensorStopBall.get();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    if (isSensorTrue()) {
      CommandBase conveyorCommand = InfeedSubsystemCommands.getRunConveyorMotorsCommand();
      conveyorCommand.schedule();
    }
  }
}
