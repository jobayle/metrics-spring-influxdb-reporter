/**
 * Copyright (C) 2018 Jonathan Bayle (jobayle@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.metrics.spring.reporter;

import com.ryantenney.metrics.spring.reporter.AbstractReporterElementParser;

/**
 * Parses the reporter configuration from a spring context, registered via SPI.
 */
public final class InfluxDBReporterElementParser extends AbstractReporterElementParser {

	private final String BOOL_STRING_REGEX = "^(true|false)$";

	@Override
	public String getType() {
		return "influxdb";
	}

	@Override
	protected Class<?> getBeanClass() {
		return InfluxDBReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		c.require(InfluxDBReporterFactoryBean.PERIOD, DURATION_STRING_REGEX,
				"Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");
		c.require(InfluxDBReporterFactoryBean.DATABASE, null, "Database attribute is required");
		c.require(InfluxDBReporterFactoryBean.LAYER,
				"^(http|tcp)$", "layer attribute is required, its value must be either \"http\" or \"tcp\"");

		c.optional(InfluxDBReporterFactoryBean.HOST);
		c.optional(InfluxDBReporterFactoryBean.PORT, PORT_NUMBER_REGEX, "Port number must be between 1 and 65536");
		c.optional(InfluxDBReporterFactoryBean.RATE_UNIT, TIMEUNIT_STRING_REGEX,
				"Rate unit must be one of the enum constants from java.util.concurrent.TimeUnit");
		c.optional(InfluxDBReporterFactoryBean.DURATION_UNIT, TIMEUNIT_STRING_REGEX,
				"Duration unit must be one of the enum constants from java.util.concurrent.TimeUnit");
		c.optional(InfluxDBReporterFactoryBean.GROUP_COUNTERS, BOOL_STRING_REGEX,
				"Group-counters must be either \"true\" of \"false\"");
		c.optional(InfluxDBReporterFactoryBean.GROUP_GAUGES, BOOL_STRING_REGEX,
				"Group-gauges must be either \"true\" of \"false\"");

		c.optional(InfluxDBReporterFactoryBean.TAG_PROVIDER_REF);
		c.optional(InfluxDBReporterFactoryBean.TEMPLATE_PROVIDER_REF);

		c.optional(InfluxDBReporterFactoryBean.FILTER_PATTERN);
		c.optional(InfluxDBReporterFactoryBean.FILTER_REF);
		if (c.has(InfluxDBReporterFactoryBean.FILTER_PATTERN) && c.has(InfluxDBReporterFactoryBean.FILTER_REF)) {
			c.reject(InfluxDBReporterFactoryBean.FILTER_REF,
					"Reporter element must not specify both the 'filter' and 'filter-ref' attributes");
		}

		c.rejectUnmatchedProperties();
	}
}
