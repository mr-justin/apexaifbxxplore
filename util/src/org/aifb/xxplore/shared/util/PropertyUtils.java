package org.aifb.xxplore.shared.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.aifb.xxplore.shared.exception.Emergency;

public class PropertyUtils {
	// ~ Class fields ----------------------------------------------------------------------------------------------------------

	/**
	 * Global Map: Class to property name array (String[])
	 */
	private static final Map classToPropertiesMap = new HashMap();

	/**
	 * Global Map: 'Class name'.'Property name' (String) to property type (Class)
	 */
	private static final Map propertyToTypeMap = new HashMap();

	// ~ Methods ---------------------------------------------------------------------------------------------------------------

	/**
	 * Creates and initializes a new Property instance from the given file.
	 *
	 * @param propertyFile path to property file
	 * @return the created property object, not null
	 * @throws com.dpwn.newops.shared.exception.EmergencyException
	 *          if file was not found
	 */
	public static Properties readFromPropertiesFile(String propertyFile) {
		return readFromPropertiesFile(propertyFile, false);
	}

	/**
	 * @param propertiesFile system dependend path of the property file
	 * @param useLogger      false to throw emergency exception if propertiesFile was not found
	 *                       true to log an info message and return null
	 * @return if file was found always returns the new property object,
	 *         else if useLogger is true an info log will be created and null will be returned.
	 *         else if useLogger is false an emergency exception will be thrown
	 */
	public static Properties readFromPropertiesFile(String propertiesFile, boolean useLogger) {
		Properties properties = new Properties();
		try {
			FileInputStream stream = new FileInputStream(propertiesFile);
			properties.load(stream);
			stream.close();
		}
		catch (IOException e) {
			File f = new File(propertiesFile);
			String message = "Unable to read properties from file '" + f.getAbsolutePath() + "'.";
			if (!useLogger) {
				Emergency.now(message, e);
			}
		}
		return properties;
	}

	/**
	 * Creates a property object by using path relativ to classloader.
	 *
	 * @param classLoader    class loader the propertiesFile path is relative on.
	 * @param propertiesFile path relativ to the base directory of the given classLoader
	 * @return the loaded properties or null
	 */
	public static Properties readFromPropertiesFile(ClassLoader classLoader, String propertiesFile) {
		Properties properties = new Properties();
		InputStream inputStream = classLoader.getResourceAsStream(propertiesFile);
		URL url = classLoader.getResource(propertiesFile);
		if (inputStream != null) {
			try {
				properties.load(inputStream);
			}
			catch (IOException e) {
			}
			finally {
				closeInputStream(inputStream);
			}
		}
		else {
		}
		closeInputStream(inputStream);
		if (properties.isEmpty()) {
		}
		return properties;
	}

	/**
	 * Returns the absolute path for the given filenName.
	 *
	 * @param fileName
	 * @return the absolute path, not null
	 */
	public static String getFileAbsolutePath(String fileName) {

		File f = new File(fileName);
		return f.getAbsolutePath();
	}

	/**
	 * Create an instance of the class with the given className
	 *
	 * @param className
	 * @return the new instance, not null
	 */
	public static Object createInstance(String className) {
		Class classObject = null;
		try {
			classObject = Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			Emergency.now("Invalid class name '" + className + "'.", e);
		}
		return createInstance(classObject);
	}

	/**
	 * Creates a new instance of the given classObject
	 *
	 * @param classObject
	 * @return the new instance, not null
	 */
	public static Object createInstance(Class classObject) {
		Object instance = null;
		try {
			instance = classObject.newInstance();
		}
		catch (Exception e) {
			Emergency.now("Error creating an instance of class '" + classObject.getName() + "'.", e);
		}
		return instance;
	}

	/**
	 * Returns the unqualified class name of the specified class. The unqualified name is the last part of the name, behind the
	 * last dot.
	 *
	 * @param classObject The class which unqualified name is to be returned
	 * @return The unqualified class name of the specified class
	 */
	public static String getUnqualifiedClassName(Class classObject) {
		String name = classObject.getName();
		int lastDotIndex = name.lastIndexOf('.');
		if (lastDotIndex >= 0) {
			name = name.substring(lastDotIndex + 1);
		}
		return name;
	}

	/**
	 * Returns a String representation for the specified bean. Pattern: [Unqualified class name]([property 1]: [value 1], ...,
	 * [property n]: [value n])
	 *
	 * @param bean The bean which representation is requested
	 * @return A String representation for the specified bean
	 */
	public static String toString(Object bean) {
		Class beanClass = bean.getClass();
		String className = getUnqualifiedClassName(beanClass);

		StringBuffer buffer = new StringBuffer();
		buffer.append(className);
		buffer.append("(");
		String[] properties = getPropertyNames(beanClass);
		for (int i = 0; i < properties.length; i++) {
			Object propertyValue = getPropertyValue(bean, properties[i]);
			if (i > 0) {
				buffer.append(", ");
			}
			buffer.append(properties[i]);
			buffer.append(": ");
			buffer.append(propertyValue);
		}
		buffer.append(")");
		return buffer.toString();
	}

	/**
	 * Returns the value of the property with the specified name from the specified bean.
	 *
	 * @param bean         The bean which property value is requested
	 * @param propertyName Name of a property of the bean
	 * @return The value of the property with the specified name from the specified bean
	 */
	public static Object getPropertyValue(Object bean, String propertyName) {
		Object value = null;

		Class beanClass = bean.getClass();
		try {
			Method getterMethod = getGetterMethod(beanClass, propertyName);
			value = getterMethod.invoke(bean, new Object[]{});
		}
		catch (Exception e) {
			Emergency.now("Unable to get value of property '" + propertyName + "' of " + beanClass + ".", e);
		}

		return value;
	}

	/**
	 * Sets the value of the property with the specified name for the specified bean.
	 *
	 * @param bean         The bean which property value is to be set
	 * @param propertyName Name of a property of this class
	 * @param value        The new value to be set
	 */
	public static void setPropertyValue(Object bean, String propertyName, Object value) {
		Class beanClass = bean.getClass();
		try {
			Method setterMethod = getSetterMethod(beanClass, propertyName);
			setterMethod.invoke(bean, new Object[]{value});
		}
		catch (Exception e) {
			Emergency.now("Unable to set value of property '" + propertyName + "' of " + beanClass + " to '" + value + "'.", e);
		}
	}

	/**
	 * Returns an array of all property names of the specified class.
	 *
	 * @param beanClass The class which property names are requested
	 * @return An array of all property names of the specified class
	 */
	public static String[] getPropertyNames(Class beanClass) {
		String[] properties = (String[]) classToPropertiesMap.get(beanClass);
		if (properties == null) {
			properties = createPropertyNames(beanClass);
			synchronized (PropertyUtils.class) {
				classToPropertiesMap.put(beanClass, properties);
			}
		}
		return properties;
	}

	/**
	 * Returns the type of the property of the specified class and name.
	 *
	 * @param beanClass    The class which property type is requested
	 * @param propertyName Name of a property of the specified class
	 * @return The type of the property of the specified class and name
	 */
	public static Class getPropertyType(Class beanClass, String propertyName) {
		String key = beanClass.getName() + "." + propertyName;
		Class type = (Class) propertyToTypeMap.get(key);
		if (type == null) {
			type = getGetterMethod(beanClass, propertyName).getReturnType();
			synchronized (PropertyUtils.class) {
				propertyToTypeMap.put(key, type);
			}
		}
		return type;
	}

	/**
	 * Returns the Getter method for the specified property of the specified class.
	 *
	 * @param beanClass    The class which method is requested
	 * @param propertyName Name of a property of the specified class
	 * @return the Getter method for the specified property of the specified class
	 */
	public static Method getGetterMethod(Class beanClass, String propertyName) {
		String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		Method getterMethod = null;
		try {
			getterMethod = beanClass.getMethod(getterName, new Class[]{});
		}
		catch (NoSuchMethodException e) {
			Emergency.now("No getter method found for property '" + propertyName + "' of " + beanClass + ".", e);
		}
		return getterMethod;
	}

	/**
	 * Returns the Setter method for the specified property of the specified class.
	 *
	 * @param beanClass    The class which method is requested
	 * @param propertyName Name of a property of the specified class
	 * @return the Setter method for the specified property of the specified class
	 */
	public static Method getSetterMethod(Class beanClass, String propertyName) {
		String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		Class propertyType = getPropertyType(beanClass, propertyName);
		Method setterMethod = null;
		try {
			setterMethod = beanClass.getMethod(setterName, new Class[]{propertyType});
		}
		catch (NoSuchMethodException e) {
			Emergency.now("No setter method found for property '" + propertyName + "' of " + beanClass + ".", e);
		}
		return setterMethod;
	}

	// ***** Private methods *****

	private static String[] createPropertyNames(Class beanClass) {
		Set propertyNameSet = new TreeSet();
		Method[] methods = beanClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getName();
			int parameterCount = methods[i].getParameterTypes().length;
			if (methodName.startsWith("get") && (!methodName.equals("getClass")) && (parameterCount == 0)) {
				String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
				propertyNameSet.add(propertyName);
			}
		}
		return (String[]) propertyNameSet.toArray(new String[propertyNameSet.size()]);
	}

	private static InputStream closeInputStream(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
				inputStream = null;
			}
			catch (IOException e) {
				Emergency.now("Failed InpurStream close", e);
			}
		}
		return inputStream;
	}
	
	
	public static Map<String, Object> convertToMap(Properties props){
		Map<String, Object> map = new HashMap<String, Object>();
		Set<Object> keys = props.keySet();

		for (Object key : keys){
			map.put((String)key, props.get(key));
		}
		return map;
	}
}