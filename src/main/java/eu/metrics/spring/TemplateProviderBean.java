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
package eu.metrics.spring;

import com.kickstarter.dropwizard.metrics.influxdb.transformer.TaggedPattern;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides templates to configure the InfluxDB reporter.
 */
public class TemplateProviderBean {

	protected final Map<String, List<String>> templates;

	/**
	 * Creates a new instance of the TemplateProviderBean.
	 *
	 * @param templates a map of (column_name, list of (pattern, matching_groups...))
	 */
	public TemplateProviderBean(Map<String, List<String>> templates) {
		Objects.requireNonNull(templates, "No templates specified to construct a TemplateProviderBean");
		this.templates = templates;
	}

	/**
	 * @return map of templates used by {@code DropwizardMeasurementParser}
	 */
	public Map<String, TaggedPattern> getTemplates() {
		return templates.entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getKey(),
						e -> {
							List<String> p = e.getValue();
							return new TaggedPattern(p.get(0), p.subList(1, p.size()));
						}
				));
	}
}
