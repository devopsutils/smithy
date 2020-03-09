/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.build.transforms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.loader.Prelude;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.TraitDefinition;
import software.amazon.smithy.model.transform.ModelTransformer;
import software.amazon.smithy.utils.Tagged;

/**
 * {@code excludeTraitsByTag} removes traits and trait definitions
 * from a model if the trait definition contains any of the provided
 * {@code tags}.
 *
 * <p>This transformer will not remove prelude trait definitions.
 */
public final class ExcludeTraitsByTag extends BackwardCompatHelper<ExcludeTraitsByTag.Config> {

    /**
     * {@code excludeTraitsByTag} configuration settings.
     */
    public static final class Config {
        private Set<String> tags = Collections.emptySet();

        /**
         * @return the list of tags that, if present, cause the trait to be removed.
         */
        public Set<String> getTags() {
            return tags;
        }

        /**
         * Sets the list of tags that, if present, cause the trait to be removed.
         *
         * @param tags Tags to set.
         */
        public void setTags(Set<String> tags) {
            this.tags = tags;
        }
    }

    @Override
    public Class<Config> getConfigType() {
        return Config.class;
    }

    @Override
    public String getName() {
        return "excludeTraitsByTag";
    }

    @Override
    String getBackwardCompatibleNameMapping() {
        return "tags";
    }

    @Override
    protected Model transformWithConfig(TransformContext context, Config config) {
        Model model = context.getModel();
        ModelTransformer transformer = context.getTransformer();
        Set<String> tags = config.getTags();
        return transformer.removeShapesIf(model, shape -> removeIfPredicate(shape, tags));
    }

    private boolean removeIfPredicate(Shape shape, Collection<String> tags) {
        return !Prelude.isPreludeShape(shape)
               && shape.hasTrait(TraitDefinition.class)
               && hasAnyTag(shape, tags);
    }

    private boolean hasAnyTag(Tagged tagged, Collection<String> tags) {
        return tagged.getTags().stream().anyMatch(tags::contains);
    }
}
