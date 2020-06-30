package net.chetch.webservices;

final public class DataField extends Object {

    private transient Object value;
    private transient String name;


    public DataField(){

    }

    public DataField(String name){
        setName(name);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setValue(Object value){
        this.value = value;
    }

    public Object getValue(){
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
