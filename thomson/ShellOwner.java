/*
 * An interface which defines methods that a shell's handler should provide.
 *
 * Cris Cecka
 */

interface ShellOwner
{
  public Shell getShell();
  public void updateShellSize();
  public void drawShell();
  public CapFrame defectIsSelected(LatticeDisk defectCap);
}