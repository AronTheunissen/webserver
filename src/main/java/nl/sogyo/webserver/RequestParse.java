package nl.sogyo.webserver;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class RequestParse implements Request {
    private final HttpMethod method;
    private final String resourcePath;
    private final HashMap<String, String> headers = new HashMap<String, String>();
    private final HashMap<String, String> parameters = new HashMap<String, String>();

    RequestParse(String request) {
        String requestJustHeaders = request.split("\\r\\n\\r\\n", 2)[0];
        String[] firstLine = requestJustHeaders.split(" ", 3);
        this.method = HttpMethod.valueOf(firstLine[0]);
        this.resourcePath = firstLine[1].split("\\?",2)[0];
        makeHeaders(requestJustHeaders);
        String[] urlLine = (firstLine[1].split("\\?",2));
        makeParameters(urlLine);
    }

    private void makeHeaders(String requestJustHeaders) {
        String[] headerLines = requestJustHeaders.split("\\r\\n", -1);
        for (int i = 1; i < headerLines.length; i++) {
            System.out.println("This test line is " + headerLines[i]);
            String[] headerSplit = headerLines[i].split(": ", 2);
            this.headers.put(headerSplit[0], headerSplit[1]);
        }
    }

    private void makeParameters(String[] urlLine) {
        if (urlLine.length > 1) {
            String[] listParameters = urlLine[1].split("&", -1);
            for (String parameter : listParameters) {
                String[] parameterSplit = parameter.split("=", 2);
                this.parameters.put(parameterSplit[0], parameterSplit[1]);
            }
        }

    }

    @Override
    public HttpMethod getHTTPMethod() {
        return this.method;
    }

    @Override
    public String getResourcePath() {
        return this.resourcePath;
    }

    @Override
    public List<String> getHeaderParameterNames() {
        return new ArrayList<String>(this.headers.keySet());
    }

    @Override
    public String getHeaderParameterValue(String name) {
        return this.headers.get(name);
    }

    @Override
    public List<String> getParameterNames() {
        return new ArrayList<String>(this.parameters.keySet());
    }

    @Override
    public String getParameterValue(String name) {
        return this.parameters.get(name);
    }


}
