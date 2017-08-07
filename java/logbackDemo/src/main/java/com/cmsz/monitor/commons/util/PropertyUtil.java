package com.cmsz.monitor.commons.util;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;



public final class PropertyUtil{

  private final static String SET = "set";
  private final static String GET = "get";
  private final static String ENTER = "\r\n";
  private final static String SPACE = " ";
  private final static String COLON = ":";

  /**
   * Get toString content.
   *
   * @param obj Object
   * @param entered boolean
   * @return String
 * @throws Exception
   */
  public static String getPropertyToString(Object obj, boolean entered) throws Exception  {

    StringBuilder desc = new StringBuilder();
    desc.append("Object:" + obj.getClass().getName());

      Field[] aobjField = getDeclaredFields(obj.getClass());
      for (Field field : aobjField)
      {

          desc.append(SPACE + field.getName() + COLON);
          Object objValue = getProperty(obj, field.getName());
          desc.append(objValue);

          if (entered){
        	  desc.append(ENTER);
          }

    	  desc= getStr(field,obj,entered);
      }
      desc.append(ENTER);

    return desc.toString();
  }

  private static StringBuilder getStr(Field field,Object obj, boolean entered){
	  StringBuilder desc = new StringBuilder();
	  try
      {
        desc.append(SPACE + field.getName() + COLON);
        Object objValue = getProperty(obj, field.getName());
        desc.append(objValue);

        if (entered){
        	 desc.append(ENTER);
        }
      } catch (Exception e)
      {
        //Do Nothing
      }
      return desc;
  }

  public static String getPropertyToString(Object obj) throws Exception {

    return getPropertyToString(obj, true);
  }

  /**
   * Get all the properties
   *
   * @param clzParse Class
   * @return Field[]
   */
  public static Field[] getDeclaredFields(Class clzParse)
  {
    Field[] aobjField = clzParse.getDeclaredFields();
    Field[] aobjFieldResult = aobjField;

    Class superClz = clzParse.getSuperclass();

    while (superClz != null)
    {
      Field[] aobjSuperField = superClz.getDeclaredFields();

      if (aobjSuperField != null && aobjSuperField.length > 0)
      {
        int srcLength = aobjFieldResult.length;
        aobjField = new Field[srcLength + aobjSuperField.length];
        System.arraycopy(aobjFieldResult, 0, aobjField, 0, srcLength);
        System.arraycopy(aobjSuperField, 0, aobjField, srcLength, aobjSuperField.length);
        aobjFieldResult = aobjField;
      }

      superClz = superClz.getSuperclass();
    }

    return aobjFieldResult;
  }

  /**
   * Get field class
   *
   * @param clzParse Class
   * @param property String
   * @return Class
   */
  public static Class getDeclaredFieldClass(Class clzParse, String property)
  {
    Field[] arrField = getDeclaredFields(clzParse);
    for (Field field : arrField)
    {
      if (field.getName().equals(property))
      {
        return field.getType();
      }
    }
    return null;
  }

  /**
   * Set the property
   *
   * @param target Object
   * @param property String
   * @param value Object
   * @throws Exception
   */
  public static void setProperty(Object target, String property, Object value)
    throws Exception
  {
    if (target == null || value == null || StringUtils.isEmpty(property)){
    	 return;
    }
    Class argumentType = getDeclaredFieldClass(target.getClass(), property);
    Class[] aobjClass =  {argumentType};
    String strFunction = SET + property.substring(0, 1).toUpperCase() + property.substring(1);

    Method objMethod = target.getClass().getMethod(strFunction, aobjClass);
    objMethod.setAccessible(true);

    Object tranferValue = value;
    if (argumentType == Integer.class)
    {
      tranferValue = Integer.parseInt((String)value);
    } else if (argumentType == Float.class)
    {
      tranferValue = Float.parseFloat((String)value);
    } else if (argumentType == Boolean.class)
    {
      tranferValue = Boolean.parseBoolean((String)value);
    } else if (argumentType == Double.class)
    {
      tranferValue = Double.parseDouble((String) value);
    } else if (argumentType == Short.class)
    {
      tranferValue = Short.parseShort((String)value);
    } else if (argumentType == Long.class)
    {
      tranferValue = Long.parseLong((String)value);
    } else if (argumentType == String.class)
    {
      tranferValue = value;
    }
    Object[] aobjPD = { tranferValue };

    objMethod.invoke(target, aobjPD);

  }


  /**
   * Get the value of property
   *
   * @param target Object
   * @param property String
   * @return Object
   * @throws Exception
   */
  public static Object getProperty(Object target, String property) throws Exception
  {
    if (target == null || property == null){
    	 return null;
    }
    String strFunction = GET + property.substring(0, 1).toUpperCase() + property.substring(1);
    Method objMethod = target.getClass().getMethod(strFunction);
    objMethod.setAccessible(true);

    return objMethod.invoke(target);
  }

  /**
   *
   * Copy property,only String - > proptype
   * @param desc Object
   * @param src Object
   * @throws Exception
   */
  public static void copyProperty(Object desc, Object src) throws Exception{
    Field[] arrField = getDeclaredFields(desc.getClass());
    for (Field field : arrField){

        setProperty(desc, field.getName(), getProperty(src, field.getName()));

    }
  }

}
