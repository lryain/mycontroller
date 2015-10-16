/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataKeyValuesJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.TypeUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsOnOffTypeDevice;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.jobs.metrics.MetricsAggregationBase;
import org.mycontroller.standalone.mysensors.MyMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/metrics")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class MetricsHandler {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsHandler.class.getName());

    @GET
    @Path("/lastMinute/{variableTypeId}/")
    public Response getLastMinute(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_MINUTE,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last5Minutes/{variableTypeId}")
    public Response getLast5Minutes(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.FIVE_MINUTES,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/lastOneHour/{variableTypeId}")
    public Response getLastOneHour(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_HOUR,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last24Hours/{variableTypeId}")
    public Response getLast24Hours(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_DAY,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last30Days/{variableTypeId}")
    public Response getLast30Days(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.THIRTY_DAYS,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/lastYear/{variableTypeId}")
    public Response getLastYear(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_YEAR,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/allYears/{variableTypeId}")
    public Response getAllYears(@PathParam("variableTypeId") int variableTypeId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ALL_DAYS,
                variableTypeId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/sensorData/{id}")
    public Response getSensorDetails(@PathParam("id") int id) {
        Sensor sensor = DaoUtils.getSensorDao().get(id);
        return RestUtils.getResponse(Status.OK, sensor);
    }

    @GET
    @Path("/batteryUsage/{nodeId}")
    public Response getBatteryUsageDetails(@PathParam("nodeId") int nodeId) {
        return RestUtils.getResponse(Status.OK, this.getBatterUsage(nodeId));
    }

    private ArrayList<MetricsChartDataKeyValuesJson> getAllAfter(AGGREGATION_TYPE aggregationType, int variableTypeId) {
        MetricsAggregationBase metricsAggregationBase = new MetricsAggregationBase();

        ArrayList<MetricsChartDataKeyValuesJson> finalData = new ArrayList<MetricsChartDataKeyValuesJson>();

        SensorValue sensorValue = DaoUtils.getSensorValueDao().get(variableTypeId);

        if (sensorValue.getMetricType() == null) {
            //Sensor pay load type not up to date
            _logger.debug("Payload type not updated in sensor.");
            return null;
        } else if (sensorValue.getMetricType() == TypeUtils.METRIC_TYPE.DOUBLE.ordinal()) {
            _logger.debug("Payload type: {}", MyMessages.PAYLOAD_TYPE.PL_DOUBLE.toString());
            List<MetricsDoubleTypeDevice> metrics = metricsAggregationBase.getMetricsDoubleTypeAllAfter(
                    aggregationType, sensorValue);
            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

            MetricsChartDataKeyValuesJson minChartData = new MetricsChartDataKeyValuesJson("Minimum");
            MetricsChartDataKeyValuesJson avgChartData = new MetricsChartDataKeyValuesJson("Average");
            MetricsChartDataKeyValuesJson maxChartData = new MetricsChartDataKeyValuesJson("Maximum");

            for (MetricsDoubleTypeDevice metric : metrics) {
                minChartData.add(new Object[] { metric.getTimestamp(), metric.getMin() });
                avgChartData.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
                maxChartData.add(new Object[] { metric.getTimestamp(), metric.getMax() });

            }

            finalData.add(minChartData);
            finalData.add(avgChartData);
            finalData.add(maxChartData);
        } else if (sensorValue.getMetricType() == TypeUtils.METRIC_TYPE.BINARY.ordinal()) {
            _logger.debug("Payload type: {}", MyMessages.PAYLOAD_TYPE.PL_BOOLEAN.toString());
            List<MetricsOnOffTypeDevice> metrics = metricsAggregationBase.getMetricsBooleanTypeAllAfter(
                    aggregationType, sensorValue);
            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

            Sensor sensor = DaoUtils.getSensorDao().get(sensorValue.getSensor().getId());

            String name = sensor.getName() != null ? sensor.getName() : "State";
            MetricsChartDataKeyValuesJson minChartData = new MetricsChartDataKeyValuesJson(name);

            for (MetricsOnOffTypeDevice metric : metrics) {
                minChartData.add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
            }

            finalData.add(minChartData);
        }

        return finalData;
    }

    private ArrayList<MetricsChartDataKeyValuesJson> getBatterUsage(int nodeId) {
        ArrayList<MetricsChartDataKeyValuesJson> finalData = new ArrayList<MetricsChartDataKeyValuesJson>();
        List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(nodeId);
        if (metrics == null) {
            _logger.debug("No data");
            return null;
        }
        else {
            MetricsChartDataKeyValuesJson chartData = new MetricsChartDataKeyValuesJson("Battery Level");
            for (MetricsBatteryUsage metric : metrics) {
                chartData.add(new Object[] { metric.getTimestamp(), metric.getValue() });
            }

            finalData.add(chartData);
        }
        return finalData;
    }
}
