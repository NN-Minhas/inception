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
package de.tudarmstadt.ukp.inception.ui.core.dashboard.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.tudarmstadt.ukp.inception.ui.core.dashboard.dashlet.ProjectDashboardDashletExtension;
import de.tudarmstadt.ukp.inception.ui.core.dashboard.dashlet.ProjectDashboardDashletExtensionPoint;
import de.tudarmstadt.ukp.inception.ui.core.dashboard.dashlet.ProjectDashboardDashletExtensionPointImpl;
import de.tudarmstadt.ukp.inception.ui.core.dashboard.settings.ProjectSettingsDashboardMenuItem;
import de.tudarmstadt.ukp.inception.ui.core.dashboard.settings.ProjectSettingsPageMenuItem;

@Configuration
public class DashboardAutoConfiguration
{
    @Bean
    @ConditionalOnProperty(prefix = "dashboard", name = "new-settings", matchIfMissing = true)
    public ProjectSettingsDashboardMenuItem projectSettingsDashboardMenuItem()
    {
        return new ProjectSettingsDashboardMenuItem();
    }

    @Bean
    @ConditionalOnProperty(prefix = "dashboard", name = "new-settings", havingValue = "false", matchIfMissing = false)
    @Deprecated
    public ProjectSettingsPageMenuItem projectSettingsPageMenuItem()
    {
        return new ProjectSettingsPageMenuItem();
    }

    @Bean
    ProjectDashboardDashletExtensionPoint projectDashboardDashletExtensionPoint(
            @Lazy @Autowired(required = false) List<ProjectDashboardDashletExtension> aExtensions)
    {
        return new ProjectDashboardDashletExtensionPointImpl(aExtensions);
    }
}
