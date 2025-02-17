package com.easynet.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONPointerException;
import org.json.JSONString;
import com.easynet.util.common;

/**
 *This class extends JSONObjectImpl class and all method are same as json object * 
 * */
public class JSONObjectImpl extends JSONObject{
	/**
	 * JSONObjectImpl.NULL is equivalent to the value that JavaScript calls null,
	 * whilst Java's null is equivalent to the value that JavaScript calls
	 * undefined.
	 */
	private static final class Null {

		/**
		 * There is only intended to be a single instance of the NULL object,
		 * so the clone method returns itself.
		 *
		 * @return NULL.
		 */
		@Override
		protected final Object clone() {
			return this;
		}

		/**
		 * A Null object is equal to the null value and to itself.
		 *
		 * @param object
		 *            An object to test for nullness.
		 * @return true if the object parameter is the JSONObjectImpl.NULL object or
		 *         null.
		 */
		@Override
		public boolean equals(Object object) {
			return object == null || object == this;
		}
		/**
		 * A Null object is equal to the null value and to itself.
		 *
		 * @return always returns 0.
		 */
		@Override
		public int hashCode() {
			return 0;
		}

		/**
		 * Get the "null" string value.
		 *
		 * @return The string "null".
		 */
		@Override
		public String toString() {
			return "null";
		}
	}

	/**
	 * The map where the JSONObjectImpl's properties are kept.
	 */
	private final Map<String, Object> map;

	/**
	 * It is sometimes more convenient and less ambiguous to have a
	 * <code>NULL</code> object than to use Java's <code>null</code> value.
	 * <code>JSONObjectImpl.NULL.equals(null)</code> returns <code>true</code>.
	 * <code>JSONObjectImpl.NULL.toString()</code> returns <code>"null"</code>.
	 */
	public static final Object NULL = new Null();

	public JSONObjectImpl(JSONObject bean) {		
		this(bean.toString());
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct an empty JSONObjectImpl.
	 */
	public JSONObjectImpl() {
		// HashMap is used on purpose to ensure that elements are unordered by 
		// the specification.
		// JSON tends to be a portable transfer format to allows the container 
		// implementations to rearrange their items for a faster element 
		// retrieval based on associative access.
		// Therefore, an implementation mustn't rely on the order of the item.
		this.map = new HashMap<String, Object>();
	}

	/**
	 * Construct a JSONObjectImpl from a subset of another JSONObjectImpl. An array of
	 * strings is used to identify the keys that should be copied. Missing keys
	 * are ignored.
	 *
	 * @param jo
	 *            A JSONObjectImpl.
	 * @param names
	 *            An array of strings.
	 */
	public JSONObjectImpl(JSONObjectImpl jo, String[] names) {
		this(names.length);
		for (int i = 0; i < names.length; i += 1) {
			try {
				this.putOnce(names[i], jo.opt(names[i]));
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * Construct a JSONObjectImpl from a JSONTokenerImpl.
	 *
	 * @param x
	 *            A JSONTokenerImpl object containing the source string.
	 * @throws JSONException
	 *             If there is a syntax error in the source string or a
	 *             duplicated key.
	 */
	public JSONObjectImpl(JSONTokenerImpl x) throws JSONException {
		this();
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObjectImpl text must begin with '{'");
		}
		for (;;) {
			c = x.nextClean();
			switch (c) {
			case 0:
				throw x.syntaxError("A JSONObjectImpl text must end with '}'");
			case '}':
				return;
			default:
				x.back();
				key = x.nextValue().toString();
			}

			// The key is followed by ':'.

			c = x.nextClean();
			if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}

			// Use syntaxError(..) to include error location

			if (key != null) {
				// Check if key exists
				if (this.opt(key) != null) {
					// key already exists
					throw x.syntaxError("Duplicate key \"" + key + "\"");
				}
				// Only add value if non-null
				Object value = x.nextValue();
				if (value!=null) {
					this.put(key, value);
				}
			}

			// Pairs are separated by ','.

			switch (x.nextClean()) {
			case ';':
			case ',':
				if (x.nextClean() == '}') {
					return;
				}
				x.back();
				break;
			case '}':
				return;
			default:
				throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Construct a JSONObjectImpl from a Map.
	 *
	 * @param m
	 *            A map object that can be used to initialize the contents of
	 *            the JSONObjectImpl.
	 */
	public JSONObjectImpl(Map<?, ?> m) {
		if (m == null) {
			this.map = new HashMap<String, Object>();
		} else {
			this.map = new HashMap<String, Object>(m.size());
			for (final Entry<?, ?> e : m.entrySet()) {
				final Object value = e.getValue();
				if (value != null) {
					this.map.put(String.valueOf(e.getKey()), wrap(value));
				}
			}
		}
	}

	/**
	 * Construct a JSONObjectImpl from an Object using bean getters. It reflects on
	 * all of the public methods of the object. For each of the methods with no
	 * parameters and a name starting with <code>"get"</code> or
	 * <code>"is"</code> followed by an uppercase letter, the method is invoked,
	 * and a key and the value returned from the getter method are put into the
	 * new JSONObjectImpl.
	 * <p>
	 * The key is formed by removing the <code>"get"</code> or <code>"is"</code>
	 * prefix. If the second remaining character is not upper case, then the
	 * first character is converted to lower case.
	 * <p>
	 * For example, if an object has a method named <code>"getName"</code>, and
	 * if the result of calling <code>object.getName()</code> is
	 * <code>"Larry Fine"</code>, then the JSONObjectImpl will contain
	 * <code>"name": "Larry Fine"</code>.
	 * <p>
	 * Methods that return <code>void</code> as well as <code>static</code>
	 * methods are ignored.
	 * 
	 * @param bean
	 *            An object that has getter methods that should be used to make
	 *            a JSONObjectImpl.
	 */
	public JSONObjectImpl(Object bean) {
		this();
		this.populateMap(bean);
	}

	/**
	 * Construct a JSONObjectImpl from an Object, using reflection to find the
	 * public members. The resulting JSONObjectImpl's keys will be the strings from
	 * the names array, and the values will be the field values associated with
	 * those keys in the object. If a key is not found or not visible, then it
	 * will not be copied into the new JSONObjectImpl.
	 *
	 * @param object
	 *            An object that has fields that should be used to make a
	 *            JSONObjectImpl.
	 * @param names
	 *            An array of strings, the names of the fields to be obtained
	 *            from the object.
	 */
	public JSONObjectImpl(Object object, String names[]) {
		this(names.length);
		Class<?> c = object.getClass();
		for (int i = 0; i < names.length; i += 1) {
			String name = names[i];
			try {
				this.putOpt(name, c.getField(name).get(object));
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * Construct a JSONObjectImpl from a source JSON text string. This is the most
	 * commonly used JSONObjectImpl constructor.
	 *
	 * @param source
	 *            A string beginning with <code>{</code>&nbsp;<small>(left
	 *            brace)</small> and ending with <code>}</code>
	 *            &nbsp;<small>(right brace)</small>.
	 * @exception JSONException
	 *                If there is a syntax error in the source string or a
	 *                duplicated key.
	 */
	public JSONObjectImpl(String source) throws JSONException {
		this(new JSONTokenerImpl(source));
	}

	/**
	 * Construct a JSONObjectImpl from a ResourceBundle.
	 *
	 * @param baseName
	 *            The ResourceBundle base name.
	 * @param locale
	 *            The Locale to load the ResourceBundle for.
	 * @throws JSONException
	 *             If any JSONExceptions are detected.
	 */
	public JSONObjectImpl(String baseName, Locale locale) throws JSONException {
		this();
		ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
				Thread.currentThread().getContextClassLoader());

		// Iterate through the keys in the bundle.

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (key != null) {

				// Go through the path, ensuring that there is a nested JSONObjectImpl for each
				// segment except the last. Add the value using the last segment's name into
				// the deepest nested JSONObjectImpl.

				String[] path = ((String) key).split("\\.");
				int last = path.length - 1;
				JSONObjectImpl target = this;
				for (int i = 0; i < last; i += 1) {
					String segment = path[i];
					JSONObjectImpl nextTarget = target.optJSONObject(segment);
					if (nextTarget == null) {
						nextTarget = new JSONObjectImpl();
						target.put(segment, nextTarget);
					}
					target = nextTarget;
				}
				target.put(path[last], bundle.getString((String) key));
			}
		}
	}

	/**
	 * Constructor to specify an initial capacity of the internal map. Useful for library 
	 * internal calls where we know, or at least can best guess, how big this JSONObjectImpl
	 * will be.
	 * 
	 * @param initialCapacity initial capacity of the internal map.
	 */
	protected JSONObjectImpl(int initialCapacity){
		this.map = new HashMap<String, Object>(initialCapacity);
	}



	/**
	 *This method is used for set null object values into json object
	 *If object is null then this method set "" as value. 
	 *
	 * Put a key/value pair in the JSONObjectImpl. If the value is null, then the
	 * key will be removed from the JSONObjectImpl if it is present.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JSONArrayImpl, JSONObjectImpl, Long,
	 *            String, or the JSONObjectImpl.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is non-finite number or if the key is null.
	 */
	@Override
	public JSONObjectImpl put(String key, Object value) throws JSONException {

		if (key == null) {
			throw new NullPointerException("Null key.");
		}

		//if null value in JSON object then set null string or empty string
		if(value==null|| "null".equals(value)){
			value="";
		}

		//for format the decimal values.
		if(value instanceof Double){
			double le_DoubleValue=(double)value;
			
			value=common.getDecFormatValueStr(le_DoubleValue);				
		}

		//convert data into string
		if(value instanceof String == false 
				&& value instanceof JSONObjectImpl== false 
				&& value instanceof JSONArrayImpl ==false
				&& value instanceof JSONObject== false 
				&& value instanceof JSONArray ==false
				&& value instanceof Boolean== false){
			
			value=String.valueOf(value);
		}

		//super class code..
		if (value != null) {
			testValidity(value);
			this.map.put(key, value);
		} else {
			this.remove(key);
		}
		return this;
	}


	/**
	 * This method is used for return value in string if null then set as empty String.
	 * Get the string associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 * @throws JSONException
	 *             if there is no string value for the key.
	 */
	@Override
	public String getString(String name) throws JSONException {
		Object data;

		data=this.get(name);

		String dataStr="";

		if(data==null || "null".equals(data.toString())){			
			return "";
		}else{
			dataStr=String.valueOf(data);	
			return dataStr;
		}					
	}


	//	public String getString(String key) throws JSONException {
	//		Object object = this.get(key);
	//		if (object instanceof String) {
	//			return (String) object;
	//		}
	//		throw new JSONException("JSONObjectImpl[" + quote(key) + "] not a string.");
	//	}
	//	

	@Override
	public int getInt(String key) throws JSONException {
		Object data;

		data=this.get(key);		

		if(data==null || "null".equals(data.toString())){			
			return 0;

		}else{
			return data instanceof Number ? ((Number) data).intValue(): Integer.parseInt((String) data);				
		}	

	}

	/**
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a JSONArrayImpl
	 * is stored under the key to hold all of the accumulated values. If there
	 * is already a JSONArrayImpl, then the new value is appended to it. In
	 * contrast, the put method replaces the previous value.
	 *
	 * If only one value is accumulated that is not a JSONArrayImpl, then the result
	 * will be the same as using put. But if multiple values are accumulated,
	 * then the result will be like append.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the value is an invalid number or if the key is null.
	 */
	public JSONObjectImpl accumulate(String key, Object value) throws JSONException {
		testValidity(value);
		Object object = this.opt(key);
		if (object == null) {
			this.put(key,
					value instanceof JSONArrayImpl ? new JSONArrayImpl().put(value)
							: value);
		} else if (object instanceof JSONArrayImpl) {
			((JSONArrayImpl) object).put(value);
		} else {
			this.put(key, new JSONArrayImpl().put(object).put(value));
		}
		return this;
	}

	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObjectImpl, then the key is put in the JSONObjectImpl with its value being a
	 * JSONArrayImpl containing the value parameter. If the key was already
	 * associated with a JSONArrayImpl, then the value parameter is appended to it.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the current value associated with
	 *             the key is not a JSONArrayImpl.
	 */
	public JSONObjectImpl append(String key, Object value) throws JSONException {
		testValidity(value);
		Object object = this.opt(key);
		if (object == null) {
			this.put(key, new JSONArrayImpl().put(value));
		} else if (object instanceof JSONArrayImpl) {
			this.put(key, ((JSONArrayImpl) object).put(value));
		} else {
			throw new JSONException("JSONObjectImpl[" + key
					+ "] is not a JSONArrayImpl.");
		}
		return this;
	}

	/**
	 * Produce a string from a double. The string "null" will be returned if the
	 * number is not finite.
	 *
	 * @param d
	 *            A double.
	 * @return A String.
	 */
	public static String doubleToString(double d) {
		if (Double.isInfinite(d) || Double.isNaN(d)) {
			return "null";
		}

		// Shave off trailing zeros and decimal point, if possible.

		String string = Double.toString(d);
		if (string.indexOf('.') > 0 && string.indexOf('e') < 0
				&& string.indexOf('E') < 0) {
			while (string.endsWith("0")) {
				string = string.substring(0, string.length() - 1);
			}
			if (string.endsWith(".")) {
				string = string.substring(0, string.length() - 1);
			}
		}
		return string;
	}

	/**
	 * Get the value object associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The object associated with the key.
	 * @throws JSONException
	 *             if the key is not found.
	 */
	public Object get(String key) throws JSONException {
		if (key == null) {
			throw new JSONException("Null key.");
		}
		Object object = this.opt(key);
		if (object == null) {
			throw new JSONException("JSONObjectImpl[" + quote(key) + "] not found.");
		}
		return object;
	}

	/**
	 * Get the enum value associated with a key.
	 * 
	 * @param clazz
	 *           The type of enum to retrieve.
	 * @param key
	 *           A key string.
	 * @return The enum value associated with the key
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be converted
	 *             to an enum.
	 */
	public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
		E val = optEnum(clazz, key);
		if(val==null) {
			// JSONException should really take a throwable argument.
			// If it did, I would re-implement this with the Enum.valueOf
			// method and place any thrown exception in the JSONException
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] is not an enum of type " + quote(clazz.getSimpleName())
			+ ".");
		}
		return val;
	}

	/**
	 * Get the boolean value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The truth.
	 * @throws JSONException
	 *             if the value is not a Boolean or the String "true" or
	 *             "false".
	 */
	public boolean getBoolean(String key) throws JSONException {
		Object object = this.get(key);
		if (object.equals(Boolean.FALSE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("false"))) {
			return false;
		} else if (object.equals(Boolean.TRUE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONObjectImpl[" + quote(key)
		+ "] is not a Boolean.");
	}

	/**
	 * Get the BigInteger value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value cannot 
	 *             be converted to BigInteger.
	 */
	public BigInteger getBigInteger(String key) throws JSONException {
		Object object = this.get(key);
		try {
			return new BigInteger(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] could not be converted to BigInteger.", e);
		}
	}

	/**
	 * Get the BigDecimal value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value
	 *             cannot be converted to BigDecimal.
	 */
	public BigDecimal getBigDecimal(String key) throws JSONException {
		Object object = this.get(key);
		if (object instanceof BigDecimal) {
			return (BigDecimal)object;
		}
		try {
			return new BigDecimal(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] could not be converted to BigDecimal.", e);
		}
	}

	/**
	 * Get the double value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a Number
	 *             object and cannot be converted to a number.
	 */
	public double getDouble(String key) throws JSONException {
		Object object = this.get(key);
		try {
			return object instanceof Number ? ((Number) object).doubleValue()
					: Double.parseDouble(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] is not a number.", e);
		}
	}

	/**
	 * Get the float value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a Number
	 *             object and cannot be converted to a number.
	 */
	public float getFloat(String key) throws JSONException {
		Object object = this.get(key);
		try {
			return object instanceof Number ? ((Number) object).floatValue()
					: Float.parseFloat(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] is not a number.", e);
		}
	}

	/**
	 * Get the Number value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a Number
	 *             object and cannot be converted to a number.
	 */
	public Number getNumber(String key) throws JSONException {
		Object object = this.get(key);
		try {
			if (object instanceof Number) {
				return (Number)object;
			}
			return stringToNumber(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] is not a number.", e);
		}
	}

	/**
	 * Get the int value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The integer value.
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be converted
	 *             to an integer.
	 */
	//	public int getInt(String key) throws JSONException {
	//		Object object = this.get(key);
	//		try {
	//			return object instanceof Number ? ((Number) object).intValue()
	//					: Integer.parseInt((String) object);
	//		} catch (Exception e) {
	//			throw new JSONException("JSONObjectImpl[" + quote(key)
	//			+ "] is not an int.", e);
	//		}
	//	}

	/**
	 * Get the JSONArrayImpl value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return A JSONArrayImpl which is the value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a JSONArrayImpl.
	 */
	public JSONArrayImpl getJSONArray(String key) throws JSONException {
		Object object = this.get(key);
		if (object instanceof JSONArrayImpl) {
			return (JSONArrayImpl) object;
		}
		throw new JSONException("JSONObjectImpl[" + quote(key)
		+ "] is not a JSONArrayImpl.");
	}

	/**
	 * Get the JSONObjectImpl value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return A JSONObjectImpl which is the value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a JSONObjectImpl.
	 */
	public JSONObjectImpl getJSONObject(String key) throws JSONException {
		Object object = this.get(key);
		if (object instanceof JSONObjectImpl) {
			return (JSONObjectImpl) object;
		}
		throw new JSONException("JSONObjectImpl[" + quote(key)
		+ "] is not a JSONObjectImpl.");
	}

	/**
	 * Get the long value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return The long value.
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be converted
	 *             to a long.
	 */
	public long getLong(String key) throws JSONException {
		Object object = this.get(key);
		try {
			return object instanceof Number ? ((Number) object).longValue()
					: Long.parseLong((String) object);
		} catch (Exception e) {
			throw new JSONException("JSONObjectImpl[" + quote(key)
			+ "] is not a long.", e);
		}
	}

	/**
	 * Get an array of field names from a JSONObjectImpl.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(JSONObjectImpl jo) {
		int length = jo.length();
		if (length == 0) {
			return null;
		}
		return jo.keySet().toArray(new String[length]);
	}

	/**
	 * Get an array of field names from an Object.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(Object object) {
		if (object == null) {
			return null;
		}
		Class<?> klass = object.getClass();
		Field[] fields = klass.getFields();
		int length = fields.length;
		if (length == 0) {
			return null;
		}
		String[] names = new String[length];
		for (int i = 0; i < length; i += 1) {
			names[i] = fields[i].getName();
		}
		return names;
	}



	/**
	 * Determine if the JSONObjectImpl contains a specific key.
	 *
	 * @param key
	 *            A key string.
	 * @return true if the key exists in the JSONObjectImpl.
	 */
	public boolean has(String key) {
		return this.map.containsKey(key);
	}

	/**
	 * Increment a property of a JSONObjectImpl. If there is no such property,
	 * create one with a value of 1. If there is such a property, and if it is
	 * an Integer, Long, Double, or Float, then add one to it.
	 *
	 * @param key
	 *            A key string.
	 * @return this.
	 * @throws JSONException
	 *             If there is already a property with this name that is not an
	 *             Integer, Long, Double, or Float.
	 */
	public JSONObjectImpl increment(String key) throws JSONException {
		Object value = this.opt(key);
		if (value == null) {
			this.put(key, 1);
		} else if (value instanceof BigInteger) {
			this.put(key, ((BigInteger)value).add(BigInteger.ONE));
		} else if (value instanceof BigDecimal) {
			this.put(key, ((BigDecimal)value).add(BigDecimal.ONE));
		} else if (value instanceof Integer) {
			this.put(key, ((Integer) value).intValue() + 1);
		} else if (value instanceof Long) {
			this.put(key, ((Long) value).longValue() + 1L);
		} else if (value instanceof Double) {
			this.put(key, ((Double) value).doubleValue() + 1.0d);
		} else if (value instanceof Float) {
			this.put(key, ((Float) value).floatValue() + 1.0f);
		} else {
			throw new JSONException("Unable to increment [" + quote(key) + "].");
		}
		return this;
	}

	/**
	 * Determine if the value associated with the key is null or if there is no
	 * value.
	 *
	 * @param key
	 *            A key string.
	 * @return true if there is no value associated with the key or if the value
	 *         is the JSONObjectImpl.NULL object.
	 */
	public boolean isNull(String key) {
		return JSONObjectImpl.NULL.equals(this.opt(key));
	}

	/**
	 * Get an enumeration of the keys of the JSONObjectImpl. Modifying this key Set will also
	 * modify the JSONObjectImpl. Use with caution.
	 *
	 * @see Set#iterator()
	 * 
	 * @return An iterator of the keys.
	 */
	public Iterator<String> keys() {
		return this.keySet().iterator();
	}

	/**
	 * Get a set of keys of the JSONObjectImpl. Modifying this key Set will also modify the
	 * JSONObjectImpl. Use with caution.
	 *
	 * @see Map#keySet()
	 *
	 * @return A keySet.
	 */
	public Set<String> keySet() {
		return this.map.keySet();
	}

	/**
	 * Get a set of entries of the JSONObjectImpl. These are raw values and may not
	 * match what is returned by the JSONObjectImpl get* and opt* functions. Modifying 
	 * the returned EntrySet or the Entry objects contained therein will modify the
	 * backing JSONObjectImpl. This does not return a clone or a read-only view.
	 * 
	 * Use with caution.
	 *
	 * @see Map#entrySet()
	 *
	 * @return An Entry Set
	 */
	protected Set<Entry<String, Object>> entrySet() {
		return this.map.entrySet();
	}

	/**
	 * Get the number of keys stored in the JSONObjectImpl.
	 *
	 * @return The number of keys in the JSONObjectImpl.
	 */
	public int length() {
		return this.map.size();
	}

	/**
	 * Produce a JSONArrayImpl containing the names of the elements of this
	 * JSONObjectImpl.
	 *
	 * @return A JSONArrayImpl containing the key strings, or null if the JSONObjectImpl
	 *         is empty.
	 */
	public JSONArrayImpl names() {
		if(this.map.isEmpty()) {
			return null;
		}
		return new JSONArrayImpl(this.map.keySet());
	}

	/**
	 * Produce a string from a Number.
	 *
	 * @param number
	 *            A Number
	 * @return A String.
	 * @throws JSONException
	 *             If n is a non-finite number.
	 */
	public static String numberToString(Number number) throws JSONException {
		if (number == null) {
			throw new JSONException("Null pointer");
		}
		testValidity(number);

		// Shave off trailing zeros and decimal point, if possible.

		String string = number.toString();
		if (string.indexOf('.') > 0 && string.indexOf('e') < 0
				&& string.indexOf('E') < 0) {
			while (string.endsWith("0")) {
				string = string.substring(0, string.length() - 1);
			}
			if (string.endsWith(".")) {
				string = string.substring(0, string.length() - 1);
			}
		}
		return string;
	}

	/**
	 * Get an optional value associated with a key.
	 *
	 * @param key
	 *            A key string.
	 * @return An object which is the value, or null if there is no value.
	 */
	public Object opt(String key) {
		return key == null ? null : this.map.get(key);
	}

	/**
	 * Get the enum value associated with a key.
	 * 
	 * @param clazz
	 *            The type of enum to retrieve.
	 * @param key
	 *            A key string.
	 * @return The enum value associated with the key or null if not found
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
		return this.optEnum(clazz, key, null);
	}

	/**
	 * Get the enum value associated with a key.
	 * 
	 * @param clazz
	 *            The type of enum to retrieve.
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default in case the value is not found
	 * @return The enum value associated with the key or defaultValue
	 *            if the value is not found or cannot be assigned to <code>clazz</code>
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
		try {
			Object val = this.opt(key);
			if (NULL.equals(val)) {
				return defaultValue;
			}
			if (clazz.isAssignableFrom(val.getClass())) {
				// we just checked it!
				@SuppressWarnings("unchecked")
				E myE = (E) val;
				return myE;
			}
			return Enum.valueOf(clazz, val.toString());
		} catch (IllegalArgumentException e) {
			return defaultValue;
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional boolean associated with a key. It returns false if there
	 * is no such key, or if the value is not Boolean.TRUE or the String "true".
	 *
	 * @param key
	 *            A key string.
	 * @return The truth.
	 */
	public boolean optBoolean(String key) {
		return this.optBoolean(key, false);
	}

	/**
	 * Get an optional boolean associated with a key. It returns the
	 * defaultValue if there is no such key, or if it is not a Boolean or the
	 * String "true" or "false" (case insensitive).
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Boolean){
			return ((Boolean) val).booleanValue();
		}
		try {
			// we'll use the get anyway because it does string conversion.
			return this.getBoolean(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional BigDecimal associated with a key, or the defaultValue if
	 * there is no such key or if its value is not a number. If the value is a
	 * string, an attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof BigDecimal){
			return (BigDecimal) val;
		}
		if (val instanceof BigInteger){
			return new BigDecimal((BigInteger) val);
		}
		if (val instanceof Double || val instanceof Float){
			return new BigDecimal(((Number) val).doubleValue());
		}
		if (val instanceof Long || val instanceof Integer
				|| val instanceof Short || val instanceof Byte){
			return new BigDecimal(((Number) val).longValue());
		}
		// don't check if it's a string in case of unchecked Number subclasses
		try {
			return new BigDecimal(val.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional BigInteger associated with a key, or the defaultValue if
	 * there is no such key or if its value is not a number. If the value is a
	 * string, an attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public BigInteger optBigInteger(String key, BigInteger defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof BigInteger){
			return (BigInteger) val;
		}
		if (val instanceof BigDecimal){
			return ((BigDecimal) val).toBigInteger();
		}
		if (val instanceof Double || val instanceof Float){
			return new BigDecimal(((Number) val).doubleValue()).toBigInteger();
		}
		if (val instanceof Long || val instanceof Integer
				|| val instanceof Short || val instanceof Byte){
			return BigInteger.valueOf(((Number) val).longValue());
		}
		// don't check if it's a string in case of unchecked Number subclasses
		try {
			// the other opt functions handle implicit conversions, i.e. 
			// jo.put("double",1.1d);
			// jo.optInt("double"); -- will return 1, not an error
			// this conversion to BigDecimal then to BigInteger is to maintain
			// that type cast support that may truncate the decimal.
			final String valStr = val.toString();
			if(isDecimalNotation(valStr)) {
				return new BigDecimal(valStr).toBigInteger();
			}
			return new BigInteger(valStr);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional double associated with a key, or NaN if there is no such
	 * key or if its value is not a number. If the value is a string, an attempt
	 * will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A string which is the key.
	 * @return An object which is the value.
	 */
	public double optDouble(String key) {
		return this.optDouble(key, Double.NaN);
	}

	/**
	 * Get an optional double associated with a key, or the defaultValue if
	 * there is no such key or if its value is not a number. If the value is a
	 * string, an attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public double optDouble(String key, double defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Number){
			return ((Number) val).doubleValue();
		}
		if (val instanceof String) {
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get the optional double value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param key
	 *            A key string.
	 * @return The value.
	 */
	public float optFloat(String key) {
		return this.optFloat(key, Float.NaN);
	}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public float optFloat(String key, float defaultValue) {
		Object val = this.opt(key);
		if (JSONObjectImpl.NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Number){
			return ((Number) val).floatValue();
		}
		if (val instanceof String) {
			try {
				return Float.parseFloat((String) val);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get an optional int value associated with a key, or zero if there is no
	 * such key or if the value is not a number. If the value is a string, an
	 * attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public int optInt(String key) {
		return this.optInt(key, 0);
	}

	/**
	 * Get an optional int value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public int optInt(String key, int defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Number){
			return ((Number) val).intValue();
		}

		if (val instanceof String) {
			try {
				return new BigDecimal((String) val).intValue();
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get an optional JSONArrayImpl associated with a key. It returns null if there
	 * is no such key, or if its value is not a JSONArrayImpl.
	 *
	 * @param key
	 *            A key string.
	 * @return A JSONArrayImpl which is the value.
	 */
	public JSONArrayImpl optJSONArray(String key) {
		Object o = this.opt(key);
		return o instanceof JSONArrayImpl ? (JSONArrayImpl) o : null;
	}

	/**
	 * Get an optional JSONObjectImpl associated with a key. It returns null if
	 * there is no such key, or if its value is not a JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @return A JSONObjectImpl which is the value.
	 */
	public JSONObjectImpl optJSONObject(String key) {
		Object object = this.opt(key);
		return object instanceof JSONObjectImpl ? (JSONObjectImpl) object : null;
	}

	/**
	 * Get an optional long value associated with a key, or zero if there is no
	 * such key or if the value is not a number. If the value is a string, an
	 * attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public long optLong(String key) {
		return this.optLong(key, 0);
	}

	/**
	 * Get an optional long value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public long optLong(String key, long defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Number){
			return ((Number) val).longValue();
		}

		if (val instanceof String) {
			try {
				return new BigDecimal((String) val).longValue();
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get an optional {@link Number} value associated with a key, or <code>null</code>
	 * if there is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
	 * would be used in cases where type coercion of the number value is unwanted.
	 *
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public Number optNumber(String key) {
		return this.optNumber(key, null);
	}

	/**
	 * Get an optional {@link Number} value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number. This method
	 * would be used in cases where type coercion of the number value is unwanted.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public Number optNumber(String key, Number defaultValue) {
		Object val = this.opt(key);
		if (NULL.equals(val)) {
			return defaultValue;
		}
		if (val instanceof Number){
			return (Number) val;
		}

		if (val instanceof String) {
			try {
				return stringToNumber((String) val);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get an optional string associated with a key. It returns an empty string
	 * if there is no such key. If the value is not a string and is not null,
	 * then it is converted to a string.
	 *
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 */
	public String optString(String key) {
		return this.optString(key, "");
	}

	/**
	 * Get an optional string associated with a key. It returns the defaultValue
	 * if there is no such key.
	 *
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return A string which is the value.
	 */
	public String optString(String key, String defaultValue) {
		Object object = this.opt(key);
		return NULL.equals(object) ? defaultValue : object.toString();
	}

	/**
	 * Populates the internal map of the JSONObjectImpl with the bean properties.
	 * The bean can not be recursive.
	 *
	 * @see JSONObjectImpl#JSONObjectImpl(Object)
	 *
	 * @param bean
	 *            the bean
	 */
	private void populateMap(Object bean) {
		Class<?> klass = bean.getClass();

		// If klass is a System class then set includeSuperClass to false.

		boolean includeSuperClass = klass.getClassLoader() != null;

		Method[] methods = includeSuperClass ? klass.getMethods() : klass
				.getDeclaredMethods();
		for (final Method method : methods) {
			final int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers)
					&& !Modifier.isStatic(modifiers)
					&& method.getParameterTypes().length == 0
					&& !method.isBridge()
					&& method.getReturnType() != Void.TYPE ) {
				final String name = method.getName();
				String key;
				if (name.startsWith("get")) {
					if ("getClass".equals(name) || "getDeclaringClass".equals(name)) {
						continue;
					}
					key = name.substring(3);
				} else if (name.startsWith("is")) {
					key = name.substring(2);
				} else {
					continue;
				}
				if (key.length() > 0
						&& Character.isUpperCase(key.charAt(0))) {
					if (key.length() == 1) {
						key = key.toLowerCase(Locale.ROOT);
					} else if (!Character.isUpperCase(key.charAt(1))) {
						key = key.substring(0, 1).toLowerCase(Locale.ROOT)
								+ key.substring(1);
					}

					try {
						final Object result = method.invoke(bean);
						if (result != null) {
							this.map.put(key, wrap(result));
							// we don't use the result anywhere outside of wrap
							// if it's a resource we should be sure to close it after calling toString
							if(result instanceof Closeable) {
								try {
									((Closeable)result).close();
								} catch (IOException ignore) {
								}
							}
						}
					} catch (IllegalAccessException ignore) {
					} catch (IllegalArgumentException ignore) {
					} catch (InvocationTargetException ignore) {
					}
				}
			}
		}
	}

	/**
	 * Put a key/boolean pair in the JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A boolean which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObjectImpl put(String key, boolean value) throws JSONException {
		this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
	 * Put a key/value pair in the JSONObjectImpl, where the value will be a
	 * JSONArrayImpl which is produced from a Collection.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JSONException
	 */
	public JSONObjectImpl put(String key, Collection<?> value) throws JSONException {
		this.put(key, new JSONArrayImpl(value));
		return this;
	}

	/**
	 * Put a key/double pair in the JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A double which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the number is invalid.
	 */
	public JSONObjectImpl put(String key, double value) throws JSONException {
		this.put(key, Double.valueOf(value));
		return this;
	}

	/**
	 * Put a key/float pair in the JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A float which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the number is invalid.
	 */
	public JSONObjectImpl put(String key, float value) throws JSONException {
		this.put(key, Float.valueOf(value));
		return this;
	}

	/**
	 * Put a key/int pair in the JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            An int which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObjectImpl put(String key, int value) throws JSONException {
		this.put(key, Integer.valueOf(value));
		return this;
	}

	/**
	 * Put a key/long pair in the JSONObjectImpl.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A long which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObjectImpl put(String key, long value) throws JSONException {
		this.put(key, Long.valueOf(value));
		return this;
	}

	/**
	 * Put a key/value pair in the JSONObjectImpl, where the value will be a
	 * JSONObjectImpl which is produced from a Map.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Map value.
	 * @return this.
	 * @throws JSONException
	 */
	public JSONObjectImpl put(String key, Map<?, ?> value) throws JSONException {
		this.put(key, new JSONObjectImpl(value));
		return this;
	}



	/**
	 * Put a key/value pair in the JSONObjectImpl, but only if the key and the value
	 * are both non-null, and only if there is not already a member with that
	 * name.
	 *
	 * @param key string
	 * @param value object
	 * @return this.
	 * @throws JSONException
	 *             if the key is a duplicate
	 */
	public JSONObjectImpl putOnce(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			if (this.opt(key) != null) {
				throw new JSONException("Duplicate key \"" + key + "\"");
			}
			this.put(key, value);
		}
		return this;
	}

	/**
	 * Put a key/value pair in the JSONObjectImpl, but only if the key and the value
	 * are both non-null.
	 *
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JSONArrayImpl, JSONObjectImpl, Long,
	 *            String, or the JSONObjectImpl.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is a non-finite number.
	 */
	public JSONObjectImpl putOpt(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			this.put(key, value);
		}
		return this;
	}

	/**
	 * Creates a JSONPointer using an initialization string and tries to 
	 * match it to an item within this JSONObjectImpl. For example, given a
	 * JSONObjectImpl initialized with this document:
	 * <pre>
	 * {
	 *     "a":{"b":"c"}
	 * }
	 * </pre>
	 * and this JSONPointer string: 
	 * <pre>
	 * "/a/b"
	 * </pre>
	 * Then this method will return the String "c".
	 * A JSONPointerException may be thrown from code called by this method.
	 *   
	 * @param jsonPointer string that can be used to create a JSONPointer
	 * @return the item matched by the JSONPointer, otherwise null
	 */
	public Object query(String jsonPointer) {
		return query(new JSONPointer(jsonPointer));
	}
	/**
	 * Uses a user initialized JSONPointer  and tries to 
	 * match it to an item within this JSONObjectImpl. For example, given a
	 * JSONObjectImpl initialized with this document:
	 * <pre>
	 * {
	 *     "a":{"b":"c"}
	 * }
	 * </pre>
	 * and this JSONPointer: 
	 * <pre>
	 * "/a/b"
	 * </pre>
	 * Then this method will return the String "c".
	 * A JSONPointerException may be thrown from code called by this method.
	 *   
	 * @param jsonPointer string that can be used to create a JSONPointer
	 * @return the item matched by the JSONPointer, otherwise null
	 */
	public Object query(JSONPointer jsonPointer) {
		return jsonPointer.queryFrom(this);
	}

	/**
	 * Queries and returns a value from this object using {@code jsonPointer}, or
	 * returns null if the query fails due to a missing key.
	 * 
	 * @param jsonPointer the string representation of the JSON pointer
	 * @return the queried value or {@code null}
	 * @throws IllegalArgumentException if {@code jsonPointer} has invalid syntax
	 */
	public Object optQuery(String jsonPointer) {
		return optQuery(new JSONPointer(jsonPointer));
	}

	/**
	 * Queries and returns a value from this object using {@code jsonPointer}, or
	 * returns null if the query fails due to a missing key.
	 * 
	 * @param jsonPointer The JSON pointer
	 * @return the queried value or {@code null}
	 * @throws IllegalArgumentException if {@code jsonPointer} has invalid syntax
	 */
	public Object optQuery(JSONPointer jsonPointer) {
		try {
			return jsonPointer.queryFrom(this);
		} catch (JSONPointerException e) {
			return null;
		}
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, producing <\/,
	 * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
	 * contain a control character or an unescaped quote or backslash.
	 *
	 * @param string
	 *            A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string) {
		StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			try {
				return quote(string, sw).toString();
			} catch (IOException ignored) {
				// will never happen - we are writing to a string writer
				return "";
			}
		}
	}

	public static Writer quote(String string, Writer w) throws IOException {
		if (string == null || string.length() == 0) {
			w.write("\"\"");
			return w;
		}

		char b;
		char c = 0;
		String hhhh;
		int i;
		int len = string.length();

		w.write('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				w.write('\\');
				w.write(c);
				break;
			case '/':
				if (b == '<') {
					w.write('\\');
				}
				w.write(c);
				break;
			case '\b':
				w.write("\\b");
				break;
			case '\t':
				w.write("\\t");
				break;
			case '\n':
				w.write("\\n");
				break;
			case '\f':
				w.write("\\f");
				break;
			case '\r':
				w.write("\\r");
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
						|| (c >= '\u2000' && c < '\u2100')) {
					w.write("\\u");
					hhhh = Integer.toHexString(c);
					w.write("0000", 0, 4 - hhhh.length());
					w.write(hhhh);
				} else {
					w.write(c);
				}
			}
		}
		w.write('"');
		return w;
	}

	/**
	 * Remove a name and its value, if present.
	 *
	 * @param key
	 *            The name to be removed.
	 * @return The value that was associated with the name, or null if there was
	 *         no value.
	 */
	public Object remove(String key) {
		return this.map.remove(key);
	}

	/**
	 * Determine if two JSONObjects are similar.
	 * They must contain the same set of names which must be associated with
	 * similar values.
	 *
	 * @param other The other JSONObjectImpl
	 * @return true if they are equal
	 */
	public boolean similar(Object other) {
		try {
			if (!(other instanceof JSONObjectImpl)) {
				return false;
			}
			if (!this.keySet().equals(((JSONObjectImpl)other).keySet())) {
				return false;
			}
			for (final Entry<String,?> entry : this.entrySet()) {
				String name = entry.getKey();
				Object valueThis = entry.getValue();
				Object valueOther = ((JSONObjectImpl)other).get(name);
				if(valueThis == valueOther) {
					return true;
				}
				if(valueThis == null) {
					return false;
				}
				if (valueThis instanceof JSONObjectImpl) {
					if (!((JSONObjectImpl)valueThis).similar(valueOther)) {
						return false;
					}
				} else if (valueThis instanceof JSONArrayImpl) {
					if (!((JSONArrayImpl)valueThis).similar(valueOther)) {
						return false;
					}
				} else if (!valueThis.equals(valueOther)) {
					return false;
				}
			}
			return true;
		} catch (Throwable exception) {
			return false;
		}
	}

	/**
	 * Tests if the value should be tried as a decimal. It makes no test if there are actual digits.
	 * 
	 * @param val value to test
	 * @return true if the string is "-0" or if it contains '.', 'e', or 'E', false otherwise.
	 */
	protected static boolean isDecimalNotation(final String val) {
		return val.indexOf('.') > -1 || val.indexOf('e') > -1
				|| val.indexOf('E') > -1 || "-0".equals(val);
	}

	/**
	 * Converts a string to a number using the narrowest possible type. Possible 
	 * returns for this function are BigDecimal, Double, BigInteger, Long, and Integer.
	 * When a Double is returned, it should always be a valid Double and not NaN or +-infinity.
	 * 
	 * @param val value to convert
	 * @return Number representation of the value.
	 * @throws NumberFormatException thrown if the value is not a valid number. A public
	 *      caller should catch this and wrap it in a {@link JSONException} if applicable.
	 */
	protected static Number stringToNumber(final String val) throws NumberFormatException {
		char initial = val.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-') {
			// decimal representation
			if (isDecimalNotation(val)) {
				// quick dirty way to see if we need a BigDecimal instead of a Double
				// this only handles some cases of overflow or underflow
				if (val.length()>14) {
					return new BigDecimal(val);
				}
				final Double d = Double.valueOf(val);
				if (d.isInfinite() || d.isNaN()) {
					// if we can't parse it as a double, go up to BigDecimal
					// this is probably due to underflow like 4.32e-678
					// or overflow like 4.65e5324. The size of the string is small
					// but can't be held in a Double.
					return new BigDecimal(val);
				}
				return d;
			}
			// integer representation.
			// This will narrow any values to the smallest reasonable Object representation
			// (Integer, Long, or BigInteger)

			// string version
			// The compare string length method reduces GC,
			// but leads to smaller integers being placed in larger wrappers even though not
			// needed. i.e. 1,000,000,000 -> Long even though it's an Integer
			// 1,000,000,000,000,000,000 -> BigInteger even though it's a Long
			//if(val.length()<=9){
			//    return Integer.valueOf(val);
			//}
			//if(val.length()<=18){
			//    return Long.valueOf(val);
			//}
			//return new BigInteger(val);

			// BigInteger version: We use a similar bitLenth compare as
			// BigInteger#intValueExact uses. Increases GC, but objects hold
			// only what they need. i.e. Less runtime overhead if the value is
			// long lived. Which is the better tradeoff? This is closer to what's
			// in stringToValue.
			BigInteger bi = new BigInteger(val);
			if(bi.bitLength()<=31){
				return Integer.valueOf(bi.intValue());
			}
			if(bi.bitLength()<=63){
				return Long.valueOf(bi.longValue());
			}
			return bi;
		}
		throw new NumberFormatException("val ["+val+"] is not a valid number.");
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 *
	 * @param string
	 *            A String.
	 * @return A simple JSON value.
	 */
	public static Object stringToValue(String string) {
		if (string.equals("")) {
			return string;
		}
		if (string.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (string.equalsIgnoreCase("null")) {
			return JSONObjectImpl.NULL;
		}

		/*
		 * If it might be a number, try converting it. If a number cannot be
		 * produced, then the value will just be a string.
		 */

		char initial = string.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-') {
			try {
				// if we want full Big Number support this block can be replaced with:
				// return stringToNumber(string);
				if (isDecimalNotation(string)) {
					Double d = Double.valueOf(string);
					if (!d.isInfinite() && !d.isNaN()) {
						return d;
					}
				} else {
					Long myLong = Long.valueOf(string);
					if (string.equals(myLong.toString())) {
						if (myLong.longValue() == myLong.intValue()) {
							return Integer.valueOf(myLong.intValue());
						}
						return myLong;
					}
				}
			} catch (Exception ignore) {
			}
		}
		return string;
	}

	/**
	 * Throw an exception if the object is a NaN or infinite number.
	 *
	 * @param o
	 *            The object to test.
	 * @throws JSONException
	 *             If o is a non-finite number.
	 */
	public static void testValidity(Object o) throws JSONException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JSONException(
							"JSON does not allow non-finite numbers.");
				}
			}
		}
	}

	/**
	 * Produce a JSONArrayImpl containing the values of the members of this
	 * JSONObjectImpl.
	 *
	 * @param names
	 *            A JSONArrayImpl containing a list of key strings. This determines
	 *            the sequence of the values in the result.
	 * @return A JSONArrayImpl of values.
	 * @throws JSONException
	 *             If any of the values are non-finite numbers.
	 */
	public JSONArrayImpl toJSONArray(JSONArrayImpl names) throws JSONException {
		if (names == null || names.length() == 0) {
			return null;
		}
		JSONArrayImpl ja = new JSONArrayImpl();
		for (int i = 0; i < names.length(); i += 1) {
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}

	/**
	 * Make a JSON text of this JSONObjectImpl. For compactness, no whitespace is
	 * added. If this would not result in a syntactically correct JSON text,
	 * then null will be returned instead.
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 * 
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 */
	@Override
	public String toString() {
		try {
			return this.toString(0);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Make a pretty-printed JSON text of this JSONObjectImpl.
	 * 
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONObjectImpl}
	 * has only one key, then the object will be output on a single line:
	 * <pre>{@code {"key": 1}}</pre>
	 * 
	 * <p>If an object has 2 or more keys, then it will be output across
	 * multiple lines: <code><pre>{
	 *  "key1": 1,
	 *  "key2": "value 2",
	 *  "key3": 3
	 * }</pre></code>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JSONException {
		StringWriter w = new StringWriter();
		synchronized (w.getBuffer()) {
			return this.write(w, indentFactor, 0).toString();
		}
	}

	/**
	 * Make a JSON text of an Object value. If the object has an
	 * value.toJSONString() method, then that method will be used to produce the
	 * JSON text. The method is required to produce a strictly conforming text.
	 * If the object does not contain a toJSONString method (which is the most
	 * common case), then a text will be produced by other means. If the value
	 * is an array or Collection, then a JSONArrayImpl will be made from it and its
	 * toJSONString method will be called. If the value is a MAP, then a
	 * JSONObjectImpl will be made from it and its toJSONString method will be
	 * called. Otherwise, the value's toString method will be called, and the
	 * result will be quoted.
	 *
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @param value
	 *            The value to be serialized.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException
	 *             If the value is or contains an invalid number.
	 */
	public static String valueToString(Object value) throws JSONException {
		if (value == null || value.equals(null)) {
			return "null";
		}
		if (value instanceof JSONString) {
			Object object;
			try {
				object = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			if (object instanceof String) {
				return (String) object;
			}
			throw new JSONException("Bad value from toJSONString: " + object);
		}
		if (value instanceof Number) {
			// not all Numbers may match actual JSON Numbers. i.e. Fractions or Complex
			final String numberAsString = numberToString((Number) value);
			try {
				// Use the BigDecimal constructor for it's parser to validate the format.
				@SuppressWarnings("unused")
				BigDecimal unused = new BigDecimal(numberAsString);
				// Close enough to a JSON number that we will return it unquoted
				return numberAsString;
			} catch (NumberFormatException ex){
				// The Number value is not a valid JSON number.
				// Instead we will quote it as a string
				return quote(numberAsString);
			}
		}
		if (value instanceof Boolean || value instanceof JSONObjectImpl
				|| value instanceof JSONArrayImpl) {
			return value.toString();
		}
		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			return new JSONObjectImpl(map).toString();
		}
		if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			return new JSONArrayImpl(coll).toString();
		}
		if (value.getClass().isArray()) {
			return new JSONArrayImpl(value).toString();
		}
		if(value instanceof Enum<?>){
			return quote(((Enum<?>)value).name());
		}
		return quote(value.toString());
	}

	/**
	 * Wrap an object, if necessary. If the object is null, return the NULL
	 * object. If it is an array or collection, wrap it in a JSONArrayImpl. If it is
	 * a map, wrap it in a JSONObjectImpl. If it is a standard property (Double,
	 * String, et al) then it is already wrapped. Otherwise, if it comes from
	 * one of the java packages, turn it into a string. And if it doesn't, try
	 * to wrap it in a JSONObjectImpl. If the wrapping fails, then null is returned.
	 *
	 * @param object
	 *            The object to wrap
	 * @return The wrapped value
	 */
	public static Object wrap(Object object) {
		try {
			if (object == null) {
				return NULL;
			}
			if (object instanceof JSONObjectImpl || object instanceof JSONArrayImpl
					|| NULL.equals(object) || object instanceof JSONString
					|| object instanceof Byte || object instanceof Character
					|| object instanceof Short || object instanceof Integer
					|| object instanceof Long || object instanceof Boolean
					|| object instanceof Float || object instanceof Double
					|| object instanceof String || object instanceof BigInteger
					|| object instanceof BigDecimal || object instanceof Enum) {
				return object;
			}

			if (object instanceof Collection) {
				Collection<?> coll = (Collection<?>) object;
				return new JSONArrayImpl(coll);
			}
			if (object.getClass().isArray()) {
				return new JSONArrayImpl(object);
			}
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				return new JSONObjectImpl(map);
			}
			Package objectPackage = object.getClass().getPackage();
			String objectPackageName = objectPackage != null ? objectPackage
					.getName() : "";
			if (objectPackageName.startsWith("java.")
					|| objectPackageName.startsWith("javax.")
					|| object.getClass().getClassLoader() == null) {
				return object.toString();
			}
			return new JSONObjectImpl(object);
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 * Write the contents of the JSONObjectImpl as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 * 
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer) throws JSONException {
		return this.write(writer, 0, 0);
	}

	static final Writer writeValue(Writer writer, Object value,
			int indentFactor, int indent) throws JSONException, IOException {
		if (value == null || value.equals(null)) {
			writer.write("null");
		} else if (value instanceof JSONString) {
			Object o;
			try {
				o = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			writer.write(o != null ? o.toString() : quote(value.toString()));
		} else if (value instanceof Number) {
			// not all Numbers may match actual JSON Numbers. i.e. fractions or Imaginary
			final String numberAsString = numberToString((Number) value);
			try {
				// Use the BigDecimal constructor for it's parser to validate the format.
				@SuppressWarnings("unused")
				BigDecimal testNum = new BigDecimal(numberAsString);
				// Close enough to a JSON number that we will use it unquoted
				writer.write(numberAsString);
			} catch (NumberFormatException ex){
				// The Number value is not a valid JSON number.
				// Instead we will quote it as a string
				quote(numberAsString, writer);
			}
		} else if (value instanceof Boolean) {
			writer.write(value.toString());
		} else if (value instanceof Enum<?>) {
			writer.write(quote(((Enum<?>)value).name()));
		} else if (value instanceof JSONObjectImpl) {
			((JSONObjectImpl) value).write(writer, indentFactor, indent);
		} else if (value instanceof JSONArrayImpl) {
			((JSONArrayImpl) value).write(writer, indentFactor, indent);
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			new JSONObjectImpl(map).write(writer, indentFactor, indent);
		} else if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			new JSONArrayImpl(coll).write(writer, indentFactor, indent);
		} else if (value.getClass().isArray()) {
			new JSONArrayImpl(value).write(writer, indentFactor, indent);
		} else {
			quote(value.toString(), writer);
		}
		return writer;
	}

	static final void indent(Writer writer, int indent) throws IOException {
		for (int i = 0; i < indent; i += 1) {
			writer.write(' ');
		}
	}

	/**
	 * Write the contents of the JSONObjectImpl as JSON text to a writer.
	 * 
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONObjectImpl}
	 * has only one key, then the object will be output on a single line:
	 * <pre>{@code {"key": 1}}</pre>
	 * 
	 * <p>If an object has 2 or more keys, then it will be output across
	 * multiple lines: <code><pre>{
	 *  "key1": 1,
	 *  "key2": "value 2",
	 *  "key3": 3
	 * }</pre></code>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @param writer
	 *            Writes the serialized JSON
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indentation of the top level.
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer, int indentFactor, int indent)
			throws JSONException {
		try {
			boolean commanate = false;
			final int length = this.length();
			writer.write('{');

			if (length == 1) {
				final Entry<String,?> entry = this.entrySet().iterator().next();
				final String key = entry.getKey();
				writer.write(quote(key));
				writer.write(':');
				if (indentFactor > 0) {
					writer.write(' ');
				}
				try{
					writeValue(writer, entry.getValue(), indentFactor, indent);
				} catch (Exception e) {
					throw new JSONException("Unable to write JSONObjectImpl value for key: " + key, e);
				}
			} else if (length != 0) {
				final int newindent = indent + indentFactor;
				for (final Entry<String,?> entry : this.entrySet()) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					indent(writer, newindent);
					final String key = entry.getKey();
					writer.write(quote(key));
					writer.write(':');
					if (indentFactor > 0) {
						writer.write(' ');
					}
					try {
						writeValue(writer, entry.getValue(), indentFactor, newindent);
					} catch (Exception e) {
						throw new JSONException("Unable to write JSONObjectImpl value for key: " + key, e);
					}
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				indent(writer, indent);
			}
			writer.write('}');
			return writer;
		} catch (IOException exception) {
			throw new JSONException(exception);
		}
	}

	/**
	 * Returns a java.util.Map containing all of the entries in this object.
	 * If an entry in the object is a JSONArrayImpl or JSONObjectImpl it will also
	 * be converted.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a java.util.Map containing the entries of this object
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> results = new HashMap<String, Object>();
		for (Entry<String, Object> entry : this.entrySet()) {
			Object value;
			if (entry.getValue() == null || NULL.equals(entry.getValue())) {
				value = null;
			} else if (entry.getValue() instanceof JSONObjectImpl) {
				value = ((JSONObjectImpl) entry.getValue()).toMap();
			} else if (entry.getValue() instanceof JSONArrayImpl) {
				value = ((JSONArrayImpl) entry.getValue()).toList();
			} else {
				value = entry.getValue();
			}
			results.put(entry.getKey(), value);
		}
		return results;
	}


}
