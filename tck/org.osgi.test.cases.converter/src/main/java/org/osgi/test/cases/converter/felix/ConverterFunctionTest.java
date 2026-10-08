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
import static org.assertj.core.api.Assertions.within;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.Rule;
import org.osgi.util.function.Function;

public class ConverterFunctionTest {
	@Test
	public void testConverterFunction() {
		Converter c = Converters.standardConverter();
		assertThat(c.convert("12.5").to(double.class)).isCloseTo(12.5, within(0.001));

		Function<Object,Double> f = c.function().to(double.class);
		assertThat(f.apply("12.5")).isCloseTo(12.5, within(0.001));
		assertThat(f.apply("50.505")).isCloseTo(50.505, within(0.001));
	}

	@Test
	public void testConverterFunctionWithModifier() {
		Converter c = Converters.standardConverter();

		Function<Object,Integer> cf = c.function()
				.defaultValue(999)
				.to(Integer.class);

		assertThat(cf.apply("")).isEqualTo(Integer.valueOf(999));
		assertThat(c.convert("").defaultValue(999).to(Integer.class)).isEqualTo(Integer.valueOf(999));

		assertThat(cf.apply("123")).isEqualTo(Integer.valueOf(123));
		assertThat(c.convert("123").defaultValue(999).to(Integer.class)).isEqualTo(Integer.valueOf(123));
	}

	@Test
	public void testConverterFunctionWithRule() {
		Converter c = Converters.standardConverter();
		Function<Object,String> cf = c.function().to(String.class);

		String[] sa = new String[] {
				"h", "i"
		};
		assertThat(cf.apply(sa)).isEqualTo("h");

		Converter ac = c.newConverterBuilder()
				.rule(new Rule<String[],String>(v -> String.join("", v)) {
				})
				.build();

		Function<Object,String> af = ac.function().to(String.class);
		assertThat(af.apply(sa)).isEqualTo("hi");
	}

	@Test
	public void testConverterFunctionAsDTO() {

		Map<Object,Object> mapEmb = new HashMap<>();
		mapEmb.put("alpha", "C");
		mapEmb.put("marco", "?");
		mapEmb.put("polo", "123456");

		Map<Object,Object> map = new HashMap<>();
		map.put("count", "TWO");
		map.put("ping", "someText");
		map.put("pong", "007");
		map.put("embedded", mapEmb);

		Function<Object,MyDTO> cvf = Converters.standardConverter()
				.function()
				.targetAsDTO()
				.to(MyDTO.class);
		MyDTO myDTO = cvf.apply(map);
		assertThat(myDTO.count).isEqualTo(MyDTO.Count.TWO);
		assertThat(myDTO.ping).isEqualTo("someText");
		assertThat(myDTO.pong).isEqualTo(7);
		assertThat(myDTO.embedded).isNotNull();
		assertThat(myDTO.embedded.alpha).isEqualTo(MyEmbeddedDTO.Alpha.C);
		assertThat(myDTO.embedded.marco).isEqualTo("?");
		assertThat(myDTO.embedded.polo).isEqualTo(123456);

	}
}
