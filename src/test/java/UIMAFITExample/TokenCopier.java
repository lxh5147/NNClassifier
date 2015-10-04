package UIMAFITExample;

import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;
@SofaCapability(inputSofas = { ViewNames.VIEW1, ViewNames.VIEW2 })
public class TokenCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            JCas view1 = jCas.getView(ViewNames.VIEW1);
            JCas view2 = jCas.getView(ViewNames.VIEW2);

            for (Token token1 : select(view1, Token.class)) {
                new Token(view2, token1.getBegin(), token1.getEnd()).addToIndexes();
            }

        } catch (CASException ce) {
            throw new AnalysisEngineProcessException(ce);
        }
    }

}
