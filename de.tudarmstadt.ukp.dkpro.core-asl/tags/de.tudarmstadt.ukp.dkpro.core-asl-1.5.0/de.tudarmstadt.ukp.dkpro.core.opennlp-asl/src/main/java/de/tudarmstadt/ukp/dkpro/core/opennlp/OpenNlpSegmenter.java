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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableStreamProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Tokenizer and sentence splitter using OpenNLP.
 *
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    outputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class OpenNlpSegmenter
	extends SegmenterBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Override the default variant used to locate the model.
	 */
	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	private CasConfigurableProviderBase<SentenceDetectorME> sentenceModelProvider;
	private CasConfigurableProviderBase<TokenizerME> tokenModelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		sentenceModelProvider = new CasConfigurableStreamProviderBase<SentenceDetectorME>() {
			{
			    setContextObject(OpenNlpSegmenter.this);

				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core.opennlp-model-sentence-${language}-${variant}");

				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/" +
						"sentence-${language}-${variant}.bin");
				setDefault(VARIANT, "maxent");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected SentenceDetectorME produceResource(InputStream aStream)
			    throws Exception
			{
				SentenceModel model = new SentenceModel(aStream);
				return new SentenceDetectorME(model);
			}
		};

		tokenModelProvider = new CasConfigurableStreamProviderBase<TokenizerME>() {
			{
                setContextObject(OpenNlpSegmenter.this);

				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core.opennlp-model-token-${language}-${variant}");

				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/" +
						"token-${language}-${variant}.bin");
				setDefault(VARIANT, "maxent");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected TokenizerME produceResource(InputStream aStream)
			    throws Exception
			{
				TokenizerModel model = new TokenizerModel(aStream);
				return new TokenizerME(model);
			}
		};
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();
		sentenceModelProvider.configure(cas);
		tokenModelProvider.configure(cas);

		super.process(aJCas);
	}

	@Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
	{
		for (Span sSpan : sentenceModelProvider.getResource().sentPosDetect(aText)) {
			createSentence(aJCas, sSpan.getStart() + aZoneBegin, sSpan.getEnd() + aZoneBegin);
			for (Span tSpan : tokenModelProvider.getResource().tokenizePos(
					aText.substring(sSpan.getStart(), sSpan.getEnd()))) {
				createToken(aJCas, tSpan.getStart() + sSpan.getStart() + aZoneBegin, tSpan.getEnd()
						+ sSpan.getStart() + aZoneBegin);
			}
		}
	}
}
