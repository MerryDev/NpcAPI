package de.digitaldevs.api.reflection;

import java.lang.reflect.Field;

public class Reflection {

  public void setValue(Object object, String name, Object value) {
    try {
      Field field = object.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(object, value);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public Object getValue(Object object, String name) {
    try {
      Field field = object.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(object);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }

}
