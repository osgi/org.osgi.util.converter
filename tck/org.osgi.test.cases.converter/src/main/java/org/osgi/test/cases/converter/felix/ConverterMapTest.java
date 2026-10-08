/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.cases.converter.felix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.cases.converter.junit.MapInterfaceJavaBeansDTOAndAnnotationConversionComplianceTest.PrefixMarkerAnnotation;
import org.osgi.util.converter.ConversionException;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.Rule;
import org.osgi.util.converter.TypeReference;

public class ConverterMapTest {
	private Converter converter;

	@BeforeEach
	public void setUp() {
		converter = Converters.standardConverter();
	}

	@AfterEach
	public void tearDown() {
		converter = null;
	}

	@Test
	public void testGenericMapConversion() {
		Map<Integer,String> m1 = Collections.singletonMap(42, "987654321");
		Map<String,Long> m2 = converter.convert(m1)
				.to(new TypeReference<Map<String,Long>>() {
				});
		assertThat(m2).hasSize(1);
		assertThat((long) m2.get("42")).isEqualTo(987654321L);
	}

	@Test
	public void testConvertMapToDictionary() throws Exception {
		Map<BigInteger,URL> m = new HashMap<>();
		BigInteger bi = new BigInteger("123");
		URL url = new URL("http://0.0.0.0:123");
		m.put(bi, url);

		@SuppressWarnings("unchecked")
		Dictionary<BigInteger,URL> d = converter.convert(m)
				.to(Dictionary.class);
		assertThat(d.size()).isOne();
		assertThat(d.keys().nextElement()).isSameAs(bi);
		assertThat(d.get(bi)).isSameAs(url);
	}

	@Test
	public void testJavaBeanToMap() {
		MyBean mb = new MyBean();
		mb.setMe("You");
		mb.setF(true);
		mb.setNumbers(new int[] {
				3, 2, 1
		});

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(mb).sourceAsBean().to(Map.class);
		assertThat(m.size()).isEqualTo(5);
		assertThat(m.get("me")).isEqualTo("You");
		assertThat((boolean) m.get("f")).isTrue();
		assertThat((boolean) m.get("enabled")).isFalse();
		assertThat((int[]) m.get("numbers")).isEqualTo(new int[] {
				3, 2, 1
		});
	}

	@Test
	public void testJavaBeanToMapCustom() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
		Date d = new Date();
		String expectedDate = sdf.format(d);

		MyBean mb = new MyBean();
		mb.setStartDate(d);
		mb.setEnabled(true);

		ConverterBuilder cb = Converters.newConverterBuilder();
		cb.rule(new Rule<Date,String>(v -> sdf.format(v)) {
		});
		cb.rule(new Rule<String,Date>(v -> {
			try {
				return sdf.parse(v);
			} catch (Exception ex) {
				return null;
			}
		}) {
		});
		Converter ca = cb.build();
		Map<String,String> m = ca.convert(mb)
				.sourceAsBean()
				.to(new TypeReference<Map<String,String>>() {
				});
		assertThat(m.get("enabled")).isEqualTo("true");
		assertThat(m.get("startDate")).isEqualTo(expectedDate);
	}

	@Test
	public void testMapToJavaBean() {
		Map<String,String> m = new HashMap<>();

		m.put("me", "Joe");
		m.put("enabled", "true");
		m.put("numbers", "42");
		m.put("s", "will disappear");
		MyBean mb = converter.convert(m).targetAsBean().to(MyBean.class);
		assertThat(mb.getMe()).isEqualTo("Joe");
		assertThat(mb.isEnabled()).isTrue();
		assertThat(mb.getF()).isNull();
		assertThat(mb.getNumbers()).isEqualTo(new int[] {
				42
		});
	}

	public void testMapToJavaBean2() {
		Map<String,String> m = new HashMap<>();

		m.put("blah", "blahblah");
		m.put("f", "true");
		MyBean mb = converter.convert(m).to(MyBean.class);
		assertThat(mb.getMe()).isNull();
		assertThat(mb.getF()).isTrue();
		assertThat(mb.isEnabled()).isFalse();
		assertThat(mb.getNumbers()).isNull();
	}

	@Test
	public void testInterfaceToMap() {
		TestInterface impl = new TestInterface() {
			@Override
			public String foo() {
				return "Chocolate!";
			}

			@Override
			public int bar() {
				return 76543;
			}

			@Override
			public int bar(String def) {
				return 0;
			}

			@Override
			public Boolean za_za() {
				return true;
			}
		};

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(impl).to(Map.class);
		assertThat(m.size()).isEqualTo(3);
		assertThat(m.get("foo")).isEqualTo("Chocolate!");
		assertThat((int) m.get("bar")).isEqualTo(76543);
		assertThat((boolean) m.get("za.za")).isTrue();
	}

	@Test
	public void testInterfaceToMapEmptySub() {
		TestInterfaceSub impl = new TestInterfaceSubClass();

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(impl).to(Map.class);
		assertThat(m.size()).isEqualTo(3);
		assertThat(m.get("foo")).isEqualTo("Chocolate!");
		assertThat((int) m.get("bar")).isEqualTo(76543);
		assertThat((boolean) m.get("za.za")).isTrue();
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	@Test
	public void testMapToInterface1() {
		Map m = new HashMap<>();
		m.put("foo", 12345);
		m.put("bar", "999");
		m.put("alt", "someval");
		m.put("za.za", true);

		TestInterface ti = converter.convert(m).to(TestInterface.class);
		assertThat(ti.foo()).isEqualTo("12345");
		assertThat(ti.bar()).isEqualTo(999);
		assertThat(ti.za_za()).isEqualTo(Boolean.TRUE);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testMapToInterface2() {
		Map m = new HashMap<>();

		TestInterface ti = converter.convert(m).to(TestInterface.class);
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a conversion exception")
				.isThrownBy(() -> ti.foo());
		assertThat(ti.bar("999")).isEqualTo(999);
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a conversion exception")
				.isThrownBy(() -> assertThat(ti.za_za()).isNull());
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	@Test
	public void testMapToAnnotation1() {
		Map m = new HashMap<>();
		m.put("foo", 12345);
		m.put("bar", "999");
		m.put("alt", "someval");
		m.put("za.za", true);

		TestAnnotation ta = converter.convert(m).to(TestAnnotation.class);
		assertThat(ta.foo()).isEqualTo("12345");
		assertThat(ta.bar()).isEqualTo(999);
		assertThat(ta.za_za()).isTrue();
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	@Test
	public void testMapToAnnotationDefaults() {
		Map m = new HashMap<>();
		m.put("alt", "someval");

		TestAnnotation ta = converter.convert(m).to(TestAnnotation.class);
		assertThat(ta.foo()).isEqualTo("fooo!");
		assertThat(ta.bar()).isEqualTo(42);
	}

	@Test
	public void testAnnotationMethods() {
		TestAnnotation ta = converter.convert(new HashMap<>())
				.to(TestAnnotation.class);
		Map<String,Object> m = converter.convert(ta)
				.view()
				.to(new TypeReference<Map<String,Object>>() {
				});
		assertThat(m.size()).isEqualTo(3);
		assertThat(m.get("foo")).isEqualTo("fooo!");
		assertThat(m.get("bar")).isEqualTo(42);
		try {
			assertThat(m.get("za.za")).isEqualTo(false);
			fail("Should have thrown a conversion exception as there is no default for 'za.za'");
		} catch (ConversionException ce) {
			// good
		}
	}

	@Test
	@SingleElementAnnotation(value = {
			"hi", "there"
	}, somethingElse = 42L)
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	public void testSingleElementAnnotation() throws Exception {
		Method method = getClass().getMethod("testSingleElementAnnotation");
		SingleElementAnnotation sea = method
				.getDeclaredAnnotation(SingleElementAnnotation.class);
		Map m = converter.convert(sea).to(Map.class);
		assertThat(m).hasSize(2);
		assertThat((String[]) m.get("single.element.annotation")).isEqualTo(new String[] {
				"hi", "there"
		});
		assertThat(m.get("somethingElse")).isEqualTo(42L);

		m.put("somethingElse", 51.0);
		SingleElementAnnotation sea2 = converter.convert(m)
				.to(SingleElementAnnotation.class);
		assertThat(sea2.value()).isEqualTo(new String[] {
				"hi", "there"
		});
		assertThat(sea2.somethingElse()).isEqualTo(51L);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	@Test
	public void testCopyMap() {
		Object obj = new Object();
		Map m = new HashMap<>();
		m.put("key", obj);
		Map cm = converter.convert(m).to(Map.class);
		assertThat(cm).isNotSameAs(m);
		assertThat(cm.get("key")).isSameAs(m.get("key"));
	}

	@Test
	public void testProxyObjectMethodsInterface() {
		Map<String,String> m = new HashMap<>();
		TestInterface ti = converter.convert(m).to(TestInterface.class);
		assertThat(ti).isEqualTo(ti);
		assertThat(ti).isNotEqualTo(new Object());
		assertThat(ti).isNotEqualTo(null);

		assertThat(ti.toString()).isNotNull();
		assertThat(ti.hashCode()).isNotEqualTo(0);
	}

	@Test
	public void testProxyObjectMethodsAnnotation() {
		Map<String,String> m = new HashMap<>();
		TestAnnotation ta = converter.convert(m).to(TestAnnotation.class);
		assertThat(ta).isEqualTo(ta);
	}

	@Test
	public void testCaseInsensitiveKeysAnnotation() {
		Map<String,Object> m = new HashMap<>();
		m.put("FOO", "Bleh");
		m.put("baR", 21);
		m.put("za.za", true);

		TestInterface ti = converter.convert(m)
				.keysIgnoreCase()
				.to(TestInterface.class);
		assertThat(ti.foo()).isEqualTo("Bleh");
		assertThat(ti.bar("42")).isEqualTo(21);
		assertThat(ti.za_za()).isTrue();
	}

	@Test
	public void testCaseSensitiveKeysAnnotation() {
		Map<String,Object> m = new HashMap<>();
		m.put("FOO", "Bleh");
		m.put("baR", 21);
		m.put("za.za", true);

		TestInterface ti = converter.convert(m).to(TestInterface.class);
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a conversion exception as 'foo' was not set")
				.isThrownBy(() -> ti.foo());
		assertThat(ti.bar("42")).isEqualTo(42);
		assertThat(ti.za_za()).isTrue();
	}

	@Test
	public void testCaseInsensitiveDTO() {
		Dictionary<String,String> d = new Hashtable<>();
		d.put("COUNT", "one");
		d.put("PinG", "Piiiiiiing!");
		d.put("pong", "999");

		MyDTO dto = converter.convert(d).keysIgnoreCase().to(MyDTO.class);
		assertThat(dto.count).isEqualTo(MyDTO.Count.ONE);
		assertThat(dto.ping).isEqualTo("Piiiiiiing!");
		assertThat(dto.pong).isEqualTo(999L);
	}

	@Test
	public void testCaseSensitiveDTO() {
		Dictionary<String,String> d = new Hashtable<>();
		d.put("COUNT", "one");
		d.put("PinG", "Piiiiiiing!");
		d.put("pong", "999");

		MyDTO dto = converter.convert(d).to(MyDTO.class);
		assertThat(dto.count).isNull();
		assertThat(dto.ping).isNull();
		assertThat(dto.pong).isEqualTo(999L);
	}

	@Test
	public void testCaseDTOOptionals() {
		Dictionary<String,String> d = new Hashtable<>();

		d.put("text", "NotNullText");

		MyDTOwithOptionals dto = converter.convert(d)
				.to(MyDTOwithOptionals.class);

		assertThat(dto.text)
				.isPresent()
				.hasValueSatisfying("NotNullText"::equals);

		assertThat(dto.textNull).isNull();

		assertThat(dto.textEmptyOptional).isEmpty();
	}

	@Test
	public void testRemovePasswords() {
		Map<String,Object> m = new LinkedHashMap<>();
		m.put("foo", "bar");
		m.put("password", "secret");

		Converter c = converter.newConverterBuilder()
				.rule(new Rule<Map<String,Object>,String>(v -> {
					Map<String,Object> cm = new LinkedHashMap<>(v);

					for (Map.Entry<String,Object> entry : cm.entrySet()) {
						if (entry.getKey().contains("password"))
							entry.setValue("xxx");
					}
					return cm.toString();
				}) {
				})
				.build();
		assertThat(c.convert(m).to(String.class)).isEqualTo("{foo=bar, password=xxx}");
		assertThat(m).as("Original should not be modified").hasToString("{foo=bar, password=secret}");
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	@Test
	public void testAnnotationDefaultMaterializer() throws Exception {
		Map<String,Object> vals = new HashMap<>();
		vals.put("bar", 99L);
		vals.put("tar", true);
		vals.put("za.za", false);

		Class< ? > ta1cls = getClass().getClassLoader()
				.loadClass(
						getClass().getPackage().getName() + ".sub1.TestAnn1");
		Object ta = converter.convert(vals).to(ta1cls);
		Map vals2 = converter.convert(ta).to(Map.class);
		vals2.putAll(vals);
		Class< ? > ta2cls = getClass().getClassLoader()
				.loadClass(
						getClass().getPackage().getName() + ".sub2.TestAnn2");
		Object ta2 = converter.convert(vals2).to(ta2cls);

		Method m1 = ta2cls.getDeclaredMethod("foo");
		m1.setAccessible(true);
		assertThat(m1.invoke(ta2)).isEqualTo("fooo!");

		Method m2 = ta2cls.getDeclaredMethod("bar");
		m2.setAccessible(true);
		assertThat(m2.invoke(ta2)).isEqualTo(99);

		Method m3 = ta2cls.getDeclaredMethod("tar");
		m3.setAccessible(true);
		assertThat(m3.invoke(ta2)).isEqualTo(true);
	}

	@Test
	public void testMapEntry() {
		Map<String,Boolean> m1 = Collections.singletonMap("Hi", Boolean.TRUE);
		Map.Entry<String,Boolean> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Boolean.class)).isTrue();
		assertThat(converter.convert(e1).to(boolean.class)).isTrue();
		assertThat(converter.convert(e1).to(String.class)).isEqualTo("Hi");

	}

	@Test
	public void testMapEntry1() {
		Map<Long,String> m1 = Collections.singletonMap(17L, "18");
		Map.Entry<Long,String> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Number.class)).isEqualTo(17L);
		assertThat(converter.convert(e1).to(String.class)).isEqualTo("18");
		assertThat(converter.convert(e1).to(Bar.class).value).isEqualTo("18");
	}

	@Test
	public void testMapEntry2() {
		Map<String,Short> m1 = Collections.singletonMap("123",
				Short.valueOf((short) 567));
		Map.Entry<String,Short> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Integer.class)).isEqualTo(Integer.valueOf(123));
	}

	@Test
	public void testMapEntry3() {
		Map<Long,Long> l1 = Collections.singletonMap(9L, 10L);
		Map.Entry<Long,Long> e1 = getMapEntry(l1);

		assertThat((long) converter.convert(e1).to(long.class)).as("Should take the key if key and value are equally suitable").isEqualTo(9L);
	}

	@Test
	public void testMapEntry4() {
		Map<Foo,Foo> m1 = Collections.singletonMap(new Foo(111), new Foo(999));
		Map.Entry<Foo,Foo> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Bar.class).value).isEqualTo("111");
	}

	@Test
	public void testMapEntry5() {
		// Key is null, value is the right type
		Map<Short,Integer> m1 = Collections.singletonMap(null, 5);
		Map.Entry<Short,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Integer.class)).isEqualTo((Integer) 5);
	}

	@Test
	public void testMapEntry6() {
		// Value is null, key is the right type
		Map<Short,Integer> m1 = Collections.singletonMap((short) 4, null);
		Map.Entry<Short,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Short.class).intValue()).isEqualTo(4);
	}

	@Test
	public void testMapEntry7() {
		// Key is null, value is assignable to the right type
		Map<Short,Integer> m1 = Collections.singletonMap(null, 5);
		Map.Entry<Short,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Number.class)).isEqualTo(5);
	}

	@Test
	public void testMapEntry8() {
		// Value is null, key is assignable to the right type
		Map<Short,Integer> m1 = Collections.singletonMap((short) 4, null);
		Map.Entry<Short,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Number.class).intValue()).isEqualTo(4);
	}

	@Test
	public void testMapEntry9() {
		// Key is null, value is a String
		Map<Short,String> m1 = Collections.singletonMap(null, "5");
		Map.Entry<Short,String> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Integer.class)).isEqualTo((Integer) 5);
	}

	@Test
	public void testMapEntry10() {
		// Value is null, key is a String
		Map<String,Integer> m1 = Collections.singletonMap("4", null);
		Map.Entry<String,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Integer.class)).isEqualTo((Integer) 4);
	}

	@Test
	public void testMapEntry11() {
		// Key is null, value is "wrong" type
		Map<Short,Integer> m1 = Collections.singletonMap(null, 5);
		Map.Entry<Short,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Long.class)).isNull();
	}

	@Test
	public void testMapEntry12() {
		// Key is null, value is null
		Map<String,Integer> m1 = Collections.singletonMap(null, null);
		Map.Entry<String,Integer> e1 = getMapEntry(m1);

		assertThat(converter.convert(e1).to(Integer.class)).isNull();
	}

	@Test
	public void testDictionaryToAnnotation() {
		Dictionary<String,Object> dict = new TestDictionary<>();
		dict.put("foo", "hello");
		TestAnnotation ta = converter.convert(dict).to(TestAnnotation.class);
		assertThat(ta.foo()).isEqualTo("hello");
	}

	@Test
	public void testDictionaryToMap() {
		Dictionary<String,Object> dict = new TestDictionary<>();
		dict.put("foo", "hello");
		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dict).to(Map.class);
		assertThat(m.get("foo")).isEqualTo("hello");
	}

	@Test
	public void testInterfaceWithGetProperties() {
		TestInterfaceWithGetProperties tiwgp = new TestInterfaceWithGetPropertiesClass();

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(tiwgp).to(Map.class);
		assertThat(m.size()).isEqualTo(2);
		assertThat(m.get("hi")).isEqualTo("ha");
		assertThat(m.get("ho")).isEqualTo("ho");
	}

	@Test
	public void testInterfaceWithGetPropertiesCopied() {
		TestInterfaceWithGetProperties tiwgp = new TestInterfaceWithGetPropertiesClass();

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(tiwgp).to(Map.class);
		assertThat(m.size()).isEqualTo(2);
		assertThat(m.get("hi")).isEqualTo("ha");
		assertThat(m.get("ho")).isEqualTo("ho");
	}

	@Test
	public void testMapWithKeywords() {
		Map<String,Object> m = new HashMap<>();
		m.put("new", "123");
		m.put("continue", 987l);

		MyDTOWithKeyWords dto = converter.convert(m)
				.to(MyDTOWithKeyWords.class);
		assertThat(dto.$new).isEqualTo(123l);
		assertThat(dto.$continue).isEqualTo("987");

		Map<String,Object> m2 = converter.convert(dto)
				.to(new TypeReference<Map<String,Object>>() {
				});
		assertThat(m2.size()).isEqualTo(2);
		assertThat(m2.get("new")).isEqualTo(123l);
		assertThat(m2.get("continue")).isEqualTo("987");
	}

	@Test
	public void testSingleElementAnnotationPrefix() {
		final Converter converter = Converters.standardConverter();
		final TestValue testValue = converter
				.convert(Collections.singletonMap("my.prefix.test.value", true))
				.to(TestValue.class);
		assertThat(testValue.value()).isTrue();
	}

	@Test
	@TestValue(true)
	public void testSingleElementAnnotationPrefixToMap() throws Exception {
		final Converter converter = Converters.standardConverter();
		Method method = getClass()
				.getMethod("testSingleElementAnnotationPrefixToMap");
		TestValue annotation = method.getDeclaredAnnotation(TestValue.class);
		Map<String,Object> map = converter.convert(annotation)
				.to(new TypeReference<Map<String,Object>>() {
				});
		assertThat((Boolean) map.get("my.prefix.test.value")).isTrue();
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

	private <K, V> Map.Entry<K,V> getMapEntry(Map<K,V> map) {
		assertThat(map).as("This method assumes a map of size 1").hasSize(1);
		return map.entrySet().iterator().next();
	}

	public interface TestInterface {
		String foo();

		int bar();

		int bar(String def);

		Boolean za_za();
	}

	public interface TestInterfaceSub extends TestInterface {
	}

	public static class TestInterfaceSubClass implements TestInterfaceSub {
		@Override
		public String foo() {
			return "Chocolate!";
		}

		@Override
		public int bar() {
			return 76543;
		}

		@Override
		public int bar(String def) {
			return 0;
		}

		@Override
		public Boolean za_za() {
			return true;
		}
	};

	public interface TestInterfaceWithGetProperties {
		int blah();

		Dictionary<String,Object> getProperties();
	}

	public static class TestInterfaceWithGetPropertiesClass
			implements TestInterfaceWithGetProperties {
		@Override
		public int blah() {
			return 99;
		}

		@Override
		public Dictionary<String,Object> getProperties() {
			Dictionary<String,Object> d = new TestDictionary<>();
			d.put("hi", "ha");
			d.put("ho", "ho");
			return d;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestAnnotation {
		String foo() default "fooo!";

		int bar() default 42;

		boolean za_za();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SingleElementAnnotation {
		String[] value();

		long somethingElse() default -87;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestValue {
		static final String PREFIX_ = "my.prefix.";

		boolean value();
	}

	private static class Foo {
		private final int value;

		Foo(int v) {
			value = v;
		}

		@Override
		public String toString() {
			return "" + value;
		}
	}

	public static class Bar {
		final String value;

		public Bar(String v) {
			value = v;
		}
	}

	public static class MyDTOWithKeyWords {
		public long		$new;
		public String	$continue;
	}
}
