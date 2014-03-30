package com.oldpotatoes.doze;

// Doze timer interface.
interface ITimerService 
{
    // Send doze details to the service
    void setTimeDetails(in String dozeName, in long dozeHour, in long dozeMinute, in long wakeHour, in long wakeMinute, in boolean activated);

	// Begin the timer
    void setActivated();

	// End the timer
    void setDeactivated();
}
