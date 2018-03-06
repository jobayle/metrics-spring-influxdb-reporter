# InfluxDB reporter for metrics-spring

Depends on [ryantenney/metrics-spring](https://github.com/ryantenney/metrics-spring) and
[kickstarter/dropwizard-influxdb-reporter](https://github.com/kickstarter/dropwizard-influxdb-reporter).

## Basic usage

XML spring configuration:

```XML
    <!-- Report metrics to an InfluxDB database, attributes are:
          - period (required) reporting rate
          - database (required) name of the database, it must be created
          - layer (required) connection layer to InfluxDB, either "http" or "tcp"
          - host (optional) of an InfluxDB instance
          - port (optional) to connect to an InfluxDB instance
          - filter (optional) filter pattern on metric names
          - filter-ref (optional) ref to a MetricFilter bean
          - duration-unit (optional) unit for durations (defaults to milliseconds)
          - rate-unit (optional) unit for rates (defaults to seconds)
          - group-counters (optional) "true" or "false" (default)
          - group-gauges (optional) "true" or "false" (default)
          - tag-provider (optional) ref to bean that provides base tags
          - template-provider (optional) ref to bean that provides templates
    -->
    <metrics:reporter type="influxdb" metric-registry="metric-registry-name" period="1m" filter="^service.*$"
                      database="influxdb-database-name" layer="http"
                      group-counters="true" group-gauges="true"
                      tag-provider="tag_provider" template-provider="template_provider" />



    <!-- Tag Provider bean used by the influxdb reporter
         Base tags to attach to every metrics (measurements)
    -->
    <bean name="tag_provider" class="eu.metrics.spring.TagProviderBean">
        <constructor-arg>
            <map>
                <entry key="column_name" value="column_value" />
                <entry key="other_column_name" value="other_column_value" />
            </map>
        </constructor-arg>
    </bean>



    <!-- Template Provider bean used by the influxdb reporter
         Every metric that match any of the following regexes will be transformed
         Referenced capturing-groups will be stored in their own column, the name of the column is the name of the capturing-group
         /!\ Warning: Templates are applied AFTER counters and gauges are grouped /!\
         If you set group-counters to true, the last element of the metric name will be eaten, same thing with group-gauges
    -->
    <bean name="template_provider" class="eu.metrics.spring.TemplateProviderBean">
        <constructor-arg>
            <map key-type="java.lang.String" value-type="java.util.List">
                <!-- metric `service.global` will be inserted in measurement `service_global_metrics` -->
                <entry key="service_global_metrics">
                    <list>
                        <!-- Pattern -->
                        <value><![CDATA[^services\.global$]]></value>
                    </list>
                </entry>
                <!-- metric `service.<servicename>.timer` will be inserted in measurement `service_timer` -->
                <entry key="service_timer">
                    <list>
                        <!-- Pattern containing a named capturing-group: `servicename` -->
                        <value><![CDATA[^service\.(?<servicename>\w+)\.timer$]]></value>
                        <!-- Reference the capturing-group `servicename` to store the name of the service in a servicename column -->
                        <value>servicename</value>
                    </list>
                </entry>
                <!-- metric `service.<servicename>.metrics.<metric>` will be inserted in measurement `service_metrics` -->
                <entry key="service_metrics">
                    <list>
                        <!-- Pattern containing two named capturing-groups: `servicename` and `metric` -->
                        <value><![CDATA[^service\.(?<servicename>\w+)\.metrics\.(?<metric>\w+)$]]></value>
                        <!-- Reference capturing-groups `servicename` and `metric` -->
                        <value>servicename</value><value>metric</value>
                    </list>
                </entry>
            </map>
        </constructor-arg>
    </bean>
```

## License

Copyright (C) 2018 Jonathan Bayle

Published under Apache Software License 2.0, see LICENSE
