package UIMAFITExample;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;

@SofaCapability(inputSofas = CAS.NAME_DEFAULT_SOFA, outputSofas = ViewNames.GOLD_VIEW)
public class GoldTagger extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            JCas defaultView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
            // see JavaDoc comment for SofaCapability for why we have to
            // retrieve the default view from
            // the JCas
            String tagData = defaultView.getDocumentText();

            JCas goldView = ViewCreatorAnnotator.createViewSafely(jCas, ViewNames.GOLD_VIEW);

            String[] wordTagPairs = tagData.split("\\s+");
            StringBuffer text = new StringBuffer();
            int offset = 0;
            List<Token> tokens = new ArrayList<Token>();
            for (String wordTagPair : wordTagPairs) {
                String word = wordTagPair.split("/")[0];
                String tag = wordTagPair.split("/")[1];
                text.append(word);
                Token token = new Token(goldView, offset, text.length());
                token.setPos(tag);
                tokens.add(token);
                text.append(" ");
                offset += word.length() + 1;
            }

            goldView.setDocumentText(text.toString().trim());

            for (Token token : tokens) {
                token.addToIndexes();
            }
        } catch (CASException ce) {
            throw new AnalysisEngineProcessException(ce);
        }

    }

}
