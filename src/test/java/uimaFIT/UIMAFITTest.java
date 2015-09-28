package uimaFIT;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

//https://uima.apache.org/d/uimafit-current/tools.uimafit.book.html
public class UIMAFITTest {
    public static class GetStartedQuickAE extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {
        public static final String PARAM_STRING = "stringParam";
        @ConfigurationParameter(name = PARAM_STRING)
        private String stringParam;
        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            System.out.println("Hello world!  Say 'hi' to " + stringParam);
        }
    }

    @Test
    public void manualTest() throws UIMAException {
        JCas jCas = JCasFactory.createJCas();
        AnalysisEngine analysisEngine = AnalysisEngineFactory.createEngine(GetStartedQuickAE.class,
                GetStartedQuickAE.PARAM_STRING, "uimaFIT");
        analysisEngine.process(jCas);
    }
}
