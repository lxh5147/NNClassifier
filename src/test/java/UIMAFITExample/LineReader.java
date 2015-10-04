package UIMAFITExample;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class LineReader extends JCasCollectionReader_ImplBase {
    public static final String PARAM_INPUT_FILE = "inputFile";
    @ConfigurationParameter
    private File inputFile;

    private String[] lines;
    private int lineIndex;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        try {
            lines = FileUtil.loadListOfStrings(inputFile);
            lineIndex = 0;
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return lineIndex < lines.length;
    }

    @Override
    public Progress[] getProgress() {
        Progress progress = new ProgressImpl(lineIndex, lines.length, Progress.ENTITIES);
        return new Progress[] { progress };
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        jCas.setDocumentText(lines[lineIndex]);
        lineIndex++;
    }
}
