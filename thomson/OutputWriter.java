/*
 * Class to easily create custom writers.
 * To create a custom writer, an anonymous class can easily
 * be created which overrides print(String s);
 *
 * For Example:
 *
 * OutputWriter myOut = new OutputWriter() {
 *      void print(String s) { System.out.print(s); }
 * };	
 *
 * will pipe all print statements to myOut to System.out
 */

class OutputWriter
{
	void print(String s)    {}
	void print(boolean x)   {print("" + x);}
	void print(char x)      {print("" + x);}
	void print(char[] x)    {print("" + x);} 
	void print(double x)    {print("" + x);} 
	void print(float x)     {print("" + x);} 
	void print(int x)       {print("" + x);}
	void print(long x)      {print("" + x);}
	void print(Object x)    {print(x.toString());}
	void println(String x)  {print(x + "\n");}
	void println(boolean x) {print(x + "\n");} 
	void println(char x)    {print(x + "\n");}
	void println(char[] x)  {print(x + "\n");}
	void println(double x)  {print(x + "\n");}
	void println(float x)   {print(x + "\n");}
	void println(int x)     {print(x + "\n");}
	void println(long x)    {print(x + "\n");}
	void println(Object x)  {print(x.toString() + "\n");}
	void println()          {print("\n");}
}