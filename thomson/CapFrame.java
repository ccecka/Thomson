/* Kevin Zielnicki
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class CapFrame extends JFrame
    implements ActionListener
{
  CapOwner cap;
  ShellOwner so;

  Container content = getContentPane();

  JButton rotateCWButton, rotateCCWButton, saveButton;

  ReadWriteFile database = new ReadWriteFile();

  CapFrame(ShellOwner so_, CapOwner cap_)
  {
    super("Cap Editor");
    so = so_;
    cap = cap_;
  }

  public void init()
  {
    content.setLayout(new BorderLayout());

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    JPanel inputsContainer = new JPanel(gridbag);

    int degree = 360/cap.numSides();
    rotateCCWButton = new JButton("Rotate " + degree + (char)0xba);
    rotateCWButton = new JButton("Rotate " + -degree + (char)0xba);
    saveButton = new JButton("Save Cap");
    rotateCCWButton.addActionListener(this);
    rotateCWButton.addActionListener(this);
    saveButton.addActionListener(this);

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(rotateCCWButton, c);
    gridbag.setConstraints(rotateCWButton, c);
    gridbag.setConstraints(saveButton, c);

    inputsContainer.add(rotateCCWButton);
    inputsContainer.add(rotateCWButton);
    inputsContainer.add(saveButton);

    content.add(inputsContainer, BorderLayout.EAST);

    setSize(300,400);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();

    if( source == rotateCCWButton ) {
      cap.rotateCap(-Const.TWOPI/cap.numSides());
      so.drawShell();
    } else if( source == rotateCWButton ) {
      cap.rotateCap(Const.TWOPI/cap.numSides());
      so.drawShell();
    } else if( source == saveButton ) {
      Cap capData = cap.getCap();
      database.writeCap(capData, ReadWriteFile.CAP_OWNER, "cap");
    }
  }

  public boolean isValid()
  {
    return cap.getGraph() == so.getShell().d;
  }
}
