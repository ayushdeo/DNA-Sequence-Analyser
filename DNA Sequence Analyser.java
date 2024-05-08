package com.mycompany.dict;

import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class ADSProject {
public static final DNASequence SENTINEL = new DNASequence("SENTINEL", "");
// 1. Data Model
public static class DNASequence {

    private final String sequence;
    private final String sequenceName;

    public DNASequence(String sequenceName, String sequence) {
        this.sequenceName = sequenceName;
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public String toString() {
        return "DNASequence{name=" + sequenceName + ", sequence=" + sequence + "}";
    }
}


// 2. File Reader (Producer)
 public static class DNAFileReader implements Runnable {

    private final String filePath;
    private final BlockingQueue<DNASequence> sharedQueue;

    public DNAFileReader(String filePath, BlockingQueue<DNASequence> sharedQueue) {
        this.filePath = filePath;
        this.sharedQueue = sharedQueue;
    }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    DNASequence dnaSequence = new DNASequence("DNA", line);
                    sharedQueue.put(dnaSequence);
                }
               
                sharedQueue.put(SENTINEL);
                sharedQueue.put(SENTINEL); 
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

// 3. Analyzer (Consumer)
public static class Analyzer implements Runnable {

    private final BlockingQueue<DNASequence> sharedQueue;

    public Analyzer(BlockingQueue<DNASequence> sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

        @Override
        public void run() {
            try {
                while (true) {
                    DNASequence dnaSequence = sharedQueue.take();
                    if (dnaSequence == SENTINEL) {
                        break;  
                    }
                String sequence = dnaSequence.getSequence();
                
                Map<Character, Integer> baseCounts = countBases(sequence);
                double gcContent = calculateGCContent(baseCounts);
                String reverseComplement = getReverseComplement(sequence);
                boolean containsSubsequence = sequence.contains("ATG"); // Example subsequence
                System.out.println("");
                System.out.println("Original sequence: " + sequence);
                System.out.println("Base Counts: " + baseCounts);
                System.out.printf("GC Content: %.2f%%\n", gcContent * 100);
                System.out.println("Reverse Complement: " + reverseComplement);
                System.out.println("Contains subsequence 'ATG': " + containsSubsequence);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<Character, Integer> countBases(String sequence) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char base : sequence.toCharArray()) {
            counts.put(base, counts.getOrDefault(base, 0) + 1);
        }
        return counts;
    }

    private double calculateGCContent(Map<Character, Integer> baseCounts) {
        int gcCount = baseCounts.getOrDefault('G', 0) + baseCounts.getOrDefault('C', 0);
        return (double) gcCount / baseCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private String getReverseComplement(String sequence) {
        StringBuilder complement = new StringBuilder();
        for (char base : sequence.toCharArray()) {
            switch (base) {
                case 'A': complement.append('T'); break;
                case 'T': complement.append('A'); break;
                case 'C': complement.append('G'); break;
                case 'G': complement.append('C'); break;
            }
        }
        return complement.reverse().toString();
    }
}

    // 5. Main Application
public static void main(String[] args) throws InterruptedException {

        String dnaFilePath = "C:\\Users\\Ayush Deo\\Downloads\\DNA.txt";

        BlockingQueue<DNASequence> sharedQueue = new LinkedBlockingQueue<>();

        long startTimeSingle = System.currentTimeMillis();

        Thread producerSingle = new Thread(new DNAFileReader(dnaFilePath, sharedQueue), "Producer-Thread");
        Thread consumerSingle = new Thread(new Analyzer(sharedQueue), "Consumer-Thread-1");

        producerSingle.start();
        consumerSingle.start();

        producerSingle.join();
        consumerSingle.join();

        long endTimeSingle = System.currentTimeMillis();
        System.out.println("Processing time with single consumer: " + (endTimeSingle - startTimeSingle) + "ms");

        // Multiple Consumers
        long startTimeMulti = System.currentTimeMillis();

        Thread producerMulti = new Thread(new DNAFileReader(dnaFilePath, sharedQueue), "Producer-Thread");
        Thread consumerMulti1 = new Thread(new Analyzer(sharedQueue), "Consumer-Thread-1");
        Thread consumerMulti2 = new Thread(new Analyzer(sharedQueue), "Consume        Thread consumerMulti2 = new Thread(new Analyzer(sharedQueue), \"Consumer-Thread-2\");\n" +
"r-Thread-2");

        producerMulti.start();
        consumerMulti1.start();
        consumerMulti2.start();

        producerMulti.join();
        consumerMulti1.join();
        consumerMulti2.join();

        long endTimeMulti = System.currentTimeMillis();
        System.out.println("Processing time with multiple consumers: " + (endTimeMulti - startTimeMulti) + "ms");
    }
}
