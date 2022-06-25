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
package de.tudarmstadt.ukp.clarin.webanno.webapp.remoteapi.aero;

import static de.tudarmstadt.ukp.clarin.webanno.security.model.Role.ROLE_ADMIN;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.WebApplicationContext;

import de.tudarmstadt.ukp.clarin.webanno.api.config.RepositoryAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.api.dao.annotationservice.config.AnnotationSchemaServiceAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.api.dao.casstorage.config.CasStorageServiceAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.api.dao.documentservice.config.DocumentServiceAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.project.config.ProjectServiceAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.security.UserDao;
import de.tudarmstadt.ukp.clarin.webanno.security.config.SecurityAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.security.model.User;
import de.tudarmstadt.ukp.clarin.webanno.text.config.TextFormatsAutoConfiguration;
import de.tudarmstadt.ukp.clarin.webanno.webapp.remoteapi.config.RemoteApiAutoConfiguration;
import de.tudarmstadt.ukp.inception.curation.config.CurationDocumentServiceAutoConfiguration;
import de.tudarmstadt.ukp.inception.export.config.DocumentImportExportServiceAutoConfiguration;
import de.tudarmstadt.ukp.inception.project.export.config.ProjectExportServiceAutoConfiguration;

@EnableAutoConfiguration(exclude = LiquibaseAutoConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, //
        properties = { //
                "spring.main.banner-mode=off", //
                "remote-api.enabled=true", //
                "repository.path=" + AeroRemoteApiController_Project_Test.TEST_OUTPUT_FOLDER })
@EnableWebSecurity
@Import({ //
        ProjectServiceAutoConfiguration.class, //
        ProjectExportServiceAutoConfiguration.class, //
        AnnotationSchemaServiceAutoConfiguration.class, //
        CasStorageServiceAutoConfiguration.class, //
        CurationDocumentServiceAutoConfiguration.class, //
        TextFormatsAutoConfiguration.class, //
        DocumentImportExportServiceAutoConfiguration.class, //
        DocumentServiceAutoConfiguration.class, //
        RepositoryAutoConfiguration.class, //
        SecurityAutoConfiguration.class, //
        RemoteApiAutoConfiguration.class })
@EntityScan({ //
        "de.tudarmstadt.ukp.clarin.webanno.model", //
        "de.tudarmstadt.ukp.clarin.webanno.security.model" })
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class AeroRemoteApiController_Project_Test
{
    static final String TEST_OUTPUT_FOLDER = "target/test-output/AeroRemoteApiController_Project_Test";

    private @Autowired WebApplicationContext context;
    private @Autowired UserDao userRepository;

    private MockAeroClient adminActor;

    @BeforeAll
    public static void setupClass()
    {
        FileSystemUtils.deleteRecursively(new File(TEST_OUTPUT_FOLDER));
    }

    @BeforeEach
    public void setup()
    {
        adminActor = new MockAeroClient(context, "admin", "ADMIN");

        userRepository.create(new User("admin", ROLE_ADMIN));

    }

    @Test
    public void testCreateAndDelete() throws Exception
    {
        adminActor.performListProjects() //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(APPLICATION_JSON_VALUE)) //
                .andExpect(jsonPath("$.messages").isEmpty());

        adminActor.performCreateProject("project1") //
                .andExpect(status().isCreated()) //
                .andExpect(content().contentType(APPLICATION_JSON_VALUE)) //
                .andExpect(jsonPath("$.body.id").value("1")) //
                .andExpect(jsonPath("$.body.name").value("project1"));

        adminActor.performListProjects() //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(APPLICATION_JSON_VALUE)) //
                .andExpect(jsonPath("$.body[0].id").value("1")) //
                .andExpect(jsonPath("$.body[0].name").value("project1"));

        adminActor.performDeleteProject(1l) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(APPLICATION_JSON_VALUE)) //
                .andExpect(jsonPath("$.messages[0].level").value("INFO")) //
                .andExpect(jsonPath("$.messages[0].message").value(containsString("deleted")));

        adminActor.performListProjects() //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(APPLICATION_JSON_VALUE)) //
                .andExpect(jsonPath("$.messages").isEmpty());

    }

    @SpringBootConfiguration
    public static class TestContext
    {
        // All handled by auto-config
    }
}
