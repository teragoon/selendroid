/*
 * Copyright 2013 selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.server;

import io.selendroid.server.handler.CreateSessionHandler;
import io.selendroid.server.handler.DeleteSessionHandler;
import io.selendroid.server.handler.GetCapabilities;
import io.selendroid.server.handler.InspectorUiHandler;
import io.selendroid.server.handler.ListSessionsHandler;
import io.selendroid.server.handler.RequestRedirectHandler;
import io.selendroid.server.model.SelendroidStandaloneDriver;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class SelendroidServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(SelendroidServlet.class.getName());
  protected Map<String, Class<? extends BaseRequestHandler>> redirectHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();
  private SelendroidStandaloneDriver driver;

  public SelendroidServlet(SelendroidStandaloneDriver driver) {
    this.driver = driver;
    init();
  }

  protected void init() {
    postHandler.put("/wd/hub/session", CreateSessionHandler.class);
    getHandler.put("/wd/hub/sessions", ListSessionsHandler.class);
    getHandler.put("/wd/hub/session/:sessionId", GetCapabilities.class);

    getHandler.put("/inspector", InspectorUiHandler.class);
    getHandler.put("/inspector/session/:sessionId", InspectorUiHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSessionHandler.class);
    redirectHandler.put("/wd/hub/session/", RequestRedirectHandler.class);
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response,
      BaseRequestHandler foundHandler) {
    BaseRequestHandler handler = null;
    if ("/favicon.ico".equals(request.uri()) && foundHandler == null) {
      response.status(404);
      response.end();
      return;
    }
    if (foundHandler == null) {
      if (redirectHandler.isEmpty() == false) {
        // trying to find an redirect handler
        for (Map.Entry<String, Class<? extends BaseRequestHandler>> entry : redirectHandler
            .entrySet()) {
          if (request.uri().startsWith(entry.getKey())) {
            String sessionId =
                getParameter("/wd/hub/session/:sessionId", request.uri(), ":sessionId", false);
            handler = instantiateHandler(entry, request);
            if (driver.isValidSession(sessionId)) {
              request.data().put(SESSION_ID_KEY, sessionId);
            }
          }
        }
      }
      if (handler == null) {
        replyWithServerError(response);
        return;
      }
    } else {
      handler = foundHandler;
    }

    String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
    if (sessionId != null) {
      request.data().put(SESSION_ID_KEY, sessionId);
    }
    request.data().put(DRIVER_KEY, driver);

    Response result;
    try {
      result = handler.handle();
    } catch (Exception e) {
      log.severe("Error occured while handlinf request: " + e.fillInStackTrace());
      replyWithServerError(response);
      return;
    }
    if (result instanceof SelendroidResponse) {
      handleResponse(request, response, (SelendroidResponse) result);
    } else {
      UiResponse uiResponse = (UiResponse) result;
      response.header("Content-Type", "text/html");
      response.charset(Charset.forName("UTF-8"));

      response.status(200);

      if (uiResponse != null) {
        if (uiResponse.getObject() instanceof byte[]) {
          byte[] data = (byte[]) uiResponse.getObject();
          response.header("Content-Length", data.length).content(ByteBuffer.wrap(data));

        } else {
          String resultString = uiResponse.render();
          response.content(resultString);

        }
      }
      response.end();
    }
  }

}
