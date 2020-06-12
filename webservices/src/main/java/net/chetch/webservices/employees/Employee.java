package net.chetch.webservices.employees;

import net.chetch.webservices.DataObject;
import net.chetch.webservices.DataObjectCollection;

public class Employee extends DataObject {

    static public DataObjectCollection<Employee> active(boolean active, DataObjectCollection<Employee> collection){
        return collection.filter("active", active ? 1 : 0);
    }

    static public DataObjectCollection<Employee> active(DataObjectCollection<Employee> collection){
        return active(true, collection);
    }

    static public DataObjectCollection<Employee>.FieldMap<String> employeeIDMap(DataObjectCollection<Employee> collection){
        try {
            return collection.asFieldMap("employee_id");
        } catch (Exception e){
            return null;
        }
    }

    private String employee_id;
    private String first_name;
    private String last_name;
    private String known_as;
    private String full_name;
    private int position_id;
    private int active = 1;

    public String getEmployeeID() {
        return employee_id;
    }
    public String getFullName(){
        return full_name;
    }
    public String getKnownAs(){
        return known_as;
    }

    public void setEmployeeID(String v){
        employee_id = v;
    }

    public void setFirstName(String v){
        first_name = v;
    }

    public void setLastName(String v){
        last_name = v;
    }

    public void setKnownAs(String v){
        known_as = v;
    }

    public void setPositionID(int v){
        position_id = v;
    }

    public void setActive(boolean v){
        active = v ? 1 : 0;
    }

    public boolean isActive(){
        return active == 1;
    }
}
