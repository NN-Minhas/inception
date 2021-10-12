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
package de.tudarmstadt.ukp.inception.curation.export;

import static java.lang.invoke.MethodHandles.lookup;

import java.io.File;
import java.util.Optional;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExportRequest;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExportTaskMonitor;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExporter;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectImportRequest;
import de.tudarmstadt.ukp.clarin.webanno.export.model.ExportedProject;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.inception.curation.config.CurationServiceAutoConfiguration;
import de.tudarmstadt.ukp.inception.curation.export.model.ExportedCurationWorkflow;
import de.tudarmstadt.ukp.inception.curation.model.CurationWorkflow;
import de.tudarmstadt.ukp.inception.curation.service.CurationService;

/**
 * <p>
 * This class is exposed as a Spring Component via
 * {@link CurationServiceAutoConfiguration#curationWorkflowExporter}.
 * </p>
 */
public class CurationWorkflowExporter
    implements ProjectExporter
{
    private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

    private static final String KEY = "curation_workflow";

    private final CurationService curationService;

    @Autowired
    public CurationWorkflowExporter(CurationService aCurationService)
    {
        curationService = aCurationService;
    }

    @Override
    public void exportData(ProjectExportRequest aRequest, ProjectExportTaskMonitor aMonitor,
            ExportedProject aExProject, File aStage)
        throws Exception
    {
        Project project = aRequest.getProject();

        CurationWorkflow curationWorkflow = curationService.readOrCreateCurationWorkflow(project);
        ExportedCurationWorkflow exportedCurationWorkflow = new ExportedCurationWorkflow();
        exportedCurationWorkflow.setMergeStrategy(curationWorkflow.getMergeStrategy());
        exportedCurationWorkflow.setMergeStrategyTraits(curationWorkflow.getMergeStrategyTraits());

        aExProject.setProperty(KEY, curationWorkflow);
        LOG.info("Exported curation workflow settings for project {}", project);
    }

    @Override
    public void importData(ProjectImportRequest aRequest, Project aProject,
            ExportedProject aExProject, ZipFile aZip)
        throws Exception
    {
        Optional<ExportedCurationWorkflow> maybeExportedCurationWorkflow = aExProject
                .getProperty(KEY, ExportedCurationWorkflow.class);

        if (maybeExportedCurationWorkflow.isEmpty()) {
            return;
        }

        ExportedCurationWorkflow exportedCurationWorkflow = maybeExportedCurationWorkflow.get();

        CurationWorkflow curationWorkflow = new CurationWorkflow();
        curationWorkflow.setMergeStrategy(exportedCurationWorkflow.getMergeStrategy());
        curationWorkflow.setMergeStrategyTraits(exportedCurationWorkflow.getMergeStrategyTraits());
        curationService.createOrUpdateCurationWorkflow(curationWorkflow);

        LOG.info("Imported curation workflow settings for project {}", aProject);
    }
}
