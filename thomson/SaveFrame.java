/*
 * A simple class to provide a popup frame which can save a shell.
 *
 * Cris Cecka
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class SaveFrame extends JFrame
  implements ActionListener
{
  JTextField nameField;
  JTextArea commentField;
  JScrollPane commentPane;
  JButton saveButton, cancelButton;
  JLabel userLabel, commentLabel;
  
  Container content = getContentPane();
  
  ReadWriteFile database;
  ShellOwner so;
  
  static String user;
  static int userID;
  
  boolean saving = false;
  
  SaveFrame(ShellOwner so_, ReadWriteFile database_)
  {
    super("Save Current Shell");
    so = so_;
    database = database_;
  }
  
  public void init()
  {
    content.setLayout(new BorderLayout());
    
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel inputPanel = new JPanel(gridbag);
    
    userLabel = new JLabel("User: ", JLabel.RIGHT);
    nameField = new JTextField(user);
    nameField.setColumns(20);
    nameField.setEnabled(false);
    
    commentLabel = new JLabel("Comment: ");
    commentField = new JTextArea();
    commentPane = new JScrollPane(commentField,
				  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    commentField.setFont(ShellApplet.OUTPUT_FONT);
    commentField.setEditable(true);
    commentField.setBackground(ShellApplet.backgroundColor);
    commentField.setColumns(25);
    commentField.setRows(3);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(saveButton = new JButton("Save"));
    saveButton.addActionListener(this);
    buttonPanel.add(cancelButton = new JButton("Cancel"));
    cancelButton.addActionListener(this);
    
    c.ipady = 5;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(userLabel, c);
    inputPanel.add(userLabel);
    
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(nameField, c);
    inputPanel.add(nameField);
    
    c.gridwidth = 1;
    gridbag.setConstraints(commentLabel, c);
    inputPanel.add(commentLabel);
    
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 2;
    gridbag.setConstraints(commentPane, c);
    inputPanel.add(commentPane);
    
    content.add(inputPanel, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.SOUTH);
    
    setSize(355,200);
    setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();
    
    if( source == saveButton ) {
      user = nameField.getText().trim();
      saving = true;
      commentField.setEnabled(false);
      commentPane.setEnabled(false);
      saveButton.setEnabled(false);
      cancelButton.setEnabled(false);
      userLabel.setEnabled(false);
      commentLabel.setEnabled(false);
      this.setEnabled(false);
      repaint();
    } else if( source == cancelButton ) {
      dispose();
    }
  }
  
  public void paint(Graphics g)
  {
    super.paint(g);
    if( saving ) {
      g.setFont(ShellApplet.TEXT_FONT);
      g.drawString("Saving.....", getWidth()/3, getHeight()/2);
      Shell shell = so.getShell();
      
      if( database.writeShell(shell, userID, commentField.getText().trim()) )
	Const.out.println("Saved " + user + "'s " + shell.numPoints()
			  + "-" + shell.getPotential() 
			  + "." + shell.getClass().getName().toLowerCase());
      
      dispose();
    }
  }
}
