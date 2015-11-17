package fhwedel.itsproject15.network;

import java.util.List;

/**
 * Interface to collect log messages. Taken from OOP SS2012 ueb07. 
 * 
 * @author tevua
 */
public interface Loggable
{

  /**
   * Saves the message
   * 
   * @param message message that is supposed to be saved
   */
  void log(String message);

  /**
   * Return all saved messages. 
   * 
   * @return List with all the messages
   */
  List < String > getLog();

}