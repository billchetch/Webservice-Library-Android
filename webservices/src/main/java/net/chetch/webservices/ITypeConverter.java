package net.chetch.webservices;

public interface ITypeConverter<T>{
    public T convert(Object data);
}
