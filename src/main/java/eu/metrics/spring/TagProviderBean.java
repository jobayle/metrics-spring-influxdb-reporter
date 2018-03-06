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

import java.util.Map;
import java.util.Objects;

/**
 * Provides global tags to configure the InfluxDB reporter.
 */
public class TagProviderBean {

	protected final Map<String, String> tags;

	/**
	 * Creates a new instance of the TagProviderBean.
	 *
	 * @param tags a map of (column_name, column_value)
	 */
	public TagProviderBean(Map<String, String> tags) {
		Objects.requireNonNull(tags, "No tags specified to construct a TagProviderBean");
		this.tags = tags;
	}

	/**
	 * @return map of global tags used by {@code DropwizardTransformer}
	 */
	public Map<String, String> getTags() {
		return tags;
	}

}
