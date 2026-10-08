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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.ConverterFunction;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.Rule;
import org.osgi.util.converter.TypeReference;
import org.osgi.util.converter.TypeRule;
import org.osgi.util.function.Function;

public class ConverterBuilderTest {
	Converter converter;

	@BeforeEach
	public void setUp() {
		converter = Converters.standardConverter();
	}

	@AfterEach
	public void tearDown() {
		converter = null;
	}

	@Test
	public void testStringArrayToStringAdapter() {
		ConverterBuilder cb = converter.newConverterBuilder();
		Converter ca = cb
				.rule(new TypeRule<String[],String>(String[].class,
						String.class,
						v -> Stream.of(v).collect(Collectors.joining(","))))
				.rule(new TypeRule<String,String[]>(String.class,
						String[].class, v -> v.split(",")))
				.build();

		assertThat(converter.convert(new String[] {
				"A", "B"
		}).to(String.class)).isEqualTo("A");
		assertThat(ca.convert(new String[] {
				"A", "B"
		}).to(String.class)).isEqualTo("A,B");

		assertThat(converter.convert("A,B").to(String[].class)).isEqualTo(new String[] {
				"A,B"
		});
		assertThat(ca.convert("A,B").to(String[].class)).isEqualTo(new String[] {
				"A", "B"
		});
	}

	static String convertToString(char[] a) {
		StringBuilder sb = new StringBuilder();
		for (char c : a) {
			sb.append(c);
		}
		return sb.toString();
	}

	@Test
	public void testSecondLevelAdapter() {
		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(new TypeRule<>(char[].class, String.class,
				ConverterBuilderTest::convertToString));
		cb.rule(Integer.class, (f, t) -> -1);
		cb.rule(Long.class, (f, t) -> -1L);
		Converter ca = cb.build();

		assertThat(ca.convert(new char[] {
				'h', 'i'
		}).to(String.class)).isEqualTo("hi");
		assertThat(ca.convert("Hello").to(Integer.class)).isEqualTo(Integer.valueOf(-1));
		assertThat(ca.convert("Hello").to(Long.class)).isEqualTo(Long.valueOf(-1));

		// Shadow the Integer variant but keep Long going to the Number variant.
		Converter ca2 = ca.newConverterBuilder()
				.rule(new TypeRule<String,Integer>(String.class, Integer.class,
						s -> s.length()))
				.build();
		assertThat((int) ca2.convert("Hello").to(Integer.class)).isEqualTo(5);
		assertThat(ca2.convert("Hello").to(Long.class)).isEqualTo(Long.valueOf(-1));
	}

	@Test
	@Disabled("unimplemented")
	public void testConvertToBaseArray() {
		// TODO
	}

	@Test
	@Disabled("unimplemented")
	public void testThrowExceptionInCustomConverter() {
		// TODO
	}

	@Test
	@Disabled("unimplemented")
	public void testMixedListToNumberCase() {
		// TODO
	}

	@Test
	public void testCannotHandleSpecific() {
		Converter ca = converter.newConverterBuilder()
				.rule(new TypeRule<>(Integer.class, Long.class,
						new Function<Integer,Long>() {
							@Override
							public Long apply(Integer obj) {
								if (obj.intValue() != 1)
									return new Long(-obj.intValue());
								return null;
							}
						}))
				.build();

		assertThat(ca.convert(Integer.valueOf(2)).to(Long.class)).isEqualTo(Long.valueOf(-2));

		// This is the exception that the rule cannot handle
		assertThat(ca.convert(Integer.valueOf(1)).to(Long.class)).isEqualTo(Long.valueOf(1));
	}

	@Test
	public void testWildcardAdapter() {
		ConverterFunction foo = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type type) throws Exception {
				if (!(obj instanceof List))
					return ConverterFunction.CANNOT_HANDLE;

				List< ? > t = (List< ? >) obj;
				if (t.size() == 0)
					// Empty list is converted to null
					return null;

				if (type instanceof Class) {
					if (Number.class.isAssignableFrom((Class< ? >) type))
						return converter.convert(t.size()).to(type);
				}
				return ConverterFunction.CANNOT_HANDLE;
			}
		};

		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(foo);
		cb.rule((v, t) -> v.toString());
		Converter ca = cb.build();

		assertThat((long) ca.convert(Arrays.asList("a", "b", "c")).to(Long.class)).isEqualTo(3L);
		assertThat((long) ca.convert(Arrays.asList("a", "b", "c"))
				.to(Integer.class)).isEqualTo(3);
		assertThat(ca.convert(Arrays.asList("a", "b", "c")).to(String.class)).isEqualTo("[a, b, c]");
		assertThat(ca.convert(Arrays.asList()).to(String.class)).isNull();
	}

	@Test
	public void testWildcardAdapter1() {
		ConverterFunction foo = new ConverterFunction() {
			@Override
			public Object apply(Object obj, Type type) throws Exception {
				if (!(obj instanceof List))
					return ConverterFunction.CANNOT_HANDLE;

				List< ? > t = (List< ? >) obj;
				if (type instanceof Class) {
					if (Number.class.isAssignableFrom((Class< ? >) type))
						return converter.convert(t.size()).to(type);
				}
				return ConverterFunction.CANNOT_HANDLE;
			}
		};

		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule((v, t) -> converter.convert(1).to(t));
		cb.rule(foo);
		Converter ca = cb.build();

		// The catchall converter should be called always because it can handle
		// all and was registered first
		assertThat((long) ca.convert(Arrays.asList("a", "b", "c")).to(Long.class)).isOne();
		assertThat((int) ca.convert(Arrays.asList("a", "b", "c"))
				.to(Integer.class)).isOne();
		assertThat(ca.convert(Arrays.asList("a", "b", "c")).to(String.class)).isEqualTo("1");
	}

	@Test
	public void testWildcardAdapter2() {
		Map<Object,Object> snooped = new HashMap<>();
		ConverterBuilder cb = converter.newConverterBuilder();
		cb.rule(new Rule<String[],ArrayList<String>>(v -> {
			Arrays.sort(v, Collections.reverseOrder());
			return new ArrayList<>(Arrays.asList(v));
		}) {
		});
		cb.rule(new Rule<String[],List<String>>(v -> {
			Arrays.sort(v, Collections.reverseOrder());
			return new CopyOnWriteArrayList<>(Arrays.asList(v));
		}) {
		});
		cb.rule((v, t) -> {
			snooped.put(v, t);
			return ConverterFunction.CANNOT_HANDLE;
		});
		Converter ca = cb.build();

		assertThat(ca.convert(new String[] {
						"a", "b", "c"
				}).to(new TypeReference<ArrayList<String>>() {
				})).isEqualTo(new ArrayList<>(Arrays.asList("c", "b", "a")));
		assertThat(snooped.size()).as("Precondition").isEqualTo(0);
		String[] sa0 = new String[] {
				"a", "b", "c"
		};
		assertThat(ca.convert(sa0).to(LinkedList.class)).isEqualTo(new LinkedList<>(Arrays.asList("a", "b", "c")));
		assertThat(snooped.size()).isEqualTo(1);
		assertThat(snooped.get(sa0)).isEqualTo(LinkedList.class);
		assertThat(ca.convert(new String[] {
						"a", "b", "c"
				}).to(new TypeReference<List<String>>() {
				})).isEqualTo(new CopyOnWriteArrayList<>(Arrays.asList("c", "b", "a")));

		snooped.clear();
		String[] sa = new String[] {
				"a", "b", "c"
		};
		assertThat(ca.convert(sa).to(CopyOnWriteArrayList.class)).isEqualTo(new CopyOnWriteArrayList<>(Arrays.asList("a", "b", "c")));
		assertThat(snooped.size()).isEqualTo(1);
		assertThat(snooped.get(sa)).isEqualTo(CopyOnWriteArrayList.class);
	}

	static interface MyIntf {
		int value();
	}

	static class MyBean implements MyIntf {
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

	static class MyCustomDTO {
		public String field;
	}
}
