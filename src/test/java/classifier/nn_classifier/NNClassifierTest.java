package classifier.nn_classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NNClassifierTest {
    @Test
    public void runPredictionTest() throws Exception{
        String binaryModel="/home/lxh5147/git/NNRuntime/tools/toy_nbow_model.bin";
        String theanoModelRoot="toy_nbow_model";
        String[] featureNames={"prefix_1", "prefix_2", "prefix_3", "suffix_1", "suffix_2", "suffix_3","words","field_ids"};
        String outputFeature="domains";
        NNClassifier classifier=loadNNClassifier(binaryModel,theanoModelRoot,featureNames,outputFeature);
        String query="<count:1>       <dialog:dm_main>        <domain:default>        <qi:default:search>     { my hovercraft is full of eels }";
        SymbolToIdLookup wordToIdLookup=classifier.getSymbolToIdLookup(featureNames.length-2);
        Annotation annotation=parse(query,wordToIdLookup);
        List<List<String>> input=getInput(annotation, featureNames);
        List<Prediction> actual=Lists.newArrayList(classifier.predict(input));
        String[] domains={"<UNK>", "</s>", "knowledge", "movietv", "video", "sports", "default", "business", "shopping", "maps", "adult", "photos"};
        double[] expectedProbs={3.805000324557826150e-15,5.585751888525787145e-16,4.636411666870117188e-01,2.968471962958574295e-03,6.844294839538633823e-04,3.390415105968713760e-03,5.174831748008728027e-01,1.115844235755503178e-03,4.151279106736183167e-03,5.496459780260920525e-04,2.982420381158590317e-03,3.033172106370329857e-03};
        List<Prediction> expected = Lists.newArrayList();
        for(int i=0;i<domains.length;++i){
            expected.add(new Prediction().withConfidence(expectedProbs[i]).withLabel(domains[i]));
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void runEvaluation() throws Exception{
        String binaryModel="zoe_random_nbow-81.model.bin";
        String theanoModelRoot="zoe_random_nbow-81.model";
        String[] featureNames={"words", "prefix_1", "prefix_3", "prefix_2", "suffix_2", "suffix_3", "suffix_1","field_ids"};
        String outputFeature="domains";
        String[] testFiles={"dma_fy14_q3.trs","DragonGO.20140714.Test.wfas.final.trs"};
        NNClassifier classifier=loadNNClassifier(binaryModel,theanoModelRoot,featureNames,outputFeature);
        SymbolToIdLookup wordToIdLookup=classifier.getSymbolToIdLookup(0);
        for(String testFile:testFiles){
            List<String> report=evaluate(testFile,classifier,featureNames, wordToIdLookup);
            showReport(testFile,report);
        }
    }

    void showReport(String testFile,List<String> report){
        System.out.println("Evaluation report for:" + testFile);
        System.out.println("Domain\tPrecision(%)\tRecall(%)\tF1(%)");
        for(String detail:report){
            System.out.println(detail);
        }
    }

    class Annotation{
        public String domain;
        public String fieldId;
        public List<String> tokens;
        public double count;
    }

//{"query_input_features": ["field_ids"], "word_input_features": ["words", "prefix_1", "prefix_3", "prefix_2", "suffix_2", "suffix_3", "suffix_1"]

    NNClassifier loadNNClassifier(String binaryModel,String theanoModelRoot, String[] inputFeatureNames, String outputFeatureName) throws IOException{
        return NNClassifier.load(new File(binaryModel), getFiles(theanoModelRoot,inputFeatureNames), getFile(theanoModelRoot,outputFeatureName));
    }

    File getFile(String theanoModelRoot,String featureName){
        return new File(theanoModelRoot, featureName+".txt");
    }

    List<File> getFiles(String theanoModelRoot,String[] featureNames){
        List<File> files=Lists.newArrayList();
        for(String featureName:featureNames){
            files.add( getFile(theanoModelRoot,featureName));
        }
        return files;
    }

    List<String> evaluate(String file, NNClassifier classifier, String[] featureNames,SymbolToIdLookup wordToIdLookup) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line=null;
        Map<String,EvaluationResult> results=Maps.newHashMap();
        while((line=reader.readLine())!=null){
            Annotation annotation=parse(line,wordToIdLookup);
            String prediction=predict(annotation,classifier,featureNames);
            accumulate(annotation,prediction,results);
        }
        reader.close();
        return report(results);
    }

    private final static Pattern linePattern = Pattern.compile("^(?<pre>.*)\\{(?<content>.*)\\}(?<post>.*)$");
    private final static Pattern prefixPattern = Pattern.compile("<(?<key>[^:>]+):?(?<value>[^>]+)?>");

    Annotation parse(String line,SymbolToIdLookup wordToIdLookup){
        //<count:1> <dialog:dm_main> <domain:default> <qi:default:search> <fileid:dma_fy14_q3_semantic_search_wfas_54> { [searchphrase] valor to me [/searchphrase] }
        //<count:1> <dialog:dm_alarmlookupmain>     <domain:adult> <qi:adult:na>    <fileid:/nlu/projects/dma/jenkins-workspace/dma-workflow/mobeus/resources/work/en-US/dma_processed_data.new/semsearch_dma/dgodata-trans.testing.final.weightedfieldannotatedstring:27>  { [searchphrase] black female with hairy pussies [/searchphrase] }
        Matcher m = linePattern.matcher(line);
        checkState(m.matches(),"match");
        String prefix  = m.group("pre").trim();
        String content = m.group("content").trim();
        Matcher preMatcher = prefixPattern.matcher(prefix);
        Annotation annotation=new Annotation();
        while(preMatcher.find()) {
            String key = preMatcher.group("key");
            String value = preMatcher.group("value");
            if(key.equals("domain")) {
                annotation.domain=value;
            }
            else if(key.equals("count")) {
                annotation.count=Double.parseDouble(value);
            }
            else if(key.equals("dialog")) {
                annotation.fieldId=value;
            }
        }
        annotation.tokens=parseAnnotatedString(content, wordToIdLookup);
        return annotation;
    }

    boolean startsAndEndsWith(String s, char start, char end) {
        return s.charAt(0) == start && s.charAt(s.length() -1) == end;
    }

    private static final String emptyString="";

    List<String>  parseAnnotatedString(String annotatedString,SymbolToIdLookup wordToIdLookup){
        Iterable<String> parts = Splitter.on(' ').trimResults().split(annotatedString);
        List<String> tokens=Lists.newArrayList();
        for(String part: parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!startsAndEndsWith(part, '[', ']')) {
              //currently any UNK token's suffix/prefix will be considered as UNK, which does not make sense; however, to make it comparable to its results, we temporally replace an UNK token with an empty string.
                if(wordToIdLookup.getId(part)==SymbolToIdLookup.UNK){
                    tokens.add(emptyString);
                }
                else{
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }

    String getPrefix(String value,int prefixLen) {
        //if value contains less than prefix len chars, end padding with ' '
        return prefixLen < value.length() ? value.substring(0,prefixLen) : Strings.padEnd(value,prefixLen, ' ');
    }

    String getSuffix(String value, int suffixLen) {
        //if value contains less than suffix len chars, start padding with ' '
        return suffixLen < value.length() ? value.substring( value.length()-suffixLen) : Strings.padStart(value,suffixLen, ' ');
    }

    Map<String,List<String>> extractFeatures(Annotation annotation){
        //"words", "prefix_1", "prefix_3", "prefix_2", "suffix_2", "suffix_3", "suffix_1","field_ids"
        Map<String,List<String>> features=Maps.newHashMap();
        //words
        features.put("words", Lists.newArrayList(annotation.tokens));
        //prefix
        int[] prefixLens={1,3,2};
        for(int prefixLen:prefixLens){
            List<String> prefixes = Lists.newArrayList();
            for(String token:annotation.tokens){
                prefixes.add(getPrefix(token,prefixLen));
            }
            features.put("prefix_"+String.valueOf(prefixLen), prefixes);
        }
        //suffix
        int[] suffixLens={2,3,1};
        for(int suffixLen:suffixLens){
            List<String> suffixes = Lists.newArrayList();
            for(String token:annotation.tokens){
                suffixes.add(getSuffix(token,suffixLen));
            }
            features.put("suffix_"+String.valueOf(suffixLen), suffixes);
        }
        //non-sequence features
        features.put("field_ids",Lists.newArrayList(annotation.fieldId));
        return features;
    }

    List<List<String>> getInput(Annotation annotation,String[] featureNames){
        List<List<String>> input = Lists.newArrayList();
        Map<String,List<String>> features=extractFeatures(annotation);
        for(String featureName:featureNames){
            input.add(features.get(featureName));
        }
        return input;
    }

    Prediction topPrediction(Iterable<Prediction> predictions){
        Prediction top=null;
        for(Prediction prediction:predictions){
            if(top==null || top.getConfidence()<prediction.getConfidence()){
                top=prediction;
            }
        }
        return top;
    }

    String predict(Annotation annotation, NNClassifier classifier,String[] featureNames){
        return topPrediction(classifier.predict(getInput(annotation,featureNames))).getLabel();
    }

    class  EvaluationResult{
        public String domain;
        public double countPrediction=0;
        public double countPredictionCorrect=0;
        public double countTruth=0;

    }

    class Measure{
        public String domain;
        public double precision;
        public double recall;
        public double f1;
    }

    Measure getMeasure(EvaluationResult evaluationResult){
        Measure measure=new Measure();
        measure.domain=evaluationResult.domain;
        //precision
        if(evaluationResult.countPrediction==0){
            measure.precision=0;
        }
        else{
            measure.precision=evaluationResult.countPredictionCorrect/evaluationResult.countPrediction;
        }
        if(evaluationResult.countTruth==0){
            measure.recall=0;
        }
        else{
            measure.recall=evaluationResult.countPredictionCorrect/evaluationResult.countTruth;
        }
        if(measure.recall==0 || measure.recall==0){
            measure.f1=0;
        }
        else{
            measure.f1=(2*measure.precision*measure.recall)/(measure.precision+measure.recall);
        }
        return measure;
    }

    void accumulate(Annotation annotation, String prediction, Map<String,EvaluationResult> results){
        String domain=annotation.domain;
        double count=annotation.count;
        boolean correct= domain.equals(prediction);
        EvaluationResult result=results.get(domain);
        if(result==null){
            result=new EvaluationResult();
            result.domain=domain;
            results.put(domain, result);
        }
        result.countTruth+=count;
        if(correct){
            result.countPredictionCorrect+=count;
        }
        result=results.get(prediction);
        if(result==null){
            result=new EvaluationResult();
            result.domain=prediction;
            results.put(prediction, result);
        }
        result.countPrediction+=count;
    }

    List<String> report(Map<String,EvaluationResult> results){
        List<String> reports=Lists.newArrayList();
        //calculate overall accuracy
        EvaluationResult overall=new EvaluationResult();
        overall.domain="Overall";
        for(String domain:results.keySet()){
            EvaluationResult result=results.get(domain);
            overall.countTruth+=result.countTruth;
            overall.countPredictionCorrect+=result.countPredictionCorrect;
        }
        overall.countPrediction=overall.countTruth;
        Measure overallMeasure=getMeasure(overall);
        reports.add(String.format("%s\t%.2f(%.1f/%.1f)\t%.2f(%.1f/%.1f)\t%.2f", overall.domain,100*overallMeasure.precision, overall.countPredictionCorrect, overall.countPrediction, 100*overallMeasure.recall, overall.countPredictionCorrect, overall.countTruth, 100*overallMeasure.f1 ));

        for(String domain:results.keySet()){
            EvaluationResult result=results.get(domain);
            Measure measure=getMeasure(result);
            reports.add(String.format("%s\t%.2f(%.1f/%.1f)\t%.2f(%.1f/%.1f)\t%.2f", domain,100*measure.precision, result.countPredictionCorrect, result.countPrediction, 100*measure.recall, result.countPredictionCorrect, result.countTruth, 100*measure.f1 ));
        }
        return reports;
    }
}
