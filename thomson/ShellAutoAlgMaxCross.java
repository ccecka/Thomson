import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class ShellAutoAlgMaxCross
    implements ShellAlgorithm
{
  AutoAlgorithm alg;

  double tStep, tStepLimit;

  double lastMeasure, lastEnergy;
  long lastUpdate;
  boolean lastFell, oscillating, energyTest;

  /** Constructor */
  ShellAutoAlgMaxCross(AutoAlgorithm alg_) {
    alg = alg_;

    lastMeasure = Double.POSITIVE_INFINITY;
    lastEnergy = Double.NEGATIVE_INFINITY;
    lastUpdate = System.currentTimeMillis();

    lastFell = true;
    oscillating = false;
    energyTest = false;

    tStep = 8.0;
    tStepLimit = 0.00006;
  }

  /** Name the algorithm */
  public String toString()
  {
    return "Auto Relax";
  }

  /** Select and initialize the auto relaxation
   */
  public void select(ShellApplet sa)
  {
    alg.select(sa);

    lastMeasure = Double.POSITIVE_INFINITY;
    lastEnergy = Double.NEGATIVE_INFINITY;
    lastUpdate = System.currentTimeMillis();

    lastFell = true;
    oscillating = false;
    energyTest = false;

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
  }

  /** Apply one step of the auto relax and respond to the ShellApplet
   */
  public void apply(ShellApplet sa)
  {
    // One step of the auto relax
    tStep = doAuto(sa.getShell(), tStep);

    // Update the visualization periodically
    long currentTime = System.currentTimeMillis();
    if( currentTime - lastUpdate > 500 ) {
      sa.optionsSpinner[0].setValue(new Double(tStep));
      lastUpdate = currentTime;
    }

    // Once tStep gets low enough, leave autorelax
    if( tStep < tStepLimit ) {
      sa.optionsSpinner[0].setValue(new Double(tStepLimit));

      // Press the auto button which should be in the "Halt" state
      if( sa.autoButton.getText().equals("Halt") )
        sa.autoButton.doClick();
    }
  }

  /** Apply the auto relax until tStepLimit is reached */
  public void apply(Shell shell, double tStepLimit)
  {
    while( tStep > tStepLimit )
      tStep = doAuto(shell, tStep);
  }

  /** Apply one step of the auto relax with time step tStep */
  private double doAuto(Shell shell, double tStep)
  {
    double currentMeasure = alg.apply(shell, tStep);

    // Decide to increase or decrease timeStep (very heuristic)
    // if maxCrossSq is oscillating, decrease tStep
    if( ((currentMeasure < lastMeasure) != lastFell) && oscillating ) {
      tStep *= .90;
      oscillating = false;
    } else if( (currentMeasure < lastMeasure) != lastFell ) {
      oscillating = true;
      // if maxCrossSq is falling, increase tStep
    } else if( lastFell ) {
      tStep *= 1.01;
      oscillating = false;
      // if maxCrossSq is rising, check the energy
    } else if( !energyTest ) {
      lastEnergy = shell.totalEnergy();
      energyTest = true;
      // if energy is rising, drop tStep quickly
    } else if( shell.totalEnergy() > lastEnergy ) {
      tStep *= .5;
      oscillating = false;
      energyTest = false;
      // if energy is falling, increase tStep
    } else {
      tStep *= 1.1;
      oscillating = false;
      energyTest = false;
    }

    lastFell = currentMeasure < lastMeasure;
    lastMeasure = currentMeasure;

    return tStep;
  }
}