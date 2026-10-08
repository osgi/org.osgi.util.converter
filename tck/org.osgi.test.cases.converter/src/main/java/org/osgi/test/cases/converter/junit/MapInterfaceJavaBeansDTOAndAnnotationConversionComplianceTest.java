/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/

package org.osgi.test.cases.converter.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.osgi.dto.DTO;
import org.osgi.test.cases.converter.junit.ConversionComplianceTest.ExtObject;
import org.osgi.util.converter.ConversionException;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterFunction;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.TypeReference;


/**
 * 707 Converter specification
 */
public class MapInterfaceJavaBeansDTOAndAnnotationConversionComplianceTest {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface AnnotationInterface {
		String prop1();

		String prop2() default "value2";

		String prop3() default "value3";

		String prop4();
	}

	@AnnotationInterface(prop1 = "definedValue1", prop4 = "definedValue4")
	public static class AnnotatedMappingClass {
		public AnnotatedMappingClass() {}
	}

	public static interface MappingInterface {

		String prop1();

		String prop2();

		String prop2(String prop2);

		String prop3();

		String prop3(String prop3);

		String prop4();
	}

	public static interface OtherInterface {

		String prop5();
	}

	public static class MultiInterfaces
			implements OtherInterface, MappingInterface {

		private String	prop1	= "value1";
		private String	prop2	= "value2";
		private String	prop3	= "value3";
		private String	prop4	= "value4";
		private String	prop5	= "value5";

		// no empty constructor
		public MultiInterfaces(boolean b) {}

		@Override
		public String prop1() {
			return this.prop1;
		}

		@Override
		public String prop2() {
			return this.prop2;
		}

		@Override
		public String prop2(String prop2) {
			String old = this.prop2;
			this.prop2 = prop2;
			return old;
		}

		@Override
		public String prop3() {
			return this.prop3;
		}

		@Override
		public String prop3(String prop3) {
			String old = this.prop3;
			this.prop3 = prop3;
			return old;
		}

		@Override
		public String prop4() {
			return this.prop4;
		}

		@Override
		public String prop5() {
			return this.prop5;
		}
	}

	public static class TypeWithoutGetProperties extends MultiInterfaces {

		public TypeWithoutGetProperties(boolean b) {
			super(b);
		}
	}

	public static class TypeWithGetProperties extends MultiInterfaces {

		public TypeWithGetProperties(boolean b) {
			super(b);
		}

		public Dictionary<String,String> getProperties() {
			Hashtable<String,String> table = new Hashtable<String,String>();
			table.put("prop1", prop1());
			table.put("prop2", prop2());
			table.put("prop3", prop3());
			table.put("prop4", prop4());
			table.put("prop5", prop5());
			return table;
		}
	}
	
	public static class MappingBean {
		private String			prop1;
		private String			prop2;
		private String			prop3;
		private ExtObject	embedded;

		// empty constructor
		public MappingBean() {}

		public void setP(String prop1) {
			this.prop1 = prop1;
		}

		public String getP() {
			return this.prop1;
		}

		public void setProp2(String prop2) {
			this.prop2 = prop2;
		}

		public String getProp2() {
			return this.prop2;
		}

		public void setProp3(String prop3) {
			this.prop3 = prop3;
		}

		public String getProp3() {
			return this.prop3;
		}

		public void setEmbedded(ExtObject embedded) {
			this.embedded = embedded;
		}

		public ExtObject getEmbedded() {
			return this.embedded;
		}
	}
	
	public static class DTOLike {

		public String	prop1;
		public String	prop2;

		public DTOLike() {}
	}

	public static class NotDTOLike {

		public String	prop1;
		public String	prop2;
		public String	prop3;

		public NotDTOLike() {}

		public void generateProp3() {
			if (prop1 == null || prop2 == null) {
				return;
			}
			prop3 = prop1.concat(prop2);
		}
	}

	public static class WithStaticAndPrivateFieldsDTOLike {
		public static final String	STATIC_FIELD	= "STATIC_FIELD";

		@SuppressWarnings("unused")
		private String				prop0			= "private";
		public String				prop1;
		public String				prop2;

		public WithStaticAndPrivateFieldsDTOLike() {}
	}

	public static class KeyMappingDTOLike {
		public static final String	PREFIX_	= "org.osgi.util.converter.test.";

		public String				special$prop;								// "org.osgi.util.converter.test.specialprop";
		public String				special$$prop;								// "org.osgi.util.converter.test.special$prop";
		public String				special_prop;								// "org.osgi.util.converter.test.special.prop";
		public String				_specialprop;								// "org.osgi.util.converter.test..specialprop";
		public String				special__prop;								// "org.osgi.util.converter.test.special_prop";
		public String				special___prop;								// "org.osgi.util.converter.test.special_.prop";
		public String				special_$__prop;							// "org.osgi.util.converter.test.special._prop";
		public String				special_$_prop;								// "org.osgi.util.converter.test.special..prop";
		public String				special$_$prop;								// "org.osgi.util.converter.test.special-prop";
		public String				special$$_$prop;							// "org.osgi.util.converter.test.special$.prop";

		public KeyMappingDTOLike() {}
	}

	public static class KeyMappingBean {
		public static final String	PREFIX_	= "org.osgi.util.converter.test.";

		private String				special$prop;								// "org.osgi.util.converter.test.specialprop";
		private String				special$$prop;								// "org.osgi.util.converter.test.special$prop";
		private String				special_prop;								// "org.osgi.util.converter.test.special.prop";
		private String				_specialprop;								// "org.osgi.util.converter.test..specialprop";
		private String				special__prop;								// "org.osgi.util.converter.test.special_prop";
		private String				special___prop;								// "org.osgi.util.converter.test.special_.prop";
		private String				special_$__prop;							// "org.osgi.util.converter.test.special._prop";
		private String				special_$_prop;								// "org.osgi.util.converter.test.special..prop";
		private String				special$_$prop;								// "org.osgi.util.converter.test.special-prop";
		private String				special$$_$prop;							// "org.osgi.util.converter.test.special$.prop";

		public KeyMappingBean() {}

		public String getSpecial$prop() {
			return special$prop;
		}

		public void setSpecial$prop(String special$prop) {
			this.special$prop = special$prop;
		}

		public String getSpecial$$prop() {
			return special$$prop;
		}

		public void setSpecial$$prop(String special$$prop) {
			this.special$$prop = special$$prop;
		}

		public String getSpecial_prop() {
			return special_prop;
		}

		public void setSpecial_prop(String special_prop) {
			this.special_prop = special_prop;
		}

		public String get_specialprop() {
			return _specialprop;
		}

		public void set_specialprop(String _specialprop) {
			this._specialprop = _specialprop;
		}

		public String getSpecial__prop() {
			return special__prop;
		}

		public void setSpecial__prop(String special__prop) {
			this.special__prop = special__prop;
		}

		public String getSpecial___prop() {
			return special___prop;
		}

		public void setSpecial___prop(String special___prop) {
			this.special___prop = special___prop;
		}

		public String getSpecial_$__prop() {
			return special_$__prop;
		}

		public void setSpecial_$__prop(String special_$__prop) {
			this.special_$__prop = special_$__prop;
		}

		public String getSpecial_$_prop() {
			return special_$_prop;
		}

		public void setSpecial_$_prop(String special_$_prop) {
			this.special_$_prop = special_$_prop;
		}

		public String getSpecial$_$prop() {
			return special$_$prop;
		}

		public void setSpecial$_$prop(String special$_$prop) {
			this.special$_$prop = special$_$prop;
		}

		public String getSpecial$$_$prop() {
			return special$$_$prop;
		}

		public void setSpecial$$_$prop(String special$$_$prop) {
			this.special$$_$prop = special$$_$prop;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface KeyMappingAnnotation {
		public static final String PREFIX_ = "org.osgi.util.converter.test.";

		public String special$prop(); // "org.osgi.util.converter.test.specialprop";

		public String special$$prop();// "org.osgi.util.converter.test.special$prop";

		public String special_prop();// "org.osgi.util.converter.test.special.prop";

		public String _specialprop();// "org.osgi.util.converter.test..specialprop";

		public String special__prop();// "org.osgi.util.converter.test.special_prop";

		public String special___prop();// "org.osgi.util.converter.test.special_.prop";

		public String special_$__prop();// "org.osgi.util.converter.test.special._prop";

		public String special_$_prop();// "org.osgi.util.converter.test.special..prop";

		public String special$_$prop();// "org.osgi.util.converter.test.special-prop";

		public String special$$_$prop();// "org.osgi.util.converter.test.special$.prop";
	}

	@KeyMappingAnnotation(_specialprop = "org.osgi.util.converter.test..specialprop", special$$_$prop = "org.osgi.util.converter.test.special$.prop", special$$prop = "org.osgi.util.converter.test.special$prop", special$_$prop = "org.osgi.util.converter.test.special-prop", special$prop = "org.osgi.util.converter.test.specialprop", special_$__prop = "org.osgi.util.converter.test.special._prop", special_$_prop = "org.osgi.util.converter.test.special..prop", special___prop = "org.osgi.util.converter.test.special_.prop", special__prop = "org.osgi.util.converter.test.special_prop", special_prop = "org.osgi.util.converter.test.special.prop")
	public static class KeyMappingAnnotatedClass {
		public KeyMappingAnnotatedClass() {}
	}

	public static interface KeyMappingInterface {
		public static final String PREFIX_ = "org.osgi.util.converter.test.";

		public String special$prop(); // "org.osgi.util.converter.test.specialprop";

		public String special$$prop();// "org.osgi.util.converter.test.special$prop";

		public String special_prop();// "org.osgi.util.converter.test.special.prop";

		public String _specialprop();// "org.osgi.util.converter.test..specialprop";

		public String special__prop();// "org.osgi.util.converter.test.special_prop";

		public String special___prop();// "org.osgi.util.converter.test.special_.prop";

		public String special_$__prop();// "org.osgi.util.converter.test.special._prop";

		public String special_$_prop();// "org.osgi.util.converter.test.special..prop";

		public String special$_$prop();// "org.osgi.util.converter.test.special-prop";

		public String special$$_$prop();// "org.osgi.util.converter.test.special$.prop";
	}

	public static class MyDTO2 extends DTO {
		public static String		shouldBeIgnored	= "ignoreme";

		public List<Long>			longList;

		public Map<String,MyDTO3>	dtoMap;
	}

	public static class MyDTO3 extends DTO {
		public Set<Character> charSet;
	}

	public static class MyGenericDTOWithVariables<T> extends DTO {
		public Set<T>	set;
		public T		raw;
		public T[]		array;
	}

	public interface MyGenericInterface {
		public Set<Character> charSet();
	}

	public interface MyGenericInterfaceWithVariables<T> {
		public Set<T> set();
		public T raw();
		public T[] array();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyMarkerAnnotation {}

	@MyMarkerAnnotation
	public interface MarkedInterface {
		String foo();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SingleElementAnnotation {
		String value() default "nothing!";
	}

	@SingleElementAnnotation("123")
	public static class SingleElementAnnotatedClass {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SingleElementAnnotationPrefix {
		public static final String PREFIX_ = "org.foo.bar.";

		long value() default 42L;
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.1 - Converting from scalar
	 * <p/>
	 * Conversions from a scalar to a map-like type are not supported by the
	 * standard converter.
	 */
	@Test
	public void testFromScalarConversion() {
		Converter converter = Converters.standardConverter();
		assertThatExceptionOfType(ConversionException.class).as("Scalar to map-like structure not supported")
				.isThrownBy(() -> converter.convert("scalar").to(Map.class));
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.2- Converting to a scalar
	 * <p/>
	 * Conversions of a map-like structure to a scalar are done by iterating
	 * through the entries of the map and taking the first Map.Entry instance.
	 * Then this instance is converted into the target scalar type as described
	 * in the section called Map.Entry.
	 * <p/>
	 * An empty map results in a null scalar value.
	 */
	@Test
	public void testToScalarConversion() {
		Converter converter = Converters.standardConverter();
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);	
		
		Calendar calendar = Calendar.getInstance(tz);	
		Date date = calendar.getTime();
		String dateStr = df.format(date);

		Map<String,String> map = new HashMap<String,String>();
		map.put(dateStr, "epoch");				
		
		date = converter.convert(map).to(Date.class);
		assertThat((date.getTime()/1000)).isEqualTo((calendar.getTime().getTime()/1000));
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.3- Converting to an Array or Collection
	 * <p/>
	 * A map-like structure is converted to an Array or Collection target type
	 * by creating an ordered collection of Map.Entry objects. Then this
	 * collection is converted to the target type as described in the section
	 * called Arrays and Collections and the section called Map.Entry.
	 * @throws ParseException 
	 */
	@Test
	public void testToArrayOrCollectionConversion() throws ParseException {
				
		TimeZone tz = TimeZone.getTimeZone("UTC");			
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		
		Calendar calendar = Calendar.getInstance(tz);			
		long day = calendar.getTimeInMillis();
		
		long millisDay = 1000 * 3600 * 24;
		
		Map<String,String> map = new HashMap<String,String>();
		for (int i = 0; i < 4; i++) {
			Date d = new Date((day + (i * millisDay)));
			map.put(df.format(d), "day".concat(String.valueOf(i)));
		}
		Converter converter = Converters.standardConverter();
		List<Date> dates = converter.convert(map).to(new TypeReference<List<Date>>() {});
		
		assertThat(dates.size()).isEqualTo(map.size());

		Iterator<String> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			if (!dates.contains(df.parse(keys.next()))) {
				fail("");
			}
		}
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.1 - Converting to a Map
	 * <p/>
	 * When converting a map-like structure to a java.util.Map the converter
	 * will return a live view over the backing object that changes when the
	 * backing object changes. The live view can be prevented by providing the
	 * copy() modifier. When converting to other map types a copy is always
	 * produced. In all cases the object returned is a separate instance that
	 * can be owned by the client. When the client modifies the returned object
	 * a live view will stop reflecting changes to the backing object.
	 * <p/>
	 * </p>
	 * <table>
	 * <tr>
	 * <th>Target</th>
	 * <th>Method</th>
	 * </tr>
	 * <tr>
	 * <td><code>java.util.Map</code></td>
	 * <td>A map view over the backing object is created, changes to the backing
	 * object will be reflected in the map, unless the map is modified by the
	 * client.</td>
	 * </tr>
	 * <tr>
	 * <td>Other Map interface</td>
	 * <td>A mutable implementation is created. For example, if the target type
	 * is <code>ConcurrentNavigableMap</code> then the implementation can create
	 * a <code>ConcurrentSkipListMap</code>.</td>
	 * </tr>
	 * <tr>
	 * <td>Map concrete type</td>
	 * <td>A new instance is created by calling <code>Class.newInstance()</code>
	 * on the provided type. For example if the target type is
	 * <code>HashMap</code> then the converter creates a target object by
	 * calling <code>HashMap.class.newInstance()</code>. The converter may
	 * choose to use a call a well-known constructor to optimize the creation of
	 * the map.</td>
	 * </tr>
	 * </table>
	 * <p/>
	 * When converting from a map-like object to a Map or sub-type, each
	 * key-value pair in the source map is converted to desired types of the
	 * target map using the generic information if available. Map type
	 * information for the target type can be made available by using the
	 * to(TypeReference) or to(Type) methods. If no type information is
	 * available, key-value pairs are used in the map as-is
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testMapConversion() {
		Converter converter = Converters.standardConverter();
		DTOLike dtolike = new DTOLike();
		dtolike.prop1 = "value1";
		dtolike.prop2 = "value2";

		// with live view
		Map<String,String> converted = converter.convert(dtolike).view().to(
				Map.class);
		assertThat(converted).isNotNull();
		assertThat(converted.size()).isEqualTo(2);
		assertThat(converted.get("prop1")).isEqualTo(dtolike.prop1);
		assertThat(converted.get("prop2")).isEqualTo(dtolike.prop2);

		// reflect backing object changes
		dtolike.prop1 = "value1bis";
		assertThat(converted.get("prop1")).isEqualTo(dtolike.prop1);

		// do not reflect backing object changes
		converted.put("prop1", "value1ter");
		dtolike.prop1 = "value1frth";
		assertThat(dtolike.prop1).isNotEqualTo(converted.get("prop1"));

		// without live view
		converted = converter.convert(dtolike).to(Map.class);
		assertThat(converted).isNotNull();
		assertThat(converted.size()).isEqualTo(2);
		assertThat(converted.get("prop1")).isEqualTo(dtolike.prop1);
		assertThat(converted.get("prop2")).isEqualTo(dtolike.prop2);

		// do not reflect backing object changes
		dtolike.prop1 = "value1fve";
		assertThat(dtolike.prop1).isNotEqualTo(converted.get("prop1"));

		// Other Map interface
		converted = converter.convert(dtolike).to(NavigableMap.class);
		assertThat(converted).isNotNull();
		assertThat(NavigableMap.class.isAssignableFrom(converted.getClass())).isTrue();

		// concrete type
		converted = converter.convert(dtolike).to(TreeMap.class);
		assertThat(converted).isNotNull();
		assertThat(TreeMap.class).isEqualTo(converted.getClass());

		// handle generic type
		Map<Character,String> converted2 = converter.convert(dtolike)
				.to(new TypeReference<Map<Character,String>>() {});
		assertThat(converted2).isNotNull();
		assertThat(converted2).hasSize(1);
		assertThat(converted2.get('p').equals(dtolike.prop1)
				|| converted2.get('p').equals(dtolike.prop2)).isTrue();
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.2 - Dictionary
	 * <p/>
	 * Converting between a map and a Dictionary is done by iterating over the
	 * source and inserting the key value pairs in the target, converting them
	 * to the requested target type, if known. As with other generic types,
	 * target type information for Dictionaries can be provided via a
	 * TypeReference.
	 */
	@Test
	public void testDictionaryConversion() {
		Converter converter = Converters.standardConverter();
		DTOLike dtolike = new DTOLike();
		dtolike.prop1 = "value1";
		dtolike.prop2 = "value2";

		@SuppressWarnings("unchecked")
		Dictionary<String,String> converted = converter.convert(dtolike)
				.to(Dictionary.class);
		assertThat(converted).isNotNull();
		assertThat(converted.size()).isEqualTo(2);
		assertThat(converted.get("prop1")).isEqualTo(dtolike.prop1);
		assertThat(converted.get("prop2")).isEqualTo(dtolike.prop2);

		Dictionary<Character,Character> converted2 = converter.convert(dtolike)
				.to(new TypeReference<Dictionary<Character,Character>>() {});
		assertThat(converted2).isNotNull();
		assertThat(converted2.size()).isOne();
		assertThat(converted2.get('p')).isEqualTo(Character.valueOf('v'));
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.3 - Interface
	 * <p/>
	 * Converting a map-like structure into an interface can be a useful way to
	 * give a map of untyped data a typed API. The converter synthesizes an
	 * interface instance to represent the conversion.
	 * <p/>
	 * Note that converting to annotations provides similar functionality with
	 * the added benefit of being able to specify default values in the
	 * annotation code.
	 * <p/>
	 * <b>Converting to an Interface</b>
	 * <p/>
	 * When converting into an interface the converter will create a dynamic
	 * proxy to implement the interface. The name of the method returning the
	 * value should match the key of the map entry, taking into account the
	 * mapping rules specified in the section called Key Mapping. The key of the
	 * map may need to be converted into a String first.
	 * <p/>
	 * Conversion is done on demand: only when the method on the interface is
	 * actually invoked. This avoids conversion errors on methods for which the
	 * information is missing or cannot be converted, but which the caller does
	 * not require. Note that the converter will not copy the source map when
	 * converting to an interface allowing changes to the source map to be
	 * reflected live to the proxy. The proxy cannot cache the conversions.
	 * Interfaces can provide methods for default values by providing a
	 * single-argument method override in addition to the no-parameter method
	 * matching the key name. If the type of the default does not match the
	 * target type it is converted first.
	 * <p/>
	 * Default values are used when the key is not present in the map for the
	 * method.
	 * <p/>
	 * If a key is present with a null value, then null is taken as the value
	 * and converted to the target type. If no default is specified and a
	 * requested value is not present in the map, a ConversionException is
	 * thrown.
	 * <p/>
	 * <b>Converting from an Interface</b>
	 * <p/>
	 * An interface can also be the source of a conversion to another map-like
	 * type. The name of each method without parameters is taken as key, taking
	 * into account the section called Key Mapping. The method is invoked using
	 * reflection to produce the associated value.
	 * <p/>
	 * Whether a conversion source object is an interface is determined
	 * dynamically. When an object implements multiple interfaces by default the
	 * first interface from these that has no-parameter methods is taken as the
	 * source type. To select a different interface use the sourceAs(Class)
	 * modifier:
	 * <p/>
	 * <code>
	 * Map m = converter.convert(myMultiInterface).
	 * sourceAs(MyInterfaceB.class).to(Map.class);</code>
	 * <p/>
	 * If the source object also has a getProperties() [...] this method is used
	 * to obtain the map view by default. This behaviour can be overridden by
	 * using the sourceAs(Class) modifier.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testInterfaceConversion() {

		DTOLike dtolike = new DTOLike();
		dtolike.prop1 = "value1";
		dtolike.prop2 = "value2";

		Converter converter = Converters.standardConverter();
		MappingInterface mappingInterface = converter.convert(dtolike)
				.to(MappingInterface.class);
		assertThat(mappingInterface).isNotNull();

		assertThat(mappingInterface.prop1()).isEqualTo(dtolike.prop1);
		assertThat(mappingInterface.prop2()).isEqualTo(dtolike.prop2);

		// report changes
		dtolike.prop2 = "value3";
		assertThat(mappingInterface.prop2()).isEqualTo(dtolike.prop2);
		assertThat(mappingInterface.prop2("defaultValue")).isEqualTo(dtolike.prop2);

		// use default value
		assertThat(mappingInterface.prop3("defaultValue")).isEqualTo("defaultValue");

		// do not use default
		dtolike.prop2 = null;
		assertThat(mappingInterface.prop2(null)).isNull();

		assertThatExceptionOfType(ConversionException.class).as("ConversionException expected : undefined in DTOLike and no default ")
				.isThrownBy(() -> mappingInterface.prop4());

		TypeWithoutGetProperties multiInterface = new TypeWithoutGetProperties(
				false);

		// select the appropriate interface
		Map<String,String> converted = converter.convert(multiInterface)
				.sourceAs(MappingInterface.class)
				.view()
				.to(Map.class);

		multiInterface.prop2("newValue2");

		assertThat(converted).isNotNull();
		assertThat(converted).hasSize(4);

		assertThat(converted.get("prop5")).isNull();

		assertThat(converted.get("prop1")).isEqualTo(multiInterface.prop1());
		assertThat(converted.get("prop2")).isEqualTo(multiInterface.prop2());
		assertThat(converted.get("prop3")).isEqualTo(multiInterface.prop3());
		assertThat(converted.get("prop4")).isEqualTo(multiInterface.prop4());

		converted.put("prop4", "newValue4");
		assertThat(multiInterface.prop4()).isNotEqualTo(converted.get("prop4"));

		// use the first implemented interface
		converted = converter.convert(multiInterface).view().to(Map.class);

		assertThat(converted).isNotNull();
		assertThat(converted).hasSize(1);
		assertThat(converted.get("prop1")).isNull();
		assertThat(converted.get("prop2")).isNull();
		assertThat(converted.get("prop3")).isNull();
		assertThat(converted.get("prop4")).isNull();
		assertThat(converted.get("prop5")).isEqualTo(multiInterface.prop5());
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.4 - Annotation
	 * <p/>
	 * Conversion to and from annotations behaves similar to interface
	 * conversion with the added capability of specifying a default in the
	 * annotation definition.
	 * <p/>
	 * When converting to an annotation type, the converter will return an
	 * instance of the requested annotation class. As with interfaces, values
	 * are only obtained from the conversion source when the annotation method
	 * is actually called. If the requested value is not available, the default
	 * as specified in the annotation class is used. If no default is specified
	 * a ConversionException is thrown.
	 * <p/>
	 * Similar to interfaces, conversions to and from annotations also follow
	 * the Key Mapping section for annotation element names.
	 */
	@Test
	public void testAnnotationConversion() {

		DTOLike dtolike = new DTOLike();
		dtolike.prop1 = "value1";
		dtolike.prop2 = "value2";

		Converter converter = Converters.standardConverter();
		AnnotationInterface annotationInterface = converter.convert(dtolike)
				.to(AnnotationInterface.class);
		assertThat(annotationInterface).isNotNull();

		assertThat(annotationInterface.prop1()).isEqualTo(dtolike.prop1);
		assertThat(annotationInterface.prop2()).isEqualTo(dtolike.prop2);

		// report changes
		dtolike.prop2 = "newValue2";
		assertThat(annotationInterface.prop2()).isEqualTo(dtolike.prop2);

		// use default value
		assertThat(annotationInterface.prop3()).isEqualTo("value3");

		// use the given null
		dtolike.prop2 = null;
		assertThat(annotationInterface.prop2()).isNull();

		assertThatExceptionOfType(ConversionException.class).as("ConversionException expected : undefined in DTOLike and no default ")
				.isThrownBy(() -> annotationInterface.prop4());

		Map<String,String> map = new HashMap<>();
		map.put("prop1", "value1");
		map.put("prop2", "value2");
		AnnotationInterface annotationInterfaceFromMap = converter.convert(map)
				.to(AnnotationInterface.class);

		assertThat(annotationInterfaceFromMap.prop1()).isEqualTo("value1");
		assertThat(annotationInterfaceFromMap.prop2()).isEqualTo("value2");
		map.put("prop2", null);
		assertThat(annotationInterfaceFromMap.prop2()).isNull();
		map.remove("prop2");
		// still use default
		assertThat(annotationInterfaceFromMap.prop2()).isEqualTo("value2");

		AnnotationInterface annotation = AnnotatedMappingClass.class
				.getAnnotation(AnnotationInterface.class);

		@SuppressWarnings("unchecked")
		Map<String,String> convertedMap = converter.convert(annotation)
				.to(Map.class);
		assertThat(convertedMap).isNotNull();
		// detach from live view
		convertedMap.put("prop5", "value5");
		assertThat(convertedMap.get("prop1")).isEqualTo(annotation.prop1());
		assertThat(convertedMap.get("prop2")).isEqualTo(annotation.prop2());
		assertThat(convertedMap.get("prop3")).isEqualTo(annotation.prop3());
		assertThat(convertedMap.get("prop4")).isEqualTo(annotation.prop4());
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.5 - Java Beans
	 * <p/>
	 * Java Beans are concrete (non-abstract) classes that follow the Java Bean
	 * naming convention. They provide public getters and setters to access
	 * their properties and have a public no-parameter constructor. When
	 * converting from a Java Bean introspection is used to find the read
	 * accessors. A read accessor must have no arguments and a non-void return
	 * value. The method name must start with get followed by a capitalized
	 * property name, for example getSize() provides access to the property
	 * size. For boolean/Boolean properties a prefix of <code><i>is<i></code> is
	 * also permitted. Properties names follow the the section called Key
	 * Mapping.
	 * <p/>
	 * For the converter to consider an object as a Java Bean the sourceAsBean()
	 * or targetAsBean() modifier needs to be invoked, for example:
	 * <p/>
	 * <code>  Map m = converter.convert(myBean).sourceAsBean().to(Map.class);</code>
	 * <p/>
	 * When converting to a Java Bean, the bean is constructed eagerly. All
	 * available properties are set in the bean using the bean's write
	 * accessors, that is, public setters methods with a single argument. All
	 * methods of the bean class itself and its super classes are considered.
	 * When a property cannot be converted this will cause a
	 * <code>ConversionException</code>. If a property is missing in the source,
	 * the property will not be set in the bean.
	 * <p/>
	 * Note: access via indexed bean properties is not supported.
	 * <p/>
	 * Note: the getClass() method of the java.lang.Object class is not
	 * considered an accessor.
	 */
	@Test
	public void testJavaBeanConversion() {

		ConversionComplianceTest.ExtObject embedded = new ConversionComplianceTest.ExtObject();

		MappingBean bean = new MappingBean();
		bean.setP("value1");
		bean.setProp2("value2");
		bean.setProp3("value3");
		bean.setEmbedded(embedded);

		Converter converter = Converters.standardConverter();
		try {
			converter.convert(bean)
					.to(new TypeReference<Map<String,String>>() {});
			fail("No rule if not declared as JavaBean");
		} catch (ConversionException e) {}

		Map<String,String> converted = converter.convert(bean)
				.sourceAsBean()
				.to(
				new TypeReference<Map<String,String>>() {});
		assertThat(converted).isNotNull();
		assertThat(converted.size()).isEqualTo(4);
		assertThat(converted.get("embedded")).isEqualTo("extended");

		assertThatExceptionOfType(ConversionException.class).as("No way to create ExtObject")
				.isThrownBy(() -> converter.convert(converted).targetAsBean().to(MappingBean.class));

		converted.remove("embedded");
		MappingBean convertedBean = converter.convert(converted)
				.targetAsBean()
				.to(MappingBean.class);
		assertThat(convertedBean).isNotNull();
		assertThat(convertedBean.getP()).isEqualTo(bean.getP());
		assertThat(convertedBean.getProp2()).isEqualTo(bean.getProp2());
		assertThat(convertedBean.getProp3()).isEqualTo(bean.getProp3());
		assertThat(convertedBean.getEmbedded()).isNull();
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.6 - DTOs
	 * <p/>
	 * DTOs are classes with public non-static fields and no methods other than
	 * the ones provided by the java.lang.Object class. OSGi DTOs extend the
	 * org.osgi.dto.DTO class, however objects following the DTO rules that do
	 * not extend the DTO class are also treated as DTOs by the converter. DTOs
	 * may have static fields, or non-public instance fields. These are ignored
	 * by the converter.
	 * <p/>
	 * When converting from a DTO to another map-like structure each public
	 * instance field is considered. The field name is taken as the key for the
	 * map entry, taking into account the section called
	 * <code><I>Key Mapping<I></code>, the field value is taken as the value for
	 * the map entry.
	 * <p/>
	 * When converting to a DTO, the converter attempts to find fields that
	 * match the key of each entry in the source map and then converts the value
	 * to the field type before assigning it. The key of the map entries may
	 * need to be converted into a String first. Keys are mapped according to
	 * the section called <code><I>Key Mapping<I></code>. The DTO is constructed
	 * using its no-parameter constructor and each public field is filled with
	 * data from the source eagerly. Fields present in the DTO but missing in
	 * the source object not be set.
	 * <p/>
	 * The converter only considers a type to be a DTO type if it declares no
	 * methods. However, if a type needs to be treated as a DTO that has
	 * methods, the converter can be instructed to do this using the
	 * sourceAsDTO() and targetAsDTO() modifiers.
	 */
	@Test
	public void testDTOConversion() {
		Converter converter = Converters.standardConverter();

		WithStaticAndPrivateFieldsDTOLike withStaticAndPrivateFieldsDTOLike = new WithStaticAndPrivateFieldsDTOLike();
		withStaticAndPrivateFieldsDTOLike.prop1 = "value1";
		withStaticAndPrivateFieldsDTOLike.prop2 = "value2";
		@SuppressWarnings("unchecked")
		Map<String,String> map = converter
				.convert(withStaticAndPrivateFieldsDTOLike)
				.sourceAsDTO()
				.to(Map.class);
		assertThat(map).isNotNull();
		assertThat(map.get("prop0")).isNull();
		assertThat(map.get("STATIC_FIELD")).isNull();
		assertThat(map.get("prop1")).isEqualTo(withStaticAndPrivateFieldsDTOLike.prop1);
		assertThat(map.get("prop2")).isEqualTo(withStaticAndPrivateFieldsDTOLike.prop2);

		NotDTOLike notDtoLike = new NotDTOLike();
		notDtoLike.prop1 = "value1_";
		notDtoLike.prop2 = "value2";
		notDtoLike.prop3 = "value3";
		AnnotationInterface annotationConverted = converter.convert(notDtoLike)
				.sourceAsDTO()
				.to(AnnotationInterface.class);
		assertThat(annotationConverted).isNotNull();
		assertThat(annotationConverted.prop1()).isEqualTo(notDtoLike.prop1);
		assertThat(annotationConverted.prop2()).isEqualTo(notDtoLike.prop2);
		assertThat(annotationConverted.prop3()).isEqualTo(notDtoLike.prop3);
		notDtoLike.generateProp3();
		assertThat(annotationConverted.prop3()).isEqualTo(notDtoLike.prop3);

		assertThatExceptionOfType(ConversionException.class).as("ConversionException expected for undefined field")
				.isThrownBy(() -> annotationConverted.prop4());

		map = new HashMap<String,String>();
		map.put("prop1", "mapValue1");
		map.put("prop3", "mapValue3");
		map.put("prop4", "mapValue4");
		NotDTOLike notDtoLikeConverted = converter.convert(map)
				.targetAsDTO()
				.to(NotDTOLike.class);
		assertThat(notDtoLikeConverted).isNotNull();
		assertThat(notDtoLikeConverted.prop2).isNull();
		assertThat(notDtoLikeConverted.prop1).isEqualTo(map.get("prop1"));
		assertThat(notDtoLikeConverted.prop3).isEqualTo(map.get("prop3"));
	}

	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.7 - Types with getProperties()
	 * <p/>
	 * The converter uses reflection to find a public java.util.Map
	 * getProperties() or java.util.Dictionary getProperties() method on the
	 * source type to obtain a map view over the source object. This map view is
	 * used to convert the source object to a map-like structure.
	 * <p/>
	 * If the source object both implements an interface and also has a public
	 * getProperties() method, the converter uses the getProperties() method to
	 * obtain the map view. This getProperties() may or may not be part of an
	 * implemented interface.
	 * <p/>
	 * Note: this mechanism can only be used to convert to another type. The
	 * reverse is not supported
	 */
	@Test
	public void testTypesWithGetPropertiesConversion() {

		Converter converter = Converters.standardConverter();
		TypeWithGetProperties typeWithGetProperties = new TypeWithGetProperties(
				true);
		@SuppressWarnings("unchecked")
		Map<String,String> map = converter.convert(typeWithGetProperties)
				.to(Map.class);

		assertThat(map).isNotNull();
		assertThat(typeWithGetProperties.prop1()).isEqualTo(map.get("prop1"));
		assertThat(typeWithGetProperties.prop2()).isEqualTo(map.get("prop2"));
		assertThat(typeWithGetProperties.prop3()).isEqualTo(map.get("prop3"));
		assertThat(typeWithGetProperties.prop4()).isEqualTo(map.get("prop4"));
		assertThat(typeWithGetProperties.prop5()).isEqualTo(map.get("prop5"));

		try {
			@SuppressWarnings("unused")
			TypeWithGetProperties convertedTypeWithGetProperties = converter
					.convert(map)
					.to(TypeWithGetProperties.class);
			fail("conversion to Type with getProperties() not supported");
		} catch (ConversionException e) {}

	}
	
	/**
	 * Section 707.4.4 : Maps, Interfaces, Java Beans, DTOs and Annotations
	 * <p/>
	 * 707.4.4.4 - Converting to a map-like structure
	 * <p/>
	 * 707.4.4.4.8 -Key Mapping
	 * <p/>
	 * When converting to or from a Java type, the key is derived from the
	 * method or field name. Certain common property name characters, such as
	 * full stop ('.' \u002E) and hyphen-minus ('-' \u002D) are not valid in
	 * Java identifiers. So the name of a method must be converted to its
	 * corresponding key name as follows:
	 * <p/>
	 * <ul>
	 * <li>A single dollar sign (<code class="code">'$' \u0024</code>) is
	 * removed unless it is followed by:
	 * <ul>
	 * <li>A low line (<code class="code">'_' \u005F</code>) and a dollar sign
	 * in which case the three consecutive characters (<code>"$_$"</code>) are
	 * converted to a single hyphen-minus (<code>'-' \u002D).</li>
	 <li>Another dollar sign in which case the two
	 consecutive dollar signs (<code>"$$"</code>) are converted to a single
	 * dollar sign.</li>
	 * </ul>
	 * </li>
	 * <li>A single low line (<code>'_' \u005F</code>) is converted into a full
	 * stop (<code>'.' \u002E</code>) unless is it followed by another low line
	 * in which case the two consecutive low lines (<code>"__"</code>) are
	 * converted to a single low line.</li>
	 * <li>All other characters are unchanged.</li>
	 * <li>If the type that declares the method also declares a static final
	 * <code>PREFIX_</code> field whose value is a compile-time constant
	 * <code>String</code>, then the key name is prefixed with the value of the
	 * <code>PREFIX_</code> field. <code>PREFIX_</code> fields in super-classes
	 * or super-interfaces are ignored.</li>
	 * </ul>
	 * <p/>
	 * However, if the type is a <em>single-element annotation</em>, then the
	 * key name for the <code>value</code> method is derived from the name of
	 * the component property type rather than the name of the method. In this
	 * case, the simple name of the component property type, that is, the name
	 * of the class without any package name or outer class name, if the
	 * component property type is an inner class, must be converted to the
	 * <code>value</code> method's property name as follows:
	 * <p/>
	 * <ul>
	 * <li>When a lower case character is followed by an upper case character, a
	 * full stop (<code>'.' \u002E</code>) is inserted between them.</li>
	 * <li>Each uppercase character is converted to lower case.</li>
	 * <li>All other characters are unchanged.</li>
	 * <li>If the annotation type declares a <code>PREFIX_</code> field whose
	 * value is a compile-time constant <code>String</code>, then the id is
	 * prefixed with the value of the <code>PREFIX_</code> field.</li>
	 * </ul>
	 */
	@Test
	public void testKeyMapping() {

		Map<String,String> resultmap = new HashMap<String,String>();
		resultmap.put("org.osgi.util.converter.test.specialprop",
				"org.osgi.util.converter.test.specialprop");
		resultmap.put("org.osgi.util.converter.test.special$prop",
				"org.osgi.util.converter.test.special$prop");
		resultmap.put("org.osgi.util.converter.test.special.prop",
				"org.osgi.util.converter.test.special.prop");
		resultmap.put("org.osgi.util.converter.test..specialprop",
				"org.osgi.util.converter.test..specialprop");
		resultmap.put("org.osgi.util.converter.test.special_prop",
				"org.osgi.util.converter.test.special_prop");
		resultmap.put("org.osgi.util.converter.test.special_.prop",
				"org.osgi.util.converter.test.special_.prop");
		resultmap.put("org.osgi.util.converter.test.special._prop",
				"org.osgi.util.converter.test.special._prop");
		resultmap.put("org.osgi.util.converter.test.special..prop",
				"org.osgi.util.converter.test.special..prop");
		resultmap.put("org.osgi.util.converter.test.special-prop",
				"org.osgi.util.converter.test.special-prop");
		resultmap.put("org.osgi.util.converter.test.special$.prop",
				"org.osgi.util.converter.test.special$.prop");

		Converter converter = Converters.standardConverter();

		KeyMappingDTOLike dto = new KeyMappingDTOLike();
		dto.special$prop = "org.osgi.util.converter.test.specialprop";
		dto.special$$prop = "org.osgi.util.converter.test.special$prop";
		dto.special_prop = "org.osgi.util.converter.test.special.prop";
		dto._specialprop = "org.osgi.util.converter.test..specialprop";
		dto.special__prop = "org.osgi.util.converter.test.special_prop";
		dto.special___prop = "org.osgi.util.converter.test.special_.prop";
		dto.special_$__prop = "org.osgi.util.converter.test.special._prop";
		dto.special_$_prop = "org.osgi.util.converter.test.special..prop";
		dto.special$_$prop = "org.osgi.util.converter.test.special-prop";
		dto.special$$_$prop = "org.osgi.util.converter.test.special$.prop";

		Map<String,String> map = converter.convert(dto)
				.to(new TypeReference<Map<String,String>>() {});
		assertThat(map).isEqualTo(resultmap);

		KeyMappingDTOLike resultdto = converter.convert(resultmap)
				.targetAsDTO()
				.to(KeyMappingDTOLike.class);

		assertThat(resultdto.special$prop).isEqualTo(dto.special$prop);
		assertThat(resultdto.special$$prop).isEqualTo(dto.special$$prop);
		assertThat(resultdto.special_prop).isEqualTo(dto.special_prop);
		assertThat(resultdto._specialprop).isEqualTo(dto._specialprop);
		assertThat(resultdto.special__prop).isEqualTo(dto.special__prop);
		assertThat(resultdto.special___prop).isEqualTo(dto.special___prop);
		assertThat(resultdto.special_$__prop).isEqualTo(dto.special_$__prop);
		assertThat(resultdto.special_$_prop).isEqualTo(dto.special_$_prop);
		assertThat(resultdto.special$_$prop).isEqualTo(dto.special$_$prop);
		assertThat(resultdto.special$$_$prop).isEqualTo(dto.special$$_$prop);

		KeyMappingAnnotation keyMappingAnnotation = KeyMappingAnnotatedClass.class
				.getAnnotation(KeyMappingAnnotation.class);

		map = converter.convert(keyMappingAnnotation)
				.to(new TypeReference<Map<String,String>>() {});
		assertThat(map).isEqualTo(resultmap);

		KeyMappingAnnotation resultkeyMappingAnnotation = converter
				.convert(resultmap)
				.to(KeyMappingAnnotation.class);

		assertThat(resultkeyMappingAnnotation.special$prop()).isEqualTo(keyMappingAnnotation.special$prop());
		assertThat(resultkeyMappingAnnotation.special$$prop()).isEqualTo(keyMappingAnnotation.special$$prop());
		assertThat(resultkeyMappingAnnotation.special_prop()).isEqualTo(keyMappingAnnotation.special_prop());
		assertThat(resultkeyMappingAnnotation._specialprop()).isEqualTo(keyMappingAnnotation._specialprop());
		assertThat(resultkeyMappingAnnotation.special__prop()).isEqualTo(keyMappingAnnotation.special__prop());
		assertThat(resultkeyMappingAnnotation.special___prop()).isEqualTo(keyMappingAnnotation.special___prop());
		assertThat(resultkeyMappingAnnotation.special_$__prop()).isEqualTo(keyMappingAnnotation.special_$__prop());
		assertThat(resultkeyMappingAnnotation.special_$_prop()).isEqualTo(keyMappingAnnotation.special_$_prop());
		assertThat(resultkeyMappingAnnotation.special$_$prop()).isEqualTo(keyMappingAnnotation.special$_$prop());
		assertThat(resultkeyMappingAnnotation.special$$_$prop()).isEqualTo(keyMappingAnnotation.special$$_$prop());

		KeyMappingBean bean = new KeyMappingBean();
		bean.setSpecial$prop("org.osgi.util.converter.test.specialprop");
		bean.setSpecial$$prop("org.osgi.util.converter.test.special$prop");
		bean.setSpecial_prop("org.osgi.util.converter.test.special.prop");
		bean.set_specialprop("org.osgi.util.converter.test..specialprop");
		bean.setSpecial__prop("org.osgi.util.converter.test.special_prop");
		bean.setSpecial___prop("org.osgi.util.converter.test.special_.prop");
		bean.setSpecial_$__prop("org.osgi.util.converter.test.special._prop");
		bean.setSpecial_$_prop("org.osgi.util.converter.test.special..prop");
		bean.setSpecial$_$prop("org.osgi.util.converter.test.special-prop");
		bean.setSpecial$$_$prop("org.osgi.util.converter.test.special$.prop");

		map = converter.convert(bean)
				.sourceAsBean()
				.to(new TypeReference<Map<String,String>>() {});
		Map<String,String> resultmap2 = new HashMap<>(resultmap);
		resultmap2.remove("org.osgi.util.converter.test..specialprop");
		assertThat(map).isEqualTo(resultmap2);

		KeyMappingBean resultbean = converter.convert(resultmap)
				.targetAsBean()
				.to(KeyMappingBean.class);
		assertThat(resultbean.getSpecial$prop()).isEqualTo(bean.getSpecial$prop());
		assertThat(resultbean.getSpecial$$prop()).isEqualTo(bean.getSpecial$$prop());
		assertThat(resultbean.getSpecial_prop()).isEqualTo(bean.getSpecial_prop());
		assertThat(resultbean.getSpecial__prop()).isEqualTo(bean.getSpecial__prop());
		assertThat(resultbean.getSpecial___prop()).isEqualTo(bean.getSpecial___prop());
		assertThat(resultbean.getSpecial_$__prop()).isEqualTo(bean.getSpecial_$__prop());
		assertThat(resultbean.getSpecial_$_prop()).isEqualTo(bean.getSpecial_$_prop());
		assertThat(resultbean.getSpecial$_$prop()).isEqualTo(bean.getSpecial$_$prop());
		assertThat(resultbean.getSpecial$$_$prop()).isEqualTo(bean.getSpecial$$_$prop());

		KeyMappingInterface inter = new KeyMappingInterface() {
			String	_specialprop	= "org.osgi.util.converter.test..specialprop";
			String	special$$_$prop	= "org.osgi.util.converter.test.special$.prop";
			String	special$$prop	= "org.osgi.util.converter.test.special$prop";
			String	special$_$prop	= "org.osgi.util.converter.test.special-prop";
			String	special$prop	= "org.osgi.util.converter.test.specialprop";
			String	special_$__prop	= "org.osgi.util.converter.test.special._prop";
			String	special_$_prop	= "org.osgi.util.converter.test.special..prop";
			String	special___prop	= "org.osgi.util.converter.test.special_.prop";
			String	special__prop	= "org.osgi.util.converter.test.special_prop";
			String	special_prop	= "org.osgi.util.converter.test.special.prop";

			@Override
			public String special$prop() {
				return special$prop;
			}

			@Override
			public String special$$prop() {
				return special$$prop;
			}

			@Override
			public String special_prop() {
				return special_prop;
			}

			@Override
			public String _specialprop() {
				return _specialprop;
			}

			@Override
			public String special__prop() {
				return special__prop;
			}

			@Override
			public String special___prop() {
				return special___prop;
			}

			@Override
			public String special_$__prop() {
				return special_$__prop;
			}

			@Override
			public String special_$_prop() {
				return special_$_prop;
			}

			@Override
			public String special$_$prop() {
				return special$_$prop;
			}

			@Override
			public String special$$_$prop() {
				return special$$_$prop;
			}
		};

		map = converter.convert(inter)
				.sourceAs(KeyMappingInterface.class)
				.to(
				new TypeReference<Map<String,String>>() {});
		assertThat(map).isEqualTo(resultmap);

		KeyMappingInterface resultinter = converter.convert(resultmap)
				.to(KeyMappingInterface.class);

		assertThat(resultinter.special$prop()).isEqualTo(inter.special$prop());
		assertThat(resultinter.special$$prop()).isEqualTo(inter.special$$prop());
		assertThat(resultinter.special_prop()).isEqualTo(inter.special_prop());
		assertThat(resultinter._specialprop()).isEqualTo(inter._specialprop());
		assertThat(resultinter.special__prop()).isEqualTo(inter.special__prop());
		assertThat(resultinter.special___prop()).isEqualTo(inter.special___prop());
		assertThat(resultinter.special_$__prop()).isEqualTo(inter.special_$__prop());
		assertThat(resultinter.special_$_prop()).isEqualTo(inter.special_$_prop());
		assertThat(resultinter.special$_$prop()).isEqualTo(inter.special$_$prop());
		assertThat(resultinter.special$$_$prop()).isEqualTo(inter.special$$_$prop());
	}

	@Test
	public void testMapToDTOWithGenerics() {
		Map<String,Object> dto = new HashMap<>();

		dto.put("longList", Arrays.asList((short) 999, "1000"));

		Map<String,Object> dtoMap = new LinkedHashMap<>();
		dto.put("dtoMap", dtoMap);

		Map<String,Object> subDTO1 = new HashMap<>();
		subDTO1.put("charSet",
				new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));
		dtoMap.put("zzz", subDTO1);

		Map<String,Object> subDTO2 = new HashMap<>();
		subDTO2.put("charSet", new HashSet<>(Arrays.asList('b', 'a', 'r')));
		dtoMap.put("aaa", subDTO2);

		Converter converter = Converters.standardConverter();
		MyDTO2 converted = converter.convert(dto).to(MyDTO2.class);

		assertThat(converted.longList).isEqualTo(Arrays.asList(999L, 1000L));
		Map<String,MyDTO3> nestedMap = converted.dtoMap;

		// Check iteration order is preserved by iterating
		int i = 0;
		for (Iterator<Map.Entry<String,MyDTO3>> it = nestedMap.entrySet()
				.iterator(); it.hasNext(); i++) {
			Map.Entry<String,MyDTO3> entry = it.next();
			switch (i) {
				case 0 :
					assertThat(entry.getKey()).isEqualTo("zzz");
					MyDTO3 dto1 = entry.getValue();
					assertThat(dto1.charSet).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
					break;
				case 1 :
					assertThat(entry.getKey()).isEqualTo("aaa");
					MyDTO3 dto2 = entry.getValue();
					assertThat(dto2.charSet).isEqualTo(new HashSet<Character>(
									Arrays.asList('b', 'a', 'r')));
					break;
				default :
					fail("Unexpected number of elements on map");
			}
		}
	}

	@Test
	public void testMapToDTOWithGenericVariables() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("set", new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));
		dto.put("raw", "1234");
		dto.put("array", Arrays.asList("foo", (int) 'o', 'o'));

		Converter converter = Converters.standardConverter();
		MyGenericDTOWithVariables<Character> converted = converter.convert(dto)
				.to(new TypeReference<MyGenericDTOWithVariables<Character>>() {});
		assertThat(converted.raw).isEqualTo(Character.valueOf('1'));
		assertThat(converted.array).isEqualTo(new Character[]{
				'f', 'o', 'o'
		});
		assertThat(converted.set).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
	}

	@Test
	public void testMapToInterfaceWithGenerics() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("charSet", new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));

		Converter converter = Converters.standardConverter();
		MyGenericInterface converted = converter.convert(dto)
				.to(MyGenericInterface.class);
		assertThat(converted.charSet()).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
	}

	@Test
	public void testMapToInterfaceWithErrorHandler() {
		Map<String,Object> dto = new HashMap<>();
		final Object BAD = new Object();
		dto.put("charSet", new HashSet<>(Arrays.asList("foo", BAD, 'o')));

		Converter converter = Converters.newConverterBuilder()
				.errorHandler(new ConverterFunction() {
					@Override
					public Object apply(Object obj, Type targetType)
							throws Exception {
						Class< ? > clazz = null;
						try {
							clazz = (Class< ? >) ((ParameterizedType) targetType)
									.getRawType();
						} catch (ClassCastException e) {
							clazz = (Class< ? >) targetType;
						}
						if ((obj == BAD)
								&& Character.class.isAssignableFrom(clazz)) {
							return Character.valueOf('O');
						}
						return ConverterFunction.CANNOT_HANDLE;
					}
				})
				.build();
		MyGenericInterface converted = converter.convert(dto)
				.to(MyGenericInterface.class);
		assertThat(converted.charSet()).containsExactlyInAnyOrder('f', 'O',
				'o');
	}

	@Test
	public void testMapToInterfaceWithGenericVariables() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("set", new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));
		dto.put("raw", "1234");
		dto.put("array", Arrays.asList("foo", (int) 'o', 'o'));

		Converter converter = Converters.standardConverter();
		MyGenericInterfaceWithVariables<Character> converted = converter
				.convert(dto)
				.to(new TypeReference<MyGenericInterfaceWithVariables<Character>>() {});
		assertThat(converted.raw()).isEqualTo(Character.valueOf('1'));
		assertThat(converted.array()).isEqualTo(new Character[]{
				'f', 'o', 'o'
		});
		assertThat(converted.set()).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
	}

	@Test
	public void testConvertMarkerAnnotation() {
		Converter converter = Converters.standardConverter();

		MyMarkerAnnotation ann = MarkedInterface.class
				.getAnnotation(MyMarkerAnnotation.class);
		Map< ? , ? > m = converter.convert(ann).to(Map.class);
		assertThat(m.size()).isOne();
		assertThat(m.get("my.marker.annotation")).isEqualTo(Boolean.TRUE);

		Object res = converter.convert(m).to(MyMarkerAnnotation.class);
		assertThat(res).isInstanceOf(MyMarkerAnnotation.class);
		Object res2 = converter.convert(
				Collections.singletonMap("my.marker.annotation", Boolean.TRUE))
				.to(MyMarkerAnnotation.class);
		assertThat(res2).isInstanceOf(MyMarkerAnnotation.class);
		Object res3 = converter
				.convert(Collections.singletonMap("my.marker.annotation",
						"true"))
				.to(MyMarkerAnnotation.class);
		assertThat(res3).isInstanceOf(MyMarkerAnnotation.class);

		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a Conversion Exception")
				.isThrownBy(() -> converter.convert(new HashMap<String,Object>()) .to(MyMarkerAnnotation.class));

		Map<String,String> m2 = converter.convert(ann)
				.to(new TypeReference<Map<String,String>>() {});
		assertThat(m2.get("my.marker.annotation")).isEqualTo("true");
	}

	@Test
	public void testConvertSingleElementAnnotation() {
		Converter converter = Converters.standardConverter();

		SingleElementAnnotation ann = SingleElementAnnotatedClass.class
				.getAnnotation(SingleElementAnnotation.class);
		Map< ? , ? > m = converter.convert(ann).to(Map.class);
		assertThat(m.size()).isOne();
		assertThat(m.get("single.element.annotation")).isEqualTo("123");
		Map<Object, Object> m2 = new HashMap<>(m);
		m2.put("some.key", "some.value");

		SingleElementAnnotation res = converter.convert(m2)
				.to(SingleElementAnnotation.class);
		assertThat(res.value()).isEqualTo("123");
		SingleElementAnnotation res2 = converter
				.convert(Collections.singletonMap("single.element.annotation",
						456))
				.to(SingleElementAnnotation.class);
		assertThat(res2.value()).isEqualTo("456");

		SingleElementAnnotation res3 = converter.convert(Collections.emptyMap())
				.to(SingleElementAnnotation.class);
		assertThat(res3.value()).isEqualTo("nothing!");

		Map<String,Long> m3 = converter.convert(res2)
				.to(new TypeReference<Map<String,Long>>() {});
		assertThat(m3.get("single.element.annotation")).isEqualTo(Long.valueOf(456L));
	}

	@Test
	public void testSingleElementAnnotationPrefix() {
		Map<String,String> m = new HashMap<>();
		m.put("org.foo.bar.single.element.annotation.prefix", "-999");
		// m.put("XXXsingle.element.annotation.prefix", "-999");

		Converter converter = Converters.standardConverter();
		SingleElementAnnotationPrefix ann = converter.convert(m)
				.to(SingleElementAnnotationPrefix.class);
		assertThat(ann.value()).isEqualTo(-999L);
	}

	static interface EmptyInterface {
	}

	static interface EmptyInterface2 extends EmptyInterface {
	}

	static interface NonEmptyInterface {
		int a();
	}

	static interface EmptyInterface3 extends NonEmptyInterface {
	}

	@Test
	public void testMapToEmptyInterface() throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("a", "b");
		EmptyInterface i = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface.class);
		assertThat(i).isInstanceOf(EmptyInterface.class);

		EmptyInterface2 j = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface2.class);
		assertThat(j).isInstanceOf(EmptyInterface2.class);

		EmptyInterface3 k = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface3.class);
		assertThat(k).isInstanceOf(EmptyInterface3.class);
	}

	public static interface InterfaceWithDefaultMethod {
		public static final String RESULT = "r";

		public default String defaultMethod() {
			return RESULT;
		}
	}

	@Test
	public void testDefaultInterfaceMethod() throws Throwable {
		Class< ? > clazz = InterfaceWithDefaultMethod.class;
		InterfaceWithDefaultMethod i = (InterfaceWithDefaultMethod) Converters
				.standardConverter()
				.convert(new HashMap<String,Object>())
				.to(clazz);
		assertThat(i.defaultMethod()).isEqualTo(InterfaceWithDefaultMethod.RESULT);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface PrefixMarkerAnnotation {
		static final String PREFIX_ = "org.foo.bar.";
	}

	@Test
	@PrefixMarkerAnnotation
	public void testMarkerAnnotationPrefixToMap() throws Exception {
		final Converter converter = Converters.standardConverter();
		Method method = getClass().getMethod("testMarkerAnnotationPrefixToMap");
		PrefixMarkerAnnotation annotation = method
				.getDeclaredAnnotation(PrefixMarkerAnnotation.class);
		Map<String,Object> map = converter.convert(annotation)
				.to(new TypeReference<Map<String,Object>>() {
				});
		assertThat(map).containsKey("org.foo.bar.prefix.marker.annotation");
		assertThat((Boolean) map.get("org.foo.bar.prefix.marker.annotation")).isTrue();
	}
}
