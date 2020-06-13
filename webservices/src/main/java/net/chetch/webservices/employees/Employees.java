package net.chetch.webservices.employees;

import net.chetch.webservices.DataObjectCollection;

public class Employees extends DataObjectCollection<Employee> {

    public Employees active(boolean active){
        return (Employees)populateFilterResults(new Employees(), "active", active ? 1 : 0);
    }

    public Employees active(){
        return active(true);
    }

    public Employees.FieldMap<String> employeeIDMap(){
        try {
            return asFieldMap("employee_id");
        } catch (Exception e){
            return null;
        }
    }
}
