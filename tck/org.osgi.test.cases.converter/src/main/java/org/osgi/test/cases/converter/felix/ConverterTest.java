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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Version;
import org.osgi.test.cases.converter.felix.MyDTO.Count;
import org.osgi.test.cases.converter.felix.MyEmbeddedDTO.Alpha;
import org.osgi.util.converter.ConversionException;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.ConverterFunction;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.Rule;
import org.osgi.util.converter.TypeReference;

public class ConverterTest {
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
	public void testVersion() {
		Version v = new Version(1, 2, 3, "qualifier");
		Converter c = Converters.standardConverter();
		String s = c.convert(v).to(String.class);
		Version v2 = c.convert(s).to(Version.class);
		assertThat(v2).isEqualTo(v);
	}

	@Test
	public void testSimpleConversions() {
		// Conversions to String
		assertThat(converter.convert("abc").to(String.class)).isEqualTo("abc");
		assertThat(converter.convert(Boolean.TRUE).to(String.class)).isEqualTo("true");
		assertThat(converter.convert('c').to(String.class)).isEqualTo("c");
		assertThat(converter.convert(123).to(String.class)).isEqualTo("123");
		assertThat(converter.convert(Long.MAX_VALUE).to(String.class)).isEqualTo("" + Long.MAX_VALUE);
		assertThat(converter.convert(12.3f).to(String.class)).isEqualTo("12.3");
		assertThat(converter.convert(12.345d).to(String.class)).isEqualTo("12.345");
		assertThat(converter.convert(null).to(String.class)).isNull();
		assertThat(converter.convert(Collections.emptyList()).to(String.class)).isNull();

		String bistr = "999999999999999999999"; // more than Long.MAX_VALUE
		assertThat(converter.convert(new BigInteger(bistr)).to(String.class)).isEqualTo(bistr);

		// Conversions to boolean
		assertThat(converter.convert("true").to(boolean.class)).isTrue();
		assertThat(converter.convert("TRUE").to(boolean.class)).isTrue();
		assertThat(converter.convert('x').to(boolean.class)).isTrue();
		assertThat(converter.convert(Long.MIN_VALUE).to(boolean.class)).isTrue();
		assertThat(converter.convert(72).to(boolean.class)).isTrue();
		assertThat(converter.convert("false").to(boolean.class)).isFalse();
		assertThat(converter.convert("bleh").to(boolean.class)).isFalse();
		assertThat(converter.convert((char) 0).to(boolean.class)).isFalse();
		assertThat(converter.convert(null).to(boolean.class)).isFalse();
		assertThat(converter.convert(Collections.emptyList()).to(boolean.class)).isFalse();

		// Conversions to integer
		assertThat(converter.convert("123").to(int.class)).isEqualTo(Integer.valueOf(123));
		assertThat((int) converter.convert(true).to(int.class)).isOne();
		assertThat((int) converter.convert(false).to(int.class)).isZero();
		assertThat((int) converter.convert('A').to(int.class)).isEqualTo(65);

		// Conversions to long
		assertThat(converter.convert('A').to(Long.class)).isEqualTo(Long.valueOf(65));

		// Conversions to Class
		assertThat(converter.convert("java.math.BigDecimal").to(Class.class)).isEqualTo(BigDecimal.class);
		assertThat(converter.convert("java.math.BigDecimal")
				.to(new TypeReference<Class< ? >>() {
				})).isEqualTo(BigDecimal.class);
		assertThat(converter.convert(null).to(Class.class)).isNull();
		assertThat(converter.convert(Collections.emptyList()).to(Class.class)).isNull();

		assertThat(converter.convert("123").to(Integer.class)).isEqualTo(Integer.valueOf(123));
		assertThat(converter.convert("123").to(Long.class)).isEqualTo(Long.valueOf(123));
		assertThat((char) converter.convert("123").to(Character.class)).isEqualTo('1');
		assertThat((char) converter.convert(null)
						.defaultValue('Q')
						.to(Character.class)).isEqualTo('Q');
		assertThat((char) converter.convert(123L).to(Character.class)).isEqualTo((char) 123);
		assertThat((char) converter.convert(123).to(Character.class)).isEqualTo((char) 123);
		assertThat(converter.convert("123").to(Byte.class)).isEqualTo(Byte.valueOf((byte) 123));
		assertThat(converter.convert("12.3").to(Float.class)).isEqualTo(Float.valueOf("12.3"));
		assertThat(converter.convert("12.3").to(Double.class)).isEqualTo(Double.valueOf("12.3"));

		// Conversions to Optional

		Optional<String> s1 = converter.convert("1").to(Optional.class);
		assertThat(s1).hasValue("1");

		Optional<String> s2 = converter.convert("2")
				.to(new TypeReference<Optional<String>>() {
				});
		assertThat(s2).hasValue("2");

		Optional<String> n1 = converter.convert(null).to(Optional.class);

		assertThat(n1).isEmpty();

		Optional<String> n2 = converter.convert(null)
				.to(new TypeReference<Optional<String>>() {
				});
		assertThat(n2).isEmpty();

		// OptionalInt
		OptionalInt oi1 = converter.convert("1").to(OptionalInt.class);
		assertThat(oi1).isNotEmpty().hasValue(1);

		OptionalInt oiNull = converter.convert(null).to(OptionalInt.class);
		assertThat(oiNull).isEmpty();

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converter.convert("badValue").to(OptionalInt.class));

		// OptionalDouble
		OptionalDouble od1 = converter.convert("1").to(OptionalDouble.class);
		assertThat(od1).isNotEmpty().hasValue(1d);

		OptionalDouble odNull = converter.convert(null)
				.to(OptionalDouble.class);
		assertThat(odNull).isNotNull().isEmpty();
		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converter.convert("badValue").to(OptionalDouble.class));

		// OptionalLong
		OptionalLong ol1 = converter.convert("1").to(OptionalLong.class);
		assertThat(ol1).isNotEmpty().hasValue(1l);

		OptionalLong olNull = converter.convert(null).to(OptionalLong.class);
		assertThat(olNull).isNotNull().isEmpty();
		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converter.convert("badValue").to(OptionalLong.class));

	}

	@Test
	public void testOptionalTyped() {
		// String to Double to Optional
		Optional<Double> d = converter.convert("12.3")
				.to(new TypeReference<Optional<Double>>() {
				});
		assertThat(d).hasValue(Double.valueOf("12.3"));

		Optional<Double> dNull = converter.convert(null)
				.to(new TypeReference<Optional<Double>>() {
				});

		assertThat(dNull).isEmpty();

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converter.convert("12.3")
				.to(new TypeReference<Optional<Integer>>() {
				}));
	}

	@Test
	public void testCharAggregateToString() {
		Converter c = Converters.newConverterBuilder()
				.rule(new Rule<List<Character>,String>(
						ConverterTest::characterListToString) {
				})
				.rule(new Rule<String,List<Character>>(
						ConverterTest::stringToCharacterList) {
				})
				.build();

		char[] ca = new char[] {
				'h', 'e', 'l', 'l', 'o'
		};
		assertThat(c.convert(ca).to(String.class)).isEqualTo("hello");

		Character[] ca2 = c.convert(ca).to(Character[].class);
		assertThat(c.convert(ca2).to(String.class)).isEqualTo("hello");

		List<Character> cl = c.convert(ca)
				.to(new TypeReference<List<Character>>() {
				});
		assertThat(c.convert(cl).to(String.class)).isEqualTo("hello");

		// And back
		assertThat(c.convert("hello").to(char[].class)).isEqualTo(ca);
		assertThat(c.convert("hello").to(Character[].class)).isEqualTo(ca2);
		assertThat(c.convert("hello").to(new TypeReference<List<Character>>() {
				})).isEqualTo(cl);
	}

	private static String characterListToString(List<Character> cl) {
		StringBuilder sb = new StringBuilder(cl.size());
		for (char c : cl) {
			sb.append(c);
		}
		return sb.toString();
	}

	private static List<Character> stringToCharacterList(String s) {
		List<Character> lc = new ArrayList<>();

		for (int i = 0; i < s.length(); i++) {
			lc.add(s.charAt(i));
		}
		return lc;
	}

	public enum TestEnum {
		FOO, BAR, BLAH, FALSE, X
	};

	public enum TestEnum2 {
		BLAH
	};

	@Test
	public void testEnums() {
		assertThat(converter.convert("BLAH").to(TestEnum.class)).isSameAs(TestEnum.BLAH);
		assertThat(converter.convert('X').to(TestEnum.class)).isSameAs(TestEnum.X);
		assertThat(converter.convert(false).to(TestEnum.class)).isSameAs(TestEnum.FALSE);
		assertThat(converter.convert(1).to(TestEnum.class)).isSameAs(TestEnum.BAR);
		assertThat(converter.convert(TestEnum2.BLAH).to(TestEnum.class)).isSameAs(TestEnum.BLAH);
		assertThat(converter.convert(null).to(TestEnum.class)).isNull();
		assertThat(converter.convert(Collections.emptySet()).to(TestEnum.class)).isNull();
	}

	@Test
	public void testToReflectType() {
		Type t = TestEnum.class;
		TestEnum e = converter.convert("X").to(t);
		assertThat(e).isEqualTo(TestEnum.X);
	}

	@Test
	public void testIdentialTarget() {
		Object o = new Object();
		assertThat(converter.convert(o).to(Object.class)).isSameAs(o);

		Thread t = new Thread(); // No converter available
		assertThat(converter.convert(t).to(Thread.class)).isSameAs(t);
		assertThat(converter.convert(t).to(Runnable.class)).isSameAs(t);
		assertThat(converter.convert(t).to(Object.class)).isSameAs(t);

		Thread st = new Thread() {
		}; // Subclass of Thread
		assertThat(converter.convert(st).to(Thread.class)).isSameAs(st);
	}

	@Test
	public void testFromUnknownDataTypeViaString() {
		class MyClass {
			@Override
			public String toString() {
				return "1234";
			}
		}
		;
		MyClass o = new MyClass();

		assertThat((int) converter.convert(o).to(int.class)).isEqualTo(1234);
		assertThat(converter.convert(o).to(String.class)).isEqualTo("1234");
	}

	@Test
	public void testToUnknownViaStringCtor() {
		class MyClass {
			@Override
			public String toString() {
				return "http://127.0.0.1:1234/blah";
			}
		}
		;
		MyClass o = new MyClass();

		URL url = converter.convert(o).to(URL.class);
		assertThat(url).hasToString("http://127.0.0.1:1234/blah");
		assertThat(url.getProtocol()).isEqualTo("http");
		assertThat(url.getHost()).isEqualTo("127.0.0.1");
		assertThat(url.getPort()).isEqualTo(1234);
		assertThat(url.getPath()).isEqualTo("/blah");

		assertThat(converter.convert(null).to(URL.class)).isNull();
		assertThat(converter.convert(Collections.emptyList()).to(URL.class)).isNull();
	}

	@Test
	public void testFromMultiToSingle() {
		assertThat(converter.convert(Collections.singleton("abc"))
				.to(String.class)).isEqualTo("abc");
		assertThat(converter.convert(asList("abc", "def", "ghi"))
						.to(String.class)).isEqualTo("abc");
		assertThat((int) converter.convert(asList("42", "17"))
				.to(Integer.class)).isEqualTo(42);
		MyClass2 mc = converter.convert(new String[] {
				"xxx", "yyy", "zzz"
		}).to(MyClass2.class);
		assertThat(mc).hasToString("xxx");
		MyClass2[] arr = new MyClass2[] {
				new MyClass2("3.1412"), new MyClass2("6.2824")
		};
		assertThat(Float.valueOf(converter.convert(arr).to(float.class))).isEqualTo(Float.valueOf(3.1412f));
	}

	@Test
	public void testFromListToSet() {
		List<Object> l = new ArrayList<>(Arrays.asList("A", 'B', 333));

		Set< ? > s = converter.convert(l).to(Set.class);
		assertThat(s.size()).isEqualTo(3);

		for (Object o : s) {
			Object expected = l.remove(0);
			assertThat(o).isEqualTo(expected);
		}
	}

	@Test
	public void testFromGenericSetToLinkedList() {
		Set<Integer> s = new LinkedHashSet<>();
		s.add(123);
		s.add(456);

		LinkedList<String> ll = converter.convert(s)
				.to(new TypeReference<LinkedList<String>>() {
				});
		assertThat(ll).isEqualTo(Arrays.asList("123", "456"));
	}

	@Test
	public void testFromArrayToGenericOrderPreservingSet() {
		String[] sa = {
				"567", "-765", "0", "-900"
		};

		// Returned set should be order preserving
		Set<Long> s = converter.convert(sa).to(new TypeReference<Set<Long>>() {
		});

		List<String> sl = new ArrayList<>(asList(sa));
		for (long l : s) {
			long expected = Long.parseLong(sl.remove(0));
			assertThat(l).isEqualTo(expected);
		}
	}

	@Test
	public void testFromSetToArray() {
		Set<Integer> s = new LinkedHashSet<>();
		s.add(Integer.MIN_VALUE);

		long[] la = converter.convert(s).to(long[].class);
		assertThat(la).hasSize(1);
		assertThat(la[0]).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	public void testStringArrayToIntegerArray() {
		String[] sa = {
				"999", "111", "-909"
		};
		Integer[] ia = converter.convert(sa).to(Integer[].class);
		assertThat(ia).hasSize(3);
		assertThat(ia).isEqualTo(new Integer[] {
				999, 111, -909
		});
	}

	@Test
	public void testCharArrayConversion() {
		char[] ca = converter.convert(new int[] {
				9, 8, 7
		}).to(char[].class);
		assertThat(ca).isEqualTo(new char[] {
				9, 8, 7
		});
		Character[] ca2 = converter.convert((long) 17).to(Character[].class);
		assertThat(ca2).isEqualTo(new Character[] {
				(char) 17
		});
		char[] ca3 = converter.convert(new short[] {
				257
		}).to(char[].class);
		assertThat(ca3).isEqualTo(new char[] {
				257
		});
		char c = converter.convert(new char[] {
				'x', 'y'
		}).to(char.class);
		assertThat(c).isEqualTo('x');
		char[] ca4a = {
				'x', 'y'
		};
		char[] ca4b = converter.convert(ca4a).to(char[].class);
		assertThat(ca4b).isEqualTo(new char[] {
				'x', 'y'
		});
		assertThat(ca4b).as("Should have created a new instance").isNotSameAs(ca4a);
	}

	/**
	 * 707.4.3.1 - null becomes an empty array
	 */
	@ParameterizedTest(name = "arrayType=\"{0}\"")
	@ValueSource(classes = {
			String[].class, //
			boolean[].class, //
			byte[].class, //
			short[].class, //
			char[].class, //
			int[].class, //
			float[].class, //
			long[].class, //
			double[].class, //

			String[][].class, //
			boolean[][].class, //
			byte[][].class, //
			short[][].class, //
			char[][].class, //
			int[][].class, //
			float[][].class, //
			long[][].class, //
			double[][].class, //

			String[][][].class, //
			boolean[][][].class, //
			byte[][][].class, //
			short[][][].class, //
			char[][][].class, //
			int[][][].class, //
			float[][][].class, //
			long[][][].class, //
			double[][][].class //
	})
	public void testNullToArrayConversion(Class< ? > arrayType) {
		assertThat(arrayType.isArray()).isTrue();

		Object array = converter.convert(null).to(arrayType);
		assertThat(Array.getLength(array)).isZero();
		assertThat(arrayType.isInstance(array)).isTrue();
	}

	@Test
	public void testPropagatingExceptionInArray() {
		try {
			Set<String> concurrentModificationSet = new HashSet<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Iterator<String> iterator() {
					return new Iterator<String>() {

						@Override
						public boolean hasNext() {
							return true;
						}

						@Override
						public String next() {
							throw new ConcurrentModificationException(
									"This iterator deliberately throws CMEs!");
						}
					};
				}

			};
			concurrentModificationSet.add("one");
			concurrentModificationSet.add("two");
			converter.convert(concurrentModificationSet).to(String[].class);
			fail("Should have thrown a Conversion Exception when a collection throwing a CME was used as source");
		} catch (ConversionException e) {
			// good
		}
	}

	@Test
	public void testLongCollectionConversion() {
		long[] l = converter.convert(Long.MAX_VALUE).to(long[].class);
		assertThat(l).isEqualTo(new long[] {
				Long.MAX_VALUE
		});
		Long[] l2 = converter.convert(Long.MAX_VALUE).to(Long[].class);
		assertThat(l2).isEqualTo(new Long[] {
				Long.MAX_VALUE
		});
		List<Long> ll = converter.convert(new long[] {
				Long.MIN_VALUE, Long.MAX_VALUE
		}).to(new TypeReference<List<Long>>() {
		});
		assertThat(ll).isEqualTo(Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE));
		List<Long> ll2 = converter.convert(Arrays.asList(123, 345))
				.to(new TypeReference<List<Long>>() {
				});
		assertThat(ll2).isEqualTo(Arrays.asList(123L, 345L));

	}

	@Test
	public void testExceptionDefaultValue() {
		assertThat((int) converter.convert("haha").defaultValue(42).to(int.class)).isEqualTo(42);
		assertThat((int) converter.convert("haha")
						.defaultValue(999)
						.to(int.class)).isEqualTo(999);
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown an exception")
				.isThrownBy(() -> converter.convert("haha").to(int.class));
	}

	@Test
	public void testStandardStringArrayConversion() {
		String[] sa = {
				"A", "B"
		};
		assertThat(converter.convert(sa)).hasToString("A");
		assertThat(converter.convert(sa).to(String.class)).isEqualTo("A");

		String[] sa2 = {
				"A"
		};
		assertThat(converter.convert("A").to(String[].class)).isEqualTo(sa2);
	}

	@Test
	public void testCustomStringArrayConversion() {
		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(new Rule<String[],String>(
				v -> Stream.of(v).collect(Collectors.joining(","))) {
		});
		cb.rule(new Rule<String,String[]>(v -> v.split(",")) {
		});

		Converter adapted = cb.build();

		String[] sa = {
				"A", "B"
		};
		assertThat(adapted.convert(sa).to(String.class)).isEqualTo("A,B");
		assertThat(adapted.convert("A,B").to(String[].class)).isEqualTo(sa);
	}

	@Test
	public void testCustomIntArrayConversion() {
		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(String.class,
				(f, t) -> f instanceof int[]
						? Arrays.stream((int[]) f)
								.mapToObj(Integer::toString)
								.collect(Collectors.joining(","))
						: null);
		cb.rule(int[].class,
				(f, t) -> f instanceof String
						? Arrays.stream(((String) f).split(","))
								.mapToInt(Integer::parseInt)
								.toArray()
						: null);
		Converter adapted = cb.build();

		int[] ia = {
				1, 2
		};
		assertThat(adapted.convert(ia).to(String.class)).isEqualTo("1,2");
		assertThat(adapted.convert("1,2").to(int[].class)).isEqualTo(ia);
	}

	@Test
	public void testCustomConverterChaining() {
		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(Date.class, (f, t) -> f instanceof String ? new Date(0)
				: ConverterFunction.CANNOT_HANDLE);
		Converter c1 = cb.build();
		assertThat(c1.convert("something").to(Date.class)).isEqualTo(new Date(0));
		assertThat(c1.convert("foo").to(Date.class)).isEqualTo(new Date(0));

		ConverterBuilder cb2 = c1.newConverterBuilder();
		cb2.rule(Date.class, (f, t) -> f.equals("foo") ? new Date(100000)
				: ConverterFunction.CANNOT_HANDLE);
		Converter c2 = cb2.build();
		assertThat(c2.convert("something").to(Date.class)).isEqualTo(new Date(0));
		assertThat(c2.convert("foo").to(Date.class)).isEqualTo(new Date(100000));
	}

	@Test
	public void testCustomErrorHandling() {
		ConverterFunction func = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type targetType) {
				if ("hello".equals(obj)) {
					return -1;
				}
				if ("goodbye".equals(obj)) {
					return null;
				}
				return ConverterFunction.CANNOT_HANDLE;
			}
		};

		ConverterBuilder cb = converter.newConverterBuilder();
		Converter adapted = cb.errorHandler(func).build();

		assertThat(adapted.convert("12").to(Integer.class)).isEqualTo(Integer.valueOf(12));
		assertThat(adapted.convert("hello").to(Integer.class)).isEqualTo(Integer.valueOf(-1));
		assertThat(adapted.convert("goodbye").to(Integer.class)).isNull();

		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a Conversion Exception when converting 'hello' to a number")
				.isThrownBy(() -> adapted.convert("nothing").to(Integer.class));

		// This is with the non-adapted converter
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown a Conversion Exception when converting 'hello' to a number")
				.isThrownBy(() -> converter.convert("hello").to(Integer.class));
	}

	@Test
	public void testCustomErrorHandlingProxy() {
		ConverterFunction errHandler = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type targetType) throws Exception {
				return 123;
			}
		};
		ConverterBuilder cb = converter.newConverterBuilder();
		Converter c = cb.errorHandler(errHandler).build();

		Map< ? , ? > m = new HashMap<>();

		MyIntf i = c.convert(m).to(MyIntf.class);
		assertThat(i.value()).isEqualTo(123);
	}

	@Test
	public void testMultipleCustomErrorHandling() {
		ConverterBuilder cb1 = converter.newConverterBuilder();
		ConverterFunction func1 = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type targetType) {
				return -1;
			}
		};
		cb1.errorHandler(func1);
		Converter c1 = cb1.build();

		ConverterBuilder cb2 = c1.newConverterBuilder();
		ConverterFunction func2 = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type targetType) {
				if ("hello".equals(obj)) {
					return 0;
				}
				return ConverterFunction.CANNOT_HANDLE;
			}
		};
		cb2.errorHandler(func2);
		Converter adapted = cb2.build();

		assertThat(adapted.convert("hello").to(Integer.class)).isEqualTo(Integer.valueOf(0));
		assertThat(adapted.convert("bye").to(Integer.class)).isEqualTo(Integer.valueOf(-1));
	}

	public static class MyConverterFunction implements ConverterFunction {
		@Override
		public Object apply(Object obj, Type targetType) throws Exception {
			if ("hello".equals(obj)) {
				return 0;
			}
			return ConverterFunction.CANNOT_HANDLE;
		}
	}

	@Test
	public void testUUIDConversion() {
		UUID uuid = UUID.randomUUID();
		String s = converter.convert(uuid).to(String.class);
		assertThat(s.length() > 0).as("UUID should be something").isTrue();
		UUID uuid2 = converter.convert(s).to(UUID.class);
		assertThat(uuid2).isEqualTo(uuid);
	}

	@Test
	public void testPatternConversion() {
		String p = "\\S*";
		Pattern pattern = converter.convert(p).to(Pattern.class);
		Matcher matcher = pattern.matcher("hi");
		assertThat(matcher.matches()).isTrue();
		String p2 = converter.convert(pattern).to(String.class);
		assertThat(p2).isEqualTo(p);
	}

	@Test
	public void testLocalDateTime() {
		LocalDateTime ldt = LocalDateTime.now();
		String s = converter.convert(ldt).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		LocalDateTime ldt2 = converter.convert(s).to(LocalDateTime.class);
		assertThat(ldt2).isEqualTo(ldt);
	}

	@Test
	public void testLocalDate() {
		LocalDate ld = LocalDate.now();
		String s = converter.convert(ld).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		LocalDate ld2 = converter.convert(s).to(LocalDate.class);
		assertThat(ld2).isEqualTo(ld);
	}

	@Test
	public void testLocalTime() {
		LocalTime lt = LocalTime.now();
		String s = converter.convert(lt).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		LocalTime lt2 = converter.convert(s).to(LocalTime.class);
		assertThat(lt2).isEqualTo(lt);
	}

	@Test
	public void testOffsetDateTime() {
		OffsetDateTime ot = OffsetDateTime.now();
		String s = converter.convert(ot).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		OffsetDateTime ot2 = converter.convert(s).to(OffsetDateTime.class);
		assertThat(ot2).isEqualTo(ot);
	}

	@Test
	public void testOffsetTime() {
		OffsetTime ot = OffsetTime.now();
		String s = converter.convert(ot).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		OffsetTime ot2 = converter.convert(s).to(OffsetTime.class);
		assertThat(ot2).isEqualTo(ot);
	}

	@Test
	public void testZonedDateTime() {
		ZonedDateTime zdt = ZonedDateTime.now();
		String s = converter.convert(zdt).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		ZonedDateTime zdt2 = converter.convert(s).to(ZonedDateTime.class);
		assertThat(zdt2).isEqualTo(zdt);
	}

	@Test
	public void testInstant() {
		Instant i = Instant.now();
		String s = converter.convert(i).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		Instant i2 = converter.convert(s).to(Instant.class);
		assertThat(i2).isEqualTo(i);
	}

	@Test
	public void testMonthDay() {
		MonthDay md = MonthDay.of(Month.APRIL, 1);
		String s = converter.convert(md).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		MonthDay md2 = converter.convert(s).to(MonthDay.class);
		assertThat(md2).isEqualTo(md);
	}

	@Test
	public void testYearMonth() {
		YearMonth ym = YearMonth.of(1999, Month.APRIL);
		String s = converter.convert(ym).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		YearMonth ym2 = converter.convert(s).to(YearMonth.class);
		assertThat(ym2).isEqualTo(ym);
	}

	@Test
	public void testYear() {
		Year y = Year.of(1999);
		String s = converter.convert(y).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		Year y2 = converter.convert(s).to(Year.class);
		assertThat(y2).isEqualTo(y);
	}

	@Test
	public void testDuration() {
		Duration d = Duration.ofSeconds(42);
		String s = converter.convert(d).to(String.class);
		assertThat(s.length()).isGreaterThan(0);
		Duration d2 = converter.convert(s).to(Duration.class);
		assertThat(d2).isEqualTo(d);
	}

	@Test
	public void testCalendarDate() {
		Calendar cal = new GregorianCalendar(1971, 1, 13, 12, 37, 41);
		TimeZone tz = TimeZone.getTimeZone("CET");
		cal.setTimeZone(tz);
		Date d = cal.getTime();

		Converter c = converter;

		String s = c.convert(d).toString();
		assertThat(s).isEqualTo("1971-02-13T11:37:41Z");
		assertThat(c.convert(s).to(Date.class)).isEqualTo(d);

		String s2 = c.convert(cal).toString();
		assertThat(s2).isEqualTo("1971-02-13T11:37:41Z");
		Calendar cal2 = c.convert(s2).to(Calendar.class);
		assertThat(cal2.getTime()).isEqualTo(cal.getTime());
	}

	@Test
	public void testCalendarLong() {
		Calendar cal = new GregorianCalendar(1971, 1, 13, 12, 37, 41);
		TimeZone tz = TimeZone.getTimeZone("UTC");
		cal.setTimeZone(tz);

		long l = converter.convert(cal).to(Long.class);
		assertThat(cal.getTimeInMillis()).isEqualTo(l);

		Calendar cal2 = converter.convert(l).to(Calendar.class);
		assertThat(cal2.getTime()).isEqualTo(cal.getTime());
	}

	@Test
	public void testDefaultValue() {
		long l = converter.convert(null).defaultValue("12").to(Long.class);
		assertThat(l).isEqualTo(12L);
		assertThat(converter.convert("haha").defaultValue(null).to(Integer.class)).isNull();
		assertThat(converter.convert("test")
				.defaultValue(null)
				.to(new TypeReference<List<Long>>() {
				})).isNull();
	}

	@Test
	public void testDTO2Map() {
		MyEmbeddedDTO embedded = new MyEmbeddedDTO();
		embedded.marco = "hohoho";
		embedded.polo = Long.MAX_VALUE;
		embedded.alpha = Alpha.A;

		MyDTO dto = new MyDTO();
		dto.ping = "lalala";
		dto.pong = Long.MIN_VALUE;
		dto.count = Count.ONE;
		dto.embedded = embedded;

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dto).to(Map.class);
		assertThat(m.size()).isEqualTo(4);
		assertThat(m.get("ping")).isEqualTo("lalala");
		assertThat(m.get("pong")).isEqualTo(Long.MIN_VALUE);
		assertThat(m.get("count")).isEqualTo(Count.ONE);
		assertThat(m.get("embedded")).isNotNull();

		MyEmbeddedDTO e = (MyEmbeddedDTO) m.get("embedded");
		assertThat(e.marco).isEqualTo("hohoho");
		assertThat(e.polo).isEqualTo(Long.MAX_VALUE);
		assertThat(e.alpha).isEqualTo(Alpha.A);
	}

	@Test
	public void testDTO2Map2() {
		MyEmbeddedDTO embedded = new MyEmbeddedDTO();
		embedded.marco = "hohoho";
		embedded.polo = Long.MAX_VALUE;
		embedded.alpha = Alpha.A;

		MyDTO dto = new MyDTO();
		dto.ping = "lalala";
		dto.pong = Long.MIN_VALUE;
		dto.count = Count.ONE;
		dto.embedded = embedded;

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dto).sourceAsDTO().to(Map.class);
		assertThat(m.size()).isEqualTo(4);
		assertThat(m.get("ping")).isEqualTo("lalala");
		assertThat(m.get("pong")).isEqualTo(Long.MIN_VALUE);
		assertThat(m.get("count")).isEqualTo(Count.ONE);
		assertThat(m.get("embedded")).isNotNull();

		MyEmbeddedDTO e = (MyEmbeddedDTO) m.get("embedded");
		assertThat(e.marco).isEqualTo("hohoho");
		assertThat(e.polo).isEqualTo(Long.MAX_VALUE);
		assertThat(e.alpha).isEqualTo(Alpha.A);

		/*
		 * TODO this is the way it was, but it does not seem right Map e =
		 * (Map)m.get("embedded"); assertThat(e.get("marco")).isEqualTo("hohoho");
		 * assertThat(e.get("polo")).isEqualTo(Long.MAX_VALUE); assertThat(* e.get("alpha")).isEqualTo(Alpha.A);
		 */
	}

	@Test
	public void testDTO2Map3() {
		MyEmbeddedDTO embedded2 = new MyEmbeddedDTO();
		embedded2.marco = "hohoho";
		embedded2.polo = Long.MAX_VALUE;
		embedded2.alpha = Alpha.A;

		MyDTOWithMethods embedded = new MyDTOWithMethods();
		embedded.ping = "lalala";
		embedded.pong = Long.MIN_VALUE;
		embedded.count = Count.ONE;
		embedded.embedded = embedded2;

		MyDTO8 dto = new MyDTO8();
		dto.ping = "lalala";
		dto.pong = Long.MIN_VALUE;
		dto.count = MyDTO8.Count.ONE;
		dto.embedded = embedded;

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dto).sourceAsDTO().to(Map.class);
		assertThat(m.size()).isEqualTo(4);
		assertThat(m.get("ping")).isEqualTo("lalala");
		assertThat(m.get("pong")).isEqualTo(Long.MIN_VALUE);
		assertThat(m.get("count")).isEqualTo(MyDTO8.Count.ONE);
		assertThat(m.get("embedded")).isNotNull();
		assertThat(m.get("embedded")).isInstanceOf(MyDTOWithMethods.class);
		MyDTOWithMethods e = (MyDTOWithMethods) m.get("embedded");
		assertThat(e.ping).isEqualTo("lalala");
		assertThat(e.pong).isEqualTo(Long.MIN_VALUE);
		assertThat(e.count).isEqualTo(Count.ONE);
		assertThat(e.embedded).isNotNull();
		assertThat(e.embedded).isInstanceOf(MyEmbeddedDTO.class);
		MyEmbeddedDTO e2 = e.embedded;
		assertThat(e2.marco).isEqualTo("hohoho");
		assertThat(e2.polo).isEqualTo(Long.MAX_VALUE);
		assertThat(e2.alpha).isEqualTo(Alpha.A);
	}

	@Test
	public void testDTO2Map4() {
		MyDefaultCtorDTOAlike dto = new MyDefaultCtorDTOAlike();
		dto.myProp = "myValue";

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dto).to(Map.class);
		assertThat(m.size()).isEqualTo(1);
		assertThat(m.get("myProp")).isEqualTo("myValue");
	}

	@Test
	public void testDTO2Map5() {
		MyDTO3 dto = new MyDTO3();
		dto.charSet = new HashSet<>(Arrays.asList('f', 'o', 'o'));

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(dto).to(new TypeReference<Map<String, ? >>() {
		});
		assertThat(m.size()).isEqualTo(1);
		assertThat(m.get("charSet")).isEqualTo(dto.charSet);

		m = converter.convert(dto)
				.to(new TypeReference<Map<String, ? extends List<String>>>() {
				});
		assertThat(m).hasSize(1);

		List<String> list = new ArrayList<>();
		for (Character character : dto.charSet) {
			list.add(String.valueOf(character));
		}

		assertThat(m.get("charSet")).isEqualTo(list);
	}

	@Test
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	public void testDTOFieldShadowing() {
		MySubDTO dto = new MySubDTO();
		dto.ping = "test";
		dto.count = Count.THREE;

		Map m = converter.convert(dto)
				.to(new TypeReference<Map<String,String>>() {
				});

		Map<String,String> expected = new HashMap<>();
		expected.put("ping", "test");
		expected.put("count", "THREE");
		expected.put("pong", "0");
		expected.put("embedded", null);
		assertThat(new HashMap<String,String>(m)).isEqualTo(expected);

		MySubDTO dto2 = converter.convert(m).to(MySubDTO.class);
		assertThat(dto2.ping).isEqualTo("test");
		assertThat(dto2.count).isEqualTo(Count.THREE);
		assertThat(dto2.pong).isZero();
		assertThat(dto2.embedded).isNull();
	}

	@Test
	public void testMap2DTO() {
		Map<String,Object> m = new HashMap<>();
		m.put("ping", "abc xyz");
		m.put("pong", 42L);
		m.put("count", Count.ONE);
		Map<String,Object> e = new HashMap<>();
		e.put("marco", "ichi ni san");
		e.put("polo", 64L);
		e.put("alpha", Alpha.A);
		m.put("embedded", e);

		MyDTO dto = converter.convert(m).to(MyDTO.class);
		assertThat(dto.ping).isEqualTo("abc xyz");
		assertThat(dto.pong).isEqualTo(42L);
		assertThat(dto.count).isEqualTo(Count.ONE);
		assertThat(dto.embedded).isNotNull();
		assertThat("ichi ni san").isEqualTo(dto.embedded.marco);
		assertThat(64L).isEqualTo(dto.embedded.polo);
		assertThat(Alpha.A).isEqualTo(dto.embedded.alpha);
	}

	@Test
	public void testMap2DTOView() {
		Map<String,Object> src = Collections.singletonMap("pong", 42);
		MyDTOWithMethods dto = converter.convert(src)
				.targetAs(MyDTO.class)
				.to(MyDTOWithMethods.class);
		assertThat(dto.pong).isEqualTo(42);
	}

	@Test
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	public void testDTOWithGenerics() {
		MyDTO2 dto = new MyDTO2();
		dto.longList = asList(999L, 1000L);
		dto.dtoMap = new LinkedHashMap<>();

		MyDTO3 subDTO1 = new MyDTO3();
		subDTO1.charSet = new HashSet<>(Arrays.asList('f', 'o', 'o'));
		dto.dtoMap.put("zzz", subDTO1);

		MyDTO3 subDTO2 = new MyDTO3();
		subDTO2.charSet = new HashSet<>(Arrays.asList('b', 'a', 'r'));
		dto.dtoMap.put("aaa", subDTO2);

		Map m = converter.convert(dto).to(Map.class);
		assertThat(m).hasSize(2);

		assertThat(m.get("longList")).isEqualTo(Arrays.asList(999L, 1000L));
		Map nestedMap = (Map) m.get("dtoMap");

		// Check iteration order is preserved by iterating
		int i = 0;
		for (Iterator<Map.Entry> it = nestedMap.entrySet().iterator(); it
				.hasNext(); i++) {
			Map.Entry entry = it.next();
			switch (i) {
				case 0 :
					assertThat(entry.getKey()).isEqualTo("zzz");
					MyDTO3 dto1 = (MyDTO3) entry.getValue();
					assertThat(dto1).as("Should have created a copy").isNotSameAs(subDTO1);
					assertThat(dto1.charSet).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
					break;
				case 1 :
					assertThat(entry.getKey()).isEqualTo("aaa");
					MyDTO3 dto2 = (MyDTO3) entry.getValue();
					assertThat(dto2).as("Should have created a copy").isNotSameAs(subDTO2);
					assertThat(dto2.charSet).isEqualTo(new HashSet<Character>(
									Arrays.asList('b', 'a', 'r')));
					break;
				default :
					fail("Unexpected number of elements on map");
			}
		}

		// convert back
		MyDTO2 dto2 = converter.convert(m).to(MyDTO2.class);
		assertThat(dto2.longList).isEqualTo(dto.longList);

		// Cannot simply do dto.equals() as the DTOs don't implement that
		assertThat(dto2.dtoMap).hasSize(dto.dtoMap.size());
		MyDTO3 dto2SubZZZ = dto2.dtoMap.get("zzz");
		assertThat(new HashSet<Character>(Arrays.asList('f', 'o'))).isEqualTo(dto2SubZZZ.charSet);
		MyDTO3 dto2SubAAA = dto2.dtoMap.get("aaa");
		assertThat(new HashSet<Character>(Arrays.asList('b', 'a', 'r'))).isEqualTo(dto2SubAAA.charSet);
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

		MyGenericDTOWithVariables<Character> converted = converter.convert(dto)
				.to(new TypeReference<MyGenericDTOWithVariables<Character>>() {
				});
		assertThat(converted.raw).isEqualTo(Character.valueOf('1'));
		assertThat(converted.array).isEqualTo(new Character[] {
				'f', 'o', 'o'
		});
		assertThat(converted.set).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
	}

	@Test
	public void testMapToDTOWithSurplusMapFiels() {
		Map<String,String> m = new HashMap<>();
		m.put("foo", "bar");
		MyDTO3 dtoDoesNotMap = converter.convert(m).to(MyDTO3.class);
		assertThat(dtoDoesNotMap.charSet).isNull();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testCopyMap() {
		Map m = new HashMap();
		Map m2 = converter.convert(m).to(Map.class);
		assertThat(m2).isEqualTo(m);
		assertThat(m2).isNotSameAs(m);
	}

	@Test
	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	public void testCopyMap2() {
		Map m = new HashMap();
		m.put("key", asList("a", "b", "c"));
		Map m2 = converter.convert(m).to(Map.class);
		assertThat(m2).isEqualTo(m);
		assertThat(m2).isNotSameAs(m);
	}

	@Test
	public void testConversionPriority() {
		MyBean mb = new MyBean();
		mb.intfVal = 17;
		mb.beanVal = "Hello";

		assertThat(converter.convert(mb).sourceAsBean().to(Map.class)).isEqualTo(Collections.singletonMap("value", "Hello"));
	}

	@Test
	public void testConvertAsInterface() {
		MyBean mb = new MyBean();
		mb.intfVal = 17;
		mb.beanVal = "Hello";

		assertThat(converter.convert(mb)
						.sourceAs(MyIntf.class)
						.to(Map.class)
						.get("value")).isEqualTo(17);
	}

	@Test
	public void testConvertAsBean() {
		MyBean mb = new MyBean();
		mb.intfVal = 17;
		mb.beanVal = "Hello";

		assertThat(converter.convert(mb).sourceAsBean().to(Map.class)).isEqualTo(Collections.singletonMap("value", "Hello"));
	}

	@Test
	public void testConvertAsDTO() {
		MyClass3 mc3 = new MyClass3(17);

		assertThat(converter.convert(mc3)
						.sourceAsDTO()
						.to(Map.class)
						.get("value")).isEqualTo(17);
	}

	@Test
	public void testDTONameMangling() {
		Map<String,String> m = new HashMap<>();
		m.put("org.osgi.framework.uuid", "test123");
		m.put("myProperty143", "true");
		m.put("my$prop", "42");
		m.put("dot.prop", "456");
		m.put(".secret", " ");
		m.put("another_prop", "lalala");
		m.put("three_.prop", "hi ha ho");
		m.put("four._prop", "");
		m.put("five..prop", "test");
		m.put("six-prop", "987");
		m.put("seven$.prop", "3.141");

		MyDTO7 dto = converter.convert(m).to(MyDTO7.class);
		assertThat(dto.org_osgi_framework_uuid).isEqualTo("test123");
		assertThat(dto.myProperty143).isTrue();
		assertThat(dto.my$$prop).isEqualTo(42);
		assertThat(dto.dot_prop).isEqualTo(Long.valueOf(456L));
		assertThat(dto._secret).isEqualTo(' ');
		assertThat(dto.another__prop).isEqualTo("lalala");
		assertThat(dto.three___prop).isEqualTo("hi ha ho");
		assertThat(dto.four_$__prop).isEmpty();
		assertThat(dto.five_$_prop).isEqualTo("test");
		assertThat(dto.six$_$prop).isEqualTo((short) 987);
		dto.seven$$_$prop = 3.141;

		// And convert back
		Map<String,String> m2 = converter.convert(dto)
				.to(new TypeReference<Map<String,String>>() {
				});
		assertThat(new HashMap<String,String>(m2)).isEqualTo(new HashMap<String,String>(m));
	}

	@Test
	public void testCollectionInterfaceMapping() {
		Collection< ? > coll = converter.convert("test").to(Collection.class);
		assertThat(coll.iterator().next()).isEqualTo("test");

		List< ? > list = converter.convert("test").to(List.class);
		assertThat(list.iterator().next()).isEqualTo("test");

		Set< ? > set = converter.convert("test").to(Set.class);
		assertThat(set.iterator().next()).isEqualTo("test");

		NavigableSet< ? > ns = converter.convert("test").to(NavigableSet.class);
		assertThat(ns.iterator().next()).isEqualTo("test");

		SortedSet< ? > ss = converter.convert("test").to(SortedSet.class);
		assertThat(ss.iterator().next()).isEqualTo("test");

		Queue< ? > q = converter.convert("test").to(Queue.class);
		assertThat(q.iterator().next()).isEqualTo("test");

		Deque< ? > dq = converter.convert("test").to(Deque.class);
		assertThat(dq.iterator().next()).isEqualTo("test");

		Map< ? , ? > m = converter.convert(Collections.singletonMap("x", "y"))
				.to(Map.class);
		assertThat(m.get("x")).isEqualTo("y");

		ConcurrentMap< ? , ? > cm = converter
				.convert(Collections.singletonMap("x", "y"))
				.to(ConcurrentMap.class);
		assertThat(cm.get("x")).isEqualTo("y");

		ConcurrentNavigableMap< ? , ? > cnm = converter
				.convert(Collections.singletonMap("x", "y"))
				.to(ConcurrentNavigableMap.class);
		assertThat(cnm.get("x")).isEqualTo("y");

		NavigableMap< ? , ? > nm = converter
				.convert(Collections.singletonMap("x", "y"))
				.to(NavigableMap.class);
		assertThat(nm.get("x")).isEqualTo("y");

		SortedMap< ? , ? > sm = converter
				.convert(Collections.singletonMap("x", "y"))
				.to(SortedMap.class);
		assertThat(sm.get("x")).isEqualTo("y");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLiveMapFromInterface() {
		int[] val = new int[1];
		val[0] = 51;

		MyIntf intf = new MyIntf() {
			@Override
			public int value() {
				return val[0];
			}
		};

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(intf).view().to(Map.class);
		assertThat(m.get("value")).isEqualTo(51);

		val[0] = 52;
		assertThat(m.get("value")).as("Changes to the backing map should be reflected").isEqualTo(52);

		m.put("value", 53);
		assertThat(m.get("value")).isEqualTo(53);

		val[0] = 54;
		assertThat(m.get("value")).as("Changes to the backing map should not be reflected any more").isEqualTo(53);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLiveMapFromDTO() {
		MyDTO8 myDTO = new MyDTO8();

		myDTO.count = MyDTO8.Count.TWO;
		myDTO.pong = 42L;

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(myDTO).view().to(Map.class);
		assertThat(m.get("pong")).isEqualTo(42L);

		myDTO.ping = "Ping!";
		assertThat(m.get("ping")).isEqualTo("Ping!");
		myDTO.pong = 52L;
		assertThat(m.get("pong")).isEqualTo(52L);
		myDTO.ping = "Pong!";
		assertThat(m.get("ping")).isEqualTo("Pong!");
		assertThat(m.get("nonexistant")).isNull();

		m.put("pong", 62L);
		myDTO.ping = "Poing!";
		myDTO.pong = 72L;
		assertThat(m.get("ping")).isEqualTo("Pong!");
		assertThat(m.get("pong")).isEqualTo(62L);
		assertThat(m.get("nonexistant")).isNull();
	}

	@Test
	public void testMapFromDTO() {
		MyDTO9 dto = new MyDTO9();
		dto.key1 = "value1";
		dto.key2 = "value2";

		Map<Character,Character> m = converter.convert(dto)
				.to(new TypeReference<Map<Character,Character>>() {
				});
		assertThat(m).hasSize(1);
		assertThat((char) m.get('k')).isEqualTo('v');

		assertThat(m).asInstanceOf(InstanceOfAssertFactories.MAP)
				.containsKey('k')
				.containsValue('v')
				.doesNotContainKey("key1")
				.doesNotContainValue("value1");
	}

	@Test
	public void testLiveMapFromDictionary() throws URISyntaxException {
		URI testURI = new URI("http://foo");
		Hashtable<String,Object> d = new Hashtable<>();
		d.put("test", testURI);

		Map<String,Object> m = converter.convert(d)
				.view()
				.to(new TypeReference<Map<String,Object>>() {
				});
		assertThat(m.get("test")).isEqualTo(testURI);

		URI testURI2 = new URI("http://bar");
		d.put("test2", testURI2);
		assertThat(m.get("test2")).isEqualTo(testURI2);
		assertThat(m.get("test")).isEqualTo(testURI);
	}

	@Test
	public void testLiveMapFromMap() {
		Map<String,String> s = new HashMap<>();

		s.put("true", "123");
		s.put("false", "456");

		Map<Boolean,Short> m = converter.convert(s)
				.view()
				.to(new TypeReference<Map<Boolean,Short>>() {
				});
		assertThat(m.get(Boolean.TRUE)).isEqualTo(Short.valueOf("123"));
		assertThat(m.get(Boolean.FALSE)).isEqualTo(Short.valueOf("456"));

		s.remove("true");
		assertThat(m.get(Boolean.TRUE)).isNull();

		s.put("TRUE", "999");
		assertThat(m.get(Boolean.TRUE)).isEqualTo(Short.valueOf("999"));
	}

	@Test
	public void testLiveMapFromBean() {
		MyBean mb = new MyBean();
		mb.beanVal = "" + Long.MAX_VALUE;

		Map<SomeEnum,Long> m = converter.convert(mb)
				.sourceAsBean()
				.view()
				.to(new TypeReference<Map<SomeEnum,Long>>() {
				});
		assertThat(m.size()).isEqualTo(1);
		assertThat(m.get(SomeEnum.VALUE)).isEqualTo(Long.valueOf(Long.MAX_VALUE));

		mb.beanVal = "" + Long.MIN_VALUE;
		assertThat(m.get(SomeEnum.VALUE)).isEqualTo(Long.valueOf(Long.MIN_VALUE));

		m.put(SomeEnum.GETVALUE, 123L);
		mb.beanVal = "12";
		assertThat(m.get(SomeEnum.VALUE)).isEqualTo(Long.valueOf(Long.MIN_VALUE));
	}

	@Test
	public void testPrefixDTO() {
		Map<String,String> m = new HashMap<>();
		m.put("org.foo.bar.width", "327");
		m.put("org.foo.bar.warp", "eeej");
		m.put("length", "12");

		PrefixDTO dto = converter.convert(m).to(PrefixDTO.class);
		assertThat(dto.width).isEqualTo(327L);
		assertThat(dto.length).as("This one should not be set").isZero();

		Map<String,String> m2 = converter.convert(dto)
				.to(new TypeReference<HashMap<String,String>>() {
				});
		Map<String,String> expected = new HashMap<>();
		expected.put("org.foo.bar.width", "327");
		expected.put("org.foo.bar.length", "0");
		assertThat(m2).isEqualTo(expected);
	}

	@Test
	public void testPrefixInterface() {
		Map<String,String> m = new HashMap<>();
		m.put("org.foo.bar.width", "327");
		m.put("org.foo.bar.warp", "eeej");
		m.put("length", "12");

		PrefixInterface i = converter.convert(m).to(PrefixInterface.class);
		assertThat(i.width()).isEqualTo(327L);
		assertThatExceptionOfType(ConversionException.class).as("Should have thrown an exception")
				.isThrownBy(() -> i.length());

		PrefixInterface i2 = new PrefixInterface() {
			@Override
			public long width() {
				return Long.MAX_VALUE;
			}

			@Override
			public int length() {
				return Integer.MIN_VALUE;
			}
		};

		Map<String,String> m2 = converter.convert(i2)
				.to(new TypeReference<Map<String,String>>() {
				});
		Map<String,String> expected = new HashMap<>();
		expected.put("org.foo.bar.width", "" + Long.MAX_VALUE);
		expected.put("org.foo.bar.length", "" + Integer.MIN_VALUE);
		assertThat(m2).isEqualTo(expected);
	}

	@Test
	public void testAnnotationInterface() {
		Map<String,String> m = new HashMap<>();
		m.put("org.foo.bar.width", "327");
		m.put("org.foo.bar.warp", "eeej");
		m.put("length", "12");

		PrefixAnnotation pa = converter.convert(m).to(PrefixAnnotation.class);
		assertThat(pa.width()).isEqualTo(327L);
		assertThat(pa.length()).isEqualTo(51);

		Map<String,String> m2 = converter.convert(pa)
				.to(new TypeReference<Map<String,String>>() {
				});
		Map<String,String> expected = new HashMap<>();
		expected.put("org.foo.bar.width", "327");
		expected.put("org.foo.bar.length", "51");
		assertThat(m2).isEqualTo(expected);
	}

	@Test
	public void testPrefixEnumAnnotation() {
		PrefixEnumAnnotation pea = converter.convert(Collections.emptyMap())
				.to(PrefixEnumAnnotation.class);

		assertThat(pea.timeout()).isEqualTo(1000);
		assertThat(pea.type()).isEqualTo(PrefixEnumAnnotation.Type.SINGLE);

		@SuppressWarnings("rawtypes")
		Map m = converter.convert(pea).to(Map.class);
		assertThat(m.get("com.acme.config.timeout")).isEqualTo(1000L);
		assertThat(m.get("com.acme.config.type")).isEqualTo(PrefixEnumAnnotation.Type.SINGLE);
	}

	@Test
	public void testTargetAsString() {
		Map<String,String> m = new HashMap<>();
		CharSequence cs = converter.convert(m)
				.targetAs(String.class)
				.to(CharSequence.class);
		assertThat(cs).isNull();

		Map<String,String> m2 = new HashMap<>();
		m2.put("Hi", "there");
		CharSequence cs2 = converter.convert(m2)
				.targetAs(String.class)
				.to(CharSequence.class);
		assertThat(cs2).isEqualTo("Hi");
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	@Test
	public void testTargetAsDTO() {
		MyDTOWithMethods expected = new MyDTOWithMethods();
		expected.count = Count.ONE;
		expected.ping = "pong";
		expected.pong = 42;
		Map m = new HashMap<>();
		m.put("count", Count.ONE);
		m.put("ping", "pong");
		m.put("pong", 42);
		MyDTOWithMethods actual = converter.convert(m)
				.targetAsDTO()
				.to(MyDTOWithMethods.class);
		assertThat(actual.count).isEqualTo(expected.count);
		assertThat(actual.ping).isEqualTo(expected.ping);
		assertThat(actual.pong).isEqualTo(expected.pong);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testLongArrayToLongCollection() {
		Long[] la = new Long[] {
				Long.MIN_VALUE, Long.MAX_VALUE
		};

		List lc = converter.convert(la).to(List.class);

		assertThat(lc).hasSameSizeAs(la);

		int i = 0;
		for (Iterator it = lc.iterator(); it.hasNext(); i++) {
			assertThat(it.next()).isEqualTo(la[i]);
		}
	}

	@Test
	public void testMapToInterfaceWithGenerics() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("charSet", new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));

		MyGenericInterface converted = converter.convert(dto)
				.to(MyGenericInterface.class);
		assertThat(converted.charSet()).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));

	}

	@Test
	public void testMapToInterfaceWithOptional() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("text", "foo");
		dto.put("textOptional", Optional.of("fooOptional"));
		dto.put("textOptionalOptional",
				Optional.of(Optional.of("fooOptionalOptional")));

		dto.put("textSetDefaultEmptyOptional", "bar");
		dto.put("textNullSet", null);
		dto.put("textNullSetDefaultEmptyOptional", null);

		dto.put("oInt", 1);
		dto.put("oIntNullSet", null);
		dto.put("oIntBadValue", "badValue");


		dto.put("oDouble", 2D);
		dto.put("oDoubleNullSet", null);
		dto.put("oDoubleBadValue", "badValue");


		dto.put("oLong", 3L);
		dto.put("oLongNullSet", null);
		dto.put("oLongBadValue", "badValue");

		MyGenericInterfaceOptional converted = converter.convert(dto)
				.to(MyGenericInterfaceOptional.class);

		assertThat(converted.text()).isPresent();
		assertThat(converted.text()).containsSame("foo");
		assertThat(converted.oInt()).hasValue(1);
		assertThat(converted.oDouble()).hasValue(2D);
		assertThat(converted.oLong()).hasValue(3L);

		assertThat(converted.textOptional()).isPresent();
		assertThat(converted.textOptional().get()).isNotNull()
				.isSameAs("fooOptional");

		assertThat(converted.textOptionalOptional()).isPresent();
		assertThat(converted.textOptionalOptional().get())
				.isInstanceOf(Optional.class);
		assertThat(converted.textOptionalOptional().get().get())
				.isNotEmpty()
				.isSameAs("fooOptionalOptional");

		assertThat(converted.textSetDefaultEmptyOptional())
				.isPresent();
		assertThat(converted.textSetDefaultEmptyOptional()).containsSame("bar");

		assertThat(converted.textNotSetDefaultEmptyOptional()).isEmpty();

		assertThat(converted.textNullSetDefaultEmptyOptional()).isEmpty();

		assertThat(converted.optionalIntNotSetDefaultEmptyOptional())
				.isNotPresent();
		assertThat(converted.optionalDoubleNotSetDefaultEmptyOptional())
				.isNotPresent();
		assertThat(converted.optionalLongNotSetDefaultEmptyOptional())
				.isNotPresent();

		assertThat(converted.textNullSet()).isEmpty();
		assertThat(converted.oDoubleNullSet()).isEmpty();
		assertThat(converted.oLongNullSet()).isEmpty();
		assertThat(converted.oIntNullSet()).isEmpty();

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.textNotSet());

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oIntNotSet());

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oDoubleNotSet());

		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oLongNotSet());

		// Conversion failures should be deferred when using interfaces
		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oIntBadValue());
		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oDoubleBadValue());
		assertThatExceptionOfType(ConversionException.class).isThrownBy(() -> converted.oLongBadValue());

	}

	@Test
	public void testMapToInterfaceWithGenericVariables() {
		Map<String,Object> dto = new HashMap<>();
		dto.put("set", new HashSet<>(Arrays.asList("foo", (int) 'o', 'o')));
		dto.put("raw", "1234");
		dto.put("array", Arrays.asList("foo", (int) 'o', 'o'));

		MyGenericInterfaceWithVariables<Character> converted = converter
				.convert(dto)
				.to(new TypeReference<MyGenericInterfaceWithVariables<Character>>() {
				});
		assertThat(converted.raw()).isEqualTo(Character.valueOf('1'));
		assertThat(converted.array()).isEqualTo(new Character[] {
				'f', 'o', 'o'
		});
		assertThat(converted.set()).isEqualTo(new HashSet<Character>(Arrays.asList('f', 'o')));
	}

	@Test
	public void testMapToInterfaceWithOptionalValue() throws Exception {
		ConverterBuilder cb = Converters.newConverterBuilder();
		cb.errorHandler(new ConverterFunction() {
			@Override
			public Object apply(Object pObj, Type pTargetType)
					throws Exception {
				if ("java.lang.Integer".equals(pTargetType.getTypeName())) {
					return 0;
				}
				return ConverterFunction.CANNOT_HANDLE;
			}
		});
		Converter convWithErrorHandler = cb.build();

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("code", "harley");
		MyIntf2 inter = convWithErrorHandler.convert(map).to(MyIntf2.class);
		assertThat(inter.code()).isEqualTo("harley");
		assertThat(inter.value()).isEqualTo(Integer.valueOf(0));
	}

	@Test
	public void testMapToEmptyInterface() throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("a", "b");
		EmptyInterface i = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface.class);
		assertThat(i).isNotNull();

		EmptyInterface2 j = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface2.class);
		assertThat(j).isNotNull();

		EmptyInterface3 k = Converters.standardConverter()
				.convert(map)
				.to(EmptyInterface3.class);
		assertThat(k).isNotNull();
	}

	@interface AnnType {
		boolean a();

		String b();
	}

	interface Interf {
		default Boolean a() {
			return null;
		}

		default Boolean b() {
			return null;
		}
	}

	@Test
	public void testDefaultInterfaceToAnnotationType() throws Throwable {
		AnnType a = Converters.standardConverter().convert(new Interf() {
		}).to(AnnType.class);
		assertThat(a.a()).isFalse();
		assertThat(a.b()).isNull();

	}

	@Test
	public void testDefaultInterfaceMethod() throws Throwable {
		Class< ? > clazz = InterfaceWithDefaultMethod.class;
		InterfaceWithDefaultMethod i = (InterfaceWithDefaultMethod) Converters
				.standardConverter()
				.convert(new HashMap<String,Object>())
				.to(clazz);
		assertThat(i.defaultMethod()).isEqualTo(InterfaceWithDefaultMethod.RESULT);
		assertThat(i.defaultMethodNull()).isNull();
		Assertions.assertThatExceptionOfType(ConversionException.class)
				.isThrownBy(() -> i.defaultMethodException());

		ConverterFunction errHandler = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type targetType) throws Exception {
				return "ok";
			}
		};
		ConverterBuilder cb = converter.newConverterBuilder();
		Converter c = cb.errorHandler(errHandler).build();

		Map< ? , ? > m = new HashMap<>();

		InterfaceWithDefaultMethod ie = c.convert(m)
				.to(InterfaceWithDefaultMethod.class);
		assertThat(ie.defaultMethodException()).isEqualTo("ok");

		Assertions.assertThatExceptionOfType(ConversionException.class)
				.isThrownBy(() -> i.nonDefault());

	}

	@Test
	public void testConvertBooleanToNumber() {
		assertThat(converter.convert(Boolean.TRUE).to(Byte.class)).isEqualTo(Byte.valueOf((byte) 1));
		assertThat(converter.convert(Boolean.TRUE).to(Short.class)).isEqualTo(Short.valueOf((short) 1));
		assertThat(converter.convert(Boolean.TRUE).to(Integer.class)).isEqualTo(Integer.valueOf(1));
		assertThat(converter.convert(Boolean.TRUE).to(Long.class)).isEqualTo(Long.valueOf(1));
		assertThat(converter.convert(Boolean.TRUE).to(Float.class)).isEqualTo(Float.valueOf(1.0f));
		assertThat(converter.convert(Boolean.TRUE).to(Double.class)).isEqualTo(Double.valueOf(1.0));
	}

	public static interface MyIntf2 {
		String code();

		Integer value();
	}

	public static interface EmptyInterface {
	}

	public static interface EmptyInterface2 extends EmptyInterface {
	}

	public static interface NonEmptyInterface {
		int a();
	}

	public static interface EmptyInterface3 extends NonEmptyInterface {
	}

	public static class MyClass2 {
		private final String value;

		public MyClass2(String v) {
			value = v;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public static interface MyIntf {
		int value();
	}

	public static class MyBean implements MyIntf {
		int		intfVal;
		String	beanVal;

		@Override
		public int value() {
			return intfVal;
		}

		public String getValue() {
			return beanVal;
		}
	}

	public static class MyClass3 {
		public int		value;
		public String	string	= "String";

		public MyClass3(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnnotation {
		int value() default 17;
	}

	public enum SomeEnum {
		VALUE, GETVALUE
	};
}
