/* Defaults can be overridden:
 * outerType = "DocumentSentiment"
 * innerType = "SentenceSentiment"
 * scoreFeature = "score"
 * polarityFeature = "polarity"
*/


import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.lang.ArrayUtils;


String outerType = "SentenceSet";
if (scriptParams?.containsKey("outerType")) {
    outerType = scriptParams.get("outerType").toString();
}

String innerType = "SentenceSentiment";
if (scriptParams?.containsKey("innerType")) {
    innerType = scriptParams.get("innerType").toString();
}

String scoreFeature = "score";
if (scriptParams?.containsKey("scoreFeature")) {
    scoreFeature = scriptParams.get("scoreFeature").toString();
}

String polarityFeature = "polarity";
if (scriptParams?.containsKey("polarityFeature")) {
    polarityFeature = scriptParams.get("polarityFeature").toString();
}


AnnotationSet outers = inputAS.get(outerType);
if (outers.isEmpty ()) {
    polarity = "neutral";
    Annotation a = outputAS.get(outputAS.add(0L,doc.getContent().size(),"SentenceSet",Factory.newFeatureMap()));
   a.getFeatures().put(polarityFeature, polarity);
return;}

for (outer in outers) {
   // first delete any existing score & polarity features
 //  outer.getFeatures().remove(scoreFeature);
   outer.getFeatures().remove(polarityFeature);

   // find the contained annotations and count and total up
   // their scoreFeature values
   AnnotationSet inners = gate.Utils.getContainedAnnotations(inputAS, outer, innerType);
   List<Double> innerScores = new ArrayList<Double>();

   for (inner in inners) {
       scoreObj = inner.getFeatures()?.get(scoreFeature);
       if (scoreObj instanceof Number) {
           innerScores.add(((Number) scoreObj).doubleValue());
       }
       else if (scoreObj instanceof String) {
           try {
               innerScores.add(Double.parseDouble((String) scoreObj));
           }
           catch (NumberFormatException e) {
               // nothing
           }
       }
   }
   

   // If we found some valid scores, average them and store the results.
   // Don't add any features unless we found valid scores.
   if (innerScores.size() > 0) {
       String polarity = "neutral";
       double[] array = ArrayUtils.toPrimitive(innerScores.toArray(new Double[1]));
       
       float mean = (float) StatUtils.mean(array);
       
       float stdDev = 0;
       if (innerScores.size() > 1) {
         stdDev = (float) Math.sqrt(StatUtils.variance(array));
       }

       if (mean > 0.0F) {
           polarity = "positive";
       }
       else if (mean < 0.0F) {
           polarity = "negative";
       }
       
       outer.getFeatures().put(scoreFeature, mean);
       outer.getFeatures().put(scoreFeature + "_std_dev", stdDev);
       outer.getFeatures().put(polarityFeature, polarity);
   }
   else {
       polarity = "neutral";
       outer.getFeatures().put(polarityFeature, polarity);
        }
 
 }

