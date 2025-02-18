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
package de.tudarmstadt.ukp.clarin.webanno.ui.curation.component.render;

import static de.tudarmstadt.ukp.clarin.webanno.support.WebAnnoConst.CHAIN_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.springframework.stereotype.Component;

import de.tudarmstadt.ukp.clarin.webanno.api.annotation.rendering.ColorRenderer;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.rendering.LabelRenderer;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.rendering.PreRenderer;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationLayer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.inception.rendering.coloring.ColoringService;
import de.tudarmstadt.ukp.inception.rendering.coloring.ColoringStrategy;
import de.tudarmstadt.ukp.inception.rendering.config.AnnotationEditorProperties;
import de.tudarmstadt.ukp.inception.rendering.editorstate.AnnotatorState;
import de.tudarmstadt.ukp.inception.rendering.request.RenderRequest;
import de.tudarmstadt.ukp.inception.rendering.vmodel.VDocument;
import de.tudarmstadt.ukp.inception.schema.AnnotationSchemaService;

@Component
public class CurationRendererImpl
    implements CurationRenderer
{
    private final PreRenderer preRenderer;
    private final AnnotationSchemaService schemaService;
    private final ColoringService coloringService;
    private final AnnotationEditorProperties annotationEditorProperties;

    public CurationRendererImpl(PreRenderer aPreRenderer, AnnotationSchemaService aSchemaService,
            ColoringService aColoringService,
            AnnotationEditorProperties aAnnotationEditorProperties)
    {
        preRenderer = aPreRenderer;
        schemaService = aSchemaService;
        coloringService = aColoringService;
        annotationEditorProperties = aAnnotationEditorProperties;
    }

    @Override
    public VDocument render(CAS aCas, AnnotatorState aState, ColoringStrategy aColoringStrategy)
        throws IOException
    {
        List<AnnotationLayer> layersToRender = new ArrayList<>();
        for (AnnotationLayer layer : aState.getAnnotationLayers()) {
            boolean isNonEditableTokenLayer = layer.getName().equals(Token.class.getName())
                    && !annotationEditorProperties.isTokenLayerEditable();
            boolean isNonEditableSentenceLayer = layer.getName().equals(Sentence.class.getName())
                    && !annotationEditorProperties.isSentenceLayerEditable();
            boolean isUnsupportedLayer = layer.getType().equals(CHAIN_TYPE);

            if (layer.isEnabled() && !isNonEditableTokenLayer && !isNonEditableSentenceLayer
                    && !isUnsupportedLayer) {
                layersToRender.add(layer);
            }
        }

        RenderRequest request = RenderRequest.builder() //
                .withState(aState) //
                .withWindow(aState.getWindowBeginOffset(), aState.getWindowEndOffset()) //
                .withCas(aCas) //
                .withVisibleLayers(layersToRender) //
                .withColoringStrategyOverride(aColoringStrategy) //
                .build();

        VDocument vdoc = new VDocument();
        preRenderer.render(vdoc, request);

        new LabelRenderer().render(vdoc, request);

        ColorRenderer colorRenderer = new ColorRenderer(schemaService, coloringService);
        colorRenderer.render(vdoc, request);

        return vdoc;
    }
}
