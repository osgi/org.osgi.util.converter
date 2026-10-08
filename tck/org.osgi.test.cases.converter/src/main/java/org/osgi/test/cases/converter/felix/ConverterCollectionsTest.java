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

import static java.lang.Long.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.TypeReference;

public class ConverterCollectionsTest {
	@Test
	public void testLiveBackingList() {
		List<Integer> l = asList(9, 8, 7);
		Converter converter = Converters.standardConverter();
		List<Short> sl = converter.convert(l)
				.view()
				.to(new TypeReference<List<Short>>() {
				});

		assertThat(sl.get(0)).isEqualTo(Short.valueOf((short) 9));
		assertThat(sl.get(1)).isEqualTo(Short.valueOf((short) 8));
		assertThat(sl.get(2)).isEqualTo(Short.valueOf((short) 7));
		assertThat(sl).hasSize(3);

		l.set(1, 11);
		assertThat(sl.get(0)).isEqualTo(Short.valueOf((short) 9));
		assertThat(sl.get(1)).isEqualTo(Short.valueOf((short) 11));
		assertThat(sl.get(2)).isEqualTo(Short.valueOf((short) 7));
		assertThat(sl).hasSize(3);

		List<Short> sl2 = converter.convert(l)
				.view()
				.to(new TypeReference<List<Short>>() {
				});
		List<Short> sl3 = converter.convert(l)
				.view()
				.to(new TypeReference<List<Short>>() {
				});
		sl3.add(Short.valueOf((short) 6));

		assertThat(sl2).hasSameHashCodeAs(sl);
		assertThat(sl.hashCode()).isNotEqualTo(sl3.hashCode());

		assertThat(sl2).isEqualTo(sl);
		assertThat(sl.equals(sl3)).isFalse();
	}

	@Test
	public void testLiveBackingList1() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		a[0] = 7l;
		l.addAll(asList(7, 6));
		a[0] = 1l;
		assertThat(l).isEqualTo(Arrays.asList(7, 8, 7, 6));
	}

	@Test
	public void testLiveBackingList2() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.addAll(1, asList(7, 6));
		a[0] = 1l;
		assertThat(l).isEqualTo(Arrays.asList(9, 7, 6, 8));
	}

	@Test
	public void testLiveBackingList3() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.removeAll(Collections.singleton(8));
		a[0] = 1l;
		assertThat(l).isEqualTo(Collections.singletonList(9));
	}

	@Test
	public void testLiveBackingList4() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.retainAll(Collections.singleton(8));
		a[1] = 1l;
		assertThat(l).isEqualTo(Collections.singletonList(8));
	}

	@Test
	public void testLiveBackingList5() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.clear();
		l.add(10);
		a[0] = 1l;
		assertThat(l).isEqualTo(Collections.singletonList(10));
	}

	@Test
	public void testLiveBackingList6() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.add(10);
		a[0] = 1l;
		assertThat(l).isEqualTo(Arrays.asList(9, 8, 10));
	}

	@Test
	public void testLiveBackingList7() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		l.add(0, 10);
		a[0] = 1l;
		assertThat(l).isEqualTo(Arrays.asList(10, 9, 8));
	}

	@Test
	public void testLiveBackingList8() {
		long[] a = new long[] {
				9l, 8l
		};

		List<Integer> l = Converters.standardConverter()
				.convert(a)
				.view()
				.to(new TypeReference<List<Integer>>() {
				});
		assertThat(l.remove(1)).isEqualTo(Integer.valueOf(8));
		a[0] = 1l;
		assertThat(l).isEqualTo(Arrays.asList(9));
	}

	@Test
	public void testLiveBackingCollection() {
		Set<String> s = new LinkedHashSet<>(asList("yo", "yo", "ma"));
		Converter converter = Converters.standardConverter();
		List<String> sl = converter.convert(s)
				.view()
				.to(new TypeReference<List<String>>() {
				});

		assertThat(sl.get(0)).isEqualTo("yo");
		assertThat(sl.get(1)).isEqualTo("ma");
		assertThat(sl).hasSize(2);

		s.add("ha");
		s.add("yo");
		assertThat(sl.get(0)).isEqualTo("yo");
		assertThat(sl.get(1)).isEqualTo("ma");
		assertThat(sl.get(2)).isEqualTo("ha");
		assertThat(sl).hasSize(3);
		assertThat(sl).isNotEmpty();

		assertThat(sl.contains("ma")).isTrue();
		assertThat(sl.contains("na")).isFalse();

		String[] sa = sl.toArray(new String[] {});
		assertThat(sa).hasSize(3);
		assertThat(sa[0]).isEqualTo("yo");
		assertThat(sa[1]).isEqualTo("ma");
		assertThat(sa[2]).isEqualTo("ha");

		assertThat(sl.containsAll(Arrays.asList("ma", "yo"))).isTrue();
		assertThat(sl.containsAll(Arrays.asList("xxx"))).isFalse();
	}

	@Test
	public void testLiveBackingEmptyCollection() {
		Set<Long> s = Collections.emptySet();
		Collection< ? > l = Converters.standardConverter()
				.convert(s)
				.view()
				.to(Collection.class);
		assertThat(l).isEmpty();
		assertThat(l.size()).isZero();
	}

	@Test
	public void testLiveBackingArray() {
		Converter converter = Converters.standardConverter();
		int[] arr = new int[] {
				1, 2
		};

		@SuppressWarnings("rawtypes")
		List l = converter.convert(arr).view().to(List.class);
		assertThat(l).hasSize(2);
		assertThat(l).isNotEmpty();
		assertThat(l.get(0)).isEqualTo(1);
		assertThat(l.get(1)).isEqualTo(2);

		assertThat(l.contains(1)).isTrue();
		assertThat(l.contains(2)).isTrue();
		assertThat(l.contains(3)).isFalse();
		assertThat(l.contains(0)).isFalse();

		arr[0] = -3;
		arr[1] = 3;
		assertThat(l.get(0)).isEqualTo(-3);
		assertThat(l.get(1)).isEqualTo(3);
	}

	@Test
	public void testLiveBackingMixedArrayWithNulls() {
		Object[] oa = new Object[] {
				"hi", null, 'x'
		};
		List< ? > l = Converters.standardConverter()
				.convert(oa)
				.view()
				.to(List.class);
		assertThat(l.contains("hi")).isTrue();
		assertThat(l.contains(null)).isTrue();
		assertThat(l.contains('x')).isTrue();
		assertThat(l.containsAll(Arrays.asList('x', 7))).isFalse();
		assertThat(l.containsAll(Arrays.asList('x', null, null, "hi", "hi"))).isTrue();
		assertThat(l.indexOf("hi")).isZero();
		assertThat(l.indexOf(null)).isOne();
		assertThat(l.indexOf('x')).isEqualTo(2);
		assertThat(l.indexOf("test")).isEqualTo(-1);

		List< ? > l0 = l.subList(1, 1);
		assertThat(l0.size()).isZero();
		List< ? > l1 = l.subList(1, 2);
		assertThat(l1).isEqualTo(Arrays.asList((Object) null));
		List< ? > l2 = l.subList(1, 3);
		assertThat(l2).isEqualTo(Arrays.asList(null, 'x'));
		List< ? > l3 = l.subList(0, 2);
		assertThat(l3).isEqualTo(asList("hi", null));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testLiveStringArray() {
		String[] sa = new String[] {
				"yo", "ho", "yo", null, "yo"
		};

		List<String> l = Converters.standardConverter()
				.convert(sa)
				.view()
				.to(new TypeReference<List<String>>() {
				});
		Object[] oa1 = l.toArray();
		String[] sa1 = l.toArray(new String[] {});
		assertThat(sa1[0]).isEqualTo("yo");
		assertThat(sa1[1]).isEqualTo("ho");
		assertThat(sa1[2]).isEqualTo("yo");
		assertThat(sa1[3]).isNull();
		assertThat(sa1[4]).isEqualTo("yo");
		assertThat(sa1[0]).isEqualTo(oa1[0]);
		assertThat(sa1[1]).isEqualTo(oa1[1]);
		assertThat(sa1[2]).isEqualTo(oa1[2]);
		assertThat(sa1[3]).isEqualTo(oa1[3]);
		assertThat(sa1[4]).isEqualTo(oa1[4]);
		assertThat(oa1).hasSize(5);
		assertThat(sa1).hasSize(5);

		String[] sa2 = l.toArray(new String[6]);
		assertThat(sa2[0]).isEqualTo(oa1[0]);
		assertThat(sa2[1]).isEqualTo(oa1[1]);
		assertThat(sa2[2]).isEqualTo(oa1[2]);
		assertThat(sa2[3]).isEqualTo(oa1[3]);
		assertThat(sa2[4]).isEqualTo(oa1[4]);
		assertThat(sa2[5]).isNull();
		assertThat(sa2).hasSize(6);

		assertThat(l.lastIndexOf("yo")).isEqualTo(4);
		assertThat(l.lastIndexOf("ho")).isOne();
		assertThat(l.lastIndexOf(null)).isEqualTo(3);
		assertThat(l.lastIndexOf(Integer.valueOf(123))).isEqualTo(-1);
	}

	@Test
	public void testLiveBackingArray0() {
		Converter converter = Converters.standardConverter();
		List< ? > l = converter.convert(new double[] {}).view().to(List.class);
		assertThat(l).isEmpty();
		assertThat(l.size()).isZero();
	}

	@Test
	public void testLiveBackingArray1() {
		Converter converter = Converters.standardConverter();
		Integer[] arr = new Integer[] {
				1, 2
		};

		@SuppressWarnings("rawtypes")
		List l = converter.convert(arr).view().to(List.class);
		assertThat(l.get(0)).isEqualTo(1);
		assertThat(l.get(1)).isEqualTo(2);

		arr[0] = -3;
		arr[1] = 3;
		assertThat(l.get(0)).isEqualTo(-3);
		assertThat(l.get(1)).isEqualTo(3);
	}

	@Test
	public void testLiveBackingArray2() {
		Converter converter = Converters.standardConverter();
		Integer[] arr = new Integer[] {
				1, 2
		};

		List<Long> l = converter.convert(arr)
				.view()
				.to(new TypeReference<List<Long>>() {
				});
		assertThat(l.contains(Long.valueOf(2))).isTrue();
		assertThat(l.containsAll(Arrays.asList(Long.valueOf(2), Long.valueOf(1)))).isTrue();
		assertThat(l.contains(Long.valueOf(3))).isFalse();
		assertThat(l.containsAll(Arrays.asList(Long.valueOf(2), Long.valueOf(3)))).isFalse();

		arr[0] = Integer.valueOf(3);
		assertThat(l.contains(Long.valueOf(2))).isTrue();
		assertThat(l.containsAll(Arrays.asList(Long.valueOf(2), Long.valueOf(1)))).isFalse();
		assertThat(l.contains(Long.valueOf(3))).isTrue();
		assertThat(l.containsAll(Arrays.asList(Long.valueOf(2), Long.valueOf(3)))).isTrue();

		l.add(valueOf(4));
		l.add(valueOf(5));
		arr[0] = Integer.valueOf(1);
		assertThat(l.containsAll(Arrays.asList(Long.valueOf(2), Long.valueOf(3),
				Long.valueOf(4), Long.valueOf(5)))).isTrue();
	}

	@Test
	public void testLiveBackingArray3() {
		Converter converter = Converters.standardConverter();
		Integer[] arr = new Integer[] {
				1, 2
		};

		List<Long> l = converter.convert(arr)
				.view()
				.to(new TypeReference<List<Long>>() {
				});
		assertThat(l.remove(valueOf(1))).isTrue();
		arr[1] = Integer.valueOf(3);
		assertThat(l).isEqualTo(Collections.singletonList(Long.valueOf(2)));
	}

	@Test
	public void testLiveArrayBackingSet() {
		char[] ca = new char[] {
				'a', 'b', 'c'
		};

		Set<Character> s = Converters.standardConverter()
				.convert(ca)
				.view()
				.to(new TypeReference<Set<Character>>() {
				});
		assertThat(s.containsAll(Arrays.asList(Character.valueOf('a'),
				Character.valueOf('b'), Character.valueOf('c')))).isTrue();

		ca[0] = 'd';
		assertThat(s.containsAll(Arrays.asList(Character.valueOf('b'),
				Character.valueOf('c'), Character.valueOf('d')))).isTrue();
	}

	@Test
	public void testLiveBackingSet() {
		List<Double> l = new ArrayList<>();
		l.add(3.1415);

		Set<Float> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<Float>>() {
				});
		Float f1 = Float.valueOf(3.1415f);
		Float f2 = Float.valueOf(1.0f);
		assertThat(s.size()).isEqualTo(1);
		assertThat(s.isEmpty()).isFalse();
		assertThat(s.contains(f1)).isTrue();
		assertThat(s.contains(f2)).isFalse();
		assertThat(s.iterator().next()).isEqualTo(f1);

		l.set(0, null);
		assertThat(s.size()).isEqualTo(1);
		assertThat(s.isEmpty()).isFalse();
		assertThat(s.contains(null)).isTrue();
		assertThat(s.contains(f2)).isFalse();
		assertThat(s.contains(f1)).isFalse();
		assertThat(s.iterator().next()).isNull();

		Float f3 = Float.valueOf(2.7182f);
		s.add(f3);
		assertThat(l).as("Original should not be modified").hasSize(1);
		l.set(0, -1.0);
		assertThat(s.size()).isEqualTo(2);
		assertThat(s.contains(null)).isTrue();
		assertThat(s.contains(f3)).isTrue();
		assertThat(s.contains(f2)).isFalse();
		assertThat(s.contains(f1)).isFalse();
	}

	@Test
	public void testLiveBackingSet0() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<String> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<String>>() {
				});
		l.set(0, "ho");

		String[] sa = s.toArray(new String[1]);
		assertThat(Arrays.asList(sa)).isEqualTo(Arrays.asList("ho", "there"));

		String[] sa2 = s.toArray(new String[4]);
		assertThat(Arrays.asList(sa2)).isEqualTo(Arrays.asList("ho", "there", null, null));

		Set<String> s2 = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<String>>() {
				});
		Set<String> s3 = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<String>>() {
				});
		s3.add("!!");
		assertThat(s2).hasSameHashCodeAs(s);
		assertThat(s.hashCode()).isNotEqualTo(s3.hashCode());

		assertThat(s.equals(s2)).isTrue();
		assertThat(s.equals(s3)).isFalse();
	}

	@Test
	public void testLiveBackingSet1() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<CharSequence> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<CharSequence>>() {
				});
		assertThat(s.containsAll(Arrays.asList("there", "hi"))).isTrue();
		s.clear();
		assertThat(l).as("Original should not be modified").isEqualTo(asList("hi", "there"));
		assertThat(s).hasSize(0);
		assertThat(s).isEmpty();
	}

	@Test
	public void testLiveBackingSet2() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<CharSequence> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<CharSequence>>() {
				});
		s.remove("yo");
		l.set(0, "xxx"); // Should not have an effect since 'remove' was called
		assertThat(s.containsAll(Arrays.asList("there", "hi"))).isTrue();

		s.remove("hi");
		assertThat(s).isEqualTo(Collections.singleton("there"));
	}

	@Test
	public void testLiveBackingSet3() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<CharSequence> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<CharSequence>>() {
				});
		assertThat(s.addAll(singleton("there"))).isFalse();
		assertThat(s.addAll(asList("there", "!!"))).isTrue();
		l.remove("hi");
		assertThat(s.containsAll(Arrays.asList("there", "hi", "!!"))).isTrue();
	}

	@Test
	public void testLiveBackingSet4() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<CharSequence> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<CharSequence>>() {
				});
		assertThat(s.removeAll(singleton("yo"))).isFalse();
		l.remove("hi");
		assertThat(s.containsAll(Arrays.asList("there", "hi"))).isTrue();
		assertThat(s.removeAll(Arrays.asList("there", "hi"))).isTrue();
		assertThat(s.size()).isEqualTo(0);
	}

	@Test
	public void testLiveBackingSet5() {
		List<String> l = new ArrayList<>();
		l.addAll(asList("hi", "there"));

		Set<CharSequence> s = Converters.standardConverter()
				.convert(l)
				.view()
				.to(new TypeReference<Set<CharSequence>>() {
				});

		assertThat(s.retainAll(Arrays.asList("hi", "!!"))).isTrue();
		assertThat(s).isEqualTo(new HashSet<>(Collections.singleton("hi")));
	}
}
