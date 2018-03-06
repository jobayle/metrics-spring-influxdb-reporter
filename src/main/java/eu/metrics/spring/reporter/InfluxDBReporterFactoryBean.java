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

import com.codahale.metrics.MetricRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.kickstarter.dropwizard.metrics.influxdb.InfluxDbMeasurementReporter;
import com.kickstarter.dropwizard.metrics.influxdb.io.InfluxDbHttpWriter;
import com.kickstarter.dropwizard.metrics.influxdb.io.InfluxDbTcpWriter;
import com.kickstarter.dropwizard.metrics.influxdb.io.InfluxDbWriter;
import com.kickstarter.dropwizard.metrics.influxdb.io.Sender;
import com.kickstarter.dropwizard.metrics.influxdb.transformer.DropwizardMeasurementParser;
import com.kickstarter.dropwizard.metrics.influxdb.transformer.DropwizardTransformer;
import com.kickstarter.dropwizard.metrics.influxdb.transformer.TaggedPattern;

import com.ryantenney.metrics.spring.reporter.AbstractScheduledReporterFactoryBean;

import eu.metrics.spring.TagProviderBean;
import eu.metrics.spring.TemplateProviderBean;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.util.Duration;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Clock;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;

/**
 * Factory bean used by the metrics-spring component.
 */
public final class InfluxDBReporterFactoryBean extends AbstractScheduledReporterFactoryBean<InfluxDbMeasurementReporter> {

	// Required
	public static final String PERIOD = "period";
	public static final String DATABASE = "database";
	public static final String LAYER = "layer";

	// Optional
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";
	public static final String GROUP_COUNTERS = "group-counters";
	public static final String GROUP_GAUGES = "group-gauges";
	public static final String TAG_PROVIDER_REF = "tag-provider";
	public static final String TEMPLATE_PROVIDER_REF = "template-provider";
	public static final String FILTER_PATTERN = "filter"; // Field is protected in parent class !?
	public static final String FILTER_REF = "filter-ref"; // Field is protected in parent class !?

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

	@Override
	public Class<? extends InfluxDbMeasurementReporter> getObjectType() {
		return InfluxDbMeasurementReporter.class;
	}

	@Override
	protected InfluxDbMeasurementReporter createInstance() throws Exception {
		MetricRegistry registry = getMetricRegistry();

		InfluxDbWriter dbWriter = null;
		String layer = getProperty(LAYER);
		String database = getProperty(DATABASE);
		String host = getProperty(HOST, "localhost");
		int port = Integer.valueOf(getProperty(PORT, "8086"));

		if (layer.equals("http")) {
			Client client = new JerseyClientBuilder(registry)
					.using(new JerseyClientConfiguration())
					.using(new ObjectMapper())
					.using(Executors.newSingleThreadExecutor())
					.build("influxdb-http-writer");
			try {
				final String query = "/write?db=" + URLEncoder.encode(database, "UTF-8");
				final URL endpoint = new URL("http", host, port, query);
				dbWriter = new InfluxDbHttpWriter(client, endpoint.toString());
			} catch (MalformedURLException | UnsupportedEncodingException ex) {
				throw new IllegalArgumentException(ex);
			}
		} else {
			dbWriter = new InfluxDbTcpWriter(host, port, Duration.milliseconds(500)); // TODO timeout configuration
		}
		Sender sender = new Sender(dbWriter);

		TimeUnit durationUnit = TimeUnit.MILLISECONDS;
		if (hasProperty(DURATION_UNIT)) {
			durationUnit = getProperty(DURATION_UNIT, TimeUnit.class);
		}
		TimeUnit rateUnit = TimeUnit.SECONDS;
		if (hasProperty(RATE_UNIT)) {
			rateUnit = getProperty(RATE_UNIT, TimeUnit.class);
		}

		boolean groupCounters = getProperty(GROUP_COUNTERS, Boolean.class, Boolean.FALSE);
		boolean groupGauges = getProperty(GROUP_GAUGES, Boolean.class, Boolean.FALSE);

		TagProviderBean tagProvider = getPropertyRef(TAG_PROVIDER_REF, TagProviderBean.class);
		Map<String, String> globalTags = tagProvider != null ? tagProvider.getTags() : Collections.EMPTY_MAP;

		TemplateProviderBean templateProvider = getPropertyRef(TEMPLATE_PROVIDER_REF, TemplateProviderBean.class);
		Map<String, TaggedPattern> metricTemplates = templateProvider != null ? templateProvider.getTemplates() : Collections.EMPTY_MAP;

		DropwizardTransformer transformer = new DropwizardTransformer(
				globalTags,
				DropwizardMeasurementParser.withTemplates(metricTemplates),
				groupCounters,
				groupGauges,
				rateUnit,
				durationUnit
		);

		return new InfluxDbMeasurementReporter(
				sender,
				registry,
				getMetricFilter(),
				rateUnit,
				durationUnit,
				Clock.systemUTC(),
				transformer
		);
	}

}
