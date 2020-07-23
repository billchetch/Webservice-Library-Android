package net.chetch.webservices.employees;

import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.WebserviceRepository;

public class Employees<E extends Employee> extends DataObjectCollection<E> {

    public Employees(){
        super(Employees.class);
    }

    public Employees(Class<E> icls){
        super(Employees.class, icls);
    }

    public <C extends Employees>Employees(Class<C> ccls, Class<E> icls){
        super(ccls, icls);
    }

    public <C extends Employees> C active(boolean active){
        return filter("active", active ? 1 : 0);
    }

    public <C extends Employees> C inactive(){
        return active(false);
    }

    public <C extends Employees> C active(){
        return active(true);
    }

    public Employees<E>.FieldMap<String> employeeIDMap(){
        try {
            return asFieldMap("employee_id");
        } catch (Exception e){
            return null;
        }
    }

    public E getByEmployeeID(String employeeID){
        return get("employee_id", employeeID);
    }
}
