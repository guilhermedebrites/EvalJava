package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Main {

    static String[] parts;
    static boolean isAnd = false;
    static boolean isOr = false;
    static boolean isNot = false;
    static boolean isStartsWith = false;
    static boolean isEndsWith = false;
    static boolean isEquals = false;
    static boolean isDifferent = false;
    static boolean isGreaterThan = false;
    static boolean isLessThan = false;
    static boolean isGreaterOrEquals = false;
    static boolean isLessOrEquals = false;

    public static void main(String[] args) {
        String algebra =
                "(13 > 10) || ('MARKETPLACE-gustavostore4' != '1290382132') || (1 == 1 && 1 != 1) || ('Criação'.startsWith('C')' && (6.0 < 3.25) && 13 <= 10.01) || ('Gustavo Store 4' == 'x' && 13 != 7)";
        algebra = "(5 < 7) || ('APP-1234' != '1234') || (0 == 0 && 2 > 1) || ('Desenvolvimento'.endsWith('mento')' && 8.0 >= 3.5 && 10 != 15) || ('LojaOnline' == 'LojaVirtual' && 20 != 20)";
        boolean result = evaluate(algebra, 1);
        System.out.println("Resultado: " + result);
    }

    public static boolean evaluate(String algebra, int number) {
        int posExpression = findExpression(algebra);
        int posEndExpression = findEndExpression(algebra, posExpression);
        String expression = algebra;

        if (posEndExpression != 0) {
            expression = algebra.substring(posExpression + 1, posEndExpression);
        }

        splitsExpression(expression);

        boolean[] results = new boolean[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if(isAnd && partsContainsTrueOrFalse(false, results, i) && i != 0){
                results[i] = false;
                break;
            } else if(isOr && partsContainsTrueOrFalse(true, results, i)) {
                results[i] = true;
                break;
            }
            results[i] = evaluateCondition(parts[i].trim());
        }

        if (posEndExpression != 0) {
            String newExpression = algebra.substring(0, posExpression) + finalResult(results) + algebra.substring(posEndExpression + 1);
            return evaluate(newExpression, number + 1);
        } else {
            return finalResult(results);
        }
    }

    public static boolean finalResult(boolean[] results) {
        boolean finalResult = results[0];
        for (int i = 1; i < results.length; i++) {
            if (isAnd) {
                finalResult = finalResult && results[i];
            } else if (isOr) {
                finalResult = finalResult || results[i];
            } else if(isNot) {
                finalResult = !results[i];
            } else if (isGreaterThan) {
                finalResult = Integer.parseInt(parts[0].trim()) > Integer.parseInt(parts[1].trim());
            } else if (isLessThan) {
                finalResult = Integer.parseInt(parts[0].trim()) < Integer.parseInt(parts[1].trim());
            } else if (isGreaterOrEquals) {
                finalResult = Integer.parseInt(parts[0].trim()) >= Integer.parseInt(parts[1].trim());
            } else if (isLessOrEquals) {
                finalResult = Integer.parseInt(parts[0].trim()) <= Integer.parseInt(parts[1].trim());
            } else if (isEquals) {
                finalResult = parts[0].trim().equals(parts[1].trim());
            } else if (isDifferent) {
                finalResult = !parts[0].trim().equals(parts[1].trim());
            } else if (isStartsWith) {
                finalResult = parts[0].trim().startsWith(parts[1].trim());
            } else if (isEndsWith) {
                finalResult = parts[0].trim().endsWith(parts[1].trim());
            }
        }
        return finalResult;
    }

    public static boolean evaluateCondition(String condition) {
        condition = condition.trim();

        if (condition.contains("==")) {
            return evaluateStringEquality(condition);
        } else if (condition.contains("!=")) {
            return evaluateStringInequality(condition);
        } else if (condition.contains("startsWith")) {
            return evaluateStringStartsWith(condition);
        } else if (condition.contains("endsWith")) {
            return evaluateStringEndsWith(condition);
        }else if (condition.contains(">") || condition.contains("<") || condition.contains(">=") || condition.contains("<=")) {
            return evaluateNumericCondition(condition);
        } else if (condition.contains("false")) {
            return false;
        } else if (condition.contains("true")) {
            return true;
        }

        throw new IllegalArgumentException("Expressão inválida: " + condition);
    }

    private static boolean evaluateStringEquality(String condition) {
        String[] parts = condition.split("==");
        String left = cleanString(parts[0]);
        String right = cleanString(parts[1]);
        return left.equals(right);
    }

    private static boolean evaluateStringInequality(String condition) {
        String[] parts = condition.split("!=");
        String left = cleanString(parts[0]);
        String right = cleanString(parts[1]);
        return !left.equals(right);
    }

    private static boolean evaluateStringStartsWith(String condition) {
        condition = condition.substring(0, condition.length() - 1);
        Pattern pattern = Pattern.compile("^(['\"])(.*?)\\1\\.startsWith\\((['\"])(.*?)\\3\\)$");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String left = matcher.group(2);
            String right = matcher.group(4);
            return left.startsWith(right);
        }

        throw new IllegalArgumentException("Expressão inválida (startsWith): " + condition);
    }

    private static boolean evaluateStringEndsWith(String condition) {
        condition = condition.substring(0, condition.length() - 1);
        Pattern pattern = Pattern.compile("^(['\"])(.*?)\\1\\.endsWith\\((['\"])(.*?)\\3\\)$");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String left = matcher.group(2);
            String right = matcher.group(4);
            return left.endsWith(right);
        }

        throw new IllegalArgumentException("Expressão inválida (endsWith): " + condition);
    }

    private static boolean evaluateNumericCondition(String condition) {
        if (condition.contains("==")) {
            return evaluateNumericEquality(condition);
        } else if (condition.contains("!=")) {
            return evaluateNumericInequality(condition);
        } else if (condition.contains(">=")) {
            return evaluateGreaterThanOrEqual(condition);
        } else if (condition.contains("<=")) {
            return evaluateLessThanOrEqual(condition);
        } else if (condition.contains(">")) {
            return evaluateGreaterThan(condition);
        } else if (condition.contains("<")) {
            return evaluateLessThan(condition);
        }
        throw new IllegalArgumentException("Expressão numérica inválida: " + condition);
    }

    private static boolean evaluateNumericEquality(String condition) {
        String[] parts = condition.split("==");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left == right;
    }

    private static boolean evaluateNumericInequality(String condition) {
        String[] parts = condition.split("!=");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left != right;
    }

    private static boolean evaluateGreaterThan(String condition) {
        String[] parts = condition.split(">");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left > right;
    }

    private static boolean evaluateLessThan(String condition) {
        String[] parts = condition.split("<");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left < right;
    }

    private static boolean evaluateGreaterThanOrEqual(String condition) {
        String[] parts = condition.split(">=");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left >= right;
    }

    private static boolean evaluateLessThanOrEqual(String condition) {
        String[] parts = condition.split("<=");
        double left = Double.parseDouble(parts[0].trim());
        double right = Double.parseDouble(parts[1].trim());
        return left <= right;
    }

    public static void splitsExpression(String expression) {
        isAnd = false;
        isOr = false;
        isNot = false;
        isStartsWith = false;
        isEndsWith = false;
        isEquals = false;
        isDifferent = false;
        isGreaterThan = false;
        isLessThan = false;
        isGreaterOrEquals = false;
        isLessOrEquals = false;

        if (expression.contains(" && ")) {
            parts = expression.split(" && ");
            isAnd = true;
        } else if (expression.contains(" || ")) {
            parts = expression.split(" \\|\\| ");
            isOr = true;
        } else if (expression.contains(" >= ")) {
            parts = new String[]{expression};
            isGreaterOrEquals = true;
        } else if (expression.contains(" <= ")) {
            parts = new String[]{expression};
            isLessOrEquals = true;
        } else if (expression.contains(" > ")) {
            parts = new String[]{expression};
            isGreaterThan = true;
        } else if (expression.contains(" < ")) {
            parts = new String[]{expression};
            isLessThan = true;
        } else if (expression.contains(" == ")) {
            parts = new String[]{expression};
            isEquals = true;
        } else if (expression.contains(" != ")) {
            parts = new String[]{expression};
            isDifferent = true;
        } else if (expression.contains("startsWith")) {
            parts = new String[]{expression};
            isStartsWith = true;
        } else if (expression.contains("endsWith")) {
            parts = new String[]{expression};
            isEndsWith = true;
        }
    }

    public static int findExpression(String texto) {
        int pos = texto.length() - 1;
        int posExpression = 0;

        while (pos > 0) {
            if (texto.charAt(pos) == '(' && texto.charAt(pos - 1) == 'h') {
                pos--;
            } else if (texto.charAt(pos) == '(') {
                return pos;
            }
            pos--;
        }

        return posExpression;
    }

    public static int findEndExpression(String texto, int pos) {
        while (pos < texto.length()) {
            if (texto.charAt(pos) == ')' && texto.charAt(pos - 1) == '\'' && texto.charAt(pos + 1) == '\'') {
                pos++;
            } else if (texto.charAt(pos) == ')') {
                return pos;
            }
            pos++;
        }

        return 0;
    }

    public static String cleanString(String str) {
        return str.trim().replaceAll("^'+|'+$", ""); // Remove aspas simples no início e fim
    }

    public static boolean partsContainsTrueOrFalse(boolean booleanValue, boolean[] results, int number) {
        for (int i = 0; i < number; i++) {
            if (results[i] == booleanValue) {
                return true;
            }
        }
        return false;
    }
}