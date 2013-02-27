/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server.inspector.view;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.inspector.SelendroidInspectorView;
import org.openqa.selendroid.server.inspector.TreeUtil;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;

public class TreeView extends SelendroidInspectorView {
  public TreeView(ServerInstrumentation serverInstrumentation, SelendroidDriver driver) {
    super(serverInstrumentation, driver);
  }

  public void render(HttpRequest request, HttpResponse response) {
    JsonObject source = null;
    try {
      source = (JsonObject) driver.getWindowSource();
    } catch (SelendroidException e) {
      source = new JsonObject();
    }
    String convertedTree = TreeUtil.createFromNativeWindowsSource(source).toString();
    response.header("Content-type", "application/x-javascript").charset(Charsets.UTF_8)
        .content(convertedTree).end();
  }
}