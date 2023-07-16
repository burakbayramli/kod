package swisseph;

public abstract class TransitCalculator {
  SwissEph sw;

  // This method changes the offset value for the transit
  /**
  * @return Returns true, if one position value is identical to another
  * position value. E.g., 360 degree is identical to 0 degree in
  * circular angles.
  * @see #rolloverVal
  */
  public abstract boolean getRollover();
  /**
  * This sets the degree or other value for the position or speed of
  * the planet to transit. It will be used on the next call to getTransit().
  * @param value The desired offset value.
  * @see #getOffset()
  */
  public abstract void setOffset(double value);
  /**
  * This returns the degree or other value of the position or speed of
  * the planet to transit.
  * @return The currently set offset value.
  * @see #setOffset(double)
  */
  public abstract double getOffset();
  /**
  * This returns all the &quot;object identifiers s&quot; used in this
  * TransitCalculator. It may be the planet number or planet numbers,
  * when calculating planets.
  * @return An array of identifiers identifying the calculated objects.
  */
  public Object[] getObjectIdentifiers() {
    return null;
  }





  //////////////////////////////////////////////////////////////////////////////


  // Rollover from 360 degrees to 0 degrees for planetary longitudinal positions
  // or similar, or continuous and unlimited values:
  protected boolean rollover = false; // We need a rollover of 360 degrees being
                                      // equal to 0 degrees for longitudinal
                                      // position transits only.
  protected double rolloverVal = 360.; // if rollover, we roll over from 360 to 0
                                       // as default. Other values than 0.0 for the
                                       // minimum values are not supported for now.

  // These methods have to return the maxima of the first derivative of the
  // function, mathematically spoken...
  protected abstract double getMaxSpeed();
  protected abstract double getMinSpeed();

  // This method returns the precision in x-direction in an x-y-coordinate
  // system for the transit calculation routine.
  protected abstract double getDegreePrecision(double jdET);

  // This method returns the precision in y-direction in an x-y-coordinate
  // system from the x-direction precision.
  protected abstract double getTimePrecision(double degPrec);

  // This is the main routine, mathematically speaking: returning f(x):
  protected abstract double calc(double jdET);
}
