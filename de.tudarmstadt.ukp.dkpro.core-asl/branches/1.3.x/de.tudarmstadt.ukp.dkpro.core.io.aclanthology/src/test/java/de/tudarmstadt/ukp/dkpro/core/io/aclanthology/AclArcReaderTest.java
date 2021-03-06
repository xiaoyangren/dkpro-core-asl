/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.aclanthology;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class AclArcReaderTest
{
    @Test
    public void aclArcReaderTest()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                AclAnthologyReader.class,
                ResourceCollectionReaderBase.PARAM_PATH,
                        new File("src/test/resources/acl/").getAbsolutePath(),
                ResourceCollectionReaderBase.PARAM_PATTERNS,
                        new String[] {ResourceCollectionReaderBase.INCLUDE_PREFIX + "**/*.txt" }
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            System.out.println(md.getDocumentUri());

            if (i < 2) {
                System.out.println(jcas.getDocumentText());
                System.out.println();
                System.out.println();
            }

            i++;
        }
        assertEquals(10, i);
    }
}