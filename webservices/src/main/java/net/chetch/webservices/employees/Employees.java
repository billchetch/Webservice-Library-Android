package net.chetch.webservices.employees;

import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.WebserviceRepository;

public class Employees extends DataObjectCollection<Employee> {

    public Employees(){
        super(Employees.class);
    }

    public Employees active(boolean active){
        return filter("active", active ? 1 : 0);
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
