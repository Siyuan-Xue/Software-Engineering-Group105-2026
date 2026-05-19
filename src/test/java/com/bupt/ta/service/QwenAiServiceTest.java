package com.bupt.ta.service;

import com.bupt.ta.service.QwenAiService.JobInfo;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QwenAiServiceTest {

    @Test
    void unconfiguredServiceShouldRejectNetworkBackedOperations() {
        QwenAiService service = new QwenAiService(null, null, null);

        assertEquals(false, service.isConfigured());
        assertThrows(IllegalStateException.class, () ->
                service.batchScoreJobs("Resume", "CS", "MASTER", "3.8", "Bio",
                        List.of(new JobInfo("job-1", "Job", "Desc", "CS"))));
    }

    @Test
    void batchScoreJobsShouldParseClampMissingScoresAndDisableThinking() throws Exception {
        List<String> requestBodies = new ArrayList<>();
        QwenAiService service = serviceReturning(200,
                "{\"choices\":[{\"message\":{\"content\":\"{\\\"job-1\\\": 120, \\\"job-2\\\": -5}\"}}]}",
                requestBodies);

        Map<String, Integer> scores = service.batchScoreJobs(
                "Teaching Resume", "CS", "MASTER", "3.8", "Long bio",
                List.of(
                        new JobInfo("job-1", "Algorithms", "Trees", "CS"),
                        new JobInfo("job-2", "Databases", "SQL", "CS"),
                        new JobInfo("job-3", "Math", "Proofs", "MATH")
                ));

        assertEquals(100, scores.get("job-1"));
        assertEquals(0, scores.get("job-2"));
        assertEquals(-1, scores.get("job-3"));
        assertTrue(requestBodies.get(0).contains("\"enable_thinking\":false"));
        assertTrue(requestBodies.get(0).contains("\"model\":\"text-test\""));
    }

    @Test
    void rankResumesShouldReturnMinusOneWhenModelResponseIsNotJson() throws Exception {
        QwenAiService service = serviceReturning(200,
                "{\"choices\":[{\"message\":{\"content\":\"not json\"}}]}",
                new ArrayList<>());

        Map<String, Integer> scores = service.rankResumesForJob(
                "Algorithms", "Trees and graphs", "CS",
                List.of(
                        new QwenAiService.ResumeInfo("resume-1", "One", "CS", "MASTER", "3.7", "Bio"),
                        new QwenAiService.ResumeInfo("resume-2", "Two", "CS", "PHD", "3.9", "Bio")
                ));

        assertEquals(-1, scores.get("resume-1"));
        assertEquals(-1, scores.get("resume-2"));
    }

    @Test
    void nonOkResponseShouldPreferStructuredApiErrorMessage() {
        QwenAiService service = serviceReturning(400,
                "{\"error\":{\"message\":\"quota exceeded\"}}",
                new ArrayList<>());

        IOException error = assertThrows(IOException.class, () ->
                service.batchScoreJobs("Resume", "CS", "MASTER", "3.8", "Bio",
                        List.of(new JobInfo("job-1", "Job", "Desc", "CS"))));

        assertEquals("Qwen API error: quota exceeded", error.getMessage());
    }

    @Test
    void emptyBatchInputsShouldReturnEmptyWithoutTransportCall() throws Exception {
        List<String> bodies = new ArrayList<>();
        QwenAiService service = serviceReturning(200, "{}", bodies);

        assertTrue(service.batchScoreJobs("Resume", "CS", "MASTER", "3.8", "Bio", List.of()).isEmpty());
        assertTrue(bodies.isEmpty());
    }

    private QwenAiService serviceReturning(int status, String body, List<String> requestBodies) {
        return new QwenAiService("test-key", "vl-test", "text-test", URI.create("https://qwen.test/chat"),
                (request, requestBody) -> {
                    requestBodies.add(requestBody);
                    return new SimpleResponse(status, body, request);
                });
    }

    private record SimpleResponse(int statusCode, String body, HttpRequest request) implements HttpResponse<String> {
        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (left, right) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
