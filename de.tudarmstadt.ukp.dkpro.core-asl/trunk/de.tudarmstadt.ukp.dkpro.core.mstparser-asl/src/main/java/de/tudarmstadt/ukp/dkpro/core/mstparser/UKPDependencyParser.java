/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mstparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mstparser.ConfidenceEstimator;
import mstparser.DependencyInstance;
import mstparser.DependencyParser;
import mstparser.DependencyPipe;
import mstparser.ParserOptions;

public class UKPDependencyParser
extends DependencyParser
{

    public UKPDependencyParser(DependencyPipe pipe, ParserOptions options)
    {
        super(pipe, options);
    }

    public List<DependencyInstance> getParses() throws IOException {

        return getParseTrees(false);
    }

    private List<DependencyInstance> getParseTrees(boolean printParses) throws IOException {

        String tFile = options.testfile;
        List <DependencyInstance> allInstances = new ArrayList<DependencyInstance>();
        ConfidenceEstimator confEstimator = null;
        if (options.confidenceEstimator != null){
            confEstimator =
                    ConfidenceEstimator.resolveByName(options.confidenceEstimator,
                            this);
            System.out.println("Applying confidence estimation: "+
                    options.confidenceEstimator);
        }


        pipe.initInputFile(tFile);

        DependencyInstance instance = pipe.nextInstance();

        int cnt = 0;

        while(instance != null) {
            cnt++;
            String[] forms = instance.forms;
            String[] formsNoRoot = new String[forms.length-1];
            String[] posNoRoot = new String[formsNoRoot.length];
            String[] labels = new String[formsNoRoot.length];
            int[] heads = new int[formsNoRoot.length];

            decode (instance,
                    options.testK,
                    params,
                    formsNoRoot,
                    posNoRoot,
                    labels,
                    heads );
            DependencyInstance parsedInstance = new DependencyInstance(formsNoRoot, posNoRoot, labels, heads);
            if (confEstimator != null ) {
                double[] confidenceScores =
                        confEstimator.estimateConfidence(instance);
                parsedInstance =  new DependencyInstance(formsNoRoot, posNoRoot, labels, heads, confidenceScores);

            }
            if (printParses){
                pipe.outputInstance(parsedInstance);
            }
            allInstances.add(parsedInstance);


            instance = pipe.nextInstance();
        }
        pipe.close();

        return allInstances;

    }
}
