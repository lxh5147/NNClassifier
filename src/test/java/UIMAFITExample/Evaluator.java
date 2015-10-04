package UIMAFITExample;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.text.NumberFormat;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;
@SofaCapability(inputSofas = { ViewNames.GOLD_VIEW, ViewNames.SYSTEM_VIEW })
public class Evaluator extends JCasAnnotator_ImplBase {

    private int totalCorrect = 0;

    private int totalWrong = 0;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            JCas goldView = jCas.getView(ViewNames.GOLD_VIEW);
            JCas systemView = jCas.getView(ViewNames.SYSTEM_VIEW);

            List<Token> goldTokens = Lists.newArrayList(select(goldView, Token.class));
            List<Token> systemTokens = Lists.newArrayList(select(systemView, Token.class));
            if (goldTokens.size() == systemTokens.size()) {
                for (int i = 0; i < goldTokens.size(); i++) {
                    String goldPos = goldTokens.get(i).getPos();
                    String systemPos = systemTokens.get(i).getPos();
                    if (goldPos.equals(systemPos)) {
                        totalCorrect++;
                    } else {
                        totalWrong++;
                    }
                }
            } else {
                throw new RuntimeException("number of tokens in gold view differs from number of tokens in system view");
            }

        } catch (CASException ce) {
            throw new AnalysisEngineProcessException(ce);
        }

    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        int total = totalCorrect + totalWrong;
        System.out.println("total tokens: " + total);
        System.out.println("correct: " + totalCorrect);
        System.out.println("wrong: " + totalWrong);
        float accuracy = (float) totalCorrect / total;
        System.out.println("accuracy: " + NumberFormat.getPercentInstance().format(accuracy));
    }


}
