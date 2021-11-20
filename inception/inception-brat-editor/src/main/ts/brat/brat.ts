/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Ajax } from "./ajax/Ajax";
import { AnnotatorUI } from "./annotator_ui/AnnotatorUI";
import Dispatcher from "./dispatcher";
import { Visualizer } from "./visualizer/Visualizer";
import { VisualizerUI } from "./visualizer_ui/VisualizerUI";

declare let Wicket;

function brat(markupId: string, callbackUrl: string) {
  const dispatcher = new Dispatcher();
  // Each visualizer talks to its own Wicket component instance
  dispatcher.ajaxUrl = callbackUrl;
  // We attach the JSON send back from the server to this HTML element
  // because we cannot directly pass it from Wicket to the caller in ajax.js.
  dispatcher.wicketId = markupId;
  const ajax = new Ajax(dispatcher);
  const visualizer = new Visualizer(dispatcher, markupId);
  const visualizerUI = new VisualizerUI(dispatcher, visualizer.svg);
  const annotatorUI = new AnnotatorUI(dispatcher, visualizer.svg);
  // js.append(("var logger = new AnnotationLog(dispatcher);");
  dispatcher.post('init');
  Wicket.$(markupId).dispatcher = dispatcher;
  Wicket.$(markupId).visualizer = visualizer;
}

export = brat;