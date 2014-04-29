package edu.mit.csail.medg.thesmap;

import java.io.File;

import org.apache.uima.jcas.JCas;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypePriorityList;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.factory.TypePrioritiesFactory;

import static org.uimafit.util.JCasUtil.*;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.cleartk.util.cr.FilesCollectionReader;
import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;


public class CtakesTest {

	public static void main(String[] args) throws Exception{
		runPipeline();

	}
	
	public static void runPipeline() throws Exception {
		//CollectionReader collectionReader = FilesCollectionReader.getCollectionReader(inputDir);

		TypePriorities typePriorities = TypePrioritiesFactory.createTypePriorities(Segment.class, Sentence.class, BaseToken.class);
		TypePriorityList typePriorityList = typePriorities.addPriorityList();
		typePriorityList.addType("org.apache.ctakes.typesystem.type.textspan.Sentence");
		typePriorityList.addType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation");

		AggregateBuilder aggregateBuilder = new AggregateBuilder(null, typePriorities, null);
		
		AnalysisEngineDescription segmentAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.core.ae.SimpleSegmentAnnotator.class);
		aggregateBuilder.add(segmentAnnotator);
		
		AnalysisEngineDescription sentenceAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.core.ae.SentenceDetector.class);
		ConfigurationParameterFactory.addConfigurationParameters(
					sentenceAnnotator,
					org.apache.ctakes.core.ae.SentenceDetector.SD_MODEL_FILE_PARAM, 
					"org/apache/ctakes/core/sentdetect/sd-med-model.zip" 
			);
		aggregateBuilder.add(sentenceAnnotator);
		

		AnalysisEngineDescription tokenizerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.core.ae.TokenizerAnnotatorPTB.class);
		// This is where you could add SegmentsToSkip parameter values. @see TokenizerAnnotatorPTB.PARAM_SEGMENTS_TO_SKIP
		aggregateBuilder.add(tokenizerAnnotator);


		//If chose to add lvg annotator by class name, the following function provides a start, but it has a TODO
		//def lvgAnnotator = lvgAnnotatorByClassName();
		//aggregateBuilder.add(lvgAnnotator);
		// lvg annotator has many parameters, using the xml descriptor to easily use the default parameters
		String lvgDescriptorLocation = "ctakes-lvg/desc/analysis_engine/LvgAnnotator"; // Note createAnalysisEngineDescription expects name to not end in .xml even though filename actually does
		AnalysisEngineDescription lvgDescriptor = AnalysisEngineFactory.createAnalysisEngineDescription(lvgDescriptorLocation); // Note, do not include .xml in the name here as createAnalysisEngineDescription will append .xml
		aggregateBuilder.add(lvgDescriptor);

		
		AnalysisEngineDescription cdtAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator.class);
		aggregateBuilder.add(cdtAnnotator);

		AnalysisEngineDescription posAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.postagger.POSTagger.class);
		ConfigurationParameterFactory.addConfigurationParameters(
					posAnnotator,
					org.apache.ctakes.postagger.POSTagger.POS_MODEL_FILE_PARAM, 
					"org/apache/ctakes/postagger/models/mayo-pos.zip" 
			);
		aggregateBuilder.add(posAnnotator);
		
		AnalysisEngineDescription chunkerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.chunker.ae.Chunker.class);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkerAnnotator,
					org.apache.ctakes.chunker.ae.Chunker.CHUNKER_MODEL_FILE_PARAM, 
					"org/apache/ctakes/chunker/models/chunker-model.zip" 
			);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkerAnnotator,
					org.apache.ctakes.chunker.ae.Chunker.CHUNKER_CREATOR_CLASS_PARAM, 
					"org.apache.ctakes.chunker.ae.PhraseTypeChunkCreator" 
			);
		aggregateBuilder.add(chunkerAnnotator);
			
		String[] nptemp = {"NP", "NP"};

		AnalysisEngineDescription chunkAdjusterNPNPAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.class);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkAdjusterNPNPAnnotator,
					org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.PARAM_CHUNK_PATTERN, 
					nptemp
			);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkAdjusterNPNPAnnotator,
					org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.PARAM_EXTEND_TO_INCLUDE_TOKEN, 
					1
			);
		aggregateBuilder.add(chunkAdjusterNPNPAnnotator);


		String[] npnptemp = {"NP", "PP", "NP"};
		AnalysisEngineDescription chunkAdjusterNPPPNPAnnotator = AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.class);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkAdjusterNPPPNPAnnotator,
					org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.PARAM_CHUNK_PATTERN, 
					npnptemp
			);
		ConfigurationParameterFactory.addConfigurationParameters(
					chunkAdjusterNPPPNPAnnotator,
					org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster.PARAM_EXTEND_TO_INCLUDE_TOKEN, 
					2
			);
		aggregateBuilder.add(chunkAdjusterNPPPNPAnnotator);

		String lwAnnotatorDescriptorLocation = "ctakes-clinical-pipeline/desc/analysis_engine/LookupWindowAnnotator"; 
		AnalysisEngineDescription lookupWindowAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(lwAnnotatorDescriptorLocation); 
		aggregateBuilder.add(lookupWindowAnnotator);


		// TODO - this is a longer range TODO item: it would be nice to be able to set values here that would be used instead of what's in the LookupDesc*.xml files
		// DictionaryLookupAnnotatorUMLS - org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator
		String dictLookupAnnotatorDescriptorLocation = "ctakes-dictionary-lookup/desc/analysis_engine/DictionaryLookupAnnotatorUMLS"; 
		AnalysisEngineDescription dictionaryLookupAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(dictLookupAnnotatorDescriptorLocation); 
		//UmlsDictionaryLookupAnnotator will look for system properties before looking at these values
		ConfigurationParameterFactory.addConfigurationParameters(
					dictionaryLookupAnnotator, "ctakes.umlsaddr", "https://uts-ws.nlm.nih.gov/restful/isValidUMLSUser");
		ConfigurationParameterFactory.addConfigurationParameters(
					dictionaryLookupAnnotator, "ctakes.umlsvendor", "NLM-6515182895");
		ConfigurationParameterFactory.addConfigurationParameters(
					dictionaryLookupAnnotator, "ctakes.umlsuser", "pszolovits");
		ConfigurationParameterFactory.addConfigurationParameters(
//				org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator.UMLSPW_PARAM, 
					dictionaryLookupAnnotator, "ctakes.umlspw", "D11Xsubj");
		aggregateBuilder.add(dictionaryLookupAnnotator);		
		
		String assertionDescriptorLocation = "ctakes-assertion/desc/AssertionMiniPipelineAnalysisEngine"; // Note createAnalysisEngineDescription expects name to not end in .xml even though filename actually does
		AnalysisEngineDescription assertionDescriptor = AnalysisEngineFactory.createAnalysisEngineDescription(assertionDescriptorLocation); // Note, do not include .xml in the name here as createAnalysisEngineDescription will append .xml
		aggregateBuilder.add(assertionDescriptor);

		String extractionPrepDescriptorLocation = "ctakes-clinical-pipeline/desc/analysis_engine/ExtractionPrepAnnotator"; 
		AnalysisEngineDescription extractionPrepDescriptor = AnalysisEngineFactory.createAnalysisEngineDescription(extractionPrepDescriptorLocation); 
		aggregateBuilder.add(extractionPrepDescriptor);				
		
		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription("org.apache.ctakes.typesystem.types.TypeSystem");
		CollectionReader collectionReader = FilesCollectionReader.getCollectionReader("data/input");
		// generic XMI writer
		// TODO When generalize this script to run specific components instead of the whole pipeline,
		// consider separate writer for each engine or a diffferent writer that produces more friendly output
		AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
					  XWriter.class,
					  typeSystemDescription,
					  XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
					  "data/output",
					  XWriter.PARAM_FILE_NAMER_CLASS_NAME,
					  CtakesFileNamer.class.getName()
					  );
		aggregateBuilder.add(xWriter);

		//println("About to run pipeline using SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate())");
		SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
	}

}
