/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.German98Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;

public class SharedDictionary
    extends Resource_ImplBase
{

    public final static String PARAM_DICTIONARY_PATH = "dictionaryPath";
    @ConfigurationParameter(name = PARAM_DICTIONARY_PATH, mandatory = false, defaultValue = "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling/de/igerman98/de_DE_igerman98.dic")
    private String dictionaryPath;

    private Dictionary dict;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            final URL uri = ResourceUtils.resolveLocation(dictionaryPath, this, null);

            final String uriString = uri.toURI().toString();

            if (uriString.endsWith(".dic")) {
                final String affixURIString = uriString.substring(0, uriString.length() - 4)
                        + ".aff";
                final URL affixURI = ResourceUtils.resolveLocation(affixURIString, this, null);
                dict = new German98Dictionary(uri.openStream(), affixURI.openStream());
            }
            else {
                dict = new SimpleDictionary(uri.openStream());
            }
        }
        catch (URISyntaxException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        return true;

    }

    public Dictionary getDictionary()
    {
        return this.dict;
    }

}
