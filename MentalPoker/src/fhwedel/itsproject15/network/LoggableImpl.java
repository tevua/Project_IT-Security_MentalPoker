package fhwedel.itsproject15.network;

import java.util.LinkedList;
import java.util.List;

import fhwedel.itsproject15.network.Loggable;

/**
 * Implementation of the Loggable interface
 * 
 * @author tevua
 */
public abstract class LoggableImpl implements Loggable
{

  /**
   * list for saving the logged messages 
   */
  protected LinkedList < String > logs; 
  
  /**
   * Construktor.
   */
  public LoggableImpl()
  {
    logs = new LinkedList < String >();
  }
  
  /**
   * Saves the message
   * 
   * @param message message that is supposed to be saved
   */
  public synchronized void log(String message)
  {
    System.err.println(message); 
    this.logs.addLast(message); 
  }

  /**
   * Return all saved messages. 
   * 
   * @return List with all the messages
   */
  public List < String > getLog()
  {
    return logs; 
  }

}
