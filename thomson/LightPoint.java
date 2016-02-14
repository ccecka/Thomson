/*
 * A slightly smaller, faster Point object. Use when accuracy is not important.
 *
 * Cris Cecka
 */


final class LightPoint
{
  public float x, y, z;

  public LightPoint() {}

  LightPoint(LightPoint p)
  {
    x = p.x;
    y = p.y;
    z = p.z;
  }

  LightPoint(Point p)
  {
    x = (float)p.x;
    y = (float)p.y;
    z = (float)p.z;
  }

  LightPoint(float a, float b, float c)
  {
    x = a;
    y = b;
    z = c;
  }

  final public void set(float a, float b, float c)
  {
    x = a;
    y = b;
    z = c;
  }
}