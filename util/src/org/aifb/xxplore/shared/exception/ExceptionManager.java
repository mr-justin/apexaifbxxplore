package org.aifb.xxplore.shared.exception;


/*
 * Copyright (c) 2001, 2002
 * software design & management AG.
 * All rights reserved.
 * This file is made available under the terms of the license
 * agreement that accompanies this distribution.
 *
 * $Revision: 1.1 $, last modified $Date: 2006/09/10 23:24:32 $ by $Author: dtr $
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.aifb.xxplore.shared.util.HashMapSize;
import org.aifb.xxplore.shared.util.Pair;

/**
 * The class <code>ExceptionManager</code> supplies a centralized mechanism for handling all type of exceptions. 
 * 
 * For each exception, for which a centralized handling is desired, an implementation for the interface
 * {@link ExceptionHandler} must be defined and registered in the exception manager using {@link #addExceptionHandler}.
 * The exception manager will become active for concrete thread, when {@link #runWithExceptionHandler} is called.
 * 
 * Whenever an exception is detected, it can be passed to the currently active exception manager (to the registered
 * exception handler, to be more precise) with the method {@link #handleException}. 
 * 
 * If the exception is not handled, it is thrown by <code>runWithExceptionHandler</code>. 
 *
 * It is possible to test an exception, whether it will be handled by the current exception manager with 
 * {@link #isHandledException}.
 *
 * 
 * In all, it can be used to 
 * a) handle specific checked exceptions by calling {@link #handleException}
 * b) report runtime exceptions by calling {@link #fail}
 * c) handle runtime exceptions thrown and not already handled in the threads by catching them in the caller of 
 * {@link #runWithExceptionHandler} 
 *
 */

public class ExceptionManager {

	private final static ThreadLocal currentException = new ThreadLocal();
    private final static ThreadLocal nextApplicableHandlers = new ThreadLocal();


    private ArrayList exceptionHandlers = new ArrayList(5);
    private ArrayList exceptionLoggers = new ArrayList(2);
    private HashMap cachedApplicableHandleMethods = new HashMap(HashMapSize.COMPACT);
    private HashMap cachedApplicableLogMethods = new HashMap(HashMapSize.COMPACT);
    private HashMap exceptionMethods = new HashMap(HashMapSize.COMPACT);

    public static abstract class TunnelException extends RuntimeException {
        private Throwable exception;

        public TunnelException(Throwable exception) {
            this.exception = exception;
        }

        public final Throwable getException() {
            return this.exception;
        }
    }

    private static class PropagateException extends TunnelException {
        public PropagateException(Throwable exception) {
            super(exception);
        }
    }

    private static class ExitException extends TunnelException {
        public ExitException(Throwable exception) {
            super(exception);
        }
    }

    /**
     * This interface must be implemented by each exception handler, that is registered for a given exception manager.
     * A concrete <code>ExceptionHandler</code> must implement one or more methods named "handleException", which have
     * one parameter for a concrete exception class:
     * <p>        public abstract void handleException("ExceptionClass" exception);
     * <p>These methods may be called by the exception manager, whenever an exception of the given class has occurred and
     * the method {@link ExceptionManager#handleException} has been called for this exception. But before these methods are called,
     * <code>isHandledException</code> is called to test, whether the given exception is really handled by this
     * exception handler.
     */
    public interface ExceptionHandler {
        /**
         * <code>isHandledException</code> will return <code>true</code>, if the given exception will be handled by this
         * exception handler.
         *
         * @param   exception   the <code>Exception</code> to be tested
         * @return  <code>true</code>, if this handler will handle the given exception
         */
        public boolean isHandledException(Throwable exception);

        //public void handleException(Throwable e);
    }

    /**
     * The abstract class <code>AbstractExceptionHandler</code> may be used as a base class when implementing the
     * {@link ExceptionManager.ExceptionHandler} interface. The default implementation for the method <code>isHandledException</code>
     * simply returns <code>true</code>, if a <code>handleException</code> method exists that can be applied to a given
     * exception.
     */
    public static abstract class AbstractExceptionHandler implements ExceptionHandler {
        public boolean isHandledException(Throwable exception) {
            Class exceptionClass = exception.getClass();
            Method[] methods = ExceptionManager.getSingleton().findExceptionMethods(getClass(), "handleException");

            if (methods.length > 0) {
                int mLength = methods.length;

                for (int i = 0; i < mLength; i++)
                    if (methods[i].getParameterTypes()[0].isAssignableFrom(exceptionClass))
                        return true;
            }

            return false;
        }
    }

    /**
     * This interface must be implemented by each exception logger, that is registered for a given exception manager.
     * A concrete <code>ExceptionLogger</code> must implement one or more methods named "logException", which have
     * one parameter for a concrete exception class:
     * <p>        public abstract void logException("ExceptionClass" exception);
     * <p>These methods may be called by the exception manager, whenever an exception of the given class has occurred and
     * the method {@link ExceptionManager#handleException} has been called for this exception.
     */
    public interface ExceptionLogger {
        // public abstract void logException("ExceptionClass" exception);
    }

    
    static ExceptionManager theInstance;
    
    
    /**
     * Returns the topmost active <code>ExceptionManager</code>, i.e. the exception manager, for which
     * {@link #runWithExceptionHandler} has been most recently called.
     *
     * @return the active <code>ExceptionManager</code>
     */
    public static synchronized ExceptionManager getSingleton() {
        
    	if (theInstance == null){
            theInstance = new ExceptionManager();
        }
        return theInstance;
	        
    }

    /**
     * <code>addExceptionHandler</code> registers the given <code>ExceptionHandler</code> for this exception manager.
     * The new exception handler will be used according to its signature for all subsequent calls to {@link #handleException}.
     *
     * @param   exceptionHandler    the new <code>ExceptionHandler</code> to be registered
     * @see     ExceptionHandler
     * @see     #findApplicableHandleExceptionMethods
     * @see     #addExceptionLogger
     */
    public void addExceptionHandler(ExceptionHandler exceptionHandler) {
        Emergency.checkPrecondition(getExceptionMethods(exceptionHandler.getClass(), "handleException").length > 0,
                            "getExceptionMethods(exceptionHandler.getClass(), \"handleException\").length > 0");

        synchronized (this) {
            ArrayList newExceptionHandlers = (ArrayList)exceptionHandlers.clone();

            newExceptionHandlers.add(exceptionHandler);

            exceptionHandlers = newExceptionHandlers;
            
            //TODO
            cachedApplicableHandleMethods = new HashMap(HashMapSize.COMPACT);
            cachedApplicableLogMethods = new HashMap(HashMapSize.COMPACT);
        }
    }

    /**
     * <code>addExceptionLogger</code> registers the given <code>ExceptionLogger</code> for this exception manager.
     * The new exception logger will be used according to its signature for all subsequent calls to {@link #handleException}.
     *
     * @param   exceptionLogger    the new <code>ExceptionLogger</code> to be registered
     * @see     ExceptionLogger
     * @see     #findApplicableLogExceptionMethods
     * @see     #addExceptionHandler
     */
    public void addExceptionLogger(ExceptionLogger exceptionLogger) {
        Emergency.checkPrecondition(getExceptionMethods(exceptionLogger.getClass(), "logException").length > 0,
                            "getExceptionMethods(exceptionLogger.getClass(), \"logException\").length > 0");

        synchronized (this) {
            ArrayList newExceptionLoggers = (ArrayList)exceptionLoggers.clone();

            newExceptionLoggers.add(exceptionLogger);

            exceptionLoggers = newExceptionLoggers;
            
            //TODO 
            cachedApplicableHandleMethods = new HashMap(HashMapSize.COMPACT);
            cachedApplicableLogMethods = new HashMap(HashMapSize.COMPACT);
        }
    }

    /**
     * This method returns all exception handler methods that may be applicable to an exception of the given class
     * sorted according to specificity. The returned array contains a <code>Pair</code> for each method. The head of
     * the pair will be the exception handler and the tail the <code>Method</code> object for the <i>handleException</i>
     * method.
     *
     * <p>Note: Before an exception handler is applied to an exception of the given class, it must be asked, whether
     * it will handle the given exception using the method {@link ExceptionManager.ExceptionHandler#isHandledException}.
     *
     * @param   exceptionClass  the class of the exception for which all applicable exception handlers should be returned
     * @return  an array of all applicable exception handler methods in the format described above
     * @see     ExceptionHandler
     * @see     #addExceptionHandler
     */
    public Pair[] findApplicableHandleExceptionMethods(Class exceptionClass) {
        Pair[] methods = (Pair[])cachedApplicableHandleMethods.get(exceptionClass);

        if (methods == null)
            synchronized (this) {
                methods = getSortedApplicableExceptionMethods(exceptionHandlers, "handleException", exceptionClass);

                cachedApplicableHandleMethods.put(exceptionClass, methods);
            }

        return methods;
    }

    /**
     * This method returns all exception logger methods that may be applicable to an exception of the given class
     * sorted according to specificity. The returned array contains a <code>Pair</code> for each method. The head of
     * the pair will be the exception logger and the tail the <code>Method</code> object for the <i>logException</i>
     * method.
     *
     * @param   exceptionClass  the class of the exception for which all applicable exception loggers should be returned
     * @return  an array of all applicable exception logger methods in the format described above
     * @see     ExceptionLogger
     * @see     #addExceptionLogger
     */
    public Pair[] findApplicableLogExceptionMethods(Class exceptionClass) {
        Pair[] methods = (Pair[])cachedApplicableLogMethods.get(exceptionClass);

        if (methods == null)
            synchronized (this) {
                methods = getSortedApplicableExceptionMethods(exceptionLoggers, "logException", exceptionClass);

                cachedApplicableLogMethods.put(exceptionClass, methods);
            }

        return methods;
    }

    /**
     * This static method may be called to test whether a given exception can be handled by at least one of the
     * active exception managers, which means that a handler for the given exception is registered for at least one
     * exception manager.
     *
     * @param       exception   the <code>Exception</code> to be tested
     * @return      <code>true</code>, if the exception can be handled
     * @see ExceptionManager.ExceptionHandler#isHandledException
     * @see #handleException
     */
    public final static boolean isHandledException(Throwable exception) {
        if (exception instanceof TunnelException)
            return true;
        else {
            ExceptionHandler lastExceptionHandler = null;
            	
        	Pair[] exceptionHandlers = getSingleton().findApplicableHandleExceptionMethods(exception.getClass());
            int eLength = exceptionHandlers.length;

            for (int i = 0; i < eLength; i++) {
                ExceptionHandler exceptionHandler = (ExceptionHandler)exceptionHandlers[i].getHead();

                if ((exceptionHandler != lastExceptionHandler) && exceptionHandler.isHandledException(exception))
                    return true;

                lastExceptionHandler = exceptionHandler;
            }

            return false;
        }
    }

    /**
     * <code>runWithExceptionHandler</code> calls the <code>run</code> method of the given <code>Runnable</code>
     * with this exception manager installed as the active one. While this exception manager is established, it
     * is possible to call {@link #handleException} whenever an exception occured.
     *
     * <p>Note: The last active exception manager will be shadowed by the current exception manager. It will become
     * active again, when <code>runWithExceptionHandler</code> exits. All installed exception managers will be asked
     * for exception handlers, when an exception occured.
     *
     * @param       runnable    the <code>Runnable</code> to be run
     * @exception   Exception   if an exception occured and was not handled
     * @see ExceptionHandler
     * @see #handleException
     */
    public void runWithExceptionHandler(Runnable runnable) throws Throwable {
        try {
            
            try {
            	
            	runnable.run();
            }
            

            catch (TunnelException e) {
                if (e instanceof PropagateException)
                    throw e;
                else
                    throw e.getException();
            }
            
            catch (Exception e){
            	
            	System.out.println("im exception manager gecatched");
            }
        }
        catch (PropagateException e) {
            ExceptionManager exceptionManager = getSingleton();

            if (exceptionManager != null)
                try {
                    exceptionManager.handleException(e.getException());
                }
                catch (PropagateException e1) {
                    throw e1.getException();
                }
            else
                throw e.getException();
        }
    }

    /**
     * <code>handleException</code> may be called by application code to handle the given exception. It computes the
     * list of applicable <code>ExceptionHandler</code>s and calls the most specific with the given exception. This handler
     * may decide to
     * <li>
     *     <ol>1. return to the caller, normally after fixing the situation,</ol>
     *     <ol>2. throw a new exception (see {@link #throwException}) or</ol>
     *     <ol>3. decide to pass the exception to the next most specific handler (see {@link #callNextHandler}).
     * </li>
     *
     * <p>Before calling all applicable handlers, <code>handleException</code> will also call all registered
     * and applicable <code>ExceptionLogger</code>s.
     *
     * <p>ATTENTION: <code>handleException</code> can only be called for the current exception manager as returned by
     * {@link #getExceptionManager}.
     *
     * @param   exception   the exception to be handled
     * @see ExceptionHandler
     * @see ExceptionManager.ExceptionHandler#isHandledException
     * @see #callNextHandler
     * @see #throwException
     */
    public void handleException(Throwable exception) {
        
        while (exception instanceof TunnelException) {
            if (exception instanceof ExitException)
                throw (ExitException)exception;
            else
                exception = ((TunnelException)exception).getException();
        }

        Class exceptionClass = exception.getClass();
        Pair[] exceptionLogger = findApplicableLogExceptionMethods(exceptionClass);
        int eLength = exceptionLogger.length;

        for (int i = 0; i < eLength; i++) {
            Pair loggerInfo = exceptionLogger[i];

            logException((ExceptionLogger)loggerInfo.getHead(), (Method)loggerInfo.getTail(), exception);
        }

        Throwable previousCurrentException = (Throwable)currentException.get();
        LinkedList previousNextApplicableHandlers = (LinkedList)nextApplicableHandlers.get();

        currentException.set(exception);
        nextApplicableHandlers.set(new LinkedList(Arrays.asList(findApplicableHandleExceptionMethods(exception.getClass()))));

        try {
            callNextHandler();
        }
        finally {
            nextApplicableHandlers.set(previousNextApplicableHandlers);
            currentException.set(previousCurrentException);
        }
    }

    /**
     * This method calls the next handler in the chain of active applicable handlers for the current exception.
     * If no more handlers exist, the exception is propagated to the next exception manager in the chain of active
     * exception managers, otherwise the current exception is thrown out of the of the most recent call to
     * {@link #runWithExceptionHandler}.
     *
     * <p>ATTENTION: <code>callNextHandler</code> must be called from an {@link ExceptionHandler}.
     *
     * @see ExceptionHandler
     * @see #handleException
     * @see #throwException
     */
    public void callNextHandler() {
        LinkedList applicableHandler = (LinkedList)nextApplicableHandlers.get();
        Throwable exception = (Throwable)currentException.get();

        if (applicableHandler.size() > 0) {
            Pair handlerInfo = (Pair)applicableHandler.removeFirst();
            ExceptionHandler exceptionHandler = (ExceptionHandler)handlerInfo.getHead();

            if (exceptionHandler.isHandledException(exception))
                handleException(exceptionHandler, (Method)handlerInfo.getTail(), exception);
            else
                callNextHandler();
        }
        else
            throw new PropagateException(exception);
    }

    /**
     * <code>throwException</code> must be used by an exception handler to throw a new exception as a result
     * of handling the current exception. The new exception will thrown out of the most recent call to
     * {@link #runWithExceptionHandler}.
     *
     * <p>ATTENTION: <code>throwException</code> cannot be called from the outside of an {@link ExceptionHandler}.
     *
     * @param   exception   the new exception to be thrown
     */
    public void throwException(Throwable exception) {
        throw new ExitException(exception);
    }

     /**
     * <code>throwException</code> must be used by an exception handler to throw a new exception as a result
     * of handling the current exception. The new exception will thrown out of the most recent call to
     * {@link #runWithExceptionHandler}.
     *
     * <p>ATTENTION: <code>throwException</code> cannot be called from the outside of an {@link ExceptionHandler}.
     *
     * @param   exception   the new exception to be thrown
     */
    public void propagateException(Throwable exception) {
        throw new PropagateException(exception);
    }

    /**
     * This method always fails. The supplied message is written to <code>Debug.getPrintStream()</code>.
     *
     * <p>Note: As a result of <code>fail</code>, an <code>InternalError</code> is thrown.
     *
     * @param	message     a message to print to <code>Debug.getPrintStream()</code>
     */
    public static final void fail(String message) {
        // Debug here --> "Fatal situation occured: " + message);

        throw new InternalError("A fatal error occured...");
    }

    /**
     * This method always fails. Information about the given exception is written to <code>Debug.getPrintStream()</code>.
     *
     * <p>Note: As a result of <code>fail</code>, an <code>InternalError</code> is thrown.
     *
     * @param	exception   the fatal exception
     */
    public static final void fail(Throwable exception) {
        
    	// Debug here --> "Fatal exception occured: " + exception);
        
        throw new InternalError("A fatal error occured...");
    }

    /**
     * This method always fails. Information about the given exception and the supplied message is written to
     * <code>Debug.getPrintStream()</code>.
     *
     * <p>Note: As a result of <code>fail</code>, an <code>InternalError</code> is thrown.
     *
     * @param	exception   the fatal exception
     * @param	message     a message to print to <code>Debug.getPrintStream()</code>
     */
    public static final void fail(Throwable exception, String message) {
        // // Debug here --> "Fatal exception occured (" + message + "): " + exception, exception);
        
        throw new InternalError("A fatal error occured...");
    }


    /**
     * This method is called internally by {@link #handleException(Throwable)} to activate the given exception handler
     * on the current exception. <code>handleException</code> handles all exceptions possibly thrown by the Java
     * reflection mechanism, when calling the exception handler.
     *
     * @param   exceptionHandler    the exception handler to be activated
     * @param   method              the <i>handleException</i> method to invoke
     * @param   exception           the current exception
     * @see     #handleException(Throwable)
     * @see     #logException
     */
    protected void handleException(ExceptionHandler exceptionHandler, Method method, Throwable exception) {
        try {
            method.invoke(exceptionHandler, new Object[] { exception });
        }
        catch (IllegalAccessException e) {
            Emergency.now("Cannot invoke exception handler due to " + e);
        }
        catch (IllegalArgumentException e) {
            Emergency.now("Cannot invoke exception handler due to " + e);
        }
        catch (InvocationTargetException e) {
            Throwable ie = e.getTargetException();

            if (ie instanceof TunnelException)
                throw (TunnelException)ie;
            else {
//                if (Debug.DEBUG)
//                    Debug.trace(TRACE_LABEL, TraceLevel.OFF, "Cannot invoke exception handler", exception);

                Emergency.now("Cannot invoke exception handler due to " + ie);
            }
        }
    }

    /**
     * This method is called internally by {@link #handleException(Throwable)} to activate the given exception logger
     * on the current exception. <code>logException</code> handles all exceptions possibly thrown by the Java reflection
     * mechanism, when calling the exception handler.
     *
     * @param   exceptionLogger    the exception logger to be activated
     * @param   method             the <i>logException</i> method to invoke
     * @param   exception          the current exception
     * @see     #handleException(Throwable)
     * @see     #handleException(ExceptionManager.ExceptionHandler, Method, Throwable)
     */
    protected void logException(ExceptionLogger exceptionLogger, Method method, Throwable exception) {
        try {
            method.invoke(exceptionLogger, new Object[] { exception });
        }
        catch (InvocationTargetException e) {
            Emergency.now("Cannot invoke exception logger due to " + e.getTargetException());
        }
        catch (Exception e) {
            Emergency.now("Cannot invoke exception logger due to " + e);
        }
    }

    /**
     * <code>getExceptionMethods</code> returns the <i>dynamic</i> <code>handleException</code> or <code>logException</code>
     * methods for a registered {@link ExceptionHandler} or {@link ExceptionLogger} implemented by the given class (see
     * parameter <code>callableClass</code>).
     *
     * <p>ATTENTION: The returned methods are NOT sorted according to specificity.
     *
     * @param   callableClass   the implementation class for the exception handler or logger, for which the method should be returned
     * @param   methodName      the name of the dynamic method, either "handleException" or "logException"
     * @return  an array of all methods with the given name
     */
    protected Method[] getExceptionMethods(Class callableClass, String methodName) {
        Method[] methods = (Method[])exceptionMethods.get(callableClass);

        if (methods == null) {
            synchronized (this) {
                methods = (Method[])exceptionMethods.get(callableClass);

                if (methods == null) {
                    HashMap newHandlerMethods = (HashMap)exceptionMethods.clone();

                    newHandlerMethods.put(callableClass, methods = findExceptionMethods(callableClass, methodName));

                    exceptionMethods = newHandlerMethods;
                }
            }
        }

        return methods;
    }

    private Method[] findExceptionMethods(Class type, String methodName) {
        Method[] methods = type.getMethods();
        ArrayList result = new ArrayList(5);
        int mLength = methods.length;

        for (int i = 0; i < mLength; i++) {
            Method candidate = methods[i];

            if (candidate.getName().equalsIgnoreCase(methodName) &&
                (candidate.getParameterTypes().length == 1) &&
                (Throwable.class.isAssignableFrom(candidate.getParameterTypes()[0])))
                result.add(candidate);
        }

        return (Method[])result.toArray(new Method[result.size()]);
    }

    private Pair[] getSortedApplicableExceptionMethods(List exceptionCallables, final String methodName, Class exceptionClass) {
        ArrayList candidates = new ArrayList(5);
        int cSize = exceptionCallables.size();

        for (int i = 0; i < cSize; i++) {
            Object callable = exceptionCallables.get(i);
            Method[] methods = findExceptionMethods(callable.getClass(), methodName);
            int mLength = methods.length;

            for (int j = 0; j < mLength; j++) {
                Method method = methods[j];

                if (method.getParameterTypes()[0].isAssignableFrom(exceptionClass))
                    candidates.add(new Pair(callable, method));
            }
        }

        Pair[] methodInfos = (Pair[])candidates.toArray(new Pair[candidates.size()]);

        Arrays.sort(methodInfos, new Comparator() {
            public int compare(Object eh1, Object eh2) {
                Class exceptionClass1 = ((Method)((Pair)eh1).getTail()).getParameterTypes()[0];
                Class exceptionClass2 = ((Method)((Pair)eh2).getTail()).getParameterTypes()[0];

                if (exceptionClass1 == exceptionClass2)
                    return 0;
                else if (exceptionClass1.isAssignableFrom(exceptionClass2))
                    return 1;
                else
                    return -1;
            }
        });

        return methodInfos;
    }

    /**
     * Returns the full stacktrace from a given <code>Throwable</code>.
     *
     * @param   t   the <code>Throwable</code>
     * @return  the stacktrace of the passed <code>Throwable</code>
     */
    public static String getStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter PrintWriter = new PrintWriter(stringWriter, true);

        t.printStackTrace(PrintWriter);
        PrintWriter.flush();
        PrintWriter.close();

        return stringWriter.toString();
    }
}
