package UIMAFITExample;

import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
public class BaselineTagger extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        for (Token token : select(jCas, Token.class)) {
            String word = token.getCoveredText();
            if (word.equals("a") || word.equals("the")) {
                token.setPos("DT");
            } else {
                token.setPos("NN");
            }
        }
    }

}
