import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;

import java.util.*;

public class HelloSelenium {
    static HashMap<String, Integer> attrGlobalFreq = new HashMap<>();
    static HashMap<String, Integer> textGlobalFreq = new HashMap<>();
    static HashMap<String, Integer> targetGlobalFreq = new HashMap<>();
    static HashMap<String, Vector<Vector<String>>> elementKeywordMap = new HashMap<>();
    static HashMap<String, Vector<Vector<String>>> targetWordMap = new HashMap<>();
    static double elementSize = 0;
    static double targetSize = 0;
    static final double alpha = 1.3;

    static final int vectorizationSize = 5;

    public static void main(String[] args) throws IOException {
        elementPrep("https://saucedemo.com/");
        targetPrep("src/main/resources/static/target.txt");
        calcHeuristic();
    }

    public static void calcHeuristic() {
        List<List<Integer>> permutations = Permutations.generatePermutations((int) elementSize, (int) targetSize);
        ArrayList<Map.Entry<String, Vector<Vector<String>>>> targetEntryList = new ArrayList<>(targetWordMap.entrySet());
        ArrayList<Map.Entry<String, Vector<Vector<String>>>> elementEntryList = new ArrayList<>(elementKeywordMap.entrySet());
        List<Integer> maxPermute = new ArrayList<>();
        double maxHeuristics = 0;
        for (List<Integer> permute : permutations) {
            for (int i = 0; i < permute.size(); i++) {
                double similarity = calcSimilarity(targetEntryList.get(i).getValue(), elementEntryList.get(permute.get(i) - 1).getValue());
                if (similarity > maxHeuristics) {
                    maxPermute = permute;
                    maxHeuristics = similarity;
                }
            }
        }

        System.out.println(maxPermute + ": " + maxHeuristics);
        for (int i = 0; i < maxPermute.size(); i++) {
            System.out.println(targetEntryList.get(i).getKey() + ": " + elementEntryList.get(maxPermute.get(i) - 1).getKey());
        }
    }

    public static double calcSimilarity(Vector<Vector<String>> targetVec, Vector<Vector<String>> elementVec) {
        double cossimTargetText, cossimTargetAttr;
        if (!elementVec.getFirst().isEmpty()) {
            cossimTargetText = CosineSimilarity.cosineSimilarity(targetVec.getFirst(), elementVec.getFirst());
        } else {
            cossimTargetText = 0;
        }
        if (!elementVec.get(1).isEmpty()) {
            cossimTargetAttr = CosineSimilarity.cosineSimilarity(targetVec.getFirst(), elementVec.get(1));
        } else {
            cossimTargetAttr = 0;
        }
        return (cossimTargetText * alpha + cossimTargetAttr) / (alpha + 1);
    }

    public static Vector<String> keywordPrep(Vector<String> keywordVec) {
        Vector<String> keywordPrepped = new Vector<>();
        for (String keyword : keywordVec) {
            keywordPrepped.addAll(arrToVec(keyword.split("[^A-Za-z0-9]")));
        }
        keywordPrepped.removeIf(String::isBlank);
        keywordPrepped.replaceAll(String::toLowerCase);
        keywordPrepped = removeStopWords(keywordPrepped);
        return keywordPrepped;
    }

    public static Vector<String> removeStopWords(List<String> words) {
        // Define a list of stop words (articles and prepositions)
        Vector<String> stopWords = arrToVec(Arrays.asList(
                "a", "an", "the", "in", "on", "at", "by", "for", "to", "with", "of", "from", "over", "under", "is", "are", "were", "was", "will",
                "would", "why", "when", "which", "what", "who").toArray(new String[0]));

        // Create a new list to store filtered words
        Vector<String> filteredWords = new Vector<>();

        // Iterate through the original list and add non-stop words to the filtered list
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                filteredWords.add(word);
            }
        }

        return filteredWords;
    }

    public static String getXpath(WebElement element) {
        String tagName = element.getTagName();
        StringBuilder xpath = new StringBuilder("//" + tagName + "[");

        if (element.getAttribute("id") != null && !element.getAttribute("id").isEmpty()) {
            xpath.append("@id='").append(element.getAttribute("id")).append("']");
            return xpath.toString();
        }

        boolean havingPreviousAttribute = false;
        List<String> attributeNames = List.of("class", "name"); // Add more attribute names if needed

        for (String attributeName : attributeNames) {
            String attributeValue = element.getAttribute(attributeName);
            if (attributeValue != null && !attributeValue.isEmpty()) {
                if (havingPreviousAttribute) {
                    xpath.append(" and @").append(attributeName).append("='").append(attributeValue).append("'");
                } else {
                    xpath.append("@").append(attributeName).append("='").append(attributeValue).append("'");
                    havingPreviousAttribute = true;
                }
            }
        }

        xpath.append("]");
        return xpath.toString();
    }

    public static void prepTFIDF(Vector<Vector<String>> valueVec) {
        Vector<String> textUniqueWord = new Vector<>();
        Vector<String> textFrequency = new Vector<>();
        Vector<String> attrUniqueWord = new Vector<>();
        Vector<String> attrFrequency = new Vector<>();

        for (String text : valueVec.getFirst()) {
            if (!textUniqueWord.contains(text)) {
                textUniqueWord.add(text);
                textFrequency.add("1");
            } else {
                int index = textUniqueWord.indexOf(text);
                String value = Integer.toString(Integer.parseInt(textFrequency.get(index)) + 1);
                textFrequency.set(index, value);
            }
        }

        for (String attr : valueVec.get(1)) {
            if (!attrUniqueWord.contains(attr)) {
                attrUniqueWord.add(attr);
                attrFrequency.add("1");
            } else {
                int index = attrUniqueWord.indexOf(attr);
                String value = Integer.toString(Integer.parseInt(attrFrequency.get(index)) + 1);
                attrFrequency.set(index, value);
            }
        }

        for (String text : textUniqueWord) {
            if (!textGlobalFreq.containsKey(text)) {
                textGlobalFreq.put(text, 1);
            } else {
                textGlobalFreq.put(text, textGlobalFreq.get(text) + 1);
            }
        }

        for (String attr : attrUniqueWord) {
            if (!attrGlobalFreq.containsKey(attr)) {
                attrGlobalFreq.put(attr, 1);
            } else {
                attrGlobalFreq.put(attr, attrGlobalFreq.get(attr) + 1);
            }
        }

        valueVec.add(1, textFrequency);
        valueVec.add(1, textUniqueWord);
        valueVec.add(attrUniqueWord);
        valueVec.add(attrFrequency);
    }

    public static void calcTFIDF(Vector<Vector<String>> valueVec) {
        for (int i = 0; i < valueVec.get(1).size(); i++) {
            double tfidf = Double.parseDouble(valueVec.get(2).get(i)) * Math.log(elementSize / textGlobalFreq.get(valueVec.get(1).get(i)));
            valueVec.get(2).set(i, Double.toString(tfidf));
        }

        for (int i = 0; i < valueVec.get(4).size(); i++) {
            double tfidf = Double.parseDouble(valueVec.get(5).get(i)) * Math.log(elementSize / attrGlobalFreq.get(valueVec.get(4).get(i)));
            valueVec.get(5).set(i, Double.toString(tfidf));
        }
    }

    public static void findVector(Vector<Vector<String>> valueVec) {
        //text
        if (!valueVec.get(1).isEmpty()) {
            double denominator = 0;
            Vector<String> numerator = new Vector<>();
            for (int i = 0; i < vectorizationSize; i++) {
                numerator.add("0");
            }
            for (int i = 0; i < valueVec.get(1).size(); i++) {
                Vector<String> textVectorization = PythonTruthTableServer.vectorization(valueVec.get(1).get(i));
                for (int j = 0; j < vectorizationSize; j++) {
                    double numeratorVal = Double.parseDouble(numerator.get(j)) + Double.parseDouble(textVectorization.get(j)) * Double.parseDouble(valueVec.get(2).get(i));
                    numerator.set(j, Double.toString(numeratorVal));
                }
                denominator += Double.parseDouble(valueVec.get(2).get(i));
            }
            for (int j = 0; j < vectorizationSize; j++) {
                double vectorVal = Double.parseDouble(numerator.get(j)) / denominator;
                numerator.set(j, Double.toString(vectorVal));
            }
            valueVec.subList(0, 3).clear();
            valueVec.addFirst(numerator);
        } else {
            valueVec.subList(0, 3).clear();
            valueVec.addFirst(new Vector<>());
        }

        //attr
        valueVec.remove(1);
        if (!valueVec.get(1).isEmpty()) {
            double denominator = 0;
            Vector<String> numerator = new Vector<>();
            for (int i = 0; i < vectorizationSize; i++) {
                numerator.add("0");
            }
            for (int i = 0; i < valueVec.get(1).size(); i++) {
                Vector<String> attrVectorization = PythonTruthTableServer.vectorization(valueVec.get(1).get(i));
                for (int j = 0; j < vectorizationSize; j++) {
                    double numeratorVal = Double.parseDouble(numerator.get(j)) + Double.parseDouble(attrVectorization.get(j)) * Double.parseDouble(valueVec.get(2).get(i));
                    numerator.set(j, Double.toString(numeratorVal));
                }
                denominator += Double.parseDouble(valueVec.get(2).get(i));
            }
            for (int j = 0; j < vectorizationSize; j++) {
                double vectorVal = Double.parseDouble(numerator.get(j)) / denominator;
                numerator.set(j, Double.toString(vectorVal));
            }
            valueVec.subList(1, 3).clear();
            valueVec.addLast(numerator);
        } else {
            valueVec.subList(1, 3).clear();
            valueVec.addLast(new Vector<>());
        }
    }

    public static void targetPrepTFIDF(Vector<Vector<String>> valueVec) {
        Vector<String> targetUniqueWord = new Vector<>();
        Vector<String> targetFrequency = new Vector<>();
        for (String target : valueVec.getFirst()) {
            if (!targetUniqueWord.contains(target)) {
                targetUniqueWord.add(target);
                targetFrequency.add("1");
            } else {
                int index = targetUniqueWord.indexOf(target);
                String value = Integer.toString(Integer.parseInt(targetFrequency.get(index)) + 1);
                targetFrequency.set(index, value);
            }
        }
        for (String target : targetUniqueWord) {
            if (!targetGlobalFreq.containsKey(target)) {
                targetGlobalFreq.put(target, 1);
            } else {
                targetGlobalFreq.put(target, targetGlobalFreq.get(target) + 1);
            }
        }

        valueVec.clear();
        valueVec.add(targetUniqueWord);
        valueVec.add(targetFrequency);
    }

    public static void elementPrep(String url) {
        WebDriver driver = new ChromeDriver();

        driver.get(url);

        // Find the <body> element
        WebElement bodyElement = driver.findElement(By.tagName("body"));

        // Find all the elements inside the <body> tag
        List<WebElement> bodyElements = bodyElement.findElements(By.xpath(".//*"));

        String[] attributes = {"id", "onclick", "name", "role", "placeholder", "value"};

        // Print the tag names of all the elements inside the <body> tag
        for (WebElement element : bodyElements) {
            if (!element.getTagName().equals("div")) {
                String xpath = getXpath(element);
                if (!xpath.isEmpty()) {
                    Vector<String> attributeStringVec = new Vector<>();
                    for (String attribute : attributes) {
                        if (element.getAttribute(attribute) != null && !element.getAttribute(attribute).isBlank()) {
                            attributeStringVec.add(element.getAttribute(attribute));
                        }
                    }
                    Vector<String> elementText = new Vector<>();
                    if (!element.getText().isBlank()) {
                        elementText.add(element.getText());
                    }
                    if (!(keywordPrep(elementText).isEmpty() && keywordPrep(attributeStringVec).isEmpty())) {
                        Vector<Vector<String>> elementKeywords = new Vector<>();
                        elementKeywords.add(keywordPrep(elementText));
                        elementKeywords.add(keywordPrep(attributeStringVec));
                        elementKeywordMap.put(xpath, elementKeywords);
                    }
                }
            }
        }

        for (Map.Entry<String, Vector<Vector<String>>> entry : elementKeywordMap.entrySet()) {
            prepTFIDF(entry.getValue());
        }
        elementSize = elementKeywordMap.entrySet().size();

        for (Map.Entry<String, Vector<Vector<String>>> entry : elementKeywordMap.entrySet()) {
            calcTFIDF(entry.getValue());
            findVector(entry.getValue());
        }

        driver.quit();
    }

    public static void targetPrep(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            Vector<String> targetWordVec = new Vector<>();
            Vector<Vector<String>> targetWordVecVec = new Vector<>();
            targetWordVec.add(line);
            targetWordVecVec.add(keywordPrep(targetWordVec));
            targetWordMap.put(line, targetWordVecVec);
        }

        for (Map.Entry<String, Vector<Vector<String>>> entry : targetWordMap.entrySet()) {
            targetPrepTFIDF(entry.getValue());
        }

        targetSize = targetWordMap.entrySet().size();

        for (Map.Entry<String, Vector<Vector<String>>> entry : targetWordMap.entrySet()) {
            targetCalcTFIDF(entry.getValue());
            targetFindVector(entry.getValue());
        }
    }

    private static void targetFindVector(Vector<Vector<String>> valueVec) {
        double denominator = 0;
        Vector<String> numerator = new Vector<>();
        for (int i = 0; i < vectorizationSize; i++) {
            numerator.add("0");
        }
        for (int i = 0; i < valueVec.get(0).size(); i++) {
            Vector<String> targetVectorization = PythonTruthTableServer.vectorization(valueVec.get(0).get(i));
            for (int j = 0; j < vectorizationSize; j++) {
                double numeratorVal = Double.parseDouble(numerator.get(j)) + Double.parseDouble(targetVectorization.get(j)) * Double.parseDouble(valueVec.get(1).get(i));
                numerator.set(j, Double.toString(numeratorVal));
            }
            denominator += Double.parseDouble(valueVec.get(1).get(i));
        }
        for (int j = 0; j < vectorizationSize; j++) {
            double vectorVal = Double.parseDouble(numerator.get(j)) / denominator;
            numerator.set(j, Double.toString(vectorVal));
        }
        valueVec.clear();
        valueVec.add(numerator);
    }

    private static void targetCalcTFIDF(Vector<Vector<String>> valueVec) {
        for (int i = 0; i < valueVec.get(0).size(); i++) {
            double tfidf = Double.parseDouble(valueVec.get(1).get(i)) * Math.log(targetSize / targetGlobalFreq.get(valueVec.get(0).get(i)));
            valueVec.get(1).set(i, Double.toString(tfidf));
        }
    }

    public static Vector<String> arrToVec(String[] arr) {
        return new Vector<>(Arrays.asList(arr));
    }
}
