/*
 * Interfaces for generalized edges which refer to an ordered parent list
 * by index.
 *
 * Cris Cecka
 */

interface EdgeByIndex
{
  public int getStartIndex();
  public int getEndIndex();
}

interface TriangleByIndex
{
  public int getA();
  public int getB();
  public int getC();
  public Point getNormal();
}

interface DirectedEdge
    extends EdgeByIndex, TriangleByIndex
{
  public DirectedEdge getNext();
  public DirectedEdge getTwin();
  public Graph.Vertex getDest();
  public void setNext(DirectedEdge e);
  public void setTwin(DirectedEdge e);
  public void setDest(Graph.Vertex v);
}
