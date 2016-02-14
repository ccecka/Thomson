/* ShellApplet.java
 *
 * Main driver class for the Thomson Applet.
 *
 * This class controls initialization, control, view, and execution of
 * the Thomson Applet.
 *
 * Cris Cecka
 */

import java.awt.*;             // Java Abstract Windowing Toolkit
import java.awt.event.*;       // Java AWT Events and Handlers
import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.applet.*;          // Java Applet related classes
import java.text.*;            // Java text and decimal formatting

public class ShellApplet extends JApplet
    implements ActionListener, ItemListener, ShellOwner
{
  public final static String ABOUT = "Thomson Applet\n - criscecka@gmail.com\n";
  public final static Color backgroundColor = Color.white;

  // Accessor methods to interface with the database
  ReadWriteFile database;

  // The shell's display panel
  ShellPanel shellPanel;

  // The current shell
  Shell shell;

  // Content pane for java.swing layout managers
  Container content = getContentPane();

  // Fonts used in the applet
  final static Font TEXT_FONT = new Font("Helvetica", Font.BOLD, 16);
  final static Font ERROR_FONT = new Font("TimesRoman", Font.BOLD, 13);
  final static Font OUTPUT_FONT = new Font("Monospace", Font.PLAIN, 11);
  final static Font SMALL_FONT = new Font("Helvetica", Font.BOLD, 10);

  // Thread used to execute algorithms and dispatch drawing events
  Algorithm animation;

  // Choice menus for algorithms and topologies
  JComboBox algChoice;

  // Minimum/Maximum number of points allowed
  final static int NMIN = 3;
  final static int NMAX = 20000;

  // Global constants for topology choices
  final static int SPHERE = 0, TORUS = 1;

  // Topology Options (correlate with global constants)
  final static String[] topChoiceString = {"   Sphere   ", "   Torus   "};

  // Number of Particles, Potential, and Torus Radius Ratio spinners
  SpinnerControl nSpinner, potSpinner, ratioSpinner;

  // Panel to hold the ratioSpinner so we can make it invisible when not in use
  JPanel ratioPanel;

  final static ShellAlgorithm ALG_RELAX        = new ShellRelax();
  final static ShellAlgorithm ALG_RELAXBH      = new ShellRelaxBH();
  final static ShellAlgorithm ALG_THERMALRELAX = new SphereThermalRelax();
  final static ShellAlgorithm ALG_LOCALRELAX   = new ShellLocalRelax();
  final static ShellAlgorithm ALG_LOCALMC      = new SphereKrauth();
  final static ShellAlgorithm ALG_MCANNEAL     = new ShellLocalMC();
  final static ShellAlgorithm ALG_LATTICE      = new ShellSpring();
  final static ShellAlgorithm ALG_GENETIC      = new SphereGenetic();
  final static ShellAlgorithm ALG_MNCONSTRUCT  = new SphereIco();
  final static ShellAlgorithm ALG_CAPCONSTRUCT = new SphereCaps();
  final static ShellAlgorithm ALG_RANDOM       = new ShellRandom();
  final static ShellAlgorithm ALG_SPIRAL       = new ShellSpiral();

  final static ShellAlgorithm[] algChoiceList = {ALG_RELAX,
                                                 //ALG_RELAXBH,
                                                 ALG_THERMALRELAX,
                                                 ALG_LOCALRELAX,
                                                 ALG_LOCALMC,
                                                 ALG_MCANNEAL,
                                                 ALG_LATTICE,
                                                 ALG_GENETIC,
                                                 ALG_MNCONSTRUCT,
                                                 ALG_CAPCONSTRUCT,
                                                 ALG_RANDOM,
                                                 ALG_SPIRAL};

  // The buttons whose state is dependent on the applet's state
  JButton startButton, autoButton;

  // The algorithm options/parameters and their names
  JSpinner[] optionsSpinner = new JSpinner[2];
  JLabel[] optionsLabel = new JLabel[2];

  // The display checkboxes
  JCheckBox draw3DCheckBox, latECheckBox, meshCheckBox,
    eColorCheckBox, backgroundCheckBox, indexCheckBox, dualCheckBox;

  // The menu items - including File and Options menus
  JMenuItem outPointsMenuItem, outAdjArrayMenuItem, outElemListMenuItem,
    exitMenuItem, addMidPointsMenuItem, addFaceCentersMenuItem, saveMenuItem,
    randomInitMenuItem, spiralInitMenuItem,
    sphereTopoMenuItem, torusTopoMenuItem;

  public void init()
  {
    System.out.println("Thomson Applet Initializing...");
    System.out.println(ABOUT);

    // Set the L&F managers of the Applet
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      System.out.println("Error setting native L&F: " + e);
    }

    // Begin Layout
    content.setBackground(backgroundColor);
    content.setLayout(new BorderLayout());

    // Begin Layout of Main Panel
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    // The entire panel containing all input objects
    JPanel inputsContainer = new JPanel(gridbag);

    // The number of points panel
    JPanel numP = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JSpinner inputNumPoints = new JSpinner(new SpinnerNumberModel(378, NMIN,
                                                                  NMAX, 1));
    JButton newButton = new JButton("New");
    numP.add(new JLabel("N = ", JLabel.RIGHT));
    numP.add(inputNumPoints);
    numP.add(newButton);
    nSpinner = new SpinnerControl(inputNumPoints);
    inputNumPoints.addChangeListener(nSpinner);
    newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          resetAnim();
        }
      });

    // The energy potential panel
    JPanel potentialPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel potentialPanel2 = new JPanel(new GridLayout(1,3));
    JSpinner potentialSpinner = new JSpinner(new SpinnerNumberModel(1,0,12,1));
    potentialPanel2.add(new JLabel("Potential: "));
    potentialPanel2.add(new JLabel("1/|r|^", JLabel.RIGHT));
    potentialPanel2.add(potentialSpinner);
    potentialPanel.add(potentialPanel2);
    potSpinner = new SpinnerControl(potentialSpinner);
    potentialSpinner.addChangeListener(potSpinner);

    // The torus radius ratio panel
    ratioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel ratioPanel2 = new JPanel(new GridLayout(1,2));
    JSpinner torusRatioSpinner = new JSpinner(new SpinnerNumberModel(1.414,1.05,10000.0,.01));
    ratioPanel2.add(new JLabel("R/r = ", JLabel.RIGHT));
    ratioPanel2.add(torusRatioSpinner);
    ratioPanel.add(ratioPanel2);
    ratioSpinner = new SpinnerControl(torusRatioSpinner);
    torusRatioSpinner.addChangeListener(ratioSpinner);
    ratioPanel.setVisible(false);

    // The algorithm choice panel
    JPanel algPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    //algChoice = new JComboBox(algChoiceString);
    algChoice = new JComboBox(algChoiceList);
    algPanel.add(new JLabel("Algorithm: ", JLabel.RIGHT));
    algPanel.add(algChoice);
    algChoice.addItemListener(this);

    // The start and reset button panel
    JPanel buttonsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel buttonPanel = new JPanel(new GridLayout(1,2));
    startButton = new JButton("Start");
    JButton resetButton = new JButton("Reset");
    buttonPanel.add(startButton);
    buttonPanel.add(resetButton);
    buttonsContainer.add(buttonPanel);
    resetButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          resetAnim();
        }
      });
    startButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if( ((JButton)e.getSource()).getText().equals("Start") ) {
            startAnim( (ShellAlgorithm) algChoice.getSelectedItem() );
          } else {
            pauseAnim();
          }
        }
      });

    // The algorithm-dependent options/parameters panel
    JPanel optionsContainer = new JPanel(new FlowLayout());
    JPanel optionsPanel = new JPanel(new GridLayout(2, 2));
    for( int i = 0; i < optionsLabel.length; ++i ) {
      optionsLabel[i] = new JLabel("", SwingConstants.RIGHT);
      optionsPanel.add(optionsLabel[i]);

      optionsSpinner[i] = new JSpinner();
      optionsPanel.add(optionsSpinner[i]);
    }
    optionsContainer.add(optionsPanel);

    // The special AutoButton in the Relax Algorithm
    JPanel autoButtonContainer = new JPanel(new FlowLayout());
    autoButton = new JButton("Auto");
    autoButtonContainer.add(autoButton);
    optionsContainer.add(autoButtonContainer);
    autoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JButton thisButton = (JButton) e.getSource();
          if( thisButton.getText().equals("Auto") ) {
            pauseAnim();
            ShellAlgorithm alg = ((AutoAlgorithm)algChoice.getSelectedItem()).getAutoAlgorithm();
            alg.select( ShellApplet.this );
            startAnim( alg );
            thisButton.setText("Halt");
          } else if( thisButton.getText().equals("Halt") ) {
            pauseAnim();
            startAnim( (ShellAlgorithm) algChoice.getSelectedItem() );
            thisButton.setText("Auto");
          }
        }
      });

    // The text output pane
    final JTextArea textOutput = new JTextArea("", 10, 15);
    final JScrollPane textPane = new JScrollPane(textOutput,
                                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textOutput.setFont(OUTPUT_FONT);
    textOutput.append(ABOUT);
    textOutput.setEditable(false);
    textOutput.setBackground(backgroundColor);

    // Override the output scheme to print public outputs to this text-area
    Const.out = new OutputWriter() {
        public void print(String s) {
          textOutput.append(s);
          textOutput.setCaretPosition(textOutput.getText().length());
        }
      };

    // The database inspired low energy buttons
    JPanel lowEPanel = new JPanel(new GridLayout(1, 2));
    JButton lowEnergyButton = new JButton("Lowest Energy");
    JButton loadLowestButton = new JButton("Load Lowest");
    lowEPanel.add(lowEnergyButton);
    lowEPanel.add(loadLowestButton);
    lowEnergyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          double energy = database.lowestEnergySeen(shell);
          if( energy == Double.POSITIVE_INFINITY )
            Const.out.println("No Data Available For N=" + shell.numPoints());
          else {
            if( algChoice.getSelectedItem() == ALG_GENETIC && animation != null )
              Const.out.println();
            Const.out.println("Lowest Energy N=" + shell.numPoints() +
                              ":\n   " + Const.format(energy,14));
          }
        }
      });
    loadLowestButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pauseAnim();
          database.loadLowest(shell);
          drawShell();
        }
      });

    // The system energy button
    JButton energyButton = new JButton("Energy");
    energyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          synchronized( shell ) {    // Lock the shell
            double energy = shell.totalEnergy();
            if( database.isEnergyNewLowest(energy, shell) ) {
              Const.out.println("New Lowest N=" + shell.numPoints() +
                                ":\n   " + Const.format(energy,14));
              database.writeGlobalMin(shell, energy);
            } else {
              Const.out.println("Energy of  N=" + shell.numPoints() +
                                ":\n   " + Const.format(energy,14));
            }
          } // end shell lock
        }
      });

    // The display options
    JPanel displayPanel    = new JPanel(new GridLayout(3,3, 2, 0));
    JPanel panel3d         = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel panelindex      = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel panelmesh       = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel panelecolor     = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel panel2d         = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel panelbackground = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    JPanel paneldual       = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
    panel3d.add(         draw3DCheckBox     = getNewCheckBox("3D",    true ) );
    panelindex.add(      indexCheckBox      = getNewCheckBox("Index", false) );
    panelmesh.add(       meshCheckBox       = getNewCheckBox("Mesh",  true ) );
    panelecolor.add(     eColorCheckBox     = getNewCheckBox("Pot E", false) );
    panel2d.add(         latECheckBox       = getNewCheckBox("Lat E", false) );
    panelbackground.add( backgroundCheckBox = getNewCheckBox("Chop",  false) );
    paneldual.add(       dualCheckBox       = getNewCheckBox("Dual",  false) );
    displayPanel.add(panel3d);
    displayPanel.add(panelindex);
    displayPanel.add(panel2d);
    displayPanel.add(panelmesh);
    displayPanel.add(panelbackground);
    displayPanel.add(panelecolor);
    displayPanel.add(paneldual);


    // Top Menu Bar
    JMenuBar menuBar = new JMenuBar();

    // File Menu
    JMenu fileMenu = new JMenu("File");
    fileMenu.add(saveMenuItem = getNewMenuItem("Save System"));
    fileMenu.add(outPointsMenuItem = getNewMenuItem("Point Set"));
    fileMenu.add(outAdjArrayMenuItem = getNewMenuItem("Adjacency Array"));
    fileMenu.add(outElemListMenuItem = getNewMenuItem("Triangle List"));
    menuBar.add(fileMenu);

    // Options Menu
    JMenu optionsMenu = new JMenu("Options");

    JMenu topoMenu = new JMenu("Topology");
    ButtonGroup topoGroup = new ButtonGroup();
    topoMenu.add(sphereTopoMenuItem = new JRadioButtonMenuItem("Sphere",true));
    topoGroup.add(sphereTopoMenuItem);
    topoMenu.add(torusTopoMenuItem = new JRadioButtonMenuItem("Torus",false));
    topoGroup.add(torusTopoMenuItem);
    // TODO: Torus Stuff
    // Look up listeners on button groups!!
    sphereTopoMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if( sphereTopoMenuItem.isSelected() ){
            algChoice.removeAllItems();
            for( int i = 0; i < algChoiceList.length; ++i )
              algChoice.addItem(algChoiceList[i]);
            ratioPanel.setVisible(false);
            if( !(shell instanceof Sphere) ) {
              initNewShell();
              drawShell();
            }
          }
        }
      });
    torusTopoMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if( torusTopoMenuItem.isSelected() ) {
            algChoice.removeAllItems();
            algChoice.addItem(ALG_RELAX);
            algChoice.addItem(ALG_RELAXBH);
            algChoice.addItem(ALG_LOCALRELAX);
            algChoice.addItem(ALG_MCANNEAL);
            //algChoice.addItem(ALG_LATTICE);
            algChoice.addItem(ALG_RANDOM);
            algChoice.addItem(ALG_SPIRAL);
            ratioPanel.setVisible(true);
            if( !(shell instanceof Torus) ) {
              initNewShell();
              drawShell();
            }
          }
        }
      });

    JMenu initMenu = new JMenu("Initialize");
    ButtonGroup initGroup = new ButtonGroup();
    initMenu.add(randomInitMenuItem = new JRadioButtonMenuItem("Random",true));
    initGroup.add(randomInitMenuItem);
    initMenu.add(spiralInitMenuItem = new JRadioButtonMenuItem("Spiral",false));
    initGroup.add(spiralInitMenuItem);

    optionsMenu.add(topoMenu);
    optionsMenu.add(initMenu);
    optionsMenu.addSeparator();
    optionsMenu.add(addMidPointsMenuItem = new JMenuItem("Add MidPoints"));
    addMidPointsMenuItem.addActionListener(this);
    optionsMenu.add(addFaceCentersMenuItem = new JMenuItem("Add Face Centers"));
    addFaceCentersMenuItem.addActionListener(this);
    menuBar.add(optionsMenu);

    // End Top Menu



    // Do initial algorithm setup
    itemStateChanged(null);

    // Set the constraints for all the panels
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    //gridbag.setConstraints(topPanel, c);
    gridbag.setConstraints(numP, c);
    gridbag.setConstraints(potentialPanel, c);
    gridbag.setConstraints(ratioPanel, c);
    gridbag.setConstraints(buttonsContainer, c);
    gridbag.setConstraints(algPanel, c);
    gridbag.setConstraints(optionsContainer, c);
    gridbag.setConstraints(textPane, c);
    gridbag.setConstraints(energyButton, c);
    gridbag.setConstraints(lowEPanel, c);
    gridbag.setConstraints(displayPanel, c);

    // Add all the panels to the main panel
    //inputsContainer.add(topPanel);
    inputsContainer.add(numP);
    inputsContainer.add(potentialPanel);
    inputsContainer.add(ratioPanel);
    inputsContainer.add(algPanel);
    inputsContainer.add(optionsContainer);
    inputsContainer.add(buttonsContainer);
    inputsContainer.add(textPane);
    inputsContainer.add(energyButton);
    inputsContainer.add(lowEPanel);
    inputsContainer.add(displayPanel);


    // Add the menu bar
    content.add(menuBar, BorderLayout.NORTH);

    // Add the main panel to the applet
    content.add(inputsContainer, BorderLayout.EAST);

    // Add the shell panel to the applet
    shellPanel = new ShellPanel(this);
    content.add(shellPanel, BorderLayout.CENTER);

    this.validate();
  }

  private JMenuItem getNewMenuItem(String s)
  {
    JMenuItem menuitem = new JMenuItem(s);
    menuitem.addActionListener(this);
    return menuitem;
  }
  private JCheckBoxMenuItem getNewCheckItem(String s)
  {
    JCheckBoxMenuItem checkboxmenuitem = new JCheckBoxMenuItem(s);
    checkboxmenuitem.addItemListener(this);
    return checkboxmenuitem;
  }
  private JCheckBox getNewCheckBox(String s, boolean b)
  {
    JCheckBox checkbox = new JCheckBox(s,b);
    checkbox.addActionListener(this);
    return checkbox;
  }

  // Called when a button or checkbox is pressed
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();

    if( algChoice.getSelectedItem() == ALG_GENETIC && animation != null )
      Const.out.println("Can't Complete, Genetic Alg is Running");

    if( source == draw3DCheckBox ) {
      boolean active = draw3DCheckBox.isSelected();
      meshCheckBox.setEnabled(active);
      eColorCheckBox.setEnabled(active);
      backgroundCheckBox.setEnabled(active);
      indexCheckBox.setEnabled(active);
      latECheckBox.setEnabled(active);
      dualCheckBox.setEnabled(active);
      if( active )
        drawShell();
      else
        shellPanel.draw(null);
    } else if( source == backgroundCheckBox ) {
      if( shellPanel.m3 != null )
        shellPanel.m3.setShowNegZ(!backgroundCheckBox.isSelected());
      shellPanel.repaint();
    } else if( source == indexCheckBox ) {
      if( shellPanel.m3 != null )
        shellPanel.m3.setShowIndex(indexCheckBox.isSelected());
      shellPanel.repaint();
    } else if( source == eColorCheckBox || source == latECheckBox ){
      boolean latE = latECheckBox.isSelected();
      boolean eCol = eColorCheckBox.isSelected();
      boolean gouraud = (latE || eCol) && meshCheckBox.isSelected();
      indexCheckBox.setEnabled(!gouraud);
      backgroundCheckBox.setEnabled(!gouraud);
      if( latE && source == latECheckBox )
        eColorCheckBox.setSelected(false);
      if( eCol && source == eColorCheckBox )
        latECheckBox.setSelected(false);
      drawShell();
    } else if( source == meshCheckBox ) {
      boolean latE = latECheckBox.isSelected();
      boolean eCol = eColorCheckBox.isSelected();
      boolean gouraud = (latE || eCol) && meshCheckBox.isSelected();
      indexCheckBox.setEnabled(!gouraud);
      backgroundCheckBox.setEnabled(!gouraud);
      if( meshCheckBox.isSelected() && dualCheckBox.isSelected() )
        dualCheckBox.doClick();
      else
        drawShell();
    } else if( source == dualCheckBox ) {
      boolean selected = dualCheckBox.isSelected();
      eColorCheckBox.setEnabled(!selected);
      latECheckBox.setEnabled(!selected);
      boolean gouraud = (latECheckBox.isSelected() || eColorCheckBox.isSelected()) && meshCheckBox.isSelected();
      backgroundCheckBox.setEnabled(selected || !gouraud);
      indexCheckBox.setEnabled(selected || !gouraud);
      if( dualCheckBox.isSelected() && meshCheckBox.isSelected() )
        meshCheckBox.doClick();
      else
        drawShell();
    } else if( source == addFaceCentersMenuItem ) {
      shell.addAtomsAtAllCenters();
      updateShellSize();
      drawShell();
      Const.out.println("Particles Added at Face Centers");
    } else if( source == addMidPointsMenuItem ) {
      shell.addAtomsAtAllMidpoints();
      updateShellSize();
      drawShell();
      Const.out.println("Particles Added at Edge Midpoints");
    } else if( source == outAdjArrayMenuItem ) {
      (new InfoFrame(this, InfoFrame.Type.ADJARRAY)).init();
    } else if( source == outElemListMenuItem ) {
      (new InfoFrame(this, InfoFrame.Type.ELEMLIST)).init();
    } else if( source == outPointsMenuItem ) {
      (new InfoFrame(this, InfoFrame.Type.POINTSET)).init();
    } else if( source == saveMenuItem ) {
      SaveFrame saveWindow = new SaveFrame(this, database);
      saveWindow.setLocationRelativeTo(this);
      saveWindow.init();
    } else {
      shellPanel.repaint();
    }
  }

  public class SpinnerControl implements ChangeListener
  {
    Object oldValue;
    JSpinner spinner;

    SpinnerControl(JSpinner s) {
      spinner = s;
      oldValue = s.getValue();
    }

    public void stateChanged(ChangeEvent e) {
      if( oldValue.equals(spinner.getValue()) )
        return;

      if( this == nSpinner ) {
        if( oldValue.equals(spinner.getPreviousValue()) ) {
          shell.addRandomAtom();
          shellPanel.transformer.newSize(shell.numPoints());
          Const.out.println("Added Random Particle");
        } else if( oldValue.equals(spinner.getNextValue()) ) {
          shell.removeRandomAtom();
          shellPanel.transformer.newSize(shell.numPoints());
          Const.out.println("Removed Random Particle");
        } else {
          pauseAnim();
          initNewShell();
        }
        oldValue = spinner.getValue();
        drawShell();
      } else if( this == potSpinner ) {
        shell.setPotential( Const.potentialList[ getIntValue() ] );
        oldValue = spinner.getValue();
        if( eColorCheckBox.isSelected() )
          drawShell();
      } else if( this == ratioSpinner ) {
        ((Torus)shell).setRatio( getDoubleValue() );
        oldValue = spinner.getValue();
        drawShell();
      }
    }

    public void setValueWithInit(int value) {
      spinner.setValue(new Integer(value));
    }

    public void updateValue(int value) {
      oldValue = new Integer(value);
      spinner.setValue(oldValue);
    }

    public void updateValue(double value) {
      oldValue = new Double(value);
      spinner.setValue(oldValue);
    }

    public int getIntValue() {
      return ((Number)spinner.getValue()).intValue();
    }

    public double getDoubleValue() {
      return ((Number)spinner.getValue()).doubleValue();
    }
  }

  // Called when the algorithm pulldown menu changes
  public void itemStateChanged(ItemEvent e)
  {
    if( e == null || e.getSource() == algChoice ) {

      pauseAnim();
      autoButton.setVisible(false);         // The Default Settings
      optionsLabel[0].setVisible(true);
      optionsSpinner[0].setVisible(true);
      optionsLabel[1].setVisible(false);
      optionsSpinner[1].setVisible(false);

      ShellAlgorithm alg = (ShellAlgorithm) algChoice.getSelectedItem();
      if( alg != null )
        alg.select(this);
    }
  }

  public void updateShellSize()
  {
    nSpinner.updateValue(shell.numPoints());
  }

  public CapFrame defectIsSelected(LatticeDisk defectCap)
  {
    if( defectCap.isValid() ) {
      CapFrame result = new CapFrame(this, defectCap);
      result.init();
      return result;
    }
    return null;
  }

  public void start()
  {
    this.validate();

    // Attempt to connect to and open database
    database = null; //new ReadWriteFile();

    // Initialize the Graphics panel
    shellPanel.init();

    try {
      String file = getParameter("LoadData");
      String capList = getParameter("caps");

      // The web page's LoadData parameter determines if and what
      // the applet should initialize with
      if( file != null ) {
        try {
          // Attempt to load the specified system
          int seperator = file.indexOf('.');
          int id = Integer.parseInt(file.substring(0,seperator));
          String db = file.substring(seperator+1, file.length());
          shell = database.readShell(id, db);

          // Configure the interface to reflect the system
          if( shell instanceof Sphere ) {
            //topChoice.setSelectedIndex(SPHERE);
            sphereTopoMenuItem.setSelected(true);
          } else if( shell instanceof Torus ) {
            //topChoice.setSelectedIndex(TORUS);
            torusTopoMenuItem.setSelected(true);
            ratioSpinner.updateValue(((Torus)shell).getRatio());
          }
          nSpinner.updateValue(shell.numPoints());
          potSpinner.updateValue(shell.getPotential());
          shellPanel.newShellCreated(shell);

        } catch( Exception e ) {
          // Loading the data failed, bail out and make new system
          Const.out.println("Loading " + file + " Failed");
          initNewShell();
        }

        // The web page's capList parameter determines if the initial
        // system should be constructed from known caps
      } else if( capList != null ) {
        // Attempt to construct the specified system
        CapPalette palette = new CapPalette( capList );
        shell = palette.toSphere();

        // Configure the interface to reflect the system
        sphereTopoMenuItem.setSelected(true);
        nSpinner.updateValue(shell.numPoints());
        potSpinner.updateValue(shell.getPotential());
        // Select Lattice Alg to stabilize construction
        algChoice.setSelectedItem( ALG_LATTICE );
        shellPanel.newShellCreated(shell);

      } else {
        // No system was specified, create a new system
        initNewShell();
      }
    } catch( NullPointerException npe ) {
      // Something went wrong, bail out and make a new system
      initNewShell();
    }

    // Attempt to load user information
    try {
      SaveFrame.user = getParameter("userName");
      SaveFrame.userID = Integer.parseInt( getParameter("userID") );
    } catch( Exception e ) {}

    drawShell();
  }

  public void stop()
  {
    if( animation != null ) {
      animation.interrupt();
      animation = null;
    }
  }

  public static void main(String args[])
  {
    JFrame f = new JFrame("Thomson Problem");
    ShellApplet shellapp = new ShellApplet();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(shellapp);
    f.setSize(900, 700);
    f.setVisible(true);
    shellapp.init();
    shellapp.start();
  }

  // Needed?
  public void update(Graphics g)
  {
    paint(g);
    shellPanel.repaint();
  }

  long lastDraw = 0;

  /** Creates a model of the system which can be passed to the graphic panel
   * for display.
   */
  public void drawShell()
  {
    // To save computation power, only render at most once every 40ms
    long currentTime = System.currentTimeMillis();
    if( currentTime - lastDraw < 40 || !draw3DCheckBox.isSelected() )
      return;
    else
      lastDraw = currentTime;

    // Declare model of the current system
    Model3DSphere m3 = null;

    // Depending on which graphics options are selected, ask the shell
    // for its model
    if( dualCheckBox.isSelected() ) {
      m3 = shell.getModel3DDual();
    } else {
      switch( (meshCheckBox.isSelected() ? 1 : 0)
              + (eColorCheckBox.isSelected() ? 2 : 0)
              + (latECheckBox.isSelected() ? 4 : 0) ) {
        // Plain
        case 0:  m3 = shell.getModel3D();           break;
          // Delaunay Meshing
        case 1:  m3 = shell.getModel3DWithMesh();   break;
          // Discrete Partial Energies
        case 2:  m3 = shell.getModel3DEColors();    break;
          // Continuous Partial Energies
        case 3:  m3 = shell.getModel3DEGouraud();   break;
          // Discrete Lattice Energies
        case 4:  m3 = shell.getModel3DLatColors();  break;
          // Continuous Lattice Energies
        case 5:  m3 = shell.getModel3DLatGouraud(); break;
      }
    }

    // Paint only the foreground of the system?
    m3.showBackground = !backgroundCheckBox.isSelected();
    // Paint the indices of the particles?
    m3.showIndex = indexCheckBox.isSelected();

    // Let the graphics panel draw the model
    shellPanel.draw(m3);
  }

  /** Shell Accessor method
   */
  public Shell getShell()
  {
    return shell;
  }

  /** Creates a new shell object. A shell may be sherical or toriodal and may
   * be created randomly or with a spiral algorithm.
   */
  public void initNewShell()
  {
    // Determine which topology to create
    if( sphereTopoMenuItem.isSelected() ) {
      shell = new Sphere();
    } else if( torusTopoMenuItem.isSelected() ) {
      shell = new Torus();
    }

    // Determine initialize randomly or use a spiral algorithm
    if( randomInitMenuItem.isSelected() ) {
      shell.randomPoints(nSpinner.getIntValue());
    } else {
      shell.spiralPoints(nSpinner.getIntValue());
    }

    // Make sure a torus knows its radial ratio
    if( shell instanceof Torus )
      ((Torus)shell).setRatio(ratioSpinner.getDoubleValue());

    // Let the shell know what potential we're working in
    shell.setPotential( Const.potentialList[ potSpinner.getIntValue() ] );

    // Let the graphics panel know we have a new shell
    shellPanel.newShellCreated(shell);

    // Reset the draw interval
    lastDraw = 0;
  }

  public void startAnim(ShellAlgorithm alg)
  {
    animation = new Algorithm(alg);
    animation.start();
    startButton.setText("Pause");
  }

  public void pauseAnim()
  {
    if( animation != null ) {
      animation.interrupt();
      animation = null;
    }

    startButton.setText("Start");
    autoButton.setText("Auto");
  }

  public void resetAnim()
  {
    if( animation != null ) {
      animation.interrupt();
      animation = null;
    }

    startButton.setText("Start");
    autoButton.setText("Auto");
    initNewShell();
    lastDraw = 0;
    drawShell();
  }

  class Algorithm extends Thread
  {
    ShellAlgorithm alg;
    boolean isActive = true;

    Algorithm(ShellAlgorithm alg_)
    {
      alg = alg_;
    }

    public void run()
    {
      while(isActive && !Thread.currentThread().isInterrupted() ) {
        alg.apply(ShellApplet.this);
        drawShell();
      }
    }

    public void interrupt()
    {
      isActive = false;
      super.interrupt();
    }
  }
}
