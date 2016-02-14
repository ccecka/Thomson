/*
 * The main graphics class of the ShellApplet.
 *
 * This class takes a graphics Model of the shell and paints it.
 * All mouseevents, view changes, and graphic events are handled here.
 *
 * Cris Cecka
 */


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Used for saving images
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.text.*;


class ShellPanel extends JPanel
	implements MouseListener, MouseMotionListener
{
	// The class reponsible for transforming the point set to the appropriate view.
	Transformer3D transformer;

	// The shell's owner so we can report changes made by the user
	ShellOwner shellOwner;

	Image offscreenImage;
	Graphics offscreenGraphics;
	int halfWidth, halfHeight;

	Model3DSphere m3;
	int prevx, prevy;
	float graphRadius;

	int draggedIndex = -1;
	IntList highlightedPoints;

	ShellPanel(ShellOwner shellOwner_)
	{
		shellOwner = shellOwner_;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	// Intialization
	public void init()
	{
		this.setBackground(ShellApplet.backgroundColor);
		offscreenImage = createImage(getWidth(), getHeight());
		offscreenGraphics = offscreenImage.getGraphics();
		halfWidth = getWidth() / 2;
		halfHeight = getHeight() / 2;
		offscreenGraphics.translate(halfWidth, halfHeight);
		graphRadius = 0.9f * halfHeight;

		transformer = new Transformer3D(1);
	}

	// A new shell has been created in Shell applet. Be ready to handle it.
	public void newShellCreated(Shell shell)
	{
		transformer.newSize(shell.numPoints());
		transformer.amat.unit();
		transformer.amat.scale((float)(graphRadius/shell.getShellRadius()));

		highlightedPoints = null;
	}

	// Draw a model to the screen.
	// This method preprocesses a model and then calls paint() to paint it.
	synchronized public void draw(Model3DSphere model3)
	{
		// Remove the cap frame if it becomes invalid
		// A capFrame is invalid when the mesh the cap was formed over is replaced
		if( capFrame != null && !capFrame.isValid() ) {
			highlightedPoints.makeEmpty();
			capFrame.dispose();
			capFrame = null;
		}

		// Replace the old model
		m3 = model3;

		if( m3 != null ) {
			m3.setTransformer(transformer);
		}

		// Tell Java to call the paint(Graphics) method
		repaint();
	}

	volatile long lastDraw = Long.MIN_VALUE;

	// Low priority repaint()
	// If the image has been repainted recently, this does nothing
	public void maybeRepaint()
	{
		long currentTime = System.currentTimeMillis();
		//System.out.println( currentTime - lastDraw );
		if( currentTime - lastDraw < 15 )
			return;
		else
			repaint();
	}

	public void update(Graphics g)
	{
		paint(g);
	}


	/*
	// More for saving frames
	int frameNumber = 0;
	NumberFormat formatter = new DecimalFormat("000000000");
	*/

	// Paints everything to the offscreenImage, then draws it to the screen.
	synchronized public void paint(Graphics g)
	{
		lastDraw = System.currentTimeMillis();

		if( m3 != null ) {
			m3.graphRadius = graphRadius;
			if( m3.transform() ) {
				offscreenGraphics.clearRect(-halfWidth, -halfHeight, getWidth(), getHeight());
				if( highlightedPoints != null )
					m3.highlight(highlightedPoints, offscreenGraphics);
				m3.paint(offscreenGraphics);
				g.drawImage(offscreenImage, 0, 0, this);
			}
		} else {
			if( offscreenGraphics != null ) {
				offscreenGraphics.clearRect(-halfWidth,-halfHeight,getWidth(),getHeight());
			}
			g.drawImage(offscreenImage, 0, 0, this);
		}


		// Used to save each frame
		/*
		try {
			BufferedImage theImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			theImage.createGraphics().drawImage(offscreenImage, 0, 0, this);
			ImageIO.write(theImage, "png", new File("frame" + formatter.format(frameNumber++) + ".png"));
		} catch(Exception e) {
			System.out.println(e);
		}
		*/
	}


	// ================ Mouse Event Methods ====================== //

	int buttonPressed;

	public void mousePressed(MouseEvent e)
	{
		if( m3 == null )
			return;

		buttonPressed = e.getButton();
		prevx = e.getX();
		prevy = e.getY();

		// If Shift and Cntrl are down, we are trying to drag a point
		if( e.isShiftDown() && e.isControlDown() ) {
			e.translatePoint(-halfWidth, -halfHeight);
			draggedIndex = m3.getPointIndexAt(e.getX(), e.getY());
		}
	}

	public void updateTransform(int newx, int newy)
	{
		if( newx == prevx && newy == prevy )
			return;

		switch( buttonPressed ) {
			// Rotate the Model
		case MouseEvent.BUTTON1:
			transformer.amat.xrot((newy - prevy) * 3.14159265f / halfWidth);
			transformer.amat.yrot((newx - prevx) * 3.14159265f / halfHeight);  
			break;
			// Zoom the Model
		case MouseEvent.BUTTON3:
			float dmzoom = 1.0f + ((float)(newy - prevy)/(float)getHeight());
			graphRadius *= dmzoom;
			transformer.amat.scale(dmzoom);									
			break;
		}
		
		// The transformer needs to update and then we paint to the screen.
		transformer.transformed = false;
		prevx = newx;
		prevy = newy;
	}

	public void mouseDragged(MouseEvent e)
	{
		if( m3 == null )
			return;

		// If this is a clean (no other buttons) mouse drag, then we change the view.
		if( !e.isShiftDown() && !e.isControlDown() ) {

			updateTransform( e.getX(), e.getY() );
			maybeRepaint();
			
			// If we're dragging a point, then we need to change the point's position.
		} else if( draggedIndex != -1 && e.isShiftDown() && e.isControlDown() ) {

			// Ask the model where we're pointing.
			e.translatePoint(-halfWidth, -halfHeight);
			Point point = m3.getNewPointCorrespondingTo(e.getX(), e.getY());
			if( point != null ) {
				m3.shell.normalizeToShell(point);
				m3.shell.setPoint(draggedIndex, point);
				// Need a new model, could have changed the energy, graph, etc.
				shellOwner.drawShell();
			}

		}
	}

	public void mouseReleased(MouseEvent e)
	{
		// Can't be dragging anything anymore
		draggedIndex = -1;

		// Do a last update
		if( !e.isShiftDown() && !e.isControlDown() ) {
			updateTransform( e.getX(), e.getY() );
			repaint();
		}
	}

	CapFrame capFrame;

	public void mouseClicked(MouseEvent e)
	{
		if( m3 == null )
			return;
		else if( e.isControlDown() ) { // Control click - Delete A Point
			e.translatePoint(-halfWidth, -halfHeight);
			int pointIndex = m3.getPointIndexAt(e.getX(), e.getY());

			if( pointIndex != -1 ) {
				m3.shell.removePoint(pointIndex);
				shellOwner.updateShellSize();
				shellOwner.drawShell();
			}
		} else if( e.isShiftDown() ) {  // Shift click - Add A Point
			e.translatePoint(-halfWidth, -halfHeight);
			int x = e.getX();
			int y = e.getY();
			Point point = m3.getNewPointCorrespondingTo(x, y);
			if( point != null ) {
				m3.shell.normalizeToShell(point);
				m3.shell.addPoint(point);
				shellOwner.updateShellSize();
				shellOwner.drawShell();
			}
		} else if( e.getClickCount() == 1 ){  // Single click - Highlight a Point
			e.translatePoint(-halfWidth, -halfHeight);
			int pIndex = m3.getPointIndexAt(e.getX(), e.getY());

			//System.out.println("Selected: " + pIndex);
			if( pIndex != -1 ) {
				highlightedPoints = new IntList(pIndex);
			} else {
				highlightedPoints = null;
			}

			if( capFrame != null ) {
				capFrame.dispose();
				capFrame = null;
			}

			repaint();
		} else if( e.getClickCount() == 2 ) { // Double click - Create a cap from a scar
			if( highlightedPoints != null ) {
				LatticeDisk defectCap = m3.shell.getSurroundingDefect(highlightedPoints.peek());
				highlightedPoints = defectCap.getPointIndexList();
				capFrame = shellOwner.defectIsSelected(defectCap);
			}
			repaint();
		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
