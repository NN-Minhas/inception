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
package de.tudarmstadt.ukp.inception.ui.kb.project;

import static de.tudarmstadt.ukp.clarin.webanno.support.lambda.LambdaBehavior.enabledWhen;
import static de.tudarmstadt.ukp.clarin.webanno.support.lambda.LambdaBehavior.visibleWhen;
import static de.tudarmstadt.ukp.inception.kb.RepositoryType.REMOTE;
import static de.tudarmstadt.ukp.inception.kb.SchemaProfile.CUSTOMSCHEMA;
import static de.tudarmstadt.ukp.inception.kb.SchemaProfile.WIKIDATASCHEMA;
import static de.tudarmstadt.ukp.inception.ui.kb.project.validators.Validators.IRI_VALIDATOR;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.eclipse.rdf4j.model.IRI;

import com.googlecode.wicket.kendo.ui.form.combobox.ComboBox;

import de.tudarmstadt.ukp.clarin.webanno.support.lambda.LambdaAjaxFormComponentUpdatingBehavior;
import de.tudarmstadt.ukp.inception.kb.IriConstants;
import de.tudarmstadt.ukp.inception.kb.KnowledgeBaseService;
import de.tudarmstadt.ukp.inception.kb.SchemaProfile;
import de.tudarmstadt.ukp.inception.kb.reification.Reification;

public class KnowledgeBaseIriPanel
    extends Panel
{
    private static final long serialVersionUID = -7189344732710228206L;

    private @SpringBean KnowledgeBaseService kbService;

    private final IModel<SchemaProfile> selectedSchemaProfile;
    private final CompoundPropertyModel<KnowledgeBaseWrapper> kbModel;

    public KnowledgeBaseIriPanel(String id, CompoundPropertyModel<KnowledgeBaseWrapper> aModel)
    {
        super(id);
        setOutputMarkupId(true);
        selectedSchemaProfile = Model.of(SchemaProfile.RDFSCHEMA);

        kbModel = aModel;

        DropDownChoice<Reification> reificationChoice = selectReificationStrategy("reification",
                "kb.reification");
        add(reificationChoice);

        // The Kendo comboboxes do not redraw properly when added directly to an
        // AjaxRequestTarget (for each combobox, a text field and a dropdown will be shown).
        // Instead, wrap all of them in a WMC and redraw that.
        WebMarkupContainer comboBoxWrapper = new WebMarkupContainer("comboBoxWrapper");
        comboBoxWrapper.setOutputMarkupId(true);
        add(comboBoxWrapper);

        // Add comboboxes for classIri, subclassIri, typeIri and descriptionIri
        ComboBox<String> classField = buildComboBox("classIri", kbModel.bind("kb.classIri"),
                IriConstants.CLASS_IRIS);
        ComboBox<String> subclassField = buildComboBox("subclassIri",
                kbModel.bind("kb.subclassIri"), IriConstants.SUBCLASS_IRIS);
        ComboBox<String> typeField = buildComboBox("typeIri", kbModel.bind("kb.typeIri"),
                IriConstants.TYPE_IRIS);
        ComboBox<String> subPropertyField = buildComboBox("subPropertyIri",
                kbModel.bind("kb.subPropertyIri"), IriConstants.SUBPROPERTY_IRIS);
        ComboBox<String> descriptionField = buildComboBox("descriptionIri",
                kbModel.bind("kb.descriptionIri"), IriConstants.DESCRIPTION_IRIS);
        ComboBox<String> labelField = buildComboBox("labelIri", kbModel.bind("kb.labelIri"),
                IriConstants.LABEL_IRIS);
        ComboBox<String> propertyTypeField = buildComboBox("propertyTypeIri",
                kbModel.bind("kb.propertyTypeIri"), IriConstants.PROPERTY_TYPE_IRIS);
        ComboBox<String> propertyLabelField = buildComboBox("propertyLabelIri",
                kbModel.bind("kb.propertyLabelIri"), IriConstants.PROPERTY_LABEL_IRIS);
        ComboBox<String> propertyDescriptionField = buildComboBox("propertyDescriptionIri",
                kbModel.bind("kb.propertyDescriptionIri"), IriConstants.PROPERTY_DESCRIPTION_IRIS);
        comboBoxWrapper.add(classField, subclassField, typeField, subPropertyField,
                descriptionField, labelField, propertyTypeField, propertyLabelField,
                propertyDescriptionField);

        // RadioGroup to select the IriSchemaType
        DropDownChoice<SchemaProfile> iriSchemaChoice = new DropDownChoice<SchemaProfile>(
                "iriSchema", selectedSchemaProfile, Arrays.asList(SchemaProfile.values()),
                new EnumChoiceRenderer<>(this))
        {
            private static final long serialVersionUID = 3863260896285332033L;

            @Override
            protected void onInitialize()
            {
                super.onInitialize();
                // Initialize according to current model values
                SchemaProfile modelProfile = SchemaProfile
                        .checkSchemaProfile(kbModel.getObject().getKb());

                setModelObject(modelProfile);
            }
        };
        iriSchemaChoice.setOutputMarkupId(true);
        // OnChange update the model with corresponding iris
        iriSchemaChoice.add(new LambdaAjaxFormComponentUpdatingBehavior("change", _target -> {
            SchemaProfile profile = iriSchemaChoice.getModelObject();
            // If the user switches to the custom profile, we retain the values from the
            // previously selected profile and just make the IRI mapping editable. If the user
            // switches to a pre-defined profile, we reset the values.
            if (SchemaProfile.CUSTOMSCHEMA != profile) {
                classField.setModelObject(profile.getClassIri());
                subclassField.setModelObject(profile.getSubclassIri());
                typeField.setModelObject(profile.getTypeIri());
                descriptionField.setModelObject(profile.getDescriptionIri());
                labelField.setModelObject(profile.getLabelIri());
                propertyTypeField.setModelObject(profile.getPropertyTypeIri());
                propertyLabelField.setModelObject(profile.getPropertyLabelIri());
                propertyDescriptionField.setModelObject(profile.getPropertyDescriptionIri());
            }
            _target.add(comboBoxWrapper, iriSchemaChoice, reificationChoice);
        }));
        comboBoxWrapper.add(iriSchemaChoice);
    }

    private ComboBox<String> buildComboBox(String id, IModel<String> model, List<IRI> iris)
    {
        // Only set model object if it has not been initialized yet
        if (model.getObject() == null) {
            model.setObject(iris.get(0).stringValue());
        }

        List<String> choices = iris.stream().map(IRI::stringValue).collect(toList());

        ComboBox<String> comboBox = new ComboBox<>(id, model, choices);
        comboBox.add(enabledWhen(() -> CUSTOMSCHEMA.equals(selectedSchemaProfile.getObject())));
        comboBox.setOutputMarkupId(true);
        comboBox.setRequired(true);
        comboBox.add(IRI_VALIDATOR);
        // Do nothing just update the model values
        comboBox.add(new LambdaAjaxFormComponentUpdatingBehavior("change"));
        return comboBox;
    }

    private DropDownChoice<Reification> selectReificationStrategy(String id, String property)
    {
        DropDownChoice<Reification> reificationDropDownChoice = new DropDownChoice<>(id,
                kbModel.bind(property), asList(Reification.values()));
        reificationDropDownChoice.setRequired(true);
        reificationDropDownChoice.setOutputMarkupPlaceholderTag(true);

        // The current reification implementation only really does something useful when the
        // Wikidata schema is used on the actual Wikidata knowledge base (or a mirror).
        // Thus, we enable the option to activate reification only in this case.
        reificationDropDownChoice
                .add(visibleWhen(() -> WIKIDATASCHEMA.equals(selectedSchemaProfile.getObject())
                        && kbModel.getObject().getKb().isReadOnly()
                        && REMOTE.equals(kbModel.getObject().getKb().getType())));

        return reificationDropDownChoice;
    }
}
