/*
 * A general Gouraud shading painter. Given a list of floats, this class
 * assigns each point a color and paints each face with bilinear interpolation.
 *
 * Cris Cecka
 */


import java.awt.*;
import java.awt.image.*;

class Model3DGouraud extends Model3DSphere
{
	float[] eHueColor;
	Graph d;

	// A list of colors
	// The colors in [.08, .69] define a nice red to light purple spectrum
	final protected static int[] H11toRGBTable = new int[256];
	static {
		int N = H11toRGBTable.length;
		for( int i = 0; i < N; ++i )
			H11toRGBTable[i] = Color.HSBtoRGB((float)(.08f + (.69f * i)/N), 1, 1);
	}

	Model3DGouraud(Shell shell, Graph d_, double[] partE, double mean)
	{
		super(shell);

		d = d_;
		eHueColor = new float[partE.length];

		double stddev = 1.5 * Const.stdDev(partE, mean);

		// Assign colors
		for( int i = partE.length - 1; i >= 0; --i ) {
			int c = (int) ((1 + (mean - partE[i])/stddev) * H11toRGBTable.length/2);

			eHueColor[i] = Const.clamp(c, 0, H11toRGBTable.length - 1);
		}
	}

	// The actual Gouraud painting method
	public void paint(Graphics g)
	{
		TriangleByIndex[] faces = d.getFaces();
		int graphwidth = Const.ceil(graphRadius+1) << 1;
		int graphheight = graphwidth;
		int[] screen_pixels = new int[graphwidth*graphheight];
		float[] zBuffer = new float[screen_pixels.length];
		float xOffset = graphRadius, yOffset = graphRadius;
		int zOffset = Integer.MAX_VALUE;

		Matrix3D mat = transformer.amat;

		for( int i = faces.length; i != 0; ) {
			
			TriangleByIndex f = faces[--i];
			Point norm = f.getNormal();
		    
			if( norm.x*mat.zx + norm.y*mat.zy + norm.z*mat.zz > 0 ) {
		    	
				int a = f.getA();
				int b = f.getB();
				int c = f.getC();
				LightPoint f1 = tvert[a];
				LightPoint f2 = tvert[b];
				LightPoint f3 = tvert[c];
				
				float zAve = f1.z + f2.z + f3.z + zOffset;
	
				float ay = f1.y + yOffset;
				float by = f2.y + yOffset;
				float cy = f3.y + yOffset;
				float top_x, left_x, right_x;
				float top_y, left_y, right_y;
				float top_color, left_color, right_color;
	
				// Orient the triangle so we know where to start, where to
				// modify our interpolation, and where to finish.
				if( by > ay ) {
					if( cy > by ) {
						top_x = f3.x + xOffset;
						left_x = f1.x + xOffset;
						right_x = f2.x + xOffset;
						top_y = cy;
						left_y = ay;
						right_y = by;
						top_color = eHueColor[c];
						left_color = eHueColor[a];
						right_color = eHueColor[b];
					} else {
						top_x = f2.x + xOffset;
						left_x = f3.x + xOffset;
						right_x = f1.x + xOffset;
						top_y = by;
						left_y = cy;
						right_y = ay;
						top_color = eHueColor[b];
						left_color = eHueColor[c];
						right_color = eHueColor[a];
					}
				} else if( cy > ay ) {
					top_x = f3.x + xOffset;
					left_x = f1.x + xOffset;
					right_x = f2.x + xOffset;
					top_y = cy;
					left_y = ay;
					right_y = by;
					top_color = eHueColor[c];
					left_color = eHueColor[a];
					right_color = eHueColor[b];
				} else {
					top_x = f1.x + xOffset;
					left_x = f2.x + xOffset;
					right_x = f3.x + xOffset;
					top_y = ay;
					left_y = by;
					right_y = cy;
					top_color = eHueColor[a];
					left_color = eHueColor[b];
					right_color = eHueColor[c];
				}
				
				float mult = 1 / (top_y - left_y);
				float leftScreenX = top_x;
				float left_dx = (left_x - top_x) * mult;
				float leftScanColor = top_color;
				float left_dcolor = (left_color - top_color) * mult;

				mult = 1 / (top_y - right_y);
				float rightScreenX = top_x;
				float right_dx = (right_x - top_x) * mult;
				float rightScanColor = top_color;
				float right_dcolor = (right_color - top_color) * mult;

				int top_screen = ((int)top_y)*graphwidth;      // Used to be ceil
				int bottom_screen, middle_screen;
				if( left_y < right_y ) {
					bottom_screen = ((int)left_y)*graphwidth;  // Used to be ceil
					middle_screen = ((int)right_y)*graphwidth;
				} else {
					bottom_screen = ((int)right_y)*graphwidth;
					middle_screen = ((int)left_y)*graphwidth;
				}
				
				int scanY = top_screen;
				if( top_screen != middle_screen ) {
					scanY -= graphwidth;	
					
					leftScreenX += left_dx;
					leftScanColor += left_dcolor;
	
					rightScreenX += right_dx;
					rightScanColor += right_dcolor;
				}
	
				// Unrolled loops for optimization
				for( ; scanY > middle_screen; scanY -= graphwidth ) {
	
					float color = leftScanColor;
					float dcolor = (leftScanColor - rightScanColor)/(leftScreenX - rightScreenX);
	
					int upperX = scanY + (int)rightScreenX;
	
					// Inner loop
					for( int s = scanY + (int)leftScreenX; s < upperX; ++s, color += dcolor ) {
						if( zAve > zBuffer[s] ) {
							zBuffer[s] = zAve;
							screen_pixels[s] = H11toRGBTable[(int)color];
						}
					}
	
					// Go to next line
					leftScreenX += left_dx;
					leftScanColor += left_dcolor;
	
					rightScreenX += right_dx;
					rightScanColor += right_dcolor;
				}
	
				// Need to change the interpolation since we've hit the
				// middle point.
				mult = 1 / (right_y - left_y);
				if( mult > 0 ) {
					rightScreenX = right_x;
					right_dx = (left_x - right_x) * mult;
					rightScanColor = right_color;
					right_dcolor = (left_color - right_color) * mult;
				} else {
					leftScreenX = left_x;
					left_dx = (left_x - right_x) * mult;
					leftScanColor = left_color;
					left_dcolor = (left_color - right_color) * mult;	
				}
	
				for( ; scanY > bottom_screen; scanY -= graphwidth ) {
	
					float color = leftScanColor;
					float dcolor = (leftScanColor - rightScanColor)/(leftScreenX - rightScreenX);
	
					int upperX = scanY + (int)rightScreenX;
	
					// Inner loop
					for( int s = scanY + (int)leftScreenX; s < upperX; ++s, color += dcolor ) {
						if( zAve > zBuffer[s] ) {
							zBuffer[s] = zAve;
							screen_pixels[s] = H11toRGBTable[(int)color];
						}
					}
	
					// Go to next line
					leftScreenX += left_dx;
					leftScanColor += left_dcolor;
	
					rightScreenX += right_dx;
					rightScanColor += right_dcolor;
				}
			}
		}

		// Create the image from the pixels and draw it.
		g.drawImage(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(graphwidth, graphheight, screen_pixels, 0, graphwidth)),
		            (int)-graphRadius, (int)-graphRadius, null);
	}
}
