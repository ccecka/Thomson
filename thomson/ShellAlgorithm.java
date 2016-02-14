
interface ShellAlgorithm
{
  /** Name the algorithm
   */
  public String toString();

  /** Select this algorithm.
   * Configure the ShellApplet to reflect this (input fields, init values, etc)
   * Also perform any algorithm initialization
   */
  public void select(ShellApplet sa);

  /** Apply the algorithm
   * Gather and check parameters from the ShellApplet
   */
  public void apply(ShellApplet sa);
}


interface AutoAlgorithm
    extends ShellAlgorithm
{
  // Returns a ShellAlgorithm that implements an automated version
  public ShellAlgorithm getAutoAlgorithm();

  // Apply the method to a sphere with a specific tStep
  public double apply(Shell shell, double tStep);
}