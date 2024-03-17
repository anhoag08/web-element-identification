import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Permutations {
    public static List<List<Integer>> generatePermutations(int n, int m) {
        List<List<Integer>> permutations = new ArrayList<>();
        generatePermutationsHelper(n, m, new ArrayList<>(), permutations);
        return permutations;
    }

    private static void generatePermutationsHelper(int n, int m, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == m) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 1; i <= n; i++) {
            if (!current.contains(i)) {
                current.add(i);
                generatePermutationsHelper(n, m, current, result);
                current.remove(current.size() - 1);
            }
        }
    }
}
