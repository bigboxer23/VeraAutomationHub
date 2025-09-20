package com.bigboxer23.lights.controllers.elastic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ElasticAnalyticsControllerTest {

	@Mock
	private RestHighLevelClient mockClient;

	@Mock
	private org.elasticsearch.client.IndicesClient mockIndicesClient;

	@Mock
	private GetIndexResponse mockGetIndexResponse;

	private ElasticAnalyticsController controller;

	@BeforeEach
	void setUp() {
		controller = new ElasticAnalyticsController();
		ReflectionTestUtils.setField(controller, "myClient", mockClient);
		when(mockClient.indices()).thenReturn(mockIndicesClient);
	}

	@Test
	void testDeleteOldMetricbeatIndices_withOldIndices() throws Exception {
		LocalDate cutoffDate = LocalDate.now().minusMonths(4);
		String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy.MM"));

		String[] mockIndices = {"metricbeat-6.3.3-" + cutoffDateStr + ".10", "metricbeat-7.0.0-" + cutoffDateStr + ".15"
		};
		when(mockGetIndexResponse.getIndices()).thenReturn(mockIndices);
		when(mockIndicesClient.get(
						any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT)))
				.thenReturn(mockGetIndexResponse);

		controller.deleteOldMetricbeatIndices();

		verify(mockIndicesClient)
				.get(any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT));
		verify(mockIndicesClient, times(2)).delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT));
	}

	@Test
	void testDeleteOldMetricbeatIndices_withNoIndices() throws Exception {
		String[] emptyIndices = {};
		when(mockGetIndexResponse.getIndices()).thenReturn(emptyIndices);
		when(mockIndicesClient.get(
						any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT)))
				.thenReturn(mockGetIndexResponse);

		controller.deleteOldMetricbeatIndices();

		verify(mockIndicesClient)
				.get(any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT));
		verify(mockIndicesClient, never()).delete(any(DeleteIndexRequest.class), any());
	}

	@Test
	void testDeleteOldMetricbeatIndices_verifyCorrectCutoffDate() throws Exception {
		LocalDate expectedCutoffDate = LocalDate.now().minusMonths(4);
		String expectedCutoffDateStr = expectedCutoffDate.format(DateTimeFormatter.ofPattern("yyyy.MM"));

		when(mockGetIndexResponse.getIndices()).thenReturn(new String[] {});
		when(mockIndicesClient.get(
						any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT)))
				.thenReturn(mockGetIndexResponse);

		controller.deleteOldMetricbeatIndices();

		verify(mockIndicesClient)
				.get(
						argThat((org.elasticsearch.client.indices.GetIndexRequest request) ->
								request.indices()[0].equals("metricbeat-*" + expectedCutoffDateStr + ".*")),
						eq(RequestOptions.DEFAULT));
	}

	@Test
	void testDeleteOldMetricbeatIndices_handlesExceptions() throws Exception {
		when(mockIndicesClient.get(
						any(org.elasticsearch.client.indices.GetIndexRequest.class), eq(RequestOptions.DEFAULT)))
				.thenThrow(new RuntimeException("Elasticsearch connection failed"));

		assertDoesNotThrow(() -> controller.deleteOldMetricbeatIndices());
	}
}
