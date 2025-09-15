package org.eci.arep.annotations;

import org.eci.arep.HttpRequest;
import org.eci.arep.HttpResponse;

public interface HttpService {
    String handle(HttpRequest request, HttpResponse response);
}
