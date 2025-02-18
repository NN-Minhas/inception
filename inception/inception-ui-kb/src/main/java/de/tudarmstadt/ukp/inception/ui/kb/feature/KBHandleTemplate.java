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
package de.tudarmstadt.ukp.inception.ui.kb.feature;

import static org.apache.wicket.RuntimeConfigurationType.DEVELOPMENT;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;

import com.googlecode.wicket.jquery.core.template.IJQueryTemplate;

final class KBHandleTemplate
    implements IJQueryTemplate
{
    private static final long serialVersionUID = 8656996525796349138L;

    @Override
    public String getText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("  <div class=\"item-title\">");
        // We cannot use && here because that causes an XML parse error in the browser - so we nest
        // the if clauses...
        sb.append("  # if (data.rank) { if (data.rank != '0') { #");
        sb.append("  <span class=\"item-rank\">");
        sb.append("    [${ data.rank }]");
        sb.append("  </span>");
        sb.append("  # } } #");
        sb.append("    ${ data.uiLabel }");
        sb.append("  </div>");
        sb.append("  <div class=\"item-identifier\">");
        sb.append("    ${ data.identifier }");
        sb.append("  </div>");
        sb.append("  <div class=\"item-description\">");
        sb.append("    ${ data.description }");
        sb.append("  </div>");
        if (DEVELOPMENT.equals(Application.get().getConfigurationType())) {
            sb.append("  <div class=\"item-description\">");
            sb.append("    ${ data.debugInfo }");
            sb.append("  </div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public List<String> getTextProperties()
    {
        List<String> properties = new ArrayList<>();
        properties.add("identifier");
        properties.add("description");
        properties.add("rank");
        if (DEVELOPMENT.equals(Application.get().getConfigurationType())) {
            properties.add("debugInfo");
        }
        return properties;
    }
}
