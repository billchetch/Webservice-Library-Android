package net.chetch.webservicestest;

import android.util.Log;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.employees.Employees;

public class Crew extends Employees<CrewMember> {

    public Crew(){
        super(Crew.class, CrewMember.class);
    }

}
