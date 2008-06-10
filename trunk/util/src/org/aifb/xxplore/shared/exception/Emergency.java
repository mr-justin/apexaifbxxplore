
package org.aifb.xxplore.shared.exception;

/**
 * This is a utility class offering service methods to be called in emergency situations or the check for emergency situations.
 * <p/>
 * An emergency is an error that cannot be solved by the program. An emergency will always cause an {@link EmergencyException}.
 * </p>
 *
 */
public class Emergency {
    //~ Methods ---------------------------------------------------------------------------------------------------------------

    /**
     * Throws an {@link EmergencyException} containing the given 'message'.
     *
     * @param message Message describing the emergency
     */
    public static void now(String message) {
        now(message, null);
    }

    /**
     * Throws an {@link EmergencyException} containing the given message amd the optional cause.
     *
     * @param message Message describing the emergency
     * @param cause   The exception/error that caused the emergency or <code>null</code>
     */
    public static void now(String message, Throwable cause) {
        if (cause == null) {
            throw new EmergencyException(message);
        }
        else {
            throw new EmergencyException(message, cause);
        }
    }

    /**
     * Checks a precondition and causes an emergency, if the precondition is violated.
     *
     * @param condition       Boolean expression declaring the precondition
     * @param conditionString String describing the precondition (may be equal to the boolean expression)
     */
    public static void checkPrecondition(boolean condition, String conditionString) {
        if (condition) {
            return;
        }

        now("The following precondition was violated: " + conditionString);
    }

    /**
     * Checks a postcondition and causes an emergency, if the postcondition is violated.
     *
     * @param condition       Boolean expression declaring the postcondition
     * @param conditionString String describing the postcondition (may be equal to the boolean expression)
     */
    public static void checkPostcondition(boolean condition, String conditionString) {
        if (condition) {
            return;
        }

        now("The following postcondition was violated: " + conditionString);
    }

    /**
     * Checks an invariant and causes an emergency, if the invariant is violated.
     *
     * @param invariant       Boolean expression declaring the invariant
     * @param invariantString String describing the invariant (may be equal to the boolean expression)
     */
    public static void checkInvariant(boolean invariant, String invariantString) {
        if (invariant) {
            return;
        }

        now("The following invariant was violated: " + invariantString);
    }

    /**
     * Causes an emergency because a block of code is reached that was predicted to be unreachable.
     */
    public static final void unreachableCode() {
        unreachableCode(null);
    }

    /**
     * Causes an emergency because a block of code is reached that was predicted to be unreachable.
     *
     * @param cause The exception/error that caused the emergency
     */
    public static void unreachableCode(Throwable cause) {
        now("Code considered unreachable was reached.", cause);
    }

    /**
     * Causes an emergency because a method was called that is not implemented by now.
     */
    public static final void methodNotImplemented() {
        now("Method not implemented.");
    }
}
