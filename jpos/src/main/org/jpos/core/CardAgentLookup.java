/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:04  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;


/**
 * CardAgentLookup is a singleton in charge
 * of registering and further locating Agents
 * @see CardAgent
 * @see CardTransaction
 */
public class CardAgentLookup {
    private static CardAgentLookup instance = new CardAgentLookup();
    private ArrayList agents;

    /**
     * no external instantiation - thank you
     */
    private CardAgentLookup () {
	agents = new ArrayList();
    }
    /**
     * add an Agent at the end of the list
     * @param agent Agent to add
     */
    synchronized public static void add (CardAgent agent) {
	instance.agents.add (agent);
    }
    /**
     * remove all ocurrences of agent
     * @param agent Agent to remove
     */
    synchronized public static void remove (CardAgent agent) {
	ArrayList a = instance.agents;
	int i;
	while ( (i=a.indexOf(agent)) >= 0)
	    a.remove(i);
    }
    /**
     * @return all available agents
     */
    synchronized public static CardAgent[] getAgents() {
	ArrayList a = instance.agents;
	return (CardAgent[]) a.toArray(new CardAgent[a.size()]);
    }
    /**
     * locate an agent giving its class Name
     * @param class name
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (String name)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if ( (a.getClass().getName()).endsWith (name) ) 
		return a;
	}
	throw new CardAgentNotFoundException (name);
    }
    /**
     * locate an agent giving its unique agent ID
     * @param id
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (int id)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.getID() == id)
		return a;
	}
	throw new CardAgentNotFoundException (Integer.toString(id));
    }

    /**
     * locate an agent giving a transaction Image
     * @param b a transaction image
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (byte[] b)
	throws CardAgentNotFoundException
    {
	try {
	    ByteArrayInputStream i = new ByteArrayInputStream (b);
	    ObjectInputStream o    = new ObjectInputStream (i);
	    int id = o.readInt();
	    return getAgent (id);
	} catch (Exception e) { }
	throw new CardAgentNotFoundException ();
    }

    /**
     * locate an agent of a given its class
     * @param class 
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (Class t)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.getClass() == t)
		return a;
	}
	throw new CardAgentNotFoundException (t.getName());
    }
    /**
     * locate an agent able to process a given CardTransaction
     * @param t CardTransaction holding an Operation to be performed
     * @return suitable agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (CardTransaction t)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.canHandle (t)) 
		return a;
	}
	throw new CardAgentNotFoundException ();
    }
    synchronized public static CardAgent invoke (CardTransaction t)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    try {
		Class[] param = { CardTransaction.class };
		Method m = a.getClass().getMethod("pay", param);
		if (a.canHandle (t)) {
		    System.out.println ("Trying to invoke ...");
		    param = new Class[1];
		    Object[] p = new Object[1];
		    p[0] = t;
		    m.invoke (a, p);
		    return a;
		}
	    } catch (NoSuchMethodException e) { 
		e.printStackTrace();
	    } catch (SecurityException e) { 
		e.printStackTrace();
	    } catch (java.lang.reflect.InvocationTargetException e) {
		e.printStackTrace();
	    } catch (java.lang.IllegalAccessException e) {
		e.printStackTrace();
	    }
	}
	throw new CardAgentNotFoundException ();
    }
}
