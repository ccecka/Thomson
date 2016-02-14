/*
 * A popup frame which is used to view point set and Adjacency array data.
 * In many cases, the data can be changed and this class reports the changes
 * back to the handler.
 *
 * Cris Cecka
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.swing.text.*;
import java.awt.Toolkit;
import java.awt.datatransfer.*;

class InfoFrame extends JFrame
{
  // Determines whether we hold Point set data or Adjacency array data
  public enum Type { POINTSET, ADJARRAY, ELEMLIST }

  JTextArea textArea;
  NumberedPanel numPanel;

  ShellOwner so;
  Type type;

  // Constructor
  InfoFrame(ShellOwner so_, Type type_)
  {
    super("Data Box");
    so = so_;
    type = type_;
    // Override paint(g) so that linenumbers stay in sync
    textArea = new JTextArea() {
        public void paint(Graphics g) {
          super.paint(g);
          numPanel.repaint();
        }
      };
    textArea.setText(getNewText());
    textArea.setFont(ShellApplet.OUTPUT_FONT);
    textArea.setEditable(true);
    textArea.setBackground(ShellApplet.backgroundColor);
  }

  // Initialization of the Frame. Lays out the buttons and viewing area, etc.
  public void init()
  {
    ActionListener commitAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          commitData();
        }
      };
    ActionListener refreshAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          textArea.setText(getNewText());
        }
      };

    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem exitMenuItem, commitMenuItem, refreshMenuItem;
    fileMenu.add(commitMenuItem = new JMenuItem("Commit to System"));
    fileMenu.add(refreshMenuItem = new JMenuItem("Refresh Data"));
    fileMenu.addSeparator();
    fileMenu.add(exitMenuItem = new JMenuItem("Exit"));
    commitMenuItem.addActionListener(commitAction);
    refreshMenuItem.addActionListener(refreshAction);
    exitMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) { dispose(); } });
    menuBar.add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    JMenuItem selectAllMenuItem, copyMenuItem, pasteMenuItem;
    editMenu.add(copyMenuItem = new JMenuItem("Copy          Ctrl+C"));
    editMenu.add(pasteMenuItem = new JMenuItem("Paste         Ctrl+V"));
    editMenu.addSeparator();
    editMenu.add(selectAllMenuItem = new JMenuItem("Select All    Ctrl+A"));
    menuBar.add(editMenu);
    copyMenuItem.addActionListener(new ActionListener(){
        //public void actionPerformed(ActionEvent e) { textArea.copy(); }
        public void actionPerformed(ActionEvent e) { System.out.println("Copying..."); Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), null);  }
      });
    pasteMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) { textArea.paste(); }
      });
    selectAllMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) { textArea.selectAll(); }
      });

    JScrollPane scrollPane = new JScrollPane(textArea,
                                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton refreshButton, commitButton;
    buttonPanel.add(refreshButton = new JButton("Refresh"));
    buttonPanel.add(commitButton = new JButton("Commit"));
    refreshButton.addActionListener(refreshAction);
    commitButton.addActionListener(commitAction);

    numPanel = new NumberedPanel(textArea, scrollPane);

    Container content = getContentPane();
    content.setLayout(new BorderLayout());
    content.add(numPanel, BorderLayout.WEST);
    content.add(menuBar, BorderLayout.NORTH);
    content.add(scrollPane, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.SOUTH);

    switch( type ) {
      case POINTSET:
        setSize(600,600); break;
      case ADJARRAY:
        setSize(300,600); break;
      case ELEMLIST:
        setSize(300,600); break;
    }
    setVisible(true);
  }

  // Called when the refresh button has been pressed
  // Could also be made public and be called when the applet makes a change.
  private String getNewText()
  {
    switch( type ) {
      case POINTSET:
        return so.getShell().getFormattedAtomString();
      case ADJARRAY:
        return IntList.getFormattedString(so.getShell().getDelaunay().getAdjArray());
      case ELEMLIST:
        TriangleByIndex[] tri = so.getShell().getDelaunay().getFaces();
        StringBuffer result = new StringBuffer();

        if( tri.length > 0 ) {
          result.append(tri[0].getA()).append("\t");
          result.append(tri[0].getB()).append("\t");
          result.append(tri[0].getC());
        }

        for( int i = 1; i < tri.length; ++i ) {
          result.append("\n");
          result.append(tri[i].getA()).append("\t");
          result.append(tri[i].getB()).append("\t");
          result.append(tri[i].getC());
        }

        return result.toString();
      default:
        return null;
    }
  }

  // The data may have been changed. We report back to the handler which
  // attempts to reflect those changes.
  private void commitData()
  {
    switch( type ) {
      case POINTSET:
        try {
          so.getShell().setPoints(Point.getPointsFromString(textArea.getText()));
        } catch ( Exception e ) {
          Const.out.println("Loading Points Failed");
          return;
        }
        so.updateShellSize();
        so.drawShell();
        textArea.setText(getNewText());
        Const.out.println("Point Set Loaded");
        break;
      case ADJARRAY:
        try {
          so.getShell().setAdjArray(IntList.getIntListArrayFromString(textArea.getText()));
        } catch ( Exception e ) {
          Const.out.println("Loading AdjArray Failed");
          return;
        }
        so.drawShell();
        textArea.setText(getNewText());
        Const.out.println("Adj Array Loaded");
        break;
      case ELEMLIST:
        Const.out.println("Element List Change Not Supported");
    }
  }

}



/* A class for a panel which numbers the lines in a scrollpane
 */
class NumberedPanel extends JPanel
{
  JTextArea pane;
  JScrollPane scrollPane;

  public NumberedPanel(JTextArea pane_, JScrollPane scrollPane_)
  {
    super();

    pane = pane_;
    scrollPane = scrollPane_;

    setPreferredSize(new Dimension(35, 30));
    setMinimumSize(new Dimension(35, 30));
    // Override paint(g) so linenumbers stay in sync
    //pane = new JTextArea() {
    //public void paint(Graphics g) {
    //	  super.paint(g);
    //	  LineNr.this.repaint();
    //	}
    //};
    //scrollPane = new JScrollPane(pane);
  }

  public void paint(Graphics g)
  {
    super.paint(g);

    // We need to properly convert the points to match the viewport
    // Read docs for viewport

    // Starting Position in Document
    int start = pane.viewToModel(scrollPane.getViewport().getViewPosition());
    // End Position in Document
    int end = pane.viewToModel(new java.awt.Point(scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                                                  scrollPane.getViewport().getViewPosition().y + pane.getHeight()));
    // end pos in doc

    // translate offsets to lines
    Document doc = pane.getDocument();
    int startline = doc.getDefaultRootElement().getElementIndex(start);
    int endline = doc.getDefaultRootElement().getElementIndex(end);

    int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
    int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
    int starting_y = -1;

    try {
      starting_y = pane.modelToView(start).y -
          scrollPane.getViewport().getViewPosition().y + fontHeight - fontDesc;
    } catch (Exception e1) {
      e1.printStackTrace();
    }

    for(int line = startline, y = starting_y;
        line <= endline;
        y += fontHeight, ++line ) {
      g.drawString(Integer.toString(line), 0, y);
    }

  }
}

