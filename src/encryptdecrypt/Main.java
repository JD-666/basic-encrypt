package encryptdecrypt;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        String result = "";

        // Parse arguments.
        String mode = getMode(args);
        String data = getData(args);
        String algorithmName = getAlgorithmName(args);
        String inputFilePath = getInFilePath(args);
        String outFilePath = getOutFile(args);
        int key = getKey(args);
        CryptoAlg algorithm = null;

        // Select input source
        // If there was an -in passed, and no -data passed. Then use file for data.
        if (inputFilePath != null && data.isEmpty()) {
            try {
                data = Files.readString(Paths.get(inputFilePath));
            } catch (IOException e) {
                System.out.println("Error. Trouble reading " + inputFilePath);
                System.exit(1);
            }
        }

        // Program logic
        algorithm = CryptoAlgFactory.makeCryptoAlg(algorithmName);
        if (algorithm == null) {
            System.out.println("Error. Invalid -alg operation.");
            System.exit(1);
        }
        algorithm.setKey(key);
        if (mode.equals("enc")) {
            result = algorithm.encrypt(data);
        } else if (mode.equals("dec")) {
            result = algorithm.decrypt(data);
        } else {
            System.out.println("Error. Invalid -mode operation.");
            System.exit(1);
        }
        // Output
        if (outFilePath != null) {
            try (PrintWriter pw = new PrintWriter(outFilePath)) {
                pw.print(result);
            } catch (FileNotFoundException e) {
                System.out.println("Error. Trouble writing to " + outFilePath);
            }
        } else {
            System.out.println(result);
        }
    }

    private static String getAlgorithmName(String[] args) {
        for (int i = 0; i < args.length -1; i++) {
            if ("-alg".equals(args[i])) {
                return args[i+1];
            }
        }
        return null;
    }

    private static String getOutFile(String[] args) {
        for (int i = 0; i < args.length -1; i++) {
            if ("-out".equals(args[i])) {
                return args[i+1];
            }
        }
        return null;
    }

    private static String getInFilePath(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-in".equals(args[i])) {
                return args[i+1];
            }
        }
        return null;
    }

    /**
     * Returns the CL argument the user supplied for '-mode'.
     * @return the string argument (should be "enc" or "dec").
     */
    private static String getMode(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-mode".equals(args[i])) {
                return args[i+1];
            }
        }
        // Default to encryption if no -mode supplied.
        return "enc";
    }

    private static int getKey(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-key".equals(args[i])) {
                return Integer.parseInt(args[i+1]);
            }
        }
        // Default to 0 if no -key supplied.
        return 0;
    }

    private static String getData(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-data".equals(args[i])) {
                return args[i + 1];
            }
        }
        return "";
    }

}

class CryptoAlgFactory {
    public static CryptoAlg makeCryptoAlg(String algName) {
        if ("unicode".equals(algName)) {
            return new UnicodeCryptoAlg();
        } else if ("shift".equals(algName)) {
            return new ShiftCryptoAlg();
        }
        return null;
    }
}

interface CryptoAlg {
    String encrypt(String data);
    String decrypt(String data);
    void setKey(int key);
}

class UnicodeCryptoAlg implements CryptoAlg {

    int key;

    @Override
    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public String encrypt(String data) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            ret.append(encryptChar(data.charAt(i), key));
        }
        return ret.toString();
    }

    @Override
    public String decrypt(String data) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            ret.append(encryptChar(data.charAt(i), -key));
        }
        return ret.toString();
    }

    private char encryptChar(char letter, int key) {
        return (char) (letter + key);
    }
}

class ShiftCryptoAlg implements CryptoAlg {

    int key;
    private String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
    private String upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public String encrypt(String data) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            ret.append(encryptChar(data.charAt(i), key));
        }
        return ret.toString();
    }

    @Override
    public String decrypt(String data) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            ret.append(encryptChar(data.charAt(i), -key));
        }
        return ret.toString();
    }

    private char encryptChar(char letter, int key) {
        int lowerIndex = lowerLetters.indexOf(letter);
        int upperIndex = upperLetters.indexOf(letter);
        if (lowerIndex != -1) {
            lowerIndex = (lowerIndex + key) % 26;
            while (lowerIndex < 0) {
                lowerIndex += 26;
            }
            return lowerLetters.charAt(lowerIndex);
        } else if (upperIndex != -1) {
            upperIndex = (upperIndex + key) % 26;
            while (upperIndex < 0) {
                upperIndex += 26;
            }
            return upperLetters.charAt(upperIndex);
        } else {
            return letter;
        }
    }

}