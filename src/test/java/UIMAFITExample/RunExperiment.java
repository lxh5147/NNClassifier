package UIMAFITExample;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

public class RunExperiment {
    @Test
    public void manualTest() throws UIMAException, IOException {
        // Choosing different location depending on whether we are in the actual
        // uimaFIT source tree
        // or in the extracted examples from the binary distribution.
        String samplePosFileName = "src/test/resources/sample-gold.txt";

        // The lineReader simply copies the lines from the input file into the
        // default view - one line per CAS
        CollectionReader lineReader = CollectionReaderFactory.createReader(LineReader.class,
                LineReader.PARAM_INPUT_FILE, samplePosFileName);

        AggregateBuilder builder = new AggregateBuilder();

        // The goldTagger parses the data in the default view into Token objects
        // along with their part-of-speech tags which will be added to the
        // GOLD_VIEW
        AnalysisEngineDescription goldTagger = AnalysisEngineFactory.createEngineDescription(GoldTagger.class);
        builder.add(goldTagger);

        // The textCopier creates the SYSTEM_VIEW and set the text of this view
        // to that of the text found in GOLD_VIEW
        AnalysisEngineDescription textCopier = AnalysisEngineFactory.createEngineDescription(
                ViewTextCopierAnnotator.class, ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, ViewNames.GOLD_VIEW,
                ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, ViewNames.SYSTEM_VIEW);
        builder.add(textCopier);

        // The tokenCopier copies Token in the GOLD_VIEW into the SYSTEM_VIEW
        AnalysisEngineDescription tokenCopier = AnalysisEngineFactory
                .createEngineDescription(TokenCopier.class);
        builder.add(tokenCopier, ViewNames.VIEW1, ViewNames.GOLD_VIEW, ViewNames.VIEW2,
                ViewNames.SYSTEM_VIEW);

        // The baselineTagger is run on the SYSTEM_VIEW
        AnalysisEngineDescription baselineTagger = AnalysisEngineFactory.createEngineDescription(BaselineTagger.class);
        builder.add(baselineTagger, CAS.NAME_DEFAULT_SOFA, ViewNames.SYSTEM_VIEW);

        // The evaluator will compare the part-of-speech tags in the SYSTEM_VIEW
        // with those in the GOLD_VIEW
        AnalysisEngineDescription evaluator = AnalysisEngineFactory.createEngineDescription(Evaluator.class);
        builder.add(evaluator);

        // The xWriter writes out the contents of each CAS (one per sentence) to
        // an XMI file. It is instructive to open one of these
        // XMI files in the CAS Visual Debugger and look at the contents of each
        // view.
        AnalysisEngineDescription xWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_OUTPUT_DIRECTORY, "examples/pos/xmi");
        builder.add(xWriter);

        // runs the collection reader and the aggregate AE.
        SimplePipeline.runPipeline(lineReader, builder.createAggregate());
    }
}
