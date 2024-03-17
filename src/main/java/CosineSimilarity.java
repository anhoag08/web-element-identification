import java.util.Arrays;
import java.util.Vector;

public class CosineSimilarity {
    public static Vector<Double> multiplyVectorByConstant(Vector<Double> vector, double k) {
        Vector<Double> result = new Vector<>();
        for (double num : vector) {
            result.add(num * k);
        }
        return result;
    }

    public static Vector<Double> addVectors(Vector<Double> vector1, Vector<Double> vector2) {
        Vector<Double> result = new Vector<>();
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        for (int i = 0; i < vector1.size(); i++) {
            result.add(vector1.get(i) + vector2.get(i));
        }
        return result;
    }
    public static Vector<Double> stringToDouble(Vector<String> stringVector) {
        Vector<Double> doubleVector = new Vector<>();
        for (String str : stringVector) {
            double num = Double.parseDouble(str);
            doubleVector.add(num);
        }
        return doubleVector;
    }
    public static double cosineSimilarity(Vector<String> as, Vector<String> bs) {
        Vector<Double> a = stringToDouble(as);
        Vector<Double> b = stringToDouble(bs);
        double dotProduct = dotProduct(a, b);
        double normA = norm(a);
        double normB = norm(b);
        return dotProduct / (normA * normB);
    }

    private static double dotProduct(Vector<Double> a, Vector<Double> b) {
        double product = 0;
        for (int i = 0; i < a.size(); i++) {
            product += a.get(i) * b.get(i);
        }
        return product;
    }

    private static double norm(Vector<Double> vector) {
        double sum = 0;
        for (double num : vector) {
            sum += num * num;
        }
        return Math.sqrt(sum);
    }
}
