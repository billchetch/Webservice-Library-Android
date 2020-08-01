package net.chetch.webservicestest;

import net.chetch.webservices.DataObject;

import java.util.Calendar;

public class CrewMemberStats extends DataObject {

    public enum State{
        MOVING,
        IDLE
    }

    /*@Override
    public void init() {
        super.init();

        asEnum("current_state", State.class);
    }*/

    public Calendar getStartedDuty(){
        return getCasted("started_duty");
    }

    public Calendar getEndedDuty(){
        return getCasted("ended_duty");
    }

}
