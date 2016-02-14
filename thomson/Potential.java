
interface Potential
{
  // Return the int identifier
  public int    id();
  // Compute the potential between a and b
  public double energy(Point a, Point b);
  // Update the forces (add and sub) between a and b
  public void   force(Point a, Point b, Point fa, Point fb);
  // Update the forces (add and sub) between a and b (including mass)
  public void   force(Point a, Point b, Point fa, double mb);
}

class Pot0
  implements Potential
{
  public int id()
  {
    return 0;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return Math.log(1/Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = mb / (dx*dx + dy*dy + dz*dz + 1e-10);
    fa.x += dx*scale; fa.y += dy*scale; fa.z += dz*scale;
  }
}


class Pot1
  implements Potential
{
  public int id()
  {
    return 1;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/Math.sqrt(scale);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += (dx*mb); fa.y += (dy*mb); fa.z += (dz*mb);
  }
}

class Pot2
  implements Potential
{
  public int id()
  {
    return 2;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/scale;
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot3
  implements Potential
{
  public int id()
  {
    return 3;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/(scale*Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot4
  implements Potential
{
  public int id()
  {
    return 4;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/(scale*scale);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot5
  implements Potential
{
  public int id()
  {
    return 5;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/(scale*scale*Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx*mb; fa.y += dy*mb; fa.z += dz*mb;
  }
}

class Pot6
  implements Potential
{
  public int id()
  {
    return 6;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/(scale*scale*scale);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale; scale *= scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale; scale *= scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot7
  implements Potential
{
  public int id()
  {
    return 7;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    return 1.0/(scale*scale*scale*Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale = temp*temp*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale = temp*temp*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot8
  implements Potential
{
  public int id()
  {
    return 8;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    scale *= scale;
    return 1.0/(scale*scale);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot9
  implements Potential
{
  public int id()
  {
    return 9;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    return 1.0/(temp*temp*Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}


class Pot10
  implements Potential
{
  public int id()
  {
    return 10;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    return 1.0/(temp*temp*scale);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale;
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot11
  implements Potential
{
  public int id()
  {
    return 11;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    return 1.0/(temp*temp*scale*Math.sqrt(scale));
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}

class Pot12
  implements Potential
{
  public int id()
  {
    return 12;
  }
  public double energy(Point a, Point b)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    return 1.0/(temp*temp*temp);
  }
  public void force(Point a, Point b, Point fa, Point fb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += dx; fa.y += dy; fa.z += dz;
    fb.x -= dx; fb.y -= dy; fb.z -= dz;
  }

  public void force(Point a, Point b, Point fa, double mb)
  {
    double dx = (a.x - b.x), dy = (a.y - b.y), dz = (a.z - b.z);
    double scale = dx*dx + dy*dy + dz*dz; // a.distanceFromSq(b);
    double temp = scale*scale;
    scale *= temp*temp*scale*Math.sqrt(scale);
    dx /= scale; dy /= scale; dz /= scale;
    fa.x += mb*dx; fa.y += mb*dy; fa.z += mb*dz;
  }
}