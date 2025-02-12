/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.examples.recipe.api.BakeStep;
import com.palantir.conjure.examples.recipe.api.Ingredient;
import com.palantir.conjure.examples.recipe.api.Recipe;
import com.palantir.conjure.examples.recipe.api.RecipeBookService;
import com.palantir.conjure.examples.recipe.api.RecipeErrors;
import com.palantir.conjure.examples.recipe.api.RecipeName;
import com.palantir.conjure.examples.recipe.api.RecipeStep;
import com.palantir.conjure.examples.recipe.api.Temperature;
import com.palantir.conjure.examples.recipe.api.TemperatureUnit;
import com.palantir.conjure.java.api.config.service.ServiceConfiguration;
import com.palantir.conjure.java.api.config.service.UserAgent;
import com.palantir.conjure.java.api.config.ssl.SslConfiguration;
import com.palantir.conjure.java.api.testing.Assertions;
import com.palantir.conjure.java.client.config.ClientConfigurations;
import com.palantir.conjure.java.client.jaxrs.JaxRsClient;
import com.palantir.conjure.java.okhttp.NoOpHostEventsSink;
import com.palantir.tokens.auth.BearerToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;

public class RecipeBookApplicationTest {

    private static RecipeBookService client;

    @BeforeAll
    public static void before() {
        client = JaxRsClient.create(
                RecipeBookService.class,
                UserAgent.of(UserAgent.Agent.of("test", "0.0.0")),
                NoOpHostEventsSink.INSTANCE,
                ClientConfigurations.of(ServiceConfiguration.builder()
                        .addUris("http://localhost:8345/api/")
                        .security(SslConfiguration.of(Paths.get("src/test/resources/certs/emptyStore.jks")))
                        .build()));
    }

    @Test
    public void getRecipeUsingInvalidName() {
        Assertions.assertThatRemoteExceptionThrownBy(() -> client.getRecipe(BearerToken.valueOf("dummy"), RecipeName.of("doesNotExist")))
                .isGeneratedFromErrorType(RecipeErrors.RECIPE_NOT_FOUND);
    }

    @Test
    public void getRecipeWithBake() {
        RecipeName recipeName = RecipeName.of("baked potatoes");
        Recipe recipe = client.getRecipe(BearerToken.valueOf("dummy"), recipeName);
        Recipe expectedRecipe = Recipe.of(
                recipeName,
                ImmutableList.of(
                        RecipeStep.mix(ImmutableSet.of(
                                Ingredient.of("rub oil all over the potatoes"),
                                Ingredient.of("Rub salt all over the potatoes"))),
                        RecipeStep.bake(BakeStep.builder()
                                .temperature(Temperature.builder()
                                        .degree(220)
                                        .unit(TemperatureUnit.CELSIUS)
                                        .build())
                                .durationInSeconds(2700)
                                .build())));
        assertThat(recipe).isEqualTo(expectedRecipe);
    }
}
