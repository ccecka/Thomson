/*
 * Singly Linked List to be used for ints
 */

import java.util.*;
import java.io.*;

public class IntList
{
  private Int head;
  private int n;

  /** Constructs an empty IntList
   */
  IntList()
  {
    n = 0;
  }

  IntList(IntList list)
  {
    n = 0;

    Iterator itr = list.getIterator();
    while( itr.hasNext() )
      add( itr.next() );
  }

  IntList(int x)
  {
    head = new Int(x);
    n = 1;
  }

  /** Return the first element (last added) in the list
   */
  public int peek()
  {
    return head.value;
  }

  /** Return the nth element in the list
   */
  public int peek(int n)
  {
    Int curr = head;
    for( ; n != 0; --n )
      curr = curr.next;

    return curr.value;
  }

  /** Determine if this IntList contains a value
   * @return True if the IntList contains the value i
   */
  public boolean contains(int i)
  {
    for( Int curr = head; curr != null; curr = curr.next )
      if( curr.value == i )
	return true;
    return false;
  }

  /** Removes the first element in the list
   * @return The value that was removed
   */
  public int pop()
  {
    int result = head.value;
    head = head.next;
    --n;
    return result;
  }

  /** Add an Int to the IntList
   */
  public void add(int i)
  {
    head = new Int(i, head);
    ++n;
  }

  /** Add an Int to the IntList
   * only if the list doesn't already contain that value
   * @return Whether the element was added or not
   */
  public boolean addUnique(int i)
  {
    if( !contains(i) ) {
      add(i);
      return true;
    }
    return false;
  }

  /** Delete all elements matching i
   * @return The number of elements deleted
   */
  public int delete(int a)
  {
    if( head != null )
      return 0;

    int N = n;
    Int curr = head;
    while( curr.next != null ) {
      if( curr.next.value == a ) {
        curr.next = curr.next.next;
        --n;
      } else {
        curr = curr.next;
      }
    }

    if( head.value == a ) {
      head = head.next;
      --n;
    }

    return N - n;
  }

  /** Replace all instances of x with y
   * @return The number of elements replaced
   */
  public int replace(int oldValue, int newValue)
  {
    if( head == null )
      return 0;

    int N = 0;
    for( Int curr = head; curr != null; curr = curr.next ) {
      if( curr.value == oldValue ) {
	curr.value = newValue;
        ++N;
      }
    }

    return N;
  }

  /** Delete the contents of this IntList
   */
  public void makeEmpty()
  {
    head = null;
    n = 0;
  }

  /** Get an Iterator through this IntList
   */
  public Iterator getIterator()
  {
    return new Iterator(head);
  }

  /** Return the number of elements this IntList contains
   */
  public int length()
  {
    return n;
  }

  /** Private class for storing an int to be used in the IntList
   */
  private static class Int
  {
    public int value;
    public Int next;

    Int(int i) {
      value = i;
      next = null;
    }

    Int(int i, Int next_) {
      value = i;
      next = next_;
    }
  }

  /** Iterator class for simple looping
   */
  public class Iterator
  {
    private Int current;

    Iterator(Int curr) {
      current = curr;
    }

    public boolean hasNext() {
      return current != null;
    }

    public int next() {
      int val = current.value;
      current = current.next;
      return val;
    }
  }

  public String toString()
  {
    StringBuffer result = new StringBuffer();

    if( head != null ) {
      result.append(head.value);

      for( Int curr = head.next; curr != null; curr = curr.next ) {
	result.append(", ").append(curr.value);
      }
    }

    return result.toString();
  }

  /** Parses ints contained in a string (separated by any format)
   */
  public static IntList parseIntList(String str)
  {
    // Split the string along anything that doesn't make a number
    String[] ints = str.split("[^-.Ee0-9]+");

    // The first can be null if non-number prefix
    int i = 0;
    if( ints[0].equals("") )
      ++i;

    IntList result = new IntList();
    for( ; i < ints.length; ++i )
      result.add( Integer.parseInt(ints[i]) );

    return result;
  }

  public static IntList[] getIntListArrayFromString(String s)
  {
    Vector<IntList> result = new Vector<IntList>();
    BufferedReader inStr = new BufferedReader(new StringReader(s));
    String str = null;
    int start;

    try {
      while( (str = inStr.readLine()) != null ) {

        str = str.trim();

        if( str.equals("") )    // Skip empty lines
          continue;

        if( (start = str.indexOf(':')) != -1 )    // Remove index tag
          str = str.substring(start+1,str.length()).trim();

        result.add( IntList.parseIntList(str) );  // Parse the rest
      }
    } catch( Exception e ) {
      Const.out.println("Error Loading AdjArray In Line " + str);
      return null;
    }

    return (IntList[]) result.toArray(new IntList[result.size()]);
  }

  public static String getFormattedString(IntList[] list)
  {
    StringBuffer result = new StringBuffer();

    if( list.length > 0 )
      result.append(list[0].toString());

    for( int i = 1; i < list.length; ++i )
      result.append("\n").append(list[i].toString());

    return result.toString();
  }
}