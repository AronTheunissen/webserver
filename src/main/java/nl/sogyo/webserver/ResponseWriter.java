package nl.sogyo.webserver;

import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class ResponseWriter implements Response{
    private final HttpStatusCode statusCode;
    private final ZonedDateTime date;
    private final HashMap<String, String> customHeaders = new HashMap<String,String>();
    private final String content;

    ResponseWriter(RequestParse request) {
        if (request.getResourcePath().equals("/sogyo")) {
            this.statusCode = HttpStatusCode.NotFound;
        }
        else {
            statusCode = HttpStatusCode.OK;
        }
        this.date = ZonedDateTime.now();
        this.content = constructContent(request);
        constructCustomHeaders();
    }

    ResponseWriter(RequestParse request, String body) {
        if (request.getResourcePath().equals("/sogyo")) {
            this.statusCode = HttpStatusCode.NotFound;
        } else {
            this.statusCode = HttpStatusCode.OK;
        }
        this.date = ZonedDateTime.now();
        System.out.println(request.getHeaderParameterValue("Content-Type"));
        if (request.getHeaderParameterValue("Content-Type").equals("application/x-www-form-urlencoded")) {
            String[] listParameters = body.split("&", -1);
            HashMap<String, String> parameters = new HashMap<>();
            for (String parameter : listParameters) {
                String[] parameterSplit = parameter.split("=", 2);
                parameters.put(parameterSplit[0], parameterSplit[1]);
            }
            this.content = constructContent(request, parameters);
        } else {
            this.content = constructContent(request);
        }
        constructCustomHeaders();
    }

    private void constructCustomHeaders() {
        this.customHeaders.put("Date", this.date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z",
                Locale.ENGLISH).withZone(ZoneId.of("GMT"))));
        this.customHeaders.put("Server", "Apache/2.4.16 (Unix) OpenSSL/1.0.2d PHP/5.4.45");
        this.customHeaders.put("Connection", "close");
        this.customHeaders.put("Content-Type", "text/html; charset=UTF-8");
        this.customHeaders.put("Content-Length", String.valueOf(this.content.length()));
    }

    @Override
    public HttpStatusCode getStatus(){
        return this.statusCode;
    }

    @Override
    public Map<String, String> getCustomHeaders(){
        return this.customHeaders;
    }

    @Override
    public ZonedDateTime getDate(){
        return this.date;
    }

    @Override
    public String getContent(){
        return this.content;
    }

    private String constructContent(RequestParse request) {
        StringBuilder buildString = new StringBuilder();
        buildString.append("<html>\r\n<body>\r\nYou did an HTTP ").append(request.getHTTPMethod())
                .append(" request.<br/>\r\nRequested resource: ").append(request.getResourcePath())
                .append("<br/>\r\n<br/>\r\nThe following header parameters were passed:<br/>\r\n");
        request.getHeaderParameterNames().forEach((parameter) -> buildString.append(parameter)
                .append(": ").append(request.getHeaderParameterValue(parameter)).append("<br/>\r\n"));
        buildString.append("</body>\r\n</html>\r\n");
        return buildString.toString();
    }

    private String constructContent(RequestParse request, HashMap<String, String> parameters) {
        StringBuilder buildString = new StringBuilder();
        buildString.append("<html>\r\n<body>\r\nYou did an HTTP ").append(request.getHTTPMethod())
                .append(" request.<br/>\r\nRequested resource: ").append(request.getResourcePath())
                .append("<br/>\r\n<br/>\r\nThe following header parameters were passed:<br/>\r\n");
        request.getHeaderParameterNames().forEach((parameter) -> buildString.append(parameter)
                .append(": ").append(request.getHeaderParameterValue(parameter)).append("<br/>\r\n"));
        buildString.append("<br/>\r\nThe following parameters were passed:<br/>\r\n");
        parameters.forEach((key, value) -> buildString.append(key).append(": ").append(value).append("<br/>\r\n"));
        buildString.append("</body>\r\n</html>\r\n");
        return buildString.toString();
    }
}
