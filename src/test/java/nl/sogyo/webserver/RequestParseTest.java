package nl.sogyo.webserver;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RequestParseTest {
    private final String requestTestString = ""
            +"GET /books?hl=nl&q=java HTTP/1.1\r\n"
            +"Host: localhost:9090\r\n"
            +"User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; nl; rv:1.9.0.11) Gecko/20100101 Firefox/40\r\n"
            +"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n"
            +"Accept-Language: nl,en-us;q=0.7,en;q=0.3\r\n"
            +"Accept-Encoding: gzip,deflate\r\n"
            +"Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n"
            +"Keep-Alive: 300\r\n"
            +"Connection: keep-alive\r\n"
            +"Content-Length: 12 ";
    private final RequestParse requestTest = new RequestParse(requestTestString);

    @Test
    public void HttpMethodCanBeParsed() {
        assertEquals(HttpMethod.GET, requestTest.getHTTPMethod());
    }

    @Test
    public void ResourcePathCanBeParsed() {
        assertEquals("/books", requestTest.getResourcePath());
    }

    @Test
    public void HeaderParameterNameCanBeParsed() {
        List<String> headerNames = requestTest.getHeaderParameterNames();
        assertEquals(9, headerNames.size());
        assertTrue(headerNames.contains("Keep-Alive"));
    }

    @Test
    public void HeaderParameterValueCanBeParsed() {
        assertEquals("keep-alive", requestTest.getHeaderParameterValue("Connection"));
    }

}
